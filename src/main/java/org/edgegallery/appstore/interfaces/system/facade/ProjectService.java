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
import java.util.concurrent.TimeUnit;
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
import org.edgegallery.appstore.domain.model.releases.EnumExperienceStatus;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.Experience;
import org.edgegallery.appstore.domain.model.system.lcm.LcmLog;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostInfo;
import org.edgegallery.appstore.domain.model.system.lcm.UploadResponse;
import org.edgegallery.appstore.domain.model.system.lcm.WorkStatusResponse;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.domain.shared.exceptions.IllegalRequestException;
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

    private static final String STATUS_DATA = "data";

    private static final String CONTAINER = "container";

    private static final String VM = "vm";

    private static final String K8S = "K8S";

    private static final String OPENSTACK = "OpenStack";

    private static final int IP_BINARY_BITS = 32;

    private static final int RESERVE_IP_COUNT = 2;

    private static final int IP_CALCULATE_BASE = 2;

    private static final String SLEEP_FAILED = "sleep fail! {}";

    private static final String SERVICES = "services";

    private static final CookieStore cookieStore = new BasicCookieStore();

    private static final int READ_BUFFER_SIZE = 256;

    private static final String TOKEN = "token";

    private static final String USER_ID = "userId";

    private static final String INSTANTIATE_FAILED = "instantiate package failed.";

    private static final String APP_INSTANCE_ID = "appInstanceId";

    /**
     * get terminate app result wait 2 minites.
     */

    private static final int GET_TERMINATE_RESULT_TIME = 5 * 1000 * 60;

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

    public void updateExperienceStatus(String packageId, int experienceStatus) {
        packageMapper.updateExperienceStatus(packageId, experienceStatus);
    }

    /**
     * deploy app to host.
     *
     * @param deployParams deployParams.
     * @param mepHost mepHost.
     * @param appReleasePo appReleasePo.
     * @param lcmLog lcmLog.
     * @return
     */
    public boolean deployTestConfigToAppLcm(Map<String, String> deployParams, MepHost mepHost,
        AppReleasePo appReleasePo, LcmLog lcmLog) {
        Map<String, String> inputParams = new HashMap<>();
        if (VM.equals(appReleasePo.getDeployMode())) {
            inputParams = getInputParams(mepHost.getParameter(), mepHost.getMecHost());
        }
        boolean uploadRes = uploadPackage(deployParams, mepHost, appReleasePo, lcmLog, inputParams);
        if (!uploadRes) {
            return false;
        }
        updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.DISTRIBUTING.getProgress());
        boolean distributeRes = distributePkg(deployParams.get(USER_ID), mepHost, deployParams.get(TOKEN), appReleasePo,
            lcmLog);
        if (!distributeRes) {
            String pkgId = appReleasePo.getInstancePackageId();
            HttpClientUtil.deletePkg(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), deployParams, pkgId);
            LOGGER.error("distributed package failed.");
            return false;
        }
        updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.INSTANTIATING.getProgress());
        // instantiate application
        boolean instantRes = instantiateApp(mepHost, deployParams, lcmLog, appReleasePo, inputParams);
        if (!instantRes) {
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.INSTANTIATE_FAILED.getProgress());
            String pkgId = appReleasePo.getInstancePackageId();
            HttpClientUtil.deleteHost(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), deployParams, pkgId,
                mepHost.getMecHost());
            HttpClientUtil.deletePkg(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), deployParams, pkgId);
            LOGGER.error(INSTANTIATE_FAILED);
            return false;
        }
        LOGGER.info("after instant {}", instantRes);
        return true;
    }

    /**
     * instantiate package.
     * @param mepHost mepHost.
     * @param deployParams deployParams.
     * @param lcmLog lcmLog.
     * @param appReleasePo appReleasePo.
     * @param inputParams inputParams.
     * @return
     */
    public boolean instantiateApp(MepHost mepHost, Map<String, String> deployParams, LcmLog lcmLog,
        AppReleasePo appReleasePo, Map<String, String> inputParams) {
        long startTime = new Date().getTime();
        boolean instantiateRes = HttpClientUtil
            .instantiateApp(mepHost, deployParams, lcmLog, appReleasePo.getInstancePackageId(), inputParams);
        LOGGER.info("instantiate res {}", instantiateRes);
        if (!instantiateRes) {
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.INSTANTIATE_FAILED.getProgress());
            LOGGER.error(INSTANTIATE_FAILED);
            lcmLog.setRetCode(ResponseConst.RET_INSTANTIATE_FAILED);
            lcmLog.setLog(INSTANTIATE_FAILED);
            return false;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.error(SLEEP_FAILED, e.getMessage());
            Thread.currentThread().interrupt();
        }

        String packageStatus = "";
        String status = "";
        long endTime;
        String enumStatus = EnumExperienceStatus.INSTANTIATED.getText();
        if (VM.equalsIgnoreCase(appReleasePo.getDeployMode())) {
            enumStatus = EnumExperienceStatus.VM_INSTANTIATED.getText();
        }
        while (!enumStatus.equalsIgnoreCase(status)) {
            try {
                packageStatus = HttpClientUtil
                    .getWorkloadStatus(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), deployParams);
                if (StringUtils.isEmpty(packageStatus) || "Failure".equalsIgnoreCase(status)) {
                    lcmLog.setLog(INSTANTIATE_FAILED);
                    return false;
                }
                status = parseInstantiateResult(packageStatus, enumStatus, appReleasePo.getDeployMode());
                TimeUnit.MILLISECONDS.sleep(3000);
                updateExperienceStatus(appReleasePo.getPackageId(),
                    EnumExperienceStatus.CHECK_INSTANTIATE.getProgress());
            } catch (InterruptedException e) {
                LOGGER.error(SLEEP_FAILED, e.getMessage());
                Thread.currentThread().interrupt();
            }
            endTime = new Date().getTime();
            if ((endTime - startTime) > GET_WORKSTATUS_WAIT_TIME) {
                lcmLog.setLog(INSTANTIATE_FAILED);
                return false;
            }
        }
        return true;
    }

    /**
     * distribute package.
     * @param userId userId.
     * @param mepHost mepHost.
     * @param token token.
     * @param appReleasePo appReleasePo.
     * @param lcmLog lcmLog.
     * @return
     */
    public boolean distributePkg(String userId, MepHost mepHost, String token, AppReleasePo appReleasePo,
        LcmLog lcmLog) {
        boolean distributeRes = HttpClientUtil
            .distributePkg(mepHost, userId, token, appReleasePo.getInstancePackageId(), lcmLog);
        LOGGER.info("distribute res {}", distributeRes);
        if (!distributeRes) {
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.DISTRIBUTE_FAILED.getProgress());
            lcmLog.setLog("distributed package failed.");
            lcmLog.setRetCode(ResponseConst.RET_DISTRIBUTE_FAILED);
            return false;
        }
        return confirmResult(mepHost, userId, token, lcmLog, appReleasePo);
    }

    /**
     * upload package.
     *
     * @param deployParams deployParams.
     * @param mepHost mepHost.
     * @param appReleasePo appReleasePo.
     * @param lcmLog lcmLog.
     * @param inputParams inputParams.
     * @return
     */
    public boolean uploadPackage(Map<String, String> deployParams, MepHost mepHost, AppReleasePo appReleasePo,
        LcmLog lcmLog, Map<String, String> inputParams) {
        String uploadRes = HttpClientUtil
            .uploadPkg(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), deployParams, lcmLog);
        LOGGER.info("upload res {}", uploadRes);
        if (StringUtils.isEmpty(uploadRes)) {
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.UPLOAD_FAILED.getProgress());
            LOGGER.error("upload to remote file server failed.");
            lcmLog.setLog("upload to remote file server failed.");
            lcmLog.setRetCode(ResponseConst.RET_UPLOAD_FILE_FAILED);
            return false;
        }
        appReleasePo.setMecHost(mepHost.getMecHost());
        JsonObject jsonObject = new JsonParser().parse(uploadRes).getAsJsonObject();
        JsonElement uploadData = jsonObject.get(STATUS_DATA);
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<UploadResponse>() { }.getType();
        UploadResponse uploadResponse = gson.fromJson(uploadData, typeEvents);
        String pkgId = uploadResponse.getPackageId();
        appReleasePo.setInstancePackageId(pkgId);
        String[] arr = mepHost.getParameter().split(";");
        String vmExperienceIP = arr[0].trim().split("=")[1];
        appReleasePo.setExperienceAbleIp(vmExperienceIP);
        packageMapper.updateAppInstanceApp(appReleasePo);
        return true;
    }

    /**
     * confirm experience result.
     * @param mepHost mepHost.
     * @param userId userId.
     * @param token token.
     * @param lcmLog lcmLog.
     * @param appReleasePo appReleasePo.
     * @return
     */
    public boolean confirmResult(MepHost mepHost, String userId, String token, LcmLog lcmLog,
        AppReleasePo appReleasePo) {
        long startTime = new Date().getTime();
        String resultInfo = "";
        String status = "";
        long endTime;
        while (!EnumExperienceStatus.DISTRIBUTED.getText().equalsIgnoreCase(status)) {
            try {
                Map<String, String> deployParams = new HashMap<>();
                deployParams.put(USER_ID, userId);
                deployParams.put(TOKEN, token);
                resultInfo = HttpClientUtil
                    .getDistributeRes(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), deployParams,
                        appReleasePo.getInstancePackageId());
                if (StringUtils.isEmpty(resultInfo)) {
                    return false;
                }
                status = parseWorkStatus(resultInfo, appReleasePo.getInstancePackageId(),
                    EnumExperienceStatus.DISTRIBUTED.getText());
                TimeUnit.MILLISECONDS.sleep(2000);
            } catch (InterruptedException e) {
                LOGGER.error(SLEEP_FAILED, e.getMessage());
                Thread.currentThread().interrupt();
            }
            endTime = new Date().getTime();
            if ((endTime - startTime) > GET_TERMINATE_RESULT_TIME) {
                return false;
            }
        }
        return true;
    }

    /**
     * parse experience workStatus.
     * @param resultInfo resultInfo.
     * @param packageId packageId.
     * @return
     */
    public String parseWorkStatus(String resultInfo, String packageId, String emunStatus) {
        String status = null;
        JsonObject jsonObject = new JsonParser().parse(resultInfo).getAsJsonObject();
        JsonElement uploadData = jsonObject.get(STATUS_DATA);
        Gson gson = new Gson();
        Type typeEvents = new TypeToken<List<WorkStatusResponse>>() { }.getType();
        List<WorkStatusResponse> uploadResponse = gson.fromJson(uploadData, typeEvents);
        for (WorkStatusResponse workStatusResponse : uploadResponse) {
            if (!packageId.equals(workStatusResponse.getPackageId()) && CollectionUtils
                .isEmpty(workStatusResponse.getMecHostInfo())) {
                return null;
            } else {
                List<MecHostInfo> mecHostInfoList = workStatusResponse.getMecHostInfo();
                for (MecHostInfo mecHostInfo : mecHostInfoList) {
                    if (!emunStatus.equalsIgnoreCase(mecHostInfo.getStatus())) {
                        return null;
                    }
                    status = mecHostInfo.getStatus();
                }
            }
        }
        return status;
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
        int count = 1;
        String n6Range = InputParameterUtil.getExperienceIp(parameter);
        String temN6Ip = IpCalculateUtil.getStartIp(n6Range, count);
        int ipCount = getIpCount(n6Range);
        for (Release mecRelease : mecHostPackage) {
            if (mecRelease.getExperienceAbleIp() == null) {
                continue;
            }
            if (mecRelease.getExperienceAbleIp().equals(temN6Ip) || count >= ipCount) {
                count++;
                temN6Ip = IpCalculateUtil.getStartIp(n6Range, count);
            }
        }
        for (Map.Entry<String, String> map : vmInputParams.entrySet()) {
            String ipKey = map.getKey();
            String ipValue = IpCalculateUtil.getStartIp(map.getValue(), count);
            vmInputParams.put(ipKey, ipValue);
        }
        return vmInputParams;
    }

    public int getIpCount(String n6Range) {
        int cou = IP_BINARY_BITS - Integer.parseInt(n6Range.substring(n6Range.lastIndexOf("/") + 1));
        return (int) Math.pow(IP_CALCULATE_BASE, cou) - RESERVE_IP_COUNT;
    }

    /**
     * cleanTestEnv.
     */
    public Either<ResponseObject, Boolean> cleanTestEnv(String packageId, String token) {
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        String instanceTenentId = appReleasePo.getInstanceTenentId();
        String appInstanceId = appReleasePo.getAppInstanceId();
        String pkgId = appReleasePo.getInstancePackageId();
        MepHost mepHost = judgeHost(appReleasePo.getDeployMode());
        boolean cleanResult = deleteDeployedApp(mepHost, instanceTenentId, appInstanceId, pkgId, token);
        if (cleanResult) {
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.CLEAN_ENV_SUCCESS.getProgress());
        }
        appReleasePo.initialConfig();
        packageMapper.updateAppInstanceApp(appReleasePo);
        return Either.right(cleanResult);
    }

    /**
     * judge vm and container host.
     *
     * @param deployMode deployMode.
     */
    public MepHost judgeHost(String deployMode) {
        String os = "";
        List<MepHost> mepHosts = null;
        if (CONTAINER.equalsIgnoreCase(deployMode)) {
            os = K8S;
        } else {
            os = OPENSTACK;
        }
        mepHosts = hostMapper.getHostsByCondition("", os);
        if (CollectionUtils.isEmpty(mepHosts)) {
            throw new IllegalRequestException("please register host", ResponseConst.HOST_EMPTY_ERROR);
        }
        //in  experience online scenario, the first sandbox of the array is used by default.
        return mepHosts.get(0);
    }

    /**
     * get WorkStatus.
     *
     * @param appInstanceId appInstanceId.
     * @param userId userId.
     * @param host host.
     * @param token token.
     */
    public String getWorkStatus(String appInstanceId, String userId, MepHost host, String token) {
        Map<String, String> deployParams = new HashMap<>();
        deployParams.put(APP_INSTANCE_ID, appInstanceId);
        deployParams.put(USER_ID, userId);
        deployParams.put(TOKEN, token);
        String workStatus = HttpClientUtil
            .getWorkloadStatus(host.getProtocol(), host.getLcmIp(), host.getPort(), deployParams);
        LOGGER.info("pod workStatus: {}", workStatus);
        return workStatus;
    }

    /**
     * get experience status.
     * @param packageId packageId.
     * @param userId userId.
     * @param mepHost mepHost.
     * @param token token.
     * @param lcmLog lcmLog.
     * @return
     */
    public String getPackageStatus(String packageId, String userId, MepHost mepHost, String token, LcmLog lcmLog) {
        String packageStatus = HttpClientUtil
            .getPackageStatus(mepHost.getProtocol(), mepHost.getLcmIp(), mepHost.getPort(), userId, token, lcmLog);
        LOGGER.info("pod workStatus: {}", packageStatus);
        return packageStatus;
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
     * parse experience status.
     * @param status status.
     * @return
     */
    public static String parseExperienceStatus(String status) {
        return new JsonParser().parse(status).getAsJsonObject().get(STATUS_DATA).getAsJsonArray().get(0)
            .getAsJsonObject().get("mecHostInfo").getAsJsonArray().get(0).getAsJsonObject().get("status").getAsString();

    }

    /**
     * parse Instantiate result.
     *
     * @param status status.
     * @param enumStatus enumStatus.
     * @return
     */
    public String parseInstantiateResult(String status, String enumStatus, String deployMode) {
        String podStatus = null;
        JsonObject jsonObject = new JsonParser().parse(status).getAsJsonObject();
        if (VM.equalsIgnoreCase(deployMode)) {
            podStatus = jsonObject.get("status").getAsString();
            if (!enumStatus.equalsIgnoreCase(podStatus)) {
                return podStatus;
            }
        } else {
            JsonArray array = jsonObject.getAsJsonArray("pods");
            for (JsonElement jsonItem : array) {
                podStatus = jsonItem.getAsJsonObject().get("podstatus").getAsString();
                if (!enumStatus.equalsIgnoreCase(podStatus)) {
                    LOGGER.info("pod start failed: {}", podStatus);
                    return null;
                }
            }
        }
        return podStatus;
    }

    /**
     * parse Vm Instantiate result.
     *
     * @param status status.
     * @return
     */
    public List<Experience> getVmExperienceInfo(String status, String serviceName) {
        List<Experience> experienceInfoList = new ArrayList<>();
        JsonObject jsonObject = new JsonParser().parse(status).getAsJsonObject();
        JsonArray vmArray = jsonObject.getAsJsonArray("data");
        for (JsonElement vmItem : vmArray) {
            JsonArray netArray = vmItem.getAsJsonObject().getAsJsonArray("networks");
            for (JsonElement netItem : netArray) {
                experienceInfoList
                    .add(new Experience(serviceName, "", netItem.getAsJsonObject().get("ip").getAsString()));
            }
        }
        return experienceInfoList;
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
            Map<String, String> deployParams = new HashMap<>();
            deployParams.put(APP_INSTANCE_ID, appInstanceId);
            deployParams.put(USER_ID, userId);
            deployParams.put(TOKEN, token);
            HttpClientUtil
                .terminateAppInstance(host.getProtocol(), host.getLcmIp(), host.getPort(), appInstanceId, userId,
                    token);
            // delete package of hosts
            boolean deleteHostRes = HttpClientUtil
                .deleteHost(host.getProtocol(), host.getLcmIp(), host.getPort(), deployParams, pkgId,
                    host.getMecHost());
            if (!deleteHostRes) {
                LOGGER.error("delete host records failed after instantiateApp.");
                return false;
            }
            // delete pkg of lcm
            boolean deletePkgRes = HttpClientUtil
                .deletePkg(host.getProtocol(), host.getLcmIp(), host.getPort(), deployParams, pkgId);
            if (!deletePkgRes) {
                LOGGER.error("delete package failed after instantiateApp.");
                return false;
            }
        }
        return true;
    }

    /**
     * get Experience Status.
     *
     * @param packageId packageId.
     * @return
     */
    public ResponseEntity<ResponseObject> getExperienceStatus(String packageId) {
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(appReleasePo.getExperienceStatus(), errMsg, null));
    }

    /**
     * deploy App By Id.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param token token.
     */
    public ResponseEntity<ResponseObject> deployAppById(String appId, String packageId, String userId, String token) {
        String showInfo = "";
        LcmLog lcmLog = new LcmLog();
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        MepHost mepHost = judgeHost(appReleasePo.getDeployMode());
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_FAIL, null);
        LOGGER.info("Get all hosts success.");
        String instanceTenentId = userId;
        Release release = packageRepository.findReleaseById(appId, packageId);
        String filePath = release.getPackageFile().getStorageAddress();
        String appInstanceId = appReleasePo.getAppInstanceId();
        if (StringUtils.isEmpty(appInstanceId)) {
            appInstanceId = UUID.randomUUID().toString();
            Map<String, String> deployParams = new HashMap<>();
            deployParams.put("filePath", filePath);
            deployParams.put(APP_INSTANCE_ID, appInstanceId);
            deployParams.put(USER_ID, userId);
            deployParams.put(TOKEN, token);
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.UPLOADING.getProgress());
            boolean instantRes = deployTestConfigToAppLcm(deployParams, mepHost, appReleasePo, lcmLog);
            if (!instantRes) {
                LOGGER.error("instantiate application failed, response is null");
                return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, lcmLog.getLog()));
            }
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.INSTANTIATED.getProgress());
            appReleasePo.setAppInstanceId(appInstanceId);
            appReleasePo.setInstanceTenentId(instanceTenentId);
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            appReleasePo.setStartExpTime(time.format(new Date()));
            packageMapper.updateAppInstanceApp(appReleasePo);
        }
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.error(SLEEP_FAILED, e.getMessage());
            Thread.currentThread().interrupt();
        }
        // If it is a vm package, it will get the IP in parameter and return it to the foreground
        String workStatus = getWorkStatus(appInstanceId, userId, mepHost, token);
        List<Experience> experienceInfoList;
        if (CONTAINER.equals(appReleasePo.getDeployMode())) {
            experienceInfoList = getExperienceInfo(workStatus, mepHost);
        } else {
            experienceInfoList = getVmExperienceInfo(workStatus, appReleasePo.getAppName());
        }
        try {
            updateExperienceStatus(appReleasePo.getPackageId(), EnumExperienceStatus.GET_STATUS_SUCCESS.getProgress());
            Thread.sleep(500);
        } catch (InterruptedException e) {
            LOGGER.error(SLEEP_FAILED, e.getMessage());
            Thread.currentThread().interrupt();
        }
        errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(experienceInfoList, errMsg, "get app url success."));
    }

    /**
     * get nodeStatus.
     *
     * @param packageId packageId.
     * @param userId userId.
     * @param token token.
     */
    public ResponseEntity<ResponseObject> getNodeStatus(String packageId, String userId, String token) {
        String workStatus = "";
        String showInfo = "";
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_FAIL, null);
        AppReleasePo appReleasePo = packageMapper.findReleaseById(packageId);
        MepHost mepHost = judgeHost(appReleasePo.getDeployMode());
        List<Experience> experienceInfoList = new ArrayList<>();
        LOGGER.info("Get all hosts success.");
        // If the VM application is not released, you can determine whether the vm application is released
        // by checking whether the instance ID is empty.
        // If the appInstanceId is null for a vm application, the appInstanceId is released
        if (StringUtils.isEmpty(appReleasePo.getAppInstanceId()) || StringUtils.isEmpty(userId) || StringUtils
            .isEmpty(token)) {
            return ResponseEntity.ok(new ResponseObject(showInfo, errMsg, "this package not instantiate"));
        }
        workStatus = getWorkStatus(appReleasePo.getAppInstanceId(), userId, mepHost, token);
        if (StringUtils.isEmpty(workStatus)) {
            return ResponseEntity.ok(new ResponseObject(experienceInfoList, errMsg, "this package not instantiate"));
        }
        if (VM.equals(appReleasePo.getDeployMode())) {
            experienceInfoList = getVmExperienceInfo(workStatus, appReleasePo.getAppName());
        } else {
            experienceInfoList = getExperienceInfo(workStatus, mepHost);
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
        JsonObject jsonObject = new JsonParser().parse(workStatus).getAsJsonObject();
        JsonArray array = jsonObject.getAsJsonArray(SERVICES);
        for (JsonElement jsonItem : array) {
            String serviceName = jsonItem.getAsJsonObject().get("serviceName").getAsString();
            String nodePort = jsonItem.getAsJsonObject().get("ports").getAsJsonArray().get(0).getAsJsonObject()
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
        // Call by service nameuser-mgmtLogin interface
        try {
            String accessToken = getAccessToken();
            if (StringUtils.isEmpty(accessToken)) {
                LOGGER.error("call login or clean env interface occur error,accesstoken is empty");
                return false;
            }
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
                    cleanTestEnv(packageObj.getPackageId(), accessToken);
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
