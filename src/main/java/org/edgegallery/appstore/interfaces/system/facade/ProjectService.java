/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.appstore.interfaces.system.facade;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.LcmLog;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.UploadResponse;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.persistence.apackage.AppReleasePo;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PackageMapper;
import org.edgegallery.appstore.infrastructure.persistence.system.HostMapper;
import org.edgegallery.appstore.infrastructure.util.FormatRespDto;
import org.edgegallery.appstore.infrastructure.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("projectService")
public class ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    private static final CookieStore cookieStore = new BasicCookieStore();

    private static final Gson gson = new Gson();

    private static final String INSTANTIATE_APPLICATION_FAIL = "instantiate application failed";

    private static final String COLON = ":";

    private static final int GET_WORKSTATUS_WAIT_TIME = 5;

    Map<String, String> hostMap = new HashMap<>();

    @Autowired
    private PackageMapper packageMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private PackageRepository packageRepository;

    private static String getXsrf() {
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("XSRF-TOKEN")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private static CloseableHttpClient createIgnoreSslHttpClient() {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setDefaultCookieStore(cookieStore).setRedirectStrategy(new DefaultRedirectStrategy()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * processConfig.
     */
    public static int getMinute(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    /**
     * append image path.
     *
     * @param str append args list.
     * @return StringBuilder.
     */
    public static StringBuilder stringBuilder(String... str) {

        StringBuilder stringBuilder = new StringBuilder();
        if (str == null || str.length <= 0) {
            return stringBuilder;
        }

        for (int i = 0; i < str.length; i++) {
            stringBuilder.append(str[i]);
        }

        return stringBuilder;
    }

    /**
     * deploy app to host.
     *
     * @param filePath csarpath。
     * @param appInstanceId appInstanceId。
     * @param userId userId.
     * @param token token.
     * @return
     */
    public boolean deployTestConfigToAppLcm(String filePath, String packageId, String appInstanceId, String userId,
        MepHost mepHost, String token) {
        LcmLog lcmLog = new LcmLog();
        String uploadRes = HttpClientUtil
            .uploadPkg(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), filePath, userId, token, lcmLog);
        if (StringUtils.isEmpty(uploadRes)) {
            return false;
        }
        LOGGER.info("upload res {}", uploadRes);
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<UploadResponse>() { }.getType();
        UploadResponse uploadResponse = gson.fromJson(uploadRes, typeEvents);
        String pkgId = uploadResponse.getPackageId();
        AppReleasePo releasePo = new AppReleasePo();
        releasePo.setInstancePackageId(pkgId);
        releasePo.setPackageId(packageId);
        packageMapper.updateAppInstanceApp(releasePo);
        // distribute pkg
        boolean distributeRes = HttpClientUtil
            .distributePkg(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), userId, token, pkgId,
                mepHost.getMecHost(), lcmLog);

        if (!distributeRes) {
            return false;
        }
        LOGGER.info("distribute res {}", distributeRes);
        // instantiate application
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("sleep fail! {}", e.getMessage());
        }
        boolean instantRes = HttpClientUtil
            .instantiateApplication(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), appInstanceId, userId,
                token, lcmLog, pkgId, mepHost.getMecHost());
        if (!instantRes) {
            cleanTestEnv(userId, packageId, mepHost.getName(), mepHost.getLcmIp(), token);
            return false;
        }
        LOGGER.info("after instant {}", instantRes);
        return true;
    }

    /**
     * cleanTestEnv.
     *
     * @return
     */
    public Either<FormatRespDto, Boolean> cleanTestEnv(String userId, String packageId, String name, String ip,
        String token) {
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        String instanceTenentId = appReleasePo.getInstanceTenentId();
        String appInstanceId = appReleasePo.getAppInstanceId();
        String pkgId = appReleasePo.getInstancePackageId();
        List<MepHost> mapHosts = hostMapper.getHostsByCondition(userId, name, ip);

        if (CollectionUtils.isEmpty(mapHosts)) {
            LOGGER.info("This project has no config, do not need to clean env.");
            return Either.right(true);
        }
        boolean cleanResult = deleteDeployApp(mapHosts.get(0), instanceTenentId, appInstanceId, pkgId, token);
        appReleasePo.initialConfig();
        packageMapper.updateAppInstanceApp(appReleasePo);
        return Either.right(cleanResult);
    }

    /**
     * get WorkStatus.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param host host.
     * @param token token.
     * @return
     */
    public String getWorkStatus(String packageId, String userId, MepHost host, String token) {
        String workStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), packageId, userId, token);
        LOGGER.info("pod workStatus: {}", workStatus);
        return workStatus;
    }

    /**
     * deleteDeployApp.
     *
     * @return
     */
    private boolean deleteDeployApp(MepHost host, String userId, String appInstanceId, String pkgId, String token) {

        if (StringUtils.isNotEmpty(appInstanceId)) {
            // delete hosts
            boolean deleteHostRes = HttpClientUtil
                .deleteHost(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, token, pkgId, host.getLcmIp());

            // delete pkg
            boolean deletePkgRes = HttpClientUtil
                .deletePkg(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, token, pkgId);
            if (!deleteHostRes || !deletePkgRes) {
                return false;
            }
        }
        if (StringUtils.isNotEmpty(appInstanceId)) {
            boolean terminateApp = HttpClientUtil
                .terminateAppInstance(host.getProtocol(), host.getLcmIp(), host.getPort(), appInstanceId, userId,
                    token);
            return terminateApp;
        }

        return true;
    }

    /**
     * deploy App By Id.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param name name.
     * @param ip ip.
     * @param token token.
     * @return
     */
    public ResponseEntity<ResponseObject> deployAppById(String appId, String packageId, String userId,
        String name, String ip,
        String token) {
        String workStatus = "";
        String showInfo = "";
        String testUsetId = "";
        List<MepHost> mapHosts = hostMapper.getHostsByCondition(testUsetId, name, ip);
        LOGGER.info("Get all hosts success.", mapHosts);
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_FAIL, null);
        if (CollectionUtils.isEmpty(mapHosts)) {
            return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "please register host."));
        } else {
            LOGGER.info("Get all hosts success.");
            String instanceTenentId = userId;
            AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
            Release release = packageRepository.findReleaseById(appId, packageId);
            String filePath = release.getPackageFile().getStorageAddress();
            String appInstanceId = appReleasePo.getAppInstanceId();
            if (StringUtils.isEmpty(appInstanceId)) {
                appInstanceId = UUID.randomUUID().toString();
                boolean instantRes = deployTestConfigToAppLcm(filePath, packageId, appInstanceId, userId,
                    mapHosts.get(0), token);
                if (!instantRes) {
                    LOGGER.error("instantiate application failed, response is null");
                    return ResponseEntity.ok(new ResponseObject(showInfo, errMsg,
                        "instantiate application failed."));
                }
                AppReleasePo releasePo = new AppReleasePo();
                releasePo.setPackageId(packageId);
                releasePo.setAppInstanceId(appInstanceId);
                releasePo.setInstanceTenentId(instanceTenentId);
                packageMapper.updateAppInstanceApp(releasePo);
            }


            int from = getMinute(new Date());
            workStatus = getWorkStatus(appInstanceId, userId, mapHosts.get(0), token);
            while (StringUtils.isEmpty(workStatus)) {
                try {
                    Thread.sleep(3000);
                    workStatus = getWorkStatus(appInstanceId, userId, mapHosts.get(0), token);
                } catch (InterruptedException e) {
                    LOGGER.error("sleep fail! {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            int to = getMinute(new Date());
            if ((to - from) > GET_WORKSTATUS_WAIT_TIME) {
                return ResponseEntity.ok(new ResponseObject(null, errMsg,
                    "get app nodeport url failed."));
            }
        }
        if (!StringUtils.isEmpty(workStatus)) {
            String serviceName = getServiceName(workStatus);
            String nodePort = String.valueOf(getNodePort(workStatus));
            String mecHost = mapHosts.get(0).getMecHost();
            showInfo = stringBuilder(serviceName, COLON, nodePort, COLON, mecHost).toString();
        }
        errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "get app url success."));
    }

    /**
     * get nodeStatus.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param name name.
     * @param ip ip.
     * @param token token.
     * @return
     */
    public ResponseEntity<ResponseObject> getNodeStatus(String packageId, String userId, String name, String ip,
        String token) {
        String workStatus = "";
        String showInfo = "";
        List<MepHost> mapHosts = hostMapper.getHostsByCondition(userId, name, ip);
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_FAIL, null);
        if (CollectionUtils.isEmpty(mapHosts)) {
            return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "please register host."));
        } else {
            LOGGER.info("Get all hosts success.");
            AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
            String appInstanceId = appReleasePo.getAppInstanceId();
            if (StringUtils.isEmpty(appInstanceId) || StringUtils.isEmpty(userId) || StringUtils.isEmpty(token)) {
                return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "get app url failed."));
            }
            workStatus = getWorkStatus(appInstanceId, userId, mapHosts.get(0), token);

        }
        if (StringUtils.isNotEmpty(workStatus)) {
            String serviceName = getServiceName(workStatus);
            String nodePort = String.valueOf(getNodePort(workStatus));
            String mecHost = mapHosts.get(0).getMecHost();
            showInfo = stringBuilder(serviceName, COLON, nodePort, COLON, mecHost).toString();
        }
        errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "get app url success."));
    }

    /**
     * get nodePort.
     *
     * @param workStatus workStatus.
     * @return
     */
    public int getNodePort(String workStatus) {
        int nodePort = new JsonParser().parse(workStatus).getAsJsonObject().get("services").getAsJsonArray().get(0)
            .getAsJsonObject().get("ports").getAsJsonArray().get(0).getAsJsonObject().get("nodePort").getAsInt();
        return nodePort;

    }

    /**
     * get serviceName.
     *
     * @param workStatus workStatus.
     * @return
     */
    public String getServiceName(String workStatus) {
        String serviceName = new JsonParser().parse(workStatus).getAsJsonObject().get("services").getAsJsonArray()
            .get(0).getAsJsonObject().get("serviceName").getAsString();
        return serviceName;

    }

}
