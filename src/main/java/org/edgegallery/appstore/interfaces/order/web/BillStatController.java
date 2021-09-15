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
import javax.validation.constraints.Pattern;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.order.facade.dto.QueryBillsReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.BillDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.StatOverallReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopOrderAppReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopSaleAppReqDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "billStat")
@RequestMapping("/mec/appstore/v1/bills")
@Api(tags = {"Bill Stat Controller"})
@Validated
public class BillStatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillStatController.class);

    /**
     * query bill list.
     */
    @ApiOperation(value = "query bill list", response = Page.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "query bill success", response = Page.class)
    })
    @PostMapping(value = "/list", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<BillDto>> queryBillList(
        @RequestParam("userId") @Pattern(regexp = Consts.REG_USER_ID) String userId,
        @ApiParam(value = "queryBillsReqDto", required = true) @RequestBody QueryBillsReqDto queryBillsReqDto) {
        LOGGER.info("enter query bill list.");
        // return ResponseEntity.ok(appServiceFacade.appRegistering(new User(userId, userName), file,
        //     new AppParam(type, shortDesc, showType, affinity, industry, Boolean.parseBoolean(experienceAble)), icon,
        //     demoVideo, new AtpMetadata(testTaskId, (String) request.getAttribute(ACCESS_TOKEN))));
        return ResponseEntity.ok().build();
    }

    /**
     * statistic overall income and expenditure.
     */
    @ApiOperation(value = "statistic overall income and expenditure", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "statistic success", response = ResponseObject.class)
    })
    @PostMapping(value = "/statistics/overall", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> statOverall(
        @RequestParam("userId") @Pattern(regexp = Consts.REG_USER_ID) String userId,
        @ApiParam(value = "statOverallReqDto", required = true) @RequestBody StatOverallReqDto statOverallReqDto) {
        LOGGER.info("enter stat overall income and expenditure.");
        // return ResponseEntity.ok(appServiceFacade.appRegistering(new User(userId, userName), file,
        //     new AppParam(type, shortDesc, showType, affinity, industry, Boolean.parseBoolean(experienceAble)), icon,
        //     demoVideo, new AtpMetadata(testTaskId, (String) request.getAttribute(ACCESS_TOKEN))));
        return ResponseEntity.ok().build();
    }

    /**
     * statistic top sale app.
     */
    @ApiOperation(value = "statistic top sale app", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "statistic success", response = ResponseObject.class)
    })
    @PostMapping(value = "/statistics/sales/topapps", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> statTopSaleApp(
        @RequestParam("userId") @Pattern(regexp = Consts.REG_USER_ID) String userId,
        @ApiParam(value = "topSaleAppReqDto", required = true) @RequestBody TopSaleAppReqDto topSaleAppReqDto) {
        LOGGER.info("enter stat top sale apps.");
        // return ResponseEntity.ok(appServiceFacade.appRegistering(new User(userId, userName), file,
        //     new AppParam(type, shortDesc, showType, affinity, industry, Boolean.parseBoolean(experienceAble)), icon,
        //     demoVideo, new AtpMetadata(testTaskId, (String) request.getAttribute(ACCESS_TOKEN))));
        return ResponseEntity.ok().build();
    }

    /**
     * statistic top order app.
     */
    @ApiOperation(value = "statistic top order app", response = ResponseObject.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "statistic success", response = ResponseObject.class)
    })
    @PostMapping(value = "/statistics/orders/topapps", produces = MediaType.APPLICATION_JSON)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<ResponseObject> statTopOrderApp(
        @RequestParam("userId") @Pattern(regexp = Consts.REG_USER_ID) String userId,
        @ApiParam(value = "topOrderAppReqDto", required = true) @RequestBody TopOrderAppReqDto topOrderAppReqDto) {
        LOGGER.info("enter stat top order apps.");
        // return ResponseEntity.ok(appServiceFacade.appRegistering(new User(userId, userName), file,
        //     new AppParam(type, shortDesc, showType, affinity, industry, Boolean.parseBoolean(experienceAble)), icon,
        //     demoVideo, new AtpMetadata(testTaskId, (String) request.getAttribute(ACCESS_TOKEN))));
        return ResponseEntity.ok().build();
    }
}
