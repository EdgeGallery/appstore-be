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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.PushablePackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppCtrlDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "v2package")
@RequestMapping("/mec/appstore/v2")
@Api(tags = {"Package V2Controller"})
@Validated
public class PackageV2Controller {

    private static final String ACCESS_TOKEN = "access_token";

    private static final String REG_APP_ID = "[0-9a-f]{32}";

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    @Autowired
    private PushablePackageServiceFacade pushablePackageServiceFacade;

    /**
     *query all the package owned by the user, and sorted by.
     *
     * @param userId userId.
     * @param limit limit.
     * @param offset offset.
     * @param appName appName.
     * @param sortType sortType by.
     * @param sortItem sortType type.
     * @param request HttpServletRequest.
     * @return Page object.
     */
    @GetMapping(value = "/packages", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app package by user id", response = PackageDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<PackageDto>> getPackageByUserIdV2(
        @ApiParam(value = "userId", required = false) @RequestParam("userId") String userId,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset,
        @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "app status", required = false) @RequestParam("status") String status,
        @ApiParam(value = "query sortType") @RequestParam("sortType") String sortType,
        @ApiParam(value = "query condition") @RequestParam("sortItem") String sortItem, HttpServletRequest request) {
        QueryAppCtrlDto queryCtrl = new QueryAppCtrlDto(offset,limit, sortItem, sortType);
        return ResponseEntity.ok(packageServiceFacade
            .getPackageByUserIdV2(userId, appName, status, queryCtrl, (String)request.getAttribute(ACCESS_TOKEN)));
    }

    @PostMapping(value = "/apps/{appId}/packages/{packageId}/action/publish", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "publish the package.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> publishPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(regexp = REG_APP_ID) String packageId,
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId) {
        return packageServiceFacade.publishPackageV2(appId, packageId);
    }

    @GetMapping(value = "/apps/{appId}/packages/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app package by package id", response = PackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> getPackageById(
        @ApiParam(value = "app Id") @PathVariable("appId") @Pattern(regexp = REG_APP_ID) String appId,
        @ApiParam(value = "package Id") @PathVariable("packageId") @Pattern(regexp = REG_APP_ID) String packageId,
        HttpServletRequest request) {
        return packageServiceFacade.queryPackageByIdV2(appId, packageId, (String) request.getAttribute(ACCESS_TOKEN));
    }

    @GetMapping(value = "/packages/pushable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pushable packages", response = PushablePackageDto.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<PushablePackageDto>> queryAllPushablePackagesV2(
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit")
            int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset")
            int offset, @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "query sortType") @RequestParam("sortType") String sortType,
        @ApiParam(value = "query condition") @RequestParam("sortItem") String sortItem) {
        return ResponseEntity
            .ok(pushablePackageServiceFacade.queryAllPushablePackagesV2(limit, offset, appName, sortType, sortItem));
    }

    @GetMapping(value = "/packages/pullable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pullable packages", response = PushablePackageDto.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<List<PushablePackageDto>> queryAllPullablePackages(
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit")
            int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset")
            int offset,
        @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "query sortType") @RequestParam("sortType") String sortType,
        @ApiParam(value = "sort condition") @RequestParam("sortItem") String sortItem) {
        return pushablePackageServiceFacade.queryAllPullablePackagesV2(limit, offset, appName, sortType, sortItem);
    }

    /**
     * query all the  pull package under appstoreId, and sorted by.
     *
     * @param platformId appstoreId.
     * @param limit limit.
     * @param offset offset.
     * @param appName appName.
     * @param sortType sortType by.
     * @param sortItem sortType type
     * @param request HttpServletRequest.
     * @return Page Object.
     */
    @GetMapping(value = "/packages/{platformId}/pullable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pullable packages by platform id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<PushablePackageDto>> getPullablePackagesV2(
        @ApiParam(value = "platform Id") @PathVariable("platformId") String platformId,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit")
            int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset")
            int offset,
        @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "query sortType") @RequestParam("sortType") String sortType,
        @ApiParam(value = "query condition") @RequestParam("sortItem") String sortItem,
        HttpServletRequest request) {
        return pushablePackageServiceFacade
            .getPullablePackagesV2(platformId, limit, offset, sortType, sortItem, appName,
                (String) request.getAttribute("userId"));
    }

}
