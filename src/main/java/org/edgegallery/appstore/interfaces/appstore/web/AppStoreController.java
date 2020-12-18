package org.edgegallery.appstore.interfaces.appstore.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;

@Controller
@RestSchema(schemaId = "appStore")
@RequestMapping("/mec/appstore/v1")
@Api(tags = {"APP Store Controller"})
@Validated
public class AppStoreController {
    private static final String REG_STORE_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REG_STORE_NAME = "^[a-zA-Z][a-zA-Z0-9_]{5,128}$";

    private static final String REG_APP_ID = "[0-9a-f]{32}";

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
    public ResponseEntity<AppStoreDto> addAppStore(
            @RequestPart("appStoreName") String appStoreName,
            @RequestPart("appStoreVersion") String appStoreVersion,
            @RequestPart("company")  String company,
            @RequestPart("url") String url,
            @ApiParam(value = "app store schema", required = false) @RequestPart("schema") String schema,
            @RequestPart("appPushIntf") String appPushIntf,
            @RequestPart("appdTransId") String appdTransId,
            @RequestPart(value = "description", required = false) String description) {
        AppStoreDto appStoreDto = new AppStoreDto(null, appStoreName, appStoreVersion, company, url, schema,
                appPushIntf, appdTransId, description, null, null);
        System.out.println("addAppStore : " + appStoreDto);
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
            @PathVariable("appStoreId") @Pattern(regexp = REG_STORE_ID) String appStoreId) {
        System.out.println("appStoreId : " + appStoreId);
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
    public ResponseEntity<AppStoreDto> editAppStore(
            @PathVariable("appStoreId") @Pattern(regexp = REG_STORE_ID) String appStoreId,
            @RequestPart(value = "appStoreName", required = false)
            @Pattern(regexp = REG_STORE_NAME) String appStoreName,
            @RequestPart(value = "appStoreVersion", required = false) String appStoreVersion,
            @RequestPart(value = "company", required = false) String company,
            @RequestPart(value = "url", required = false) String url,
            @RequestPart(value = "schema", required = false) String schema,
            @RequestPart(value = "appPushIntf", required = false) String appPushIntf,
            @RequestPart(value = "appdTransId", required = false) String appdTransId,
            @RequestPart(value = "description", required = false)
            @Pattern(regexp = REG_STORE_NAME) String description) {
        AppStoreDto appStoreDto = new AppStoreDto(appStoreId, appStoreName, appStoreVersion,
                company, url, schema, appPushIntf, appPushIntf, description,
                null, null);
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
            @PathParam("appStoreName") String appStoreName,
            @PathParam("company") String company) {
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
            @PathVariable("appStoreId") @Pattern(regexp = REG_STORE_ID) String appStoreId) {
        return appStoreServiceFacade.queryAppStore(appStoreId);
    }
}
