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

package org.edgegallery.appstore.interfaces.order.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.order.facade.MecmWrapperServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "mecmWrapper")
@RequestMapping("/mec/appstore/v1")
@Api(tags = {"Mecm Wrapper Controller"})
@Validated
public class MecmWrapperController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MecmWrapperController.class);

    @Autowired
    private MecmWrapperServiceFacade mecmWrapperServiceFacade;

    /**
     * query mecm hosts.
     */
    @ApiOperation(value = "query mecm hosts", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "query mecm hosts success", response = ResponseObject.class)
    })
    @GetMapping(value = "/mechosts", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> queryMecmHosts(
        @ApiParam(value = "app id") @RequestParam("appId") String appId,
        @ApiParam(value = "package id") @RequestParam("packageId") String packageId,
        HttpServletRequest httpServletRequest) {
        LOGGER.info("enter query mecm hosts.");
        return mecmWrapperServiceFacade.getAllMecmHosts(
            (String) httpServletRequest.getAttribute(Consts.ACCESS_TOKEN_STR),
            (String) httpServletRequest.getAttribute(Consts.USERID), appId, packageId);
    }
}

