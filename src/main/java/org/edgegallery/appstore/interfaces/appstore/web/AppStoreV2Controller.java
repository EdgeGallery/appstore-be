/* Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.appstore.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.Min;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.interfaces.appstore.facade.AppStoreServiceFacade;
import org.edgegallery.appstore.interfaces.appstore.facade.dto.AppStoreDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "appStoreV2")
@RequestMapping("/mec/appstore/v2/appstores")
@Api(tags = {"APP StoreV2 Controller"})
@Validated
public class AppStoreV2Controller {

    @Autowired
    private AppStoreServiceFacade appStoreServiceFacade;

    /**
     * query appstore list.
     *
     * @param appStoreName appStoreName,
     * @param company company.
     * @param limit limit.
     * @param offset offset.
     * @return page object.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "query app stores.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<AppStoreDto>> queryAppStores(
        @ApiParam(value = "app store name") @RequestParam("appStoreName") String appStoreName,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit")
            int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset")
            int offset) {
        return ResponseEntity.ok(appStoreServiceFacade.queryAppStoresV2(appStoreName, limit, offset));
    }

}
