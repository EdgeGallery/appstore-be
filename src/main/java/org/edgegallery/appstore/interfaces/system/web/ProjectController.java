/*
 *    Copyright 2021-2022 Huawei Technologies Co., Ltd.
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
import javax.servlet.http.HttpServletRequest;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.app.ErrorRespDto;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.util.ResponseDataUtil;
import org.edgegallery.appstore.interfaces.app.facade.dto.AppDto;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "projects")
@RequestMapping("/mec/appstore/v1")
@Api(tags = "Project")
@Validated
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * show application.
     *
     * @param appId appId.
     * @param packageId packageId.
     * @param userId userId.
     * @param request request.
     */
    @PostMapping(value = "/experience/deploy", produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app detail app id.", response = AppDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<ResponseObject> deployAppById(@ApiParam(value = "app id") @RequestParam("appId") String appId,
        @ApiParam(value = "package id") @RequestParam("packageId") String packageId,
        @ApiParam(value = "user id") @RequestParam("userId") String userId, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        return projectService.deployAppById(appId, packageId, userId, token);
    }

    @GetMapping(value = "/experience/packages/{packageId}/status",
        produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app detail app id.", response = AppDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<ResponseObject> getExperienceStatus(
        @ApiParam(value = "packageId", required = true) @PathVariable("packageId") String packageId) {
        return projectService.getExperienceStatus(packageId);
    }

    /**
     * clean test env.
     *
     * @param packageId packageId.
     * @param request request.
     */
    @PostMapping(value = "/experience/clean", produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @ApiOperation(value = "add app store.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Boolean> clean(
        @ApiParam(value = "packageId", required = true) @RequestParam("packageId") String packageId,
        HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        Either<ResponseObject, Boolean> either = projectService.cleanTestEnv(packageId, token);
        return ResponseDataUtil.buildResponse(either);
    }

    /**
     * get pod workStatus.
     */
    @ApiOperation(value = "get container workStatus", response = Boolean.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/experience/container/workStatus", method = RequestMethod.GET,
        produces = javax.ws.rs.core.MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<ResponseObject> getNodeStatus(
        @ApiParam(value = "package id") @RequestParam("packageId") String packageId,
        @ApiParam(value = "user id") @RequestParam("userId") String userId, HttpServletRequest request) {
        String token = request.getHeader(Consts.ACCESS_TOKEN_STR);
        return projectService.getNodeStatus(packageId, userId, token);
    }
}
