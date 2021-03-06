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

package org.edgegallery.appstore.interfaces.system.web;

import com.spencerwi.either.Either;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.app.ErrorRespDto;
import org.edgegallery.appstore.domain.model.system.MepCreateHost;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.model.system.lcm.UploadedFile;
import org.edgegallery.appstore.infrastructure.util.FormatRespDto;
import org.edgegallery.appstore.infrastructure.util.ResponseDataUtil;
import org.edgegallery.appstore.interfaces.system.facade.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "system")
@RequestMapping("/mec/appstore/v1/system")
@Api(tags = "system")
@Validated
public class SystemController {

    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private SystemService systemService;

    /**
     * getAllHosts.
     *
     * @return
     */
    @ApiOperation(value = "get all server(build and test app)", response = MepHost.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<List<MepHost>> getAllHosts(
        @ApiParam(value = "name", required = false) @RequestParam(value = "name", required = false) String name,
        @ApiParam(value = "ip", required = false) @RequestParam(value = "ip", required = false) String ip) {
        return ResponseEntity.ok(systemService.getAllHosts(name, ip));
    }

    /**
     * getHost.
     *
     * @return
     */
    @ApiOperation(value = "get one server by hostId", response = MepHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<MepHost> getHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
        @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, MepHost> either = systemService.getHost(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * createHost.
     *
     * @return
     */
    @ApiOperation(value = "create one server", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> createHost(
        @ApiParam(value = "MepHost", required = true) @Validated @RequestBody MepCreateHost host,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = systemService.createHost(host, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * deleteHost.
     *
     * @return
     */
    @ApiOperation(value = "delete one server by hostId", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> deleteHost(@ApiParam(value = "hostId", required = true) @PathVariable("hostId")
        @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId) {
        Either<FormatRespDto, Boolean> either = systemService.deleteHost(hostId);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * modifyHost.
     *
     * @return
     */
    @ApiOperation(value = "update one server by hostId", response = MepCreateHost.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = MepCreateHost.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/hosts/{hostId}", method = RequestMethod.PUT,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> modifyHost(
        @PathVariable("hostId") @Pattern(regexp = REG_UUID, message = "hostId must be in UUID format") String hostId,
        @Validated @RequestBody MepCreateHost host, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<FormatRespDto, Boolean> either = systemService.updateHost(hostId, host, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * upload file.
     */
    @ApiOperation(value = "upload file", response = UploadedFile.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = UploadedFile.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/host/files", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<UploadedFile> uploadFile(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile uploadFile,
        @Pattern(regexp = REG_UUID, message = "userId must be in UUID format")
        @ApiParam(value = "userId", required = true) @RequestParam("userId") String userId) {
        Either<FormatRespDto, UploadedFile> either = systemService.uploadFile(userId, uploadFile);
        return ResponseDataUtil.buildResponse(either);
    }

}
