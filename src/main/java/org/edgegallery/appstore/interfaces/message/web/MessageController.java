/* Copyright 2020 Huawei Technologies Co., Ltd.
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
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.interfaces.message.facade.MessageServiceFacade;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageReqDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageRespDto;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RestSchema(schemaId = "message")
@RequestMapping("/mec/appstore/v1/messages")
@Api(tags = {"Message Controller"})
@Validated
public class MessageController {

    @Autowired
    private MessageServiceFacade messageServiceFacade;

    /**
     * add a message.
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "add a message", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> addMessage(@RequestBody MessageReqDto dto) {
        return messageServiceFacade.addMessage(dto);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the messages", response = MessageRespDto.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_TENANT')")
    public ResponseEntity<List<MessageRespDto>> getAllMessages(
        @ApiParam(value = "messageType") @QueryParam("messageType") EnumMessageType messageType) {
        return messageServiceFacade.getAllMessages(messageType);
    }

    @GetMapping(value = "/{messageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get a message", response = MessageRespDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<MessageRespDto> getMessage(
        @ApiParam(value = "messageId") @PathVariable("messageId") String messageId) {
        return messageServiceFacade.getMessage(messageId);
    }

    @DeleteMapping(value = "/{messageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete a message by id.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "forbidden", response = String.class),
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> deleteMessage(
        @ApiParam(value = "messageId") @PathVariable("messageId") String messageId) {
        return messageServiceFacade.deleteMessage(messageId);
    }

    @GetMapping(value = "/{messageId}/action/download", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "download the package in the message.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> download(
        @ApiParam(value = "messageId") @PathVariable("messageId") String messageId, HttpServletRequest request) {
        return messageServiceFacade.downloadFromMessage(messageId, new User((String) request.getAttribute("userId"),
            (String) request.getAttribute("userName")));
    }

    @PutMapping(value = "/{messageId}/action/readed", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update the message to readed.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> updateReaded(
        @ApiParam(value = "messageId") @PathVariable("messageId") String messageId) {
        return messageServiceFacade.updateMessageReaded(messageId);
    }

    @GetMapping(value = "/{messageId}/report-data", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "query the report data in the message.", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<String> queryReportData(
        @ApiParam(value = "messageId") @PathVariable("messageId") String messageId, HttpServletRequest request) {
        return messageServiceFacade.queryReportData(messageId);
    }
}
