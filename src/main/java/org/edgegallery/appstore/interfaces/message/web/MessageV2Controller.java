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

package org.edgegallery.appstore.interfaces.message.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.Min;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.message.facade.MessageServiceFacade;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageReqDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageRespDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.QueryMessageReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "v2message")
@RequestMapping("/mec/appstore/v2/messages")
@Api(tags = {"Message V2Controller"})
@Validated
public class MessageV2Controller {

    @Autowired
    private MessageServiceFacade messageServiceFacade;

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the messages", response = MessageRespDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<MessageRespDto>> getAllMessages(
        @ApiParam(value = "messageType") @QueryParam("messageType") EnumMessageType messageType,
        @ApiParam(value = "app Name") @RequestParam("appName") String appName,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset,
        @ApiParam(value = "query sortType") @RequestParam("sortType") String sortType,
        @ApiParam(value = "query condition")  @RequestParam("sortItem") String sortItem) {
        return ResponseEntity
            .ok(messageServiceFacade.getAllMessagesV2(messageType, appName, limit, offset, sortType, sortItem));
    }

    /**
     * get message center list.
     * @param queryMessageReqDto queryMessageReqDto.
     * @return
     */
    @PostMapping(value = "/center/query", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get app list by condition. if the userId is null, it will return all published apps, "
        + "else will return all apps.", response = MessageRespDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<MessageRespDto>> queryMsgCenterList(
        @ApiParam(value = "queryMessageReqDto", required = true) @RequestBody QueryMessageReqDto queryMessageReqDto) {
        return ResponseEntity
            .ok(messageServiceFacade.queryMsgCenterList(queryMessageReqDto));
    }

    /**
     * add a message.
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "add a message", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<ResponseObject> addMessageV2(@RequestBody MessageReqDto dto) {
        return messageServiceFacade.addMessageV2(dto);
    }
}
