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

package org.edgegallery.appstore.application.inner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.files.LocalFileServiceImpl;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageRepository;
import org.edgegallery.appstore.infrastructure.util.AppUtil;
import org.edgegallery.appstore.interfaces.app.facade.AppParam;
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
    private LocalFileServiceImpl fileService;

    @Autowired
    private AppService appService;

    @Autowired
    private AppUtil appUtil;

    /**
     * add a message.
     *
     * @param dto dto
     * @return result
     */
    public String addMessage(MessageReqDto dto) {
        LOGGER.info("Receive notice message from {}", dto.getSourceAppStore());
        messageRepository.addMessage(dto.toMessage(EnumMessageType.NOTICE));
        LOGGER.info("Successfully add a message");
        return "Successfully add a message";
    }

    public List<Message> getAllMessagesV2(Map<String, Object> params) {
        return messageRepository.getAllMessagesV2(params);
    }

    public List<Message> getAllMessages() {
        return messageRepository.getAllMessages();
    }

    public long getAllMessageCount(Map<String, Object> param) {
        return messageRepository.getAllMessageCount(param);
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
        String demoVideoDownloadUrl = message.getDemoVideoDownloadUrl();
        if (packageDownloadUrl == null || iconDownloadUrl == null) {
            LOGGER.error("download url null: package download url is {}, icon download url is {}", packageDownloadUrl,
                iconDownloadUrl);
            throw new AppException("download url is null.",
                ResponseConst.RET_MESSAGE_DOWNLOAD_URL_NULL, packageDownloadUrl, iconDownloadUrl);
        }
        try {
            String parentPath = dir + File.separator + UUID.randomUUID().toString();
            String targetAppstore = message.getTargetAppStore();
            File tempPackage = fileService.downloadFile(packageDownloadUrl, parentPath, targetAppstore);
            File tempIcon = fileService.downloadFile(iconDownloadUrl, parentPath, targetAppstore);
            AFile demoVideo = null;
            if (demoVideoDownloadUrl != null) {
                File tempDemoVideo = fileService.downloadFile(demoVideoDownloadUrl, parentPath, targetAppstore);
                demoVideo = new AFile(tempDemoVideo.getName(), tempDemoVideo.getCanonicalPath());
            }
            AFile apackage = new AFile(tempPackage.getName(), tempPackage.getCanonicalPath());
            AFile icon = new AFile(tempIcon.getName(), tempIcon.getCanonicalPath());
            apackage.setFileSize(tempPackage.length());
            String appClass = appUtil.getAppClass(apackage.getStorageAddress());
            String showType = "public";
            AppParam appParam = new AppParam(message.getBasicInfo().getType(), message.getBasicInfo().getShortDesc(),
                showType, message.getBasicInfo().getAffinity(), message.getBasicInfo().getIndustry(), false);
            Release release = new Release(apackage, icon, demoVideo, user, appParam, appClass);
            // the package pulled from third appstore need to be tested by local appstore's atp
            release.setStatus(EnumPackageStatus.Upload);
            appService.registerApp(release);

            addDownloadMessage(message);
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            throw new AppException("download file from message.", ResponseConst.RET_DOWNLOAD_FROM_MESSAGE_FAILED);
        }
    }

    private void addDownloadMessage(Message message) {
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageType(EnumMessageType.PULL);
        message.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        message.setDescription(String.format("%s 从 %s 下载应用", message.getSourceAppStore(), message.getTargetAppStore()));
        messageRepository.addMessage(message);
    }

    public void updateMessageReaded(String messageId) {
        messageRepository.updateMessageReaded(messageId);
    }
}
