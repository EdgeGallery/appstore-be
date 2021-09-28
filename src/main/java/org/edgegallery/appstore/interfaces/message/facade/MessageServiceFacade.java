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

package org.edgegallery.appstore.interfaces.message.facade;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgegallery.appstore.application.inner.MessageService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageDateEnum;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageReqDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageRespDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.QueryMessageReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("MessageServiceFacade")
public class MessageServiceFacade {

    @Autowired
    private MessageService messageService;

    /**
     * add a message.
     *
     * @param dto dto
     * @return ok
     */
    public ResponseEntity<String> addMessage(MessageReqDto dto) {
        return ResponseEntity.ok(messageService.addMessage(dto));
    }

    /**
     * get all messages by type.
     *
     * @param messageType type
     * @return list
     */
    public ResponseEntity<List<MessageRespDto>> getAllMessages(EnumMessageType messageType) {
        List<Message> messages = messageService.getAllMessages();
        return ResponseEntity
            .ok(messages.stream().filter(m -> messageType == null ? m != null : m.getMessageType() == messageType)
                .sorted(Comparator.comparing(Message::getTime).reversed()).map(MessageRespDto::of)
                .collect(Collectors.toList()));
    }

    public ResponseEntity<MessageRespDto> getMessage(String messageId) {
        return ResponseEntity.ok(MessageRespDto.of(messageService.getMessageById(messageId)));
    }

    public ResponseEntity<String> deleteMessage(String messageId) {
        messageService.deleteMessageById(messageId);
        return ResponseEntity.ok("delete success");
    }

    public ResponseEntity<String> downloadFromMessage(String messageId, User user) {
        messageService.downloadFromMessage(messageId, user);
        return ResponseEntity.ok("success");
    }

    public ResponseEntity<String> updateMessageReaded(String messageId) {
        messageService.updateMessageReaded(messageId);
        return ResponseEntity.ok("success");
    }

    /**
     * add a message.
     *
     * @param dto dto
     * @return ok
     */
    public ResponseEntity<ResponseObject> addMessageV2(MessageReqDto dto) {
        String result = messageService.addMessage(dto);
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(result, errMsg, "add message success."));
    }

    /**
     * get all messages by type limit offset.
     *
     * @param queryMessageReqDto queryMessageReqDto.
     * @return Page.
     */
    public Page<MessageRespDto> getAllMessagesV2(QueryMessageReqDto queryMessageReqDto) {
        Map<String, Object> params = new HashMap<>();
        params.put("time", "time");
        params.put("appName", queryMessageReqDto.getAppName());
        params.put("messageType", queryMessageReqDto.getMessageType());
        params.put("timeFlag", getMessageDate(queryMessageReqDto.getTimeFlag()));
        params.put("limit", queryMessageReqDto.getLimit());
        params.put("offset", queryMessageReqDto.getOffset());
        params.put("sortItem", queryMessageReqDto.getSortItem());
        params.put("sortType", queryMessageReqDto.getSortType());
        if (!queryMessageReqDto.isAllMessage()) {
            params.put("readable", queryMessageReqDto.isReadable());
        }
        List<Message> messages = messageService.getAllMessagesV2(params);
        long total = messageService.getAllMessageCount(params);
        return new Page<>(messages.stream().map(MessageRespDto::of).collect(Collectors.toList()),
            queryMessageReqDto.getLimit(), queryMessageReqDto.getOffset(), total);
    }

    /**
     * get time format by time flag.
     *
     * @param timeFlag timeFlag.
     * @return
     */
    public String getMessageDate(MessageDateEnum timeFlag) {
        if (timeFlag == null || timeFlag.name().equals("EARLIER")) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, timeFlag.dayValue);
        return format.format(calendar.getTime());
    }

}
