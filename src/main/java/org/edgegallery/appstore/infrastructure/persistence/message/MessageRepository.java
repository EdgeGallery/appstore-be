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

package org.edgegallery.appstore.infrastructure.persistence.message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MessageRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRepository.class);

    @Autowired
    private MessageMapper messageMapper;

    private static final String MESSAGE_NOT_EXISTED = "message {} do not existed";

    private static final String MESSAGE_NOT_EXISTED_FORMAT = "message %s do not existed";

    /**
     * add a message to db.
     *
     * @param message message obj
     */
    public void addMessage(Message message) {
        try {
            MessagePo messagePo = messageMapper.getOneMessage(message.getMessageId());
            if (messagePo != null) {
                LOGGER.error("message {} has existed", message.getMessageId());
                throw new AppException(String.format("message %s has existed", message.getMessageId()),
                    ResponseConst.RET_MESSAGE_EXISTED);
            }
            messageMapper.insert(MessagePo.of(message));
        } catch (Exception e) {
            LOGGER.error("add message to db error: {}", e.getMessage());
            throw new AppException("db operate error", ResponseConst.RET_DB_ERROR);
        }

    }

    /**
     * get all messages in db.
     *
     * @return list
     */
    public List<Message> getAllMessagesV2(Map<String, Object> params) {
        return messageMapper.getAllMessagesV2(params).stream().map(MessagePo::toDomainModel)
            .collect(Collectors.toList());
    }

    /**
     * get all messages in db.
     *
     * @return list
     */
    public List<Message> getAllMessages() {
        return messageMapper.getAllMessages().stream().map(MessagePo::toDomainModel).collect(Collectors.toList());
    }

    /**
     * get all messages count in db.
     *
     * @return long
     */
    public long getAllMessageCount(Map<String, Object> param) {
        return messageMapper.getAllMessageCount(param);
    }

    /**
     * get one message by id.
     *
     * @param messageId id
     * @return message obj
     */
    public Message getOneMessage(String messageId) {
        MessagePo messagePo = messageMapper.getOneMessage(messageId);
        if (messagePo == null) {
            LOGGER.error(MESSAGE_NOT_EXISTED, messageId);
            throw new EntityNotFoundException(String.format(MESSAGE_NOT_EXISTED_FORMAT, messageId),
                ResponseConst.RET_MESSAGE_NOT_FOUND);
        }
        return messagePo.toDomainModel();
    }

    /**
     * delete one message by id.
     *
     * @param messageId id
     */
    public void deleteOneMessage(String messageId) {
        MessagePo messagePo = messageMapper.getOneMessage(messageId);
        if (messagePo == null) {
            LOGGER.error(MESSAGE_NOT_EXISTED, messageId);
            throw new EntityNotFoundException(String.format(MESSAGE_NOT_EXISTED_FORMAT, messageId),
                ResponseConst.RET_MESSAGE_NOT_FOUND);
        }
        messageMapper.deleteOneMessage(messageId);
    }

    /**
     * update message to readed.
     *
     * @param messageId id
     */
    public void updateMessageReaded(String messageId) {
        MessagePo messagePo = messageMapper.getOneMessage(messageId);
        if (messagePo == null) {
            LOGGER.error(MESSAGE_NOT_EXISTED, messageId);
            throw new EntityNotFoundException(String.format(MESSAGE_NOT_EXISTED_FORMAT, messageId),
                ResponseConst.RET_MESSAGE_NOT_FOUND);
        }
        messagePo.setReaded(true);
        messageMapper.update(messagePo);
    }
}
