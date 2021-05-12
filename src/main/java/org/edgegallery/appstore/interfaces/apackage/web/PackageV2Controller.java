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
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.PushablePackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "v2package")
@RequestMapping("/mec/appstore/v2")
@Api(tags = {"Package V2Controller"})
@Validated
public class PackageV2Controller {

    private static final String REG_USER_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String ACCESS_TOKEN = "access_token";

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    @Autowired
    private PushablePackageServiceFacade pushablePackageServiceFacade;

    /**
     *
     * @param userId userId.
     * @param limitSize limitSize.
     * @param offsetPage offsetPage.
     * @param appName appName.
     * @param order order by.
     * @param prop order type.
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
        @RequestParam("userId") @Pattern(regexp = REG_USER_ID) String userId,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limitSize")
            int limitSize,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offsetPage")
            int offsetPage, @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "query order") @RequestParam("order") String order,
        @ApiParam(value = "query condition") @RequestParam("prop") String prop, HttpServletRequest request) {
        return ResponseEntity.ok(packageServiceFacade
            .getPackageByUserIdV2(userId, limitSize, offsetPage, appName, prop, order,
                (String) request.getAttribute(ACCESS_TOKEN)));
    }

    @GetMapping(value = "/packages/pushable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pushable packages", response = PushablePackageDto.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<PushablePackageDto>> queryAllPushablePackagesV2(
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limitSize")
            int limitSize,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offsetPage")
            int offsetPage, @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "query order") @RequestParam("order") String order,
        @ApiParam(value = "query condition") @RequestParam("prop") String prop) {
        return ResponseEntity
            .ok(pushablePackageServiceFacade.queryAllPushablePackagesV2(limitSize, offsetPage, appName, order, prop));
    }

    @GetMapping(value = "/packages/pullable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pullable packages", response = PushablePackageDto.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<List<PushablePackageDto>> queryAllPullablePackages(
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limitSize")
            int limitSize,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offsetPage")
            int offsetPage,
        @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "query order") @RequestParam("order") String order,
        @ApiParam(value = "sort condition") @RequestParam("prop") String prop) {
        return pushablePackageServiceFacade.queryAllPullablePackagesV2(limitSize, offsetPage, appName, order, prop);
    }

    @GetMapping(value = "/packages/{platformId}/pullable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pullable packages by platform id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<PushablePackageDto>> getPullablePackagesV2(
        @ApiParam(value = "platform Id") @PathVariable("platformId") String platformId,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limitSize")
            int limitSize,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offsetPage")
            int offsetPage,
        @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "query order") @RequestParam("order") String order,
        @ApiParam(value = "query condition") @RequestParam("prop") String prop,
        HttpServletRequest request) {
        return pushablePackageServiceFacade
            .getPullablePackagesV2(platformId, limitSize, offsetPage, order, prop, appName,
                (String) request.getAttribute("userId"));
    }

}
