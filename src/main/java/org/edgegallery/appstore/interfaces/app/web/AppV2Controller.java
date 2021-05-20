/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.app.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.app.facade.AppParam;
import org.edgegallery.appstore.interfaces.app.facade.AppServiceFacade;
import org.edgegallery.appstore.interfaces.app.facade.dto.AppDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppReqDto;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "v2app")
@RequestMapping("/mec/appstore/v2")
@Api(tags = {"APP V2Controller"})
@Validated
public class AppV2Controller {

    private static final String REG_USER_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REG_APP_ID = "[0-9a-f]{32}";

    private static final int MAX_DETAILS_STRING_LENGTH = 1024;

    private static final String ACCESS_TOKEN = "access_token";

    @Autowired
    private AppServiceFacade appServiceFacade;

    @PostMapping(value = "/query/apps", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app list by condition. if the userId is null, it will return all published apps, "
        + "else will return all apps.", response = AppDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<Page<AppDto>> queryAppsByCondV2(
        @ApiParam(value = "QueryAppReqDto", required = true) @RequestBody QueryAppReqDto queryAppReqDto) {
        return appServiceFacade.queryAppsByCondV2(queryAppReqDto);
    }

    /**
     * app find function.
     */
    @GetMapping(value = "/apps/{appId}/packages", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app package list by appId", response = PackageDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "resource not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<List<PackageDto>> queryPackageListByAppId(
        @ApiParam(value = "appId") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId,
        @QueryParam("userId") @Pattern(regexp = REG_USER_ID) String userId,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset,
        HttpServletRequest request) {
        return appServiceFacade
            .findAllPackages(appId, userId, limit, offset, (String) request.getAttribute(ACCESS_TOKEN));
    }

    /**
     * app upload function.
     */
    @PostMapping(value = "/apps", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "upload app package", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> appRegisteringV2(
        @RequestParam("userId") @Pattern(regexp = REG_USER_ID) String userId,
        @RequestParam("userName") String userName,
        @ApiParam(value = "csar package", required = true) @RequestPart("file") MultipartFile file,
        @ApiParam(value = "file icon", required = true) @RequestPart("icon") MultipartFile icon,
        @ApiParam(value = "demo file") @RequestPart(name = "demoVideo", required = false) MultipartFile demoVideo,
        @ApiParam(value = "app type", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "type should not be null.") @RequestPart("type") String type,
        @ApiParam(value = "app shortDesc", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "shortDesc should not be null.") @RequestPart("shortDesc") String shortDesc,
        @ApiParam(value = "app showType") @RequestPart(name = "showType", required = false) String showType,
        @ApiParam(value = "app affinity", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "affinity should not be null.") @RequestPart("affinity") String affinity,
        @ApiParam(value = "app industry", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "industry should not be null.") @RequestPart("industry") String industry,
        @ApiParam(value = "test task id") @RequestPart(name = "testTaskId", required = false) String testTaskId,
        HttpServletRequest request) {
        return appServiceFacade.appV2Registering(new User(userId, userName), file,
                new AppParam(type, shortDesc, showType, affinity, industry),
                icon, demoVideo, new AtpMetadata(testTaskId, (String) request.getAttribute(ACCESS_TOKEN)));
    }

    @GetMapping(value = "/apps/{appId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app detail app id.", response = AppDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<ResponseObject> queryAppByIdV2(
        @ApiParam(value = "app id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId) {
        return appServiceFacade.queryByAppIdV2(appId);
    }
}
