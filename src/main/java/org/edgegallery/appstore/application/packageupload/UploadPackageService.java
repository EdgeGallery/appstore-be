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

package org.edgegallery.appstore.application.packageupload;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import java.io.File;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("UploadPackageService")
public class UploadPackageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadPackageService.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Autowired
    UploadHelper uploadHelper;

    @Value("${thirdSystem.url}")
    private String thirdSystemHost;

    /**
     * uploadPackage.
     *
     * @param filePath file path
     * @return JSONObject
     */
    public JSONObject uploadPackage(String filePath, String packageId, String meaoId, String token, String progressId) {
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        String taskName = fileName.substring(0, fileName.indexOf("."));
        JSONObject reqJson = new JSONObject();
        reqJson.put("taskName", taskName);
        reqJson.put("packageId", packageId);
        reqJson.put("meaoId", meaoId);
        reqJson.put("progressId", progressId);

        ServiceDef serviceDef = new ServiceDef();
        serviceDef.setName(taskName);
        serviceDef.setServiceType("vnfpackage");
        serviceDef.setSpecification("APP");
        serviceDef.setAction("create");
        serviceDef.setMode("normal");
        serviceDef.setFileName(fileName);
        JSONObject vnfpackageInfo = new JSONObject();
        vnfpackageInfo.put("serviceDef", serviceDef);
        reqJson.put("vnfpackageInfo", vnfpackageInfo);

        // query meao info from third party system by meaoId
        ThirdSystem meaoInfo = getMeaoInfo(meaoId, token);
        if (meaoInfo == null) {
            LOGGER.error("get meao info fail.");
            throw new AppException("get meao info fail.");
        }

        reqJson.put("vendor", meaoInfo.getVendor());
        String meaoUrl = meaoInfo.getUrl();
        LOGGER.info("meaoUrl: {}", meaoUrl);

        JSONObject session = getMeaoSession(meaoUrl, meaoInfo.getUsername(), meaoInfo.getPassword(),
            meaoInfo.getVendor());
        String csrfToken = session.getString("csrfToken");
        String cookie = session.getString("session");

        String meaoHost = meaoUrl.split("//")[1];
        return uploadHelper.uploadBigSoftware(filePath, reqJson, csrfToken, cookie, meaoHost);
    }

    private JSONObject getMeaoSession(String meaoUrl, String username, String password, String vendor) {
        String url = thirdSystemHost + String.format(Consts.MEAO_SESSION_URL, vendor);

        JSONObject obj = new JSONObject();
        obj.put("meaoUrl", meaoUrl);
        obj.put("username", username);
        obj.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> request = new HttpEntity<>(obj, headers);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.POST, request, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(response.getStatusCode())) {
                return JSON.parseObject(response.getBody());
            }
        } catch (RestClientException e) {
            LOGGER.error("Upload file shard failed, exception {}", e.getMessage());
        }
        throw new AppException("Get meao session failed");
    }

    /**
     * get meao info from third system.
     * @param meaoId meaoId
     * @param token token
     * @return ThirdSystem
     */
    public ThirdSystem getMeaoInfo(String meaoId, String token) {
        String url = thirdSystemHost + Consts.THIRD_SYSTEM_URL + "/" + meaoId;
        HttpHeaders headers = new HttpHeaders();
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.GET, request, String.class);
            if (HttpStatus.OK.equals(response.getStatusCode()) || HttpStatus.ACCEPTED
                .equals(response.getStatusCode())) {
                return new Gson().fromJson(response.getBody(), ThirdSystem.class);
            }
            LOGGER.error("Failed to query meao info from third system, code is {}", response.getStatusCode());
        } catch (RestClientException e) {
            LOGGER.error("Failed to query meao info from third system, exception {}", e.getMessage());
        }
        throw new AppException("Get meao info failed.");
    }
}
