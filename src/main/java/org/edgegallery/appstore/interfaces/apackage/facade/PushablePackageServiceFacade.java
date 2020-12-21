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

package org.edgegallery.appstore.interfaces.apackage.facade;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.PushablePackageService;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.releases.UnknownReleaseExecption;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageRepository;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("PushablePackageServiceFacade")
public class PushablePackageServiceFacade {

    @Autowired
    private AppService appService;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private PushablePackageService pushablePackageService;

    @Autowired
    private MessageRepository messageRepository;

    /**
     * query all pushable packages.
     *
     * @return list
     */
    public ResponseEntity<List<PushablePackageDto>> queryAllPushablePackages() {
        return ResponseEntity.ok(pushablePackageService.queryAllPushablePackages());
    }

    /**
     * get one pushable package by id.
     *
     * @param packageId id
     * @return dto
     */
    public ResponseEntity<PushablePackageDto> getPushablePackage(String packageId) {
        PushablePackageDto dto = pushablePackageService.getPushablePackage(packageId);
        if (dto == null) {
            throw new UnknownReleaseExecption(packageId);
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * push a package to target appstore.
     *
     * @param packageId id
     * @param targetAppStore target appstore
     */
    public List<Boolean> pushPackage(String packageId, PushTargetAppStoreDto targetAppStore) {
        // find the package
        return pushablePackageService.pushPackage(packageId, targetAppStore);
    }

    /**
     * download a package by id.
     *
     * @param packageId id
     * @return file stream
     * @throws FileNotFoundException e
     */
    public ResponseEntity<InputStreamResource> downloadPackage(String packageId) throws FileNotFoundException {
        PushablePackageDto packageDto = pushablePackageService.getPushablePackage(packageId);
        Release release = appService.download(packageDto.getAppId(), packageId);
        InputStream ins = fileService.get(release.getPackageFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + release.getPackageFile().getOriginalFileName());

        // add message log for this action
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageType(EnumMessageType.BE_DOWNLOADED);
        BasicMessageInfo basicMessageInfo = new BasicMessageInfo(packageDto);
        message.setBasicInfo(basicMessageInfo);

        // store message to the db
        messageRepository.addMessage(message);

        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * download a icon by id.
     *
     * @param packageId id
     * @return icon stream
     */
    public ResponseEntity<InputStreamResource> downloadIcon(String packageId) throws FileNotFoundException {
        PushablePackageDto packageDto = pushablePackageService.getPushablePackage(packageId);
        Release release = appService.download(packageDto.getAppId(), packageId);
        InputStream ins = fileService.get(release.getIcon());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + release.getIcon().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }
}
