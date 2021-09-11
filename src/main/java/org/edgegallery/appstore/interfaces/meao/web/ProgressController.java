package org.edgegallery.appstore.interfaces.meao.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;
import org.edgegallery.appstore.interfaces.meao.facade.ProgressFacade;
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
