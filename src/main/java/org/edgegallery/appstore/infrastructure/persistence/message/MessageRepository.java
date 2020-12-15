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

package org.edgegallery.appstore.infrastructure.persistence.message;

import java.util.List;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.shared.exceptions.DomainException;
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

    public void addMessage(Message message) {
        MessagePo messagePo = messageMapper.getOneMessage(message.getMessageId());
        if (messagePo != null) {
            LOGGER.error("message {} has existed", message.getMessageId());
            throw new DomainException(String.format("message %s has existed", message.getMessageId()));
        }
        messageMapper.insert(MessagePo.of(message));
    }

    public List<Message> getAllMessages() {
        return messageMapper.getAllMessages().stream().map(MessagePo::toDomainModel).collect(Collectors.toList());
    }

    public Message getOneMessage(String messageId) {
        MessagePo messagePo = messageMapper.getOneMessage(messageId);
        if (messagePo == null) {
            LOGGER.error("message {} do not existed", messageId);
            throw new DomainException(String.format("message %s do not existed", messageId));
        }
        return messagePo.toDomainModel();
    }

    public void deleteOneMessage(String messageId) {
        MessagePo messagePo = messageMapper.getOneMessage(messageId);
        if (messagePo == null) {
            LOGGER.error("message {} do not existed", messageId);
            throw new EntityNotFoundException(String.format("message %s do not existed", messageId));
        }
        messageMapper.deleteOneMessage(messageId);
    }
}
