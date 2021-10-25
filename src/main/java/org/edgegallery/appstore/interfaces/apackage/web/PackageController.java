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

package org.edgegallery.appstore.interfaces.apackage.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PublishAppReqDto;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "package")
@RequestMapping("/mec/appstore/v1")
@Api(tags = {"Package Controller"})
@Validated
public class PackageController {

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    /**
     * delete application package.
     *
     * @param userId user id
     * @param userName user name
     * @param appId app id
     * @param packageId package id
     * @param request token
     */
    @DeleteMapping(value = "/apps/{appId}/packages/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete a package", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "forbidden", response = String.class),
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> unPublishPackage(
        @RequestParam("userId") @Pattern(regexp = Consts.REG_USER_ID) String userId,
        @RequestParam("userName") String userName,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(
            regexp = Consts.REG_APP_ID) String packageId,
        HttpServletRequest request) {
        packageServiceFacade.unPublishPackage(appId, packageId, new User(userId, userName),
            (String) request.getAttribute(Consts.ACCESS_TOKEN_STR));
        return ResponseEntity.ok("delete App package success.");
    }

    /**
     * query package by package id.
     *
     * @param appId app id
     * @param packageId package id
     * @param request request
     * @return packageDto
     */
    @GetMapping(value = "/apps/{appId}/packages/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app package by package id", response = PackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<PackageDto> getPackageById(
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(
            regexp = Consts.REG_APP_ID) String packageId,
        HttpServletRequest request) {
        return ResponseEntity
            .ok(packageServiceFacade.queryPackageById(appId, packageId,
                (String) request.getAttribute(Consts.ACCESS_TOKEN_STR)));
    }

    @GetMapping(value = "/apps/{appId}/packages/{packageId}/action/download", produces = "application/octet-stream")
    @ApiOperation(value = "download the package by package id.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<InputStreamResource> downloadPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "isDownloadImage")
        @RequestParam(value = "isDownloadImage", required = false, defaultValue = "false") boolean isDownloadImage,
        HttpServletRequest request) throws IOException {
        return packageServiceFacade
            .downloadPackage(appId, packageId, isDownloadImage, (String) request.getAttribute(Consts.ACCESS_TOKEN_STR));
    }

    @GetMapping(value = "/apps/{appId}/packages/{packageId}/icon", produces = "application/octet-stream")
    @ApiOperation(value = "get app icon by appId.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<InputStreamResource> downloadIcon(
        @ApiParam(value = "appId", required = true) @PathVariable("appId") @Pattern(
            regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId) throws IOException {
        return packageServiceFacade.downloadIcon(appId, packageId);
    }

    @GetMapping(value = "/apps/{appId}/packages/{packageId}/meao/{meaoId}/action/sync",
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "sync the package to meao.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> syncPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "meao Id") @PathVariable("meaoId") String meaoId, HttpServletRequest request)
        throws IOException {
        return packageServiceFacade.syncPackage(appId, packageId, meaoId,
            (String) request.getAttribute(Consts.ACCESS_TOKEN_STR));
    }

    @PostMapping(value = "/apps/{appId}/packages/{packageId}/files", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get csar file uri by appId and packageId", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> getCsarFileByName(
        @ApiParam(value = "package Id", required = true) @PathVariable("packageId")
        @Pattern(regexp = Consts.REG_APP_ID) String packageId,
        @Length(max = Consts.MAX_DETAILS_STRING_LENGTH) @FormParam("filePath") String filePath,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId) {
        String fileContent = packageServiceFacade.getCsarFileByName(appId, packageId, filePath);
        return new ResponseEntity<>(fileContent, HttpStatus.OK);
    }

    @PostMapping(value = "/apps/{appId}/packages/{packageId}/action/publish", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "publish the package.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> publishPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(
            regexp = Consts.REG_APP_ID) String packageId,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "PublishAppDto", required = true) @RequestBody PublishAppReqDto publishAppReq) {
        return packageServiceFacade.publishPackage(appId, packageId, publishAppReq);
    }

    @PostMapping(value = "/apps/{appId}/packages/{packageId}/action/test", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "test the package by atp.", response = AtpTestDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<AtpTestDto> testPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(
            regexp = Consts.REG_APP_ID) String packageId,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId,
        HttpServletRequest request) {
        return packageServiceFacade.testPackage(appId, packageId,
            (String) request.getAttribute(Consts.ACCESS_TOKEN_STR));
    }

    @GetMapping(value = "/packages", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app package by user id", response = PackageDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<List<PackageDto>> getPackageByUserId(
        @RequestParam("userId") @Pattern(regexp = Consts.REG_USER_ID) String userId, HttpServletRequest request) {
        return packageServiceFacade.getPackageByUserId(userId, (String) request.getAttribute(Consts.ACCESS_TOKEN_STR));
    }

    /**
     * modify app package info.
     */
    @PutMapping(value = "/apps/{appId}/package/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "modify the app.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<PackageDto> modifyAppAttr(
        @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) @NotNull(
            message = "appId should not be null.") String appId,
        @PathVariable("packageId") @Pattern(regexp = Consts.REG_APP_ID) @NotNull(
            message = "packageId should not be null.") String packageId,
        @ApiParam(value = "app industry") @RequestPart(value = "industry", required = false) String industry,
        @ApiParam(value = "app type") @RequestPart(value = "type", required = false) String type,
        @ApiParam(value = "app icon") @RequestPart(value = "icon", required = false) MultipartFile icon,
        @ApiParam(value = "app video") @RequestPart(value = "video", required = false) MultipartFile video,
        @ApiParam(value = "app affinity") @RequestPart(value = "affinity", required = false) String affinity,
        @ApiParam(value = "app shortDesc") @RequestPart(value = "shortDesc", required = false) String shortDesc,
        @ApiParam(value = "app showType") @RequestPart(value = "showType", required = false) String showType,
        @ApiParam(value = "app experienceAble") @RequestPart(name = "experienceAble", required = false)
            String experienceAble) {
        PackageDto packageDto = new PackageDto();
        packageDto.setAppId(appId);
        packageDto.setPackageId(packageId);
        packageDto.setIndustry(industry);
        packageDto.setType(type);
        packageDto.setAffinity(affinity);
        packageDto.setShortDesc(shortDesc);
        packageDto.setShowType(showType);
        packageDto.setExperienceAble(Boolean.parseBoolean(experienceAble));
        return packageServiceFacade.updateAppById(appId, packageId, icon, video, packageDto);
    }
}
