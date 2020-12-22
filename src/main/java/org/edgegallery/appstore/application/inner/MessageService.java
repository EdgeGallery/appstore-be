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

package org.edgegallery.appstore.application.inner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.exceptions.DomainException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageRepository;
import org.edgegallery.appstore.interfaces.app.facade.AppParam;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageReqDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("MessageService")
public class MessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    @Value("${appstore-be.package-path}")
    private String dir;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private AppService appService;

    @Autowired
    private PackageService packageService;

    /**
     * add a message.
     *
     * @param dto dto
     * @return result
     */
    public String addMessage(MessageReqDto dto) {
        LOGGER.info("receive notice message from {}", dto.getSourceAppStore());
        messageRepository.addMessage(dto.toMessage(EnumMessageType.NOTICE));
        LOGGER.info("add a message success");
        return "add a message success";
    }

    public List<Message> getAllMessages() {
        return messageRepository.getAllMessages();
    }

    public Message getMessageById(String messageId) {
        return messageRepository.getOneMessage(messageId);
    }

    public void deleteMessageById(String messageId) {
        messageRepository.deleteOneMessage(messageId);
    }

    /**
     * download package and icon by the url in message.
     *
     * @param messageId message id
     * @param user user info
     */
    public void downloadFromMessage(String messageId, User user) {
        Message message = messageRepository.getOneMessage(messageId);
        String packageDownloadUrl = message.getPackageDownloadUrl();
        String iconDownloadUrl = message.getIconDownloadUrl();
        if (packageDownloadUrl == null || iconDownloadUrl == null) {
            LOGGER.error("download url null: package download url is {}, icon download url is {}", packageDownloadUrl,
                iconDownloadUrl);
            throw new DomainException("download url is null");
        }
        try {
            String parentPath = dir + File.separator + UUID.randomUUID().toString();
            File tempPackage = fileService.downloadFile(packageDownloadUrl, parentPath);
            File tempIcon = fileService.downloadFile(iconDownloadUrl, parentPath);
            AFile apackage = new AFile(tempPackage.getName(), tempPackage.getCanonicalPath());
            AFile icon = new AFile(tempIcon.getName(), tempIcon.getCanonicalPath());
            AppParam appParam = new AppParam(message.getBasicInfo().getType(), message.getBasicInfo().getShortDesc(),
                message.getBasicInfo().getAffinity(), message.getBasicInfo().getIndustry());
            Release release = new Release(apackage, icon, user, appParam);
            release.setStatus(EnumPackageStatus.Test_success);
            appService.registerApp(release);

            addDownloadMessage(message);
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            throw new DomainException("file download exception");
        }
    }

    private void addDownloadMessage(Message message) {
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageType(EnumMessageType.PULL);
        String currentAppStore = message.getTargetAppStore();
        message.setTargetAppStore(message.getSourceAppStore());
        message.setSourceAppStore(currentAppStore);
        message.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        message.setDescription(String.format("%s 从 %s 下载应用", message.getSourceAppStore(), message.getTargetAppStore()));
        messageRepository.addMessage(message);
    }

    public void updateMessageReaded(String messageId) {
        messageRepository.updateMessageReaded(messageId);
    }
}
