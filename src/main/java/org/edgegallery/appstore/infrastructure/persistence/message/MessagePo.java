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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.infrastructure.persistence.PersistenceObject;

@Getter
@Setter
@Entity
@Table(name = "app_table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePo implements PersistenceObject<Message> {

    @Column(name = "MESSAGEID")
    private String messageId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PROVIDER")
    private String provider;

    @Column(name = "VERSION")
    private String version;

    @Column(name = "AFFINITY")
    private String affinity;

    @Column(name = "SHORTDESC")
    private String shortDesc;

    @Column(name = "INDUSTRY")
    private String industry;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "RESULT")
    private String result;

    @Column(name = "READED")
    private boolean readed;

    @Column(name = "MESSAGETYPE")
    private EnumMessageType messageType;

    @Column(name = "SOURCEAPPSTORE")
    private String sourceAppStore;

    @Column(name = "TARGETAPPSTORE")
    private String targetAppStore;

    @Column(name = "TIME")
    private String time;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ATPTESTSTATUS")
    private String atpTestStatus;

    @Column(name = "ATPTESTTASKID")
    private String atpTestTaskId;

    @Column(name = "ATPTESTREPORTURL")
    private String atpTestReportUrl;

    @Column(name = "PACKAGEDOWNLOADURL")
    private String packageDownloadUrl;

    @Column(name = "ICONDOWNLOADURL")
    private String iconDownloadUrl;

    @Override
    public Message toDomainModel() {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setResult(result);
        message.setReaded(readed);
        message.setMessageType(messageType);
        message.setSourceAppStore(sourceAppStore);
        message.setTargetAppStore(targetAppStore);
        message.setTime(time);
        message.setDescription(description);
        message.setAtpTestStatus(atpTestStatus);
        message.setAtpTestTaskId(atpTestTaskId);
        message.setAtpTestReportUrl(atpTestReportUrl);
        message.setPackageDownloadUrl(packageDownloadUrl);
        message.setIconDownloadUrl(iconDownloadUrl);
        BasicMessageInfo basicInfo = new BasicMessageInfo();
        basicInfo.setName(name);
        basicInfo.setProvider(provider);
        basicInfo.setVersion(version);
        basicInfo.setAffinity(affinity);
        basicInfo.setIndustry(industry);
        basicInfo.setType(type);
        basicInfo.setShortDesc(shortDesc);
        message.setBasicInfo(basicInfo);
        return message;
    }

    static MessagePo of(Message message) {
        MessagePo po = new MessagePo();
        po.messageId = message.getMessageId();
        po.name = message.getBasicInfo().getName();
        po.provider = message.getBasicInfo().getProvider();
        po.version = message.getBasicInfo().getVersion();
        po.affinity = message.getBasicInfo().getAffinity();
        po.industry = message.getBasicInfo().getIndustry();
        po.shortDesc = message.getBasicInfo().getShortDesc();
        po.type = message.getBasicInfo().getType();
        po.atpTestStatus = message.getAtpTestStatus();
        po.atpTestTaskId = message.getAtpTestTaskId();
        po.atpTestReportUrl = message.getAtpTestReportUrl();
        po.result = message.getResult();
        po.readed = message.isReaded();
        po.messageType = message.getMessageType();
        po.time = message.getTime();
        po.sourceAppStore = message.getSourceAppStore();
        po.targetAppStore = message.getTargetAppStore();
        po.packageDownloadUrl = message.getPackageDownloadUrl();
        po.iconDownloadUrl = message.getIconDownloadUrl();
        po.description = message.getDescription();
        return po;
    }
}
