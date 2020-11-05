/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.validation.constraints.Pattern;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "package")
@RequestMapping("/mec/appstore/v1")
@Api(tags = {"Package Controller"})
@Validated
public class PackageController {

    private static final String REG_USER_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REG_USER_NAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

    private static final String REG_APP_ID = "[0-9a-f]{32}";

    private static final int MAX_PATH_STRING_LENGTH = 1024;

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    @DeleteMapping(value = "/apps/{appId}/packages/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete a package", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "forbidden", response = String.class),
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<String> unPublishPackage(@RequestParam("userId") @Pattern(regexp = REG_USER_ID) String userId,
        @RequestParam("userName") @Pattern(regexp = REG_USER_NAME) String userName,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId,
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(regexp = REG_APP_ID) String packageId) {
        packageServiceFacade.unPublishPackage(appId, packageId, new User(userId, userName));
        return ResponseEntity.ok("delete App package success.");
    }

    @GetMapping(value = "/apps/{appId}/packages/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app package by package id", response = PackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<PackageDto> getPackageById(
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId,
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(regexp = REG_APP_ID) String packageId) {
        return new ResponseEntity<>(packageServiceFacade.queryPackageById(appId, packageId), HttpStatus.OK);
    }

    @GetMapping(value = "/apps/{appId}/packages/{packageId}/action/download", produces = "application/octet-stream")
    @ApiOperation(value = "download the package by package id.", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<InputStreamResource> downloadPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(regexp = REG_APP_ID) String packageId,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId)
        throws FileNotFoundException {
        return packageServiceFacade.downloadPackage(appId, packageId);
    }

    @PostMapping(value = "/apps/{appId}/packages/{packageId}/files", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get csar file uri by csarId", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<String> getCsarFileByName(
        @ApiParam(value = "package Id", required = true) @PathVariable("packageId")
        @Pattern(regexp = REG_APP_ID) String packageId,
        @Length(max = MAX_PATH_STRING_LENGTH) @FormParam("filePath") String filePath,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId)
        throws IOException {
        String fileContent = packageServiceFacade.getCsarFileByName(appId, packageId, filePath);
        return new ResponseEntity<>(fileContent, HttpStatus.OK);
    }
}
