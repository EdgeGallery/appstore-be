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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.spencerwi.either.Either;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.Experience;
import org.edgegallery.appstore.domain.model.system.lcm.LcmLog;
import org.edgegallery.appstore.domain.model.system.lcm.UploadResponse;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.persistence.apackage.AppReleasePo;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PackageMapper;
import org.edgegallery.appstore.infrastructure.persistence.system.HostMapper;
import org.edgegallery.appstore.infrastructure.util.HttpClientUtil;
import org.edgegallery.appstore.infrastructure.util.InputParameterUtil;
import org.edgegallery.appstore.infrastructure.util.IpCalculateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("projectService")
public class ProjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectService.class);

    private static final int CLEAN_ENV_WAIT_TIME = 24;

    /**
     * get worksrtatus wait 5 minites.
     */
    private static final int GET_WORKSTATUS_WAIT_TIME = 5 * 1000 * 60;

    /**
     * get terminate app result wait 2 minites.
     */

    private static final int STATUS_SUCCESS = 0;

    private static final String CONTAINER = "container";

    private static final String VM = "vm";

    private static final String K8S = "K8S";

    private static final String OPENSTACK = "OpenStack";

    private static final String VM_EXPERIENCE_IP = "app_n6_ip";

    private static final String VM_SEMICOLON = ";";

    private static final String VM_EQUAL = "=";

    private static final int IP_BINARY_BITS = 32;

    private static final int RESERVE_IP_COUNT = 2;

    private static final int IP_CALCULATE_BASE = 2;

    private static final String SLEEP_FAILED = "sleep fail! {}";

    private static final String SERVICES = "services";

    private static final CookieStore cookieStore = new BasicCookieStore();

    private static final int READ_BUFFER_SIZE = 256;

    @Value("${security.oauth2.resource.jwt.key-uri:}")
    private String loginUrl;

    @Value("${client.client-id:}")
    private String clientId;

    @Value("${client.client-secret:}")
    private String clientPW;

    @Setter
    private int instantiateAppSleepTime = 50000;

    @Setter
    private int uploadPkgSleepTime = 5000;

    @Autowired
    private PackageMapper packageMapper;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private PackageRepository packageRepository;

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
            SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

            return HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
                .setDefaultCookieStore(cookieStore).setRedirectStrategy(new DefaultRedirectStrategy()).build();
        } catch (Exception e) {
            LOGGER.error("call sslConnectionSocketFactory to clean env interface occur error {}", e.getMessage());
        }
        return null;
    }

    /**
     * deploy app to host.
     *
     * @param filePath csarpath.
     * @param appInstanceId appInstanceId.
     * @param userId userId.
     * @param token token.
     */
    public boolean deployTestConfigToAppLcm(String filePath, String packageId, String appInstanceId, String userId,
        MepHost mepHost, String token, AppReleasePo appReleasePo, LcmLog lcmLog) {
        String uploadRes = HttpClientUtil
            .uploadPkg(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), filePath, userId, token, lcmLog);
        if (StringUtils.isEmpty(uploadRes)) {
            return false;
        }
        try {
            Thread.sleep(uploadPkgSleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error(SLEEP_FAILED, e.getMessage());
        }
        LOGGER.info("upload res {}", uploadRes);
        appReleasePo.setMecHost(mepHost.getMecHost());
        packageMapper.updateAppInstanceApp(appReleasePo);
        JsonObject jsonObject = new JsonParser().parse(uploadRes).getAsJsonObject();
        JsonElement uploadData = jsonObject.get("data");
        Map<String, String> inputParams = new HashMap<>();
        if (appReleasePo.getDeployMode().equals(VM)) {
            inputParams = getInputParams(mepHost.getParameter(), mepHost.getMecHost());
        }
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<UploadResponse>() { }.getType();
        UploadResponse uploadResponse = gson.fromJson(uploadData, typeEvents);
        String pkgId = uploadResponse.getPackageId();
        appReleasePo.setInstancePackageId(pkgId);
        appReleasePo.setExperienceableIp(inputParams.get(VM_EXPERIENCE_IP));
        packageMapper.updateAppInstanceApp(appReleasePo);
        // distribute pkg
        boolean distributeRes = HttpClientUtil.distributePkg(mepHost, userId, token, pkgId, lcmLog);
        if (!distributeRes) {
            cleanTestEnv(packageId, mepHost.getName(), mepHost.getLcmIp(), token);
            return false;
        }
        LOGGER.info("distribute res {}", distributeRes);
        // instantiate application
        try {
            Thread.sleep(instantiateAppSleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error(SLEEP_FAILED, e.getMessage());
        }

        boolean instantRes = HttpClientUtil
            .instantiateApp(mepHost, appInstanceId, userId, token, lcmLog, pkgId, inputParams);
        if (!instantRes) {
            cleanTestEnv(packageId, mepHost.getName(), mepHost.getLcmIp(), token);
            return false;
        }
        LOGGER.info("after instant {}", instantRes);
        return true;
    }

    /**
     * get instantiate parameter.
     *
     * @param parameter parameter.
     * @param mecHost mecHost.
     */
    public Map<String, String> getInputParams(String parameter, String mecHost) {

        List<Release> mecHostPackage = packageRepository.findReleaseByMecHost(mecHost);
        Map<String, String> vmInputParams = InputParameterUtil.getParams(parameter);
        int count = 0;
        String n6Range = vmInputParams.get(VM_EXPERIENCE_IP);
        String temN6Ip = IpCalculateUtil.getStartIp(n6Range, count);
        int ipCount = getIpCount(n6Range);
        for (Release mecRelease : mecHostPackage) {
            if (mecRelease.getExperienceableIp() == null) {
                continue;
            }
            if (mecRelease.getExperienceableIp().equals(temN6Ip) || count >= ipCount) {
                count++;
                temN6Ip = IpCalculateUtil.getStartIp(n6Range, count);
            }
        }
        String mepRange = vmInputParams.get("app_mp1_ip");
        String internetRange = vmInputParams.get("app_internet_ip");
        vmInputParams.put(VM_EXPERIENCE_IP, temN6Ip);
        vmInputParams.put("app_mp1_ip", IpCalculateUtil.getStartIp(mepRange, count));
        vmInputParams.put("app_internet_ip", IpCalculateUtil.getStartIp(internetRange, count));
        vmInputParams.put("app_n6_mask", IpCalculateUtil.getNetMask(n6Range.split("/")[1]));
        vmInputParams.put("app_mp1_mask", IpCalculateUtil.getNetMask(mepRange.split("/")[1]));
        vmInputParams.put("app_internet_mask", IpCalculateUtil.getNetMask(internetRange.split("/")[1]));
        vmInputParams.put("app_n6_gw", IpCalculateUtil.getStartIp(n6Range, 0));
        vmInputParams.put("app_mp1_gw", IpCalculateUtil.getStartIp(mepRange, 0));
        vmInputParams.put("app_internet_gw", IpCalculateUtil.getStartIp(internetRange, 0));
        return vmInputParams;
    }

    public int getIpCount(String n6Range) {
        int cou = IP_BINARY_BITS - Integer.parseInt(n6Range.substring(n6Range.lastIndexOf("/") + 1));
        return (int) Math.pow(IP_CALCULATE_BASE, cou) - RESERVE_IP_COUNT;
    }

    /**
     * cleanTestEnv.
     */
    public Either<ResponseObject, Boolean> cleanTestEnv(String packageId, String name, String ip, String token) {
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        String instanceTenentId = appReleasePo.getInstanceTenentId();
        String appInstanceId = appReleasePo.getAppInstanceId();
        String pkgId = appReleasePo.getInstancePackageId();
        List<MepHost> mapHosts = judgeHost(name, ip, appReleasePo.getDeployMode());
        MepHost mepHost = mapHosts.get(0);
        if (CollectionUtils.isEmpty(mapHosts)) {
            LOGGER.info("This project has no config, do not need to clean env.");
            return Either.right(false);
        }
        boolean cleanResult = deleteDeployedApp(mepHost, instanceTenentId, appInstanceId, pkgId, token);
        appReleasePo.initialConfig();
        packageMapper.updateAppInstanceApp(appReleasePo);
        return Either.right(cleanResult);
    }

    /**
     * judge vm and container host.
     *
     * @param name host name.
     * @param ip host ip.
     * @param deployMode deployMode.
     */
    public List<MepHost> judgeHost(String name, String ip, String deployMode) {
        String os = "";
        List<MepHost> mapHosts = null;
        if (CONTAINER.equalsIgnoreCase(deployMode)) {
            os = K8S;
        } else {
            os = OPENSTACK;
        }
        mapHosts = hostMapper.getHostsByCondition(name, ip, os);
        return mapHosts;
    }

    /**
     * get WorkStatus.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param host host.
     * @param token token.
     */
    public String getWorkStatus(String packageId, String userId, MepHost host, String token) {
        String workStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), packageId, userId, token);
        LOGGER.info("pod workStatus: {}", workStatus);
        return workStatus;
    }

    /**
     * parse uninstall result.
     *
     * @param status status.
     */
    public static int parseStatus(String status) {
        JsonObject jsonObject = new JsonParser().parse(status).getAsJsonObject();
        return jsonObject.get("retCode").getAsInt();

    }

    /**
     * delete Deployed App.
     *
     * @param host host.
     * @param userId userId.
     * @param appInstanceId appInstanceId
     * @param pkgId pkgId.
     * @param token token.
     */
    private boolean deleteDeployedApp(MepHost host, String userId, String appInstanceId, String pkgId, String token) {
        if (StringUtils.isNotEmpty(appInstanceId)) {
            HttpClientUtil
                .terminateAppInstance(host.getProtocol(), host.getMecHost(), host.getPort(), appInstanceId, userId,
                    token);
            // delete hosts
            boolean deleteHostRes = HttpClientUtil
                .deleteHost(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, token, pkgId,
                    host.getMecHost());
            // delete pkg
            boolean deletePkgRes = HttpClientUtil
                .deletePkg(host.getProtocol(), host.getLcmIp(), host.getPort(), userId, token, pkgId);
            if (!deleteHostRes || !deletePkgRes) {
                LOGGER.error("delete package failed after instantiateApp.");
                return false;
            }
        }
        return true;
    }

    /**
     * get nodePort.
     *
     * @param workStatus workStatus.
     */
    public int getNodePort(String workStatus) {
        JsonObject jsonObjects = new JsonParser().parse(workStatus).getAsJsonObject();
        String uploadData = jsonObjects.get("data").getAsString();
        JsonObject jsonCode = new JsonParser().parse(uploadData).getAsJsonObject();
        return jsonCode.get(SERVICES).getAsJsonArray().get(0).getAsJsonObject().get("ports").getAsJsonArray().get(0)
            .getAsJsonObject().get("nodePort").getAsInt();

    }

    /**
     * deploy App By Id.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param name name.
     * @param ip ip.
     * @param token token.
     */
    public ResponseEntity<ResponseObject> deployAppById(String appId, String packageId, String userId, String name,
        String ip, String token) {
        String showInfo = "";
        LcmLog lcmLog = new LcmLog();
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        List<MepHost> mapHosts = judgeHost(name, ip, appReleasePo.getDeployMode());
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_FAIL, null);
        if (CollectionUtils.isEmpty(mapHosts)) {
            return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "please register host."));
        }
        LOGGER.info("Get all hosts success.");
        String instanceTenentId = userId;
        Release release = packageRepository.findReleaseById(appId, packageId);
        String filePath = release.getPackageFile().getStorageAddress();
        String appInstanceId = appReleasePo.getAppInstanceId();
        if (StringUtils.isEmpty(appInstanceId)) {
            appInstanceId = UUID.randomUUID().toString();
            boolean instantRes = deployTestConfigToAppLcm(filePath, packageId, appInstanceId, userId, mapHosts.get(0),
                token, appReleasePo, lcmLog);
            if (!instantRes) {
                LOGGER.error("instantiate application failed, response is null");
                String errorlog = lcmLog.getLog();
                return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, errorlog));
            }
            appReleasePo.setAppInstanceId(appInstanceId);
            appReleasePo.setInstanceTenentId(instanceTenentId);
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            appReleasePo.setStartExpTime(time.format(new Date()));
            packageMapper.updateAppInstanceApp(appReleasePo);
        }
        //If it is a vm package, it will get the IP in parameter and return it to the foreground
        String serviceName = "";
        String nodePort = "";
        String mecHost = "";
        long from = new Date().getTime();
        String workStatus = getWorkStatus(appInstanceId, userId, mapHosts.get(0), token);
        int status = parseStatus(workStatus);
        long to;
        while (status != STATUS_SUCCESS) {
            try {
                Thread.sleep(3000);
                workStatus = getWorkStatus(appInstanceId, userId, mapHosts.get(0), token);
            } catch (InterruptedException e) {
                LOGGER.error(SLEEP_FAILED, e.getMessage());
                Thread.currentThread().interrupt();
            }
            to = new Date().getTime();
            if ((to - from) > GET_WORKSTATUS_WAIT_TIME) {
                return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "get app nodeport url failed."));
            }
        }
        List<Experience> experienceInfoList = new ArrayList<>();
        if (appReleasePo.getDeployMode().equals(CONTAINER)) {
            experienceInfoList = getExperienceInfo(workStatus, mapHosts.get(0));

        } else {
            serviceName = appReleasePo.getAppName();
            mecHost = appReleasePo.getExperienceableIp();
            experienceInfoList.add(new Experience(serviceName, nodePort, mecHost));

        }
        errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(experienceInfoList, errMsg, "get app url success."));
    }

    /**
     * get vm experience ip.
     *
     * @param parameter mapHost parameter.
     */
    private String getVmExperienceIp(String parameter) {
        String[] parameters = parameter.split(VM_SEMICOLON);
        for (String vmIp : parameters) {
            if (vmIp.contains(VM_EXPERIENCE_IP)) {
                return vmIp.substring(vmIp.lastIndexOf(VM_EQUAL) + 1);
            }
        }
        return null;
    }

    /**
     * get nodeStatus.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param name name.
     * @param ip ip.
     * @param token token.
     */
    public ResponseEntity<ResponseObject> getNodeStatus(String packageId, String userId, String name, String ip,
        String token) {
        String workStatus = "";
        String showInfo = "";
        String serviceName = "";
        String nodePort = "";
        String mecHost = "";
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_FAIL, null);
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        List<MepHost> mapHosts = judgeHost(name, ip, appReleasePo.getDeployMode());
        List<Experience> experienceInfoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(mapHosts)) {
            return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "please register host."));
        } else {
            LOGGER.info("Get all hosts success.");
            //If the VM application is not released, you can determine whether the vm application is released
            // by checking whether the instance ID is empty.
            // If the appInstanceId is null for a vm application, the appInstanceId is released
            if (StringUtils.isEmpty(appReleasePo.getAppInstanceId()) || StringUtils.isEmpty(userId) || StringUtils
                .isEmpty(token)) {
                return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "this pacakge not instantiate"));
            }

            if (appReleasePo.getDeployMode().equals(VM)) {
                serviceName = appReleasePo.getAppName();
                mecHost = getVmExperienceIp(mapHosts.get(0).getParameter());
                experienceInfoList.add(new Experience(serviceName, nodePort, mecHost));
            } else {
                workStatus = getWorkStatus(appReleasePo.getAppInstanceId(), userId, mapHosts.get(0), token);
            }
        }

        if (StringUtils.isNotEmpty(workStatus)) {
            experienceInfoList = getExperienceInfo(workStatus, mapHosts.get(0));
        }
        errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(experienceInfoList, errMsg, "get app url success."));
    }

    /**
     * get Experience Information.
     *
     * @param workStatus workStatus.
     * @param mepHost mepHost.
     */
    private List<Experience> getExperienceInfo(String workStatus, MepHost mepHost) {
        List<Experience> experienceInfoList = new ArrayList<>();
        JsonObject jsonObjects = new JsonParser().parse(workStatus).getAsJsonObject();
        String uploadData = jsonObjects.get("data").getAsString();
        JsonObject jsonCode = new JsonParser().parse(uploadData).getAsJsonObject();
        JsonArray array = jsonCode.get(SERVICES).getAsJsonArray();
        for (int i = 0; i < array.size(); i++) {
            String serviceName = array.get(i).getAsJsonObject().get("serviceName").getAsString();
            String nodePort = array.get(i).getAsJsonObject().get("ports").getAsJsonArray().get(0).getAsJsonObject()
                .get("nodePort").getAsString();
            experienceInfoList.add(new Experience(serviceName, nodePort, mepHost.getMecHost()));
        }
        return experienceInfoList;
    }

    /**
     * cleanUnreleasedEnv.
     */
    public boolean cleanUnreleasedEnv() {
        List<AppReleasePo> packageList = packageMapper.findReleaseNoCondtion();
        if (CollectionUtils.isEmpty(packageList)) {
            LOGGER.error("get package List is empty");
            return false;
        }
        //Call by service nameuser-mgmtLogin interface
        try {
            String accessToken = getAccessToken();
            if (StringUtils.isEmpty(accessToken)) {
                LOGGER.error("call login or clean env interface occur error,accesstoken is empty");
                return false;
            }
            String name = "";
            String ip = "";
            Instant dateOfProject = null;
            for (AppReleasePo packageObj : packageList) {
                DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String createDate = packageObj.getStartExpTime();
                if (StringUtils.isEmpty(createDate)) {
                    dateOfProject = Instant.now();
                } else {
                    dateOfProject = fmt.parse(createDate).toInstant();
                }
                Instant now = Instant.now();
                Long timeDiff = Duration.between(dateOfProject, now).toHours();
                if (timeDiff.intValue() >= CLEAN_ENV_WAIT_TIME) {
                    cleanTestEnv(packageObj.getPackageId(), name, ip, accessToken);
                }
            }
        } catch (ParseException e) {
            LOGGER.error("call login or clean env interface occur error {}", e.getMessage());
        }
        return true;
    }

    private String getAccessToken() {
        int count = 0;
        CloseableHttpClient client = createIgnoreSslHttpClient();
        if (client == null) {
            LOGGER.error("call client interface occur error");
            return null;
        }
        while (count < 10) {
            String authResult = getAuthResult(client);
            if (StringUtils.isNotEmpty(authResult) && authResult.contains("\"accessToken\":")) {
                String tokenArr = getTokenString(authResult);
                if (tokenArr != null) {
                    return tokenArr;
                }
            } else {
                count++;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.error(SLEEP_FAILED, e.getMessage());
                }
            }
        }
        return "";
    }

    private String getTokenString(String authResult) {
        String[] authResults = authResult.split(",");
        for (String authRes : authResults) {
            if (authRes.contains("accessToken")) {
                String[] tokenArr = authRes.split(":");
                if (tokenArr != null && tokenArr.length > 1) {
                    return tokenArr[1].substring(1, tokenArr[1].length() - 1);
                }
            }
        }
        return null;
    }

    private String getAuthResult(CloseableHttpClient client) {
        try {
            URL url = new URL(loginUrl);
            String userLoginUrl = url.getProtocol() + "://" + url.getAuthority() + "/login";
            LOGGER.warn("user login url: {}", userLoginUrl);
            HttpPost httpPost = new HttpPost(userLoginUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("username", clientId + ":" + new Date().getTime());
            builder.addTextBody("password", clientPW);
            httpPost.setEntity(builder.build());
            // first call login interface
            client.execute(httpPost);
            String xsrf = getXsrf();
            httpPost.setHeader("X-XSRF-TOKEN", xsrf);
            // secode call login interface
            client.execute(httpPost);
            String xsrfToken = getXsrf();
            //third call auth login-info interface
            String getTokenUrl = url.getProtocol() + "://" + url.getHost() + ":30091/auth/login-info";
            LOGGER.warn("user login-info url: {}", getTokenUrl);
            HttpGet httpGet = new HttpGet(getTokenUrl);
            httpGet.setHeader("X-XSRF-TOKEN", xsrfToken);
            CloseableHttpResponse res = client.execute(httpGet);
            InputStream inputStream = res.getEntity().getContent();
            byte[] bytes = new byte[READ_BUFFER_SIZE];
            StringBuilder buf = new StringBuilder();
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                buf.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
            }
            if (buf.length() > 0) {
                LOGGER.info("response token length: {}", buf.length());
                return buf.toString();
            }
        } catch (IOException e) {
            LOGGER.error("call login or clean env interface occur error {}", e.getMessage());
        }
        return null;
    }

}
