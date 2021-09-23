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

package org.edgegallery.appstore.interfaces.order.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.order.facade.SplitConfigServiceFacade;
import org.edgegallery.appstore.interfaces.order.facade.dto.SplitConfigOperReqDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "splitConfig")
@RequestMapping("/mec/appstore/v1/apps/splitconfigs")
@Api(tags = {"Split Config Controller"})
@Validated
public class SplitConfigController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitConfigController.class);

    @Autowired
    private SplitConfigServiceFacade splitConfigServiceFacade;

    /**
     * query split configs.
     */
    @ApiOperation(value = "query split configs", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "query split configs success", response = ResponseObject.class)
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> querySplitConfigs(HttpServletRequest httpServletRequest) {
        LOGGER.info("enter query split configs.");
        String userName = (String) httpServletRequest.getAttribute(Consts.USERNAME);
        if (!Consts.SUPER_ADMIN_NAME.equalsIgnoreCase(userName)) {
            LOGGER.error("forbidden to query split configs.");
            ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_PARAM_INVALID, null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseObject(null, resultMsg, "forbidden to query split configs."));
        }
        return splitConfigServiceFacade.queryAllSplitConfigs();
    }

    /**
     * batch add app's split configs.
     */
    @ApiOperation(value = "batch add app's split configs", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "add split configs success", response = ResponseObject.class)
    })
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> addSplitConfigs(
        HttpServletRequest httpServletRequest,
        @ApiParam(value = "splitConfigOperReqDto", required = true) @RequestBody
            SplitConfigOperReqDto splitConfigOperReqDto) {
        LOGGER.info("enter add split configs.");
        String userName = (String) httpServletRequest.getAttribute(Consts.USERNAME);
        if (!Consts.SUPER_ADMIN_NAME.equalsIgnoreCase(userName)) {
            LOGGER.error("forbidden to add split configs.");
            ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_PARAM_INVALID, null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseObject(null, resultMsg, "forbidden to add split configs."));
        }
        return splitConfigServiceFacade.addSplitConfig(splitConfigOperReqDto);
    }

    /**
     * modify one app's split configs.
     */
    @ApiOperation(value = "modify one app's split configs", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "modify split configs success", response = ResponseObject.class)
    })
    @PutMapping(value = "/{appId}", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> modifySplitConfigs(
        HttpServletRequest httpServletRequest,
        @ApiParam(value = "appId") @PathVariable("appId") @Pattern(regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "splitConfigOperReqDto", required = true) @RequestBody
            SplitConfigOperReqDto splitConfigOperReqDto) {
        LOGGER.info("enter modify split configs.");
        String userName = (String) httpServletRequest.getAttribute(Consts.USERNAME);
        if (!Consts.SUPER_ADMIN_NAME.equalsIgnoreCase(userName)) {
            LOGGER.error("forbidden to modify split configs.");
            ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_PARAM_INVALID, null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseObject(null, resultMsg, "forbidden to modify split configs."));
        }
        return splitConfigServiceFacade.modifySplitConfig(appId, splitConfigOperReqDto);
    }

    /**
     * batch delete app's split configs.
     */
    @ApiOperation(value = "batch delete app's split configs", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "delete split configs success", response = ResponseObject.class)
    })
    @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteSplitConfigs(
        HttpServletRequest httpServletRequest,
        @ApiParam(value = "splitConfigOperReqDto", required = true) @RequestBody
            SplitConfigOperReqDto splitConfigOperReqDto) {
        LOGGER.info("enter delete split configs.");
        String userName = (String) httpServletRequest.getAttribute(Consts.USERNAME);
        if (!Consts.SUPER_ADMIN_NAME.equalsIgnoreCase(userName)) {
            LOGGER.error("forbidden to delete split configs.");
            ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_PARAM_INVALID, null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ResponseObject(null, resultMsg, "forbidden to delete split configs."));
        }
        return splitConfigServiceFacade.deleteSplitConfig(splitConfigOperReqDto);
    }
}

