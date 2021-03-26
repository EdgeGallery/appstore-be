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

package org.edgegallery.appstore.interfaces.message.facade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRespDto {

    private String messageId;

    private BasicMessageInfo basicInfo;

    private EnumMessageType messageType;

    private String sourceAppStore;

    private String targetAppStore;

    private String time;

    private String description;

    private String atpTestStatus;

    private String atpTestTaskId;

    private String atpTestReportUrl;

    private String iconDownloadUrl;

    private boolean readed;

    /**
     * transform message obj to response obj.
     *
     * @param message obj
     * @return dto obj
     */
    public static MessageRespDto of(Message message) {
        MessageRespDto dto = new MessageRespDto();
        dto.messageId = message.getMessageId();
        dto.messageType = message.getMessageType();
        dto.sourceAppStore = message.getSourceAppStore();
        dto.targetAppStore = message.getTargetAppStore();
        dto.time = message.getTime();
        dto.description = message.getDescription();
        dto.atpTestStatus = message.getAtpTestStatus();
        dto.atpTestTaskId = message.getAtpTestTaskId();
        dto.atpTestReportUrl = message.getAtpTestReportUrl();
        dto.basicInfo = message.getBasicInfo();
        dto.readed = message.isReaded();
        dto.iconDownloadUrl = message.getIconDownloadUrl();
        return dto;
    }
}
