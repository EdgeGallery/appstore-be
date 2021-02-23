/* Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.interfaces.apackage.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.FileNotFoundException;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.interfaces.apackage.facade.PushablePackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PullAppReqDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
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

@Controller
@RestSchema(schemaId = "pushable-package")
@RequestMapping("/mec/appstore/v1/packages")
@Api(tags = {"Pushable Package Controller"})
@Validated
public class PushablePackageController {

    @Autowired
    private PushablePackageServiceFacade pushablePackageServiceFacade;

    @GetMapping(value = "/pushable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pushable packages", response = PushablePackageDto.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<List<PushablePackageDto>> queryAllPushablePackages() {
        return pushablePackageServiceFacade.queryAllPushablePackages();
    }

    @GetMapping(value = "/{packageId}/pushable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get one the pushable packages by id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<PushablePackageDto> getPushablePackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId) {
        return pushablePackageServiceFacade.getPushablePackage(packageId);
    }

    @PostMapping(value = "/{packageId}/action/push", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get one the pushable packages by id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<List<Boolean>> pushPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId,
        @ApiParam(value = "3rd AppStore") @RequestBody() PushTargetAppStoreDto dto) {
        List<Boolean> pushResult = pushablePackageServiceFacade.pushPackage(packageId, dto);
        return ResponseEntity.ok(pushResult);
    }

    @GetMapping(value = "/{packageId}/action/download-package", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "download packages by id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<InputStreamResource> downloadPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId,
        @RequestParam("targetAppstore") String targetAppstore) throws FileNotFoundException {
        return pushablePackageServiceFacade.downloadPackage(packageId, targetAppstore);
    }

    @GetMapping(value = "/{packageId}/action/download-icon", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "download icon by id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<InputStreamResource> downloadIcon(
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId) throws FileNotFoundException {
        return pushablePackageServiceFacade.downloadIcon(packageId);
    }

    /**
     * get pullable packages by id.
     *
     * @param platformId source appstore id
     */
    @GetMapping(value = "/{platformId}/pullable", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pullable packages by platform id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<List<PushablePackageDto>> getPullablePackages(
        @ApiParam(value = "platform Id") @PathVariable("platformId") String platformId) {
        return pushablePackageServiceFacade.getPullablePackages(platformId);
    }

    /**
     * pull package by package id.
     *
     * @param packageId package id
     * @param dto source appStore id and user info
     */
    @PostMapping(value = "/{packageId}/action/pull", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "pull one package by id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<Boolean> pullPackage(
        @ApiParam(value = "package Id") @PathVariable("packageId") String packageId,
        @ApiParam(value = "source AppStore Id and user info") @RequestBody() PullAppReqDto dto) {
        Boolean pullResult = pushablePackageServiceFacade.pullPackage(packageId, dto.getSourceStoreId(),
            new User(dto.getUserId(), dto.getUserName()));
        return ResponseEntity.ok(pullResult);
    }
}
