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

package org.edgegallery.appstore.interfaces.meao.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;
import org.edgegallery.appstore.interfaces.meao.facade.ProgressFacade;
import org.edgegallery.appstore.interfaces.meao.facade.dto.PackageProgressDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "progress")
@RequestMapping("/mec/appstore/v1/upload_progress")
@Api(tags = {"Progress Controller"})
@Validated
public class ProgressController {
    @Autowired
    ProgressFacade progressFacade;

    /**
     * create a progress.
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create a progress", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> createProgress(@RequestBody PackageUploadProgress progress) {
        return progressFacade.createProgress(progress);
    }

    /**
     * query a progress.
     */
    @GetMapping(value = "/{progressId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get a progress", response = PackageUploadProgress.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<PackageUploadProgress> getProgress(
        @ApiParam(value = "progressId") @PathVariable("progressId") String progressId) {
        return progressFacade.getProgress(progressId);
    }

    /**
     * query  progress by package and meao.
     */
    @GetMapping(value = "/package/{packageId}/meao/{meaoId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get a progress by package and meao", response = PackageUploadProgress.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<List<PackageUploadProgress>> getProgressByPackageAndMeao(
        @ApiParam(value = "packageId") @PathVariable("packageId") String packageId,
        @ApiParam(value = "meaoId") @PathVariable("meaoId") String meaoId) {
        return progressFacade.getProgressByPackageAndMeao(packageId, meaoId);
    }

    /**
     * query  progress by package id.
     */
    @GetMapping(value = "/package/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get a progress by package id", response = PackageUploadProgress.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<List<PackageProgressDto>> getProgressByPackageId(
        @ApiParam(value = "packageId") @PathVariable("packageId") String packageId, HttpServletRequest request) {
        return progressFacade.getProgressByPackageId(packageId,(String) request.getAttribute(Consts.ACCESS_TOKEN_STR));
    }

    /**
     * update a progress.
     */
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update a progress", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> updateProgress(@RequestBody PackageUploadProgress progress) {
        return progressFacade.updateProgress(progress);
    }

    /**
     * delete a progress.
     */
    @DeleteMapping(value = "/{progressId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete a progress", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> deleteProgress(
        @ApiParam(value = "progressId") @PathVariable("progressId") String progressId) {
        return progressFacade.deleteProgress(progressId);
    }
}
