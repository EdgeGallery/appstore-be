package org.edgegallery.appstore.interfaces.appstore.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.interfaces.appstore.facade.AppStoreServiceFacade;
import org.edgegallery.appstore.interfaces.appstore.facade.dto.AppStoreDto;
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
@RestSchema(schemaId = "appStore")
@RequestMapping("/mec/appstore/poke")
@Api(tags = {"APP Store Controller"})
@Validated
public class AppStoreController {
    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    @Autowired
    private AppStoreServiceFacade appStoreServiceFacade;

    /**
     * add app store.
     */
    @PostMapping(value = "/appstores", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "add app store.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<AppStoreDto> addAppStore(@RequestBody AppStoreDto appStoreDto) {
        return appStoreServiceFacade.addAppStore(appStoreDto);
    }

    /**
     * delete app store.
     */
    @DeleteMapping(value = "/appstores/{appStoreId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete app store.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> deleteAppStore(
        @ApiParam(value = "app store id") @PathVariable("appStoreId") @Pattern(regexp = REG_UUID) @NotNull(
            message = "appStoreId should not be null.") String appStoreId) {
        return appStoreServiceFacade.deleteAppStore(appStoreId);
    }

    /**
     * edit app store.
     */
    @PutMapping(value = "/appstores/{appStoreId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "edit app store.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<AppStoreDto> editAppStore(@RequestBody AppStoreDto appStoreDto,
        @PathVariable("appStoreId") @Pattern(regexp = REG_UUID) @NotNull(
            message = "appStoreId should not be null.") String appStoreId) {
        appStoreDto.setAppStoreId(appStoreId);
        return appStoreServiceFacade.editAppStore(appStoreDto);
    }

    /**
     * query app stores.
     */
    @GetMapping(value = "/appstores", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "query app stores.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<List<AppStoreDto>> queryAppStores(
        @ApiParam(value = "app store name") @PathParam("appStoreName") String appStoreName,
        @ApiParam(value = "app store company") @PathParam("company") String company) {
        return appStoreServiceFacade.queryAppStores(appStoreName, company);
    }

    /**
     * query app store.
     */
    @GetMapping(value = "/appstores/{appStoreId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "query app store.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<AppStoreDto> queryAppStore(
        @ApiParam(value = "app store id") @PathVariable("appStoreId") @Pattern(regexp = REG_UUID) @NotNull(
            message = "appStoreId should not be null.") String appStoreId) {
        return appStoreServiceFacade.queryAppStore(appStoreId);
    }
}
