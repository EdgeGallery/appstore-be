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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.domain.model.app.Chunk;
import org.edgegallery.appstore.domain.model.app.ErrorRespDto;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.app.facade.AppParam;
import org.edgegallery.appstore.interfaces.app.facade.AppServiceFacade;
import org.edgegallery.appstore.interfaces.app.facade.dto.AppDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "app")
@RequestMapping("/mec/appstore/v1")
@Api(tags = {"APP Controller"})
@Validated
public class AppController {

    private static final String REG_USER_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REG_APP_ID = "[0-9a-f]{32}";

    private static final int MAX_COMMON_STRING_LENGTH = 255;

    private static final int MAX_DETAILS_STRING_LENGTH = 1024;

    private static final String ACCESS_TOKEN = "access_token";

    private static final String ACCESS_AUTIORITIES = "authorities";

    @Autowired
    private AppServiceFacade appServiceFacade;

    /**
     * upload image.
     */
    @ApiOperation(value = "upload image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/apps/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> uploadImage(HttpServletRequest request, Chunk chunk) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        return appServiceFacade.uploadImage(isMultipart,chunk);
    }

    /**
     * merge image.
     */
    @ApiOperation(value = "merge image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/apps/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity<Object> merge(@RequestParam(value = "fileName") String fileName,
        @RequestParam(value = "guid") String guid) throws Exception {
        return appServiceFacade.merge(fileName,guid);
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
    public ResponseEntity<RegisterRespDto> appRegistering(
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
        return ResponseEntity.ok(appServiceFacade.appRegistering(new User(userId, userName), file,
            new AppParam(type, shortDesc, showType, affinity, industry), icon, demoVideo,
            new AtpMetadata(testTaskId, (String) request.getAttribute(ACCESS_TOKEN))));
    }

    /**
     * app upload function.
     */
    @PostMapping(value = "/apps/vm-register", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "upload app package", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<RegisterRespDto> appRegister(
        @RequestParam("userId") @Pattern(regexp = REG_USER_ID) String userId,
        @RequestParam("userName") String userName,
        @ApiParam(value = "app fileAddress", required = true) @NotNull(
            message = "address should not be null.") @RequestPart("fileAddress") String fileAddress,
        @ApiParam(value = "file icon", required = true) @RequestPart("icon") MultipartFile icon,
        @ApiParam(value = "demo file") @RequestPart(name = "demoVideo", required = false) MultipartFile demoVideo,
        @ApiParam(value = "app type", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "type should not be null.") @RequestPart("type") String type,
        @ApiParam(value = "app shortDesc", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "shortDesc should not be null.") @RequestPart("shortDesc") String shortDesc,
        @ApiParam(value = "app showType", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "showType should not be null.") @RequestPart("showType") String showType,
        @ApiParam(value = "app affinity", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "affinity should not be null.") @RequestPart("affinity") String affinity,
        @ApiParam(value = "app industry", required = true) @Length(max = MAX_DETAILS_STRING_LENGTH) @NotNull(
            message = "industry should not be null.") @RequestPart("industry") String industry,
        @ApiParam(value = "test task id") @RequestPart(name = "testTaskId", required = false) String testTaskId,
        HttpServletRequest request) throws IOException {
        return appServiceFacade
            .appRegister(new User(userId, userName), new AppParam(type, shortDesc, showType, affinity, industry), icon,
                demoVideo, new AtpMetadata(testTaskId, (String) request.getAttribute(ACCESS_TOKEN)), fileAddress);
    }

    @GetMapping(value = "/apps", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app list by condition. if the userId is null, it will return all published apps, "
        + "else will return all apps.", response = AppDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<List<AppDto>> queryAppsByCond(
        @ApiParam(value = "app name") @Length(max = MAX_COMMON_STRING_LENGTH) @QueryParam("name") String name,
        @ApiParam(value = "app provider") @Length(max = MAX_COMMON_STRING_LENGTH) @QueryParam("provider")
            String provider,
        @ApiParam(value = "app type") @Length(max = MAX_COMMON_STRING_LENGTH) @QueryParam("type") String type,
        @ApiParam(value = "app affinity") @Length(max = MAX_COMMON_STRING_LENGTH) @QueryParam("affinity")
            String affinity, @QueryParam("userId") @Pattern(regexp = REG_USER_ID) String userId) {
        return appServiceFacade.queryAppsByCond(name, provider, type, affinity, userId, 100, 0);
    }

    @GetMapping(value = "/apps/{appId}/action/download", produces = "application/octet-stream")
    @ApiOperation(value = "download the latest version of package.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<InputStreamResource> download(
        @ApiParam(value = "app id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId)
        throws FileNotFoundException {
        return appServiceFacade.downloadApp(appId);
    }

    @GetMapping(value = "/apps/{appId}/icon", produces = "application/octet-stream")
    @ApiOperation(value = "get app icon by appId.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<InputStreamResource> downloadIcon(
        @ApiParam(value = "appId", required = true) @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId)
        throws FileNotFoundException {
        return appServiceFacade.downloadIcon(appId);
    }

    @GetMapping(value = "/apps/{appId}/demoVideo", produces = "video/mp4")
    @ApiOperation(value = "get demo Video by appId.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<byte[]> downloadDemoVideo(
            @ApiParam(value = "appId", required = true) @PathVariable("appId")
            @Pattern(regexp = REG_APP_ID) String appId) {
        return appServiceFacade.downloadDemoVideo(appId);
    }

    @GetMapping(value = "/apps/{appId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app detail app id.", response = AppDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<AppDto> queryAppById(
        @ApiParam(value = "app id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId) {
        return ResponseEntity.ok(AppDto.of(appServiceFacade.queryByAppId(appId)));
    }

    /**
     * app delete function.
     */
    @DeleteMapping(value = "/apps/{appId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete app and package list by id.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "forbidden", response = String.class),
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> deleteAppById(@RequestParam("userId") @Pattern(regexp = REG_USER_ID) String userId,
        @RequestParam("userName") String userName,
        @ApiParam(value = "app id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId,
        HttpServletRequest request) {
        appServiceFacade
            .unPublishApp(appId, new User(userId, userName), (String) request.getAttribute(ACCESS_AUTIORITIES));
        return ResponseEntity.ok("delete App success.");
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
        @QueryParam("userId") @Pattern(regexp = REG_USER_ID) String userId, HttpServletRequest request) {
        return appServiceFacade.findAllPackages(appId, userId, 100, 0, (String) request.getAttribute(ACCESS_TOKEN));
    }
}
