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
import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.AbstractFileChecker;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostBody;
import org.edgegallery.appstore.domain.model.system.lcm.UploadedFile;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.domain.shared.exceptions.CustomException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.edgegallery.appstore.domain.shared.exceptions.HostException;
import org.edgegallery.appstore.domain.shared.exceptions.IllegalRequestException;
import org.edgegallery.appstore.infrastructure.persistence.system.HostMapper;
import org.edgegallery.appstore.infrastructure.persistence.system.UploadedFileMapper;
import org.edgegallery.appstore.infrastructure.util.BusinessConfigUtil;
import org.edgegallery.appstore.infrastructure.util.HttpClientUtil;
import org.edgegallery.appstore.infrastructure.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service("systemService")
public class SystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemService.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String TENANT_ID = "tenantId";

    private static final int VNC_PORT = 22;

    private static final String ADD_MEC_HOST_FAILED = "add mec host to lcm failed.";

    private static final String HEALTH_CHECK_FAILED = "health check failed, current ip or port can't be used.";

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }

    /**
     * getAllHosts.
     * @param name name.
     * @param os os.
     * @return
     */
    public List<MepHost> getAllHosts(String name, String os) {
        LOGGER.info("Get all hosts success.");
        return hostMapper.getHostsByCondition(name, os);
    }

    /**
     * createHost.
     *
     * @return
     */
    @Transactional
    public Either<ResponseObject, Boolean> createHost(MepHost host, String token) {
        if (StringUtils.isBlank(host.getUserId())) {
            LOGGER.error("Create host failed, userId is empty");
            throw new EntityNotFoundException("Create host failed, userId is empty.", ResponseConst.USERID_IS_EMPTY);
        }
        boolean addMecHostRes = addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            throw new EntityNotFoundException(ADD_MEC_HOST_FAILED, ResponseConst.ADD_HOST_TO_LCM_FAILED);
        }
        // upload config file
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = uploadFileToLcm(host, uploadedFile.getFilePath(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                throw new EntityNotFoundException("Create host failed,upload config file error.",
                    ResponseConst.UPLOAD_CONFIG_FILE_ERROR);
            }
        }
        host.setHostId(UUID.randomUUID().toString()); // no need to set hostId by user
        host.setVncPort(VNC_PORT);
        int ret = hostMapper.createHost(host);
        if (ret <= 0) {
            LOGGER.error("Create host failed ");
            throw new EntityNotFoundException("Can not create a host.", ResponseConst.CREATE_HOST_ERROR);
        }
        LOGGER.info("Crete host {} success ", host.getHostId());
        return Either.right(true);

    }

    /**
     * deleteHost.
     *
     * @return
     */
    @Transactional
    public Either<ResponseObject, Boolean> deleteHost(String hostId, String token) {
        MepHost host = hostMapper.getHost(hostId);
        //health check
        boolean healRes = HttpClientUtil.getHealth(host.getProtocol(), host.getLcmIp(), host.getPort());
        if (!healRes) {
            LOGGER.error(HEALTH_CHECK_FAILED);
            throw new HostException(HEALTH_CHECK_FAILED, ResponseConst.HEALTH_CHECK_FAILED);
        }
        // delete mechost from lcm
        boolean deleteLcmMecHostRes = deleteMecHostFromLcm(host, token);
        if (!deleteLcmMecHostRes) {
            String msg = "delete mec host from lcm fail";
            LOGGER.error(msg);
            throw new HostException(ADD_MEC_HOST_FAILED, ResponseConst.DELETE_HOST_FROM_LCM_FAILED);
        }
        int res = hostMapper.deleteHost(hostId);
        if (res < 1) {
            LOGGER.error("Delete host {} failed", hostId);
            throw new EntityNotFoundException("Delete host failed.", ResponseConst.DELETE_HOST_FAILED);
        }
        LOGGER.info("Delete host {} success", hostId);
        return Either.right(true);
    }

    /**
     * updateHost.
     *
     * @return
     */
    @Transactional
    public Either<ResponseObject, Boolean> updateHost(String hostId, MepHost host, String token) {
        //health check
        boolean healRes = HttpClientUtil.getHealth(host.getProtocol(), host.getLcmIp(), host.getPort());
        if (!healRes) {
            LOGGER.error(HEALTH_CHECK_FAILED);
            throw new HostException(HEALTH_CHECK_FAILED, ResponseConst.HEALTH_CHECK_FAILED);
        }
        // add mechost to lcm
        boolean addMecHostRes = addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            throw new HostException(ADD_MEC_HOST_FAILED, ResponseConst.ADD_HOST_TO_LCM_FAILED);
        }
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = uploadFileToLcm(host, uploadedFile.getFilePath(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                throw new HostException("Create host failed,upload config file error.",
                    ResponseConst.UPLOAD_CONFIG_FILE_ERROR);
            }
        }
        MepHost currentHost = hostMapper.getHost(hostId);
        if (currentHost == null) {
            LOGGER.error("Can not find host by {}", hostId);
            throw new HostException("Can not find the host.}", ResponseConst.NOT_GET_HOST_ERROR);
        }

        host.setHostId(hostId); // no need to set hostId by user
        host.setUserId(currentHost.getUserId());
        int ret = hostMapper.updateHostSelected(host);
        if (ret > 0) {
            LOGGER.info("Update host {} success", hostId);
            return Either.right(true);
        }
        LOGGER.error("Update host {} failed", hostId);
        throw new HostException("Can not update the host.", ResponseConst.RET_UPDATE_HOST_FAILED);
    }

    /**
     * getHost.
     *
     * @return
     */
    public Either<ResponseObject, MepHost> getHost(String hostId) {
        MepHost host = hostMapper.getHost(hostId);
        if (host != null) {
            LOGGER.info("Get host {} success", hostId);
            return Either.right(host);
        } else {
            LOGGER.error("Can not find host by {}", hostId);
            ErrorMessage errMsg = new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), null);
            ResponseObject error = new ResponseObject(null, errMsg, "Can not find the host.");
            return Either.left(error);
        }
    }

    /**
     * uploadFileToLcm.
     * @param host mecHost.
     * @param filePath filePath.
     * @param token token.
     * @return
     */
    public boolean uploadFileToLcm(MepHost host, String filePath, String token) {
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + filePath);
        RestTemplate restTemplate = RestTemplateBuilder.create();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("configFile", new FileSystemResource(file));
        body.add("hostIp", host.getMecHost());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            String url = getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort()) + Consts.APP_LCM_UPLOAD_FILE
                .replace(TENANT_ID, host.getUserId());
            LOGGER.info(" upload file url is {}", url);
            response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("upload file lcm log:{}", response);
        } catch (Exception e) {
            LOGGER.error("Failed to upload file lcm, exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to upload file lcm, filePath is {}", filePath);
        return false;
    }

    private boolean deleteMecHostFromLcm(MepHost host, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(null, headers);
        String url = getUrlPrefix(host.getProtocol(),  host.getLcmIp(), host.getPort()) + Consts.APP_LCM_DELETE_MECHOST
            .replace(TENANT_ID, host.getUserId()).replace("hostIp", host.getMecHost());
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
            LOGGER.info("APPlCM delete mec host log:{}", response);
        } catch (CustomException e) {
            LOGGER.error("Failed delete mec host mecHost exception {}", e.getBody());
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed delete mec host mecHost is {} exception {}", host.getMecHost(), e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed to delete mec host which mecHost is {}", host.getMecHost());
        return false;
    }

    private boolean addMecHostToLcm(MepHost host) {
        MecHostBody body = new MecHostBody();
        body.setAffinity(host.getArchitecture());
        body.setCity(host.getAddress());
        body.setMechostIp(host.getMecHost());
        body.setMechostName(host.getName());
        if (host.getOs().equals("OpenStack") || host.getOs().equals("FusionSphere")) {
            body.setVim("OpenStack");
        } else {
            body.setVim("K8s");
        }
        body.setOrigin("appstore");
        //add headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(body), headers);
        String url = getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort()) + Consts.APP_LCM_ADD_MECHOST
            .replace(TENANT_ID, host.getUserId());
        LOGGER.info("add mec host url:{}", url);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("add mec host to lcm log:{}", response);
        } catch (CustomException e) {
            LOGGER.error("Failed add mec host to lcm exception {}", e.getBody());
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Failed add mec host to lcm exception {}", e.getMessage());
            return false;
        }
        if (response.getStatusCode() == HttpStatus.OK) {
            return true;
        }
        LOGGER.error("Failed add mec host to lcm");
        return false;
    }

    /**
     * uploadFile.
     *
     * @return
     */
    public Either<ResponseObject, UploadedFile> uploadFile(String userId, MultipartFile uploadFile) {
        LOGGER.info("Begin upload file");
        UploadedFile result = new UploadedFile();
        String fileName = uploadFile.getOriginalFilename();
        if (!AbstractFileChecker.isValid(fileName)) {
            LOGGER.error("File Name is invalid.");
            throw new IllegalRequestException("File Name is invalid.", ResponseConst.RET_FILE_NAME_INVALID);
        }
        String fileId = UUID.randomUUID().toString();
        String upLoadDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath();
        String fileRealPath = upLoadDir + fileId;
        File dir = new File(upLoadDir);
        if (!dir.isDirectory()) {
            boolean isSuccess = dir.mkdirs();
            if (!isSuccess) {
                throw new FileOperateException("create folder failed", ResponseConst.RET_MAKE_DIR_FAILED);
            }
        }

        File newFile = new File(fileRealPath);
        try {
            uploadFile.transferTo(newFile);
            result.setFileName(fileName);
            result.setFileId(fileId);
            result.setUserId(userId);
            result.setUploadDate(new Date());
            result.setTemp(true);
            result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId);
            uploadedFileMapper.saveFile(result);
        } catch (IOException e) {
            LOGGER.error("Failed to save file with IOException. {}", e.getMessage());
            throw new FileOperateException("save file exception.", ResponseConst.RET_SAVE_FILE_EXCEPTION);
        }
        LOGGER.info("upload file success {}", fileName);
        //upload success
        result.setFilePath("");
        return Either.right(result);
    }


}
