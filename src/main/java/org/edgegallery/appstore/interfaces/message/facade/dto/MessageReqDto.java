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

package org.edgegallery.appstore.interfaces.message.facade.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.config.ApplicationContext;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.springframework.beans.factory.annotation.Autowired;

@Getter
@Setter
@NoArgsConstructor
public class MessageReqDto {

    private BasicMessageInfo basicInfo;

    private String sourceAppStore;

    private String atpTestStatus;

    private String atpTestTaskId;

    private String atpTestReportUrl;

    private String packageDownloadUrl;

    private String iconDownloadUrl;

    @Autowired
    private ApplicationContext context;

    /**
     * transform the dto to message obj.
     *
     * @return message obj
     */
    public Message toMessage() {
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setReaded(false);
        message.setBasicInfo(basicInfo);
        message.setMessageType(EnumMessageType.NOTICE);
        message.setSourceAppStore(sourceAppStore);
        message.setTargetAppStore(context.platformName);
        message.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        message.setDescription(generateDescription());
        message.setAtpTestStatus(atpTestStatus);
        message.setAtpTestTaskId(atpTestTaskId);
        message.setAtpTestReportUrl(atpTestReportUrl);
        message.setPackageDownloadUrl(packageDownloadUrl);
        message.setIconDownloadUrl(iconDownloadUrl);
        return message;
    }

    private String generateDescription() {
        return String.format("%s申请推广此应用到当前平台", sourceAppStore);
    }
}

