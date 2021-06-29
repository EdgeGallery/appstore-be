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

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import com.spencerwi.either.Either;
import java.io.File;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.system.MepCreateHost;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.MecHostBody;
import org.edgegallery.appstore.domain.model.system.lcm.UploadedFile;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.exceptions.CustomException;
import org.edgegallery.appstore.infrastructure.persistence.system.HostMapper;
import org.edgegallery.appstore.infrastructure.persistence.system.UploadedFileMapper;
import org.edgegallery.appstore.infrastructure.util.CustomResponseErrorHandler;
import org.edgegallery.appstore.infrastructure.util.FormatRespDto;
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

@Service("systemService")
public class SystemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemService.class);

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final int VNC_PORT = 22;

    @Autowired
    private HostMapper hostMapper;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Autowired
    private ProjectService projectService;

    private static String getUrlPrefix(String protocol, String ip, int port) {
        return protocol + "://" + ip + ":" + port;
    }

    /**
     * getALlHosts.
     *
     * @return
     */
    public Page<MepHost> getAllHosts(String userId, String name, String ip, int limit, int offset) {
        PageHelper.offsetPage(offset, limit);
        List<MepHost> mapHosts = hostMapper.getHostsByCondition(userId, name, ip);
        PageInfo pageInfo = new PageInfo<MepHost>(mapHosts);
        LOGGER.info("Get all hosts success.", mapHosts);
        return new Page<MepHost>(pageInfo.getList(), limit, offset, pageInfo.getTotal());
    }

    /**
     * createHost.
     *
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Boolean> createHost(MepCreateHost host, String token) {
        if (StringUtils.isBlank(host.getUserId())) {
            LOGGER.error("Create host failed, userId is empty");
            return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "userId is empty"));
        }
        boolean addMecHostRes = addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // upload config file
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = uploadFileToLcm(host.getLcmIp(), host.getPort(), uploadedFile.getFilePath(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
                return Either.left(dto);
            }
        }
        host.setHostId(UUID.randomUUID().toString()); // no need to set hostId by user
        host.setVncPort(VNC_PORT);
        int ret = hostMapper.createHost(host);
        if (ret > 0) {
            LOGGER.info("Crete host {} success ", host.getHostId());
            return Either.right(true);
        }
        LOGGER.error("Create host failed ");
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not create a host."));
    }

    /**
     * deleteHost.
     *
     * @return
     */
    @Transactional
    public Either<FormatRespDto, Boolean> deleteHost(String hostId) {
        int res = hostMapper.deleteHost(hostId);
        if (res < 1) {
            LOGGER.error("Delete host {} failed", hostId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "delete failed.");
            return Either.left(error);
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
    public Either<FormatRespDto, Boolean> updateHost(String hostId, MepCreateHost host, String token) {
        //health check
        String healRes = HttpClientUtil.getHealth(host.getProtocol(), host.getLcmIp(), host.getPort());
        if (healRes == null) {
            String msg = "health check faild,current ip or port cann't be used!";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        // add mechost to lcm
        boolean addMecHostRes = addMecHostToLcm(host);
        if (!addMecHostRes) {
            String msg = "add mec host to lcm fail";
            LOGGER.error(msg);
            FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
            return Either.left(dto);
        }
        if (StringUtils.isNotBlank(host.getConfigId())) {
            // upload file
            UploadedFile uploadedFile = uploadedFileMapper.getFileById(host.getConfigId());
            boolean uploadRes = uploadFileToLcm(host.getLcmIp(), host.getPort(), uploadedFile.getFilePath(), token);
            if (!uploadRes) {
                String msg = "Create host failed,upload config file error";
                LOGGER.error(msg);
                FormatRespDto dto = new FormatRespDto(Response.Status.BAD_REQUEST, msg);
                return Either.left(dto);
            }
        }
        MepHost currentHost = hostMapper.getHost(hostId);
        if (currentHost == null) {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }

        host.setHostId(hostId); // no need to set hostId by user
        host.setUserId(currentHost.getUserId());
        int ret = hostMapper.updateHostSelected(host);
        if (ret > 0) {
            LOGGER.info("Update host {} success", hostId);
            return Either.right(true);
        }
        LOGGER.error("Update host {} failed", hostId);
        return Either.left(new FormatRespDto(Response.Status.BAD_REQUEST, "Can not update the host"));
    }

    /**
     * getHost.
     *
     * @return
     */
    public Either<FormatRespDto, MepHost> getHost(String hostId) {
        MepHost host = hostMapper.getHost(hostId);
        if (host != null) {
            LOGGER.info("Get host {} success", hostId);
            return Either.right(host);
        } else {
            LOGGER.error("Can not find host by {}", hostId);
            FormatRespDto error = new FormatRespDto(Response.Status.BAD_REQUEST, "Can not find the host.");
            return Either.left(error);
        }
    }

    private boolean uploadFileToLcm(String hostIp, int port, String filePath, String token) {
        File file = new File(InitConfigUtil.getWorkSpaceBaseDir() + filePath);
        RestTemplate restTemplate = RestTemplateBuilder.create();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("configFile", new FileSystemResource(file));
        body.add("hostIp", hostIp);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set(Consts.ACCESS_TOKEN_STR, token);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response;
        try {
            String url = getUrlPrefix("https", hostIp, port) + Consts.APP_LCM_UPLOAD_FILE;
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

    private boolean addMecHostToLcm(MepCreateHost host) {
        MecHostBody body = new MecHostBody();
        body.setAffinity(host.getArchitecture());
        body.setCity(host.getAddress());
        body.setMechostIp(host.getMecHost());
        body.setMechostName(host.getName());
        body.setVim(host.getOs());
        body.setOrigin("developer");
        //add headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Gson gson = new Gson();
        HttpEntity<String> requestEntity = new HttpEntity<>(gson.toJson(body), headers);
        String url = getUrlPrefix(host.getProtocol(), host.getLcmIp(), host.getPort()) + Consts.APP_LCM_ADD_MECHOST;
        LOGGER.info("add mec host url:{}", url);
        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
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

}
