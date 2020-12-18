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
import org.hibernate.validator.constraints.Length;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "appStore")
@RequestMapping("/mec/appstore/v1")
@Api(tags = {"APP Store Controller"})
@Validated
public class AppStoreController {
    private static final String REG_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REG_STORE_NAME = "^[a-zA-Z][a-zA-Z0-9_]{5,128}$";

    private static final String REG_SCHEMA = "(http)|(https)";

    private static final int MAX_VERSION_LEN = 64;

    private static final int MAX_COMPANY_LEN = 64;

    private static final int MAX_URL_LEN = 256;

    private static final int MAX_DESC_LEN = 256;

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
            @ApiParam(value = "app store name") @RequestParam("appStoreName")
            @Pattern(regexp = REG_STORE_NAME) String appStoreName,
            @ApiParam(value = "app store version") @RequestParam("appStoreVersion")
            @Length(max = MAX_VERSION_LEN, min = 1) String appStoreVersion,
            @ApiParam(value = "app store company") @RequestParam("company")
            @Length(max = MAX_COMPANY_LEN, min = 1) String company,
            @ApiParam(value = "app store url") @RequestParam("url")
            @Length(max = MAX_URL_LEN, min = 1) String url,
            @ApiParam(value = "app store schema", required = false) @RequestParam("schema")
            @Pattern(regexp = REG_SCHEMA) String schema,
            @ApiParam(value = "app store push interface") @RequestParam("appPushIntf")
            @Length(max = MAX_URL_LEN, min = 1) String appPushIntf,
            @ApiParam(value = "appd translate id") @RequestParam("appdTransId")
            @Pattern(regexp = REG_UUID) String appdTransId,
            @ApiParam(value = "app store description", required = false)
            @RequestParam(value = "description", required = false)
            @Length(max = MAX_DESC_LEN) String description) {
        AppStoreDto appStoreDto = new AppStoreDto(null, appStoreName, appStoreVersion, company, url, schema,
                appPushIntf, appdTransId, description, null, null);
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
            @ApiParam(value = "app store id") @PathVariable("appStoreId")
            @Pattern(regexp = REG_UUID) String appStoreId) {
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
            @PathVariable("appStoreId") @Pattern(regexp = REG_UUID) String appStoreId,
            @ApiParam(value = "app store name") @RequestParam(value = "appStoreName", required = false)
            @Pattern(regexp = REG_STORE_NAME) String appStoreName,
            @ApiParam(value = "app store version") @RequestParam(value = "appStoreVersion", required = false)
            @Length(max = MAX_VERSION_LEN, min = 1) String appStoreVersion,
            @ApiParam(value = "app store company") @RequestParam(value = "company", required = false)
            @Length(max = MAX_COMPANY_LEN, min = 1) String company,
            @ApiParam(value = "app store url") @RequestParam(value = "url", required = false)
            @Length(max = MAX_URL_LEN, min = 1) String url,
            @ApiParam(value = "app store schema") @RequestParam(value = "schema", required = false)
            @Pattern(regexp = REG_SCHEMA) String schema,
            @ApiParam(value = "app push interface") @RequestParam(value = "appPushIntf", required = false)
            @Length(max = MAX_URL_LEN, min = 1) String appPushIntf,
            @ApiParam(value = "appd translate id") @RequestParam(value = "appdTransId", required = false)
            @Pattern(regexp = REG_UUID) String appdTransId,
            @ApiParam(value = "app store description") @RequestParam(value = "description", required = false)
            @Length(max = MAX_DESC_LEN) String description) {
        AppStoreDto appStoreDto = new AppStoreDto(appStoreId, appStoreName, appStoreVersion,
                company, url, schema, appPushIntf, appdTransId, description,
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
            @ApiParam(value = "app store id") @PathVariable("appStoreId")
            @Pattern(regexp = REG_UUID) String appStoreId) {
        return appStoreServiceFacade.queryAppStore(appStoreId);
    }
}
