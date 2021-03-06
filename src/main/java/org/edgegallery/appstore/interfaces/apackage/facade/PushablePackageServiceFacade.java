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

package org.edgegallery.appstore.interfaces.apackage.facade;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.PullablePackageService;
import org.edgegallery.appstore.application.inner.PushablePackageService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.releases.UnknownReleaseExecption;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageRepository;
import org.edgegallery.appstore.infrastructure.util.AppUtil;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PullAppReqDto;
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

    @Autowired
    private PullablePackageService pullablePackageService;

    @Autowired
    private AppUtil appUtil;

    /**
     * query all pushable packages.
     *
     * @return list
     */
    public Page<PushablePackageDto> queryAllPushablePackagesV2(int limit, int offset, String appName, String sortType,
        String sortItem) {
        return pushablePackageService.queryAllPushablePackagesV2(limit, offset, appName, sortType, sortItem);
    }

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
            throw new UnknownReleaseExecption(packageId, ResponseConst.RET_PACKAGE_NOT_FOUND);
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
    public ResponseEntity<InputStreamResource> downloadPackage(String packageId, String targetAppstore)
        throws FileNotFoundException {
        PushablePackageDto packageDto = pushablePackageService.getPushablePackage(packageId);
        // add message log for this action
        recordLog(packageDto, targetAppstore);
        Release release = appService.download(packageDto.getAppId(), packageId);
        String fileName = appUtil.getFileName(release, release.getPackageFile());
        InputStream ins = fileService.get(release.getPackageFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    private void recordLog(PushablePackageDto packageDto, String targetAppstore) {
        // add message log for this action
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageType(EnumMessageType.BE_DOWNLOADED);
        BasicMessageInfo basicMessageInfo = new BasicMessageInfo(packageDto);
        String sourceAppstore = packageDto.getSourcePlatform();
        message.setSourceAppStore(sourceAppstore);
        message.setTargetAppStore(targetAppstore);
        message.setDescription(generateDescription(EnumMessageType.BE_DOWNLOADED, sourceAppstore, targetAppstore));
        message.setAtpTestStatus(packageDto.getAtpTestStatus());
        message.setBasicInfo(basicMessageInfo);
        message.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // store message to the db
        messageRepository.addMessage(message);
    }

    private String generateDescription(EnumMessageType type, String sourceAppstore, String targetAppstore) {
        if (type == EnumMessageType.BE_DOWNLOADED) {
            return String.format("%s download this app from %s.", targetAppstore, sourceAppstore);
        }
        return "";
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
        String fileName = appUtil.getFileName(release, release.getIcon());
        InputStream ins = fileService.get(release.getIcon());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * query all pullable packages.
     *
     * @param limit appstore size
     * @param offset appstore list offset
     * @return list
     */
    public ResponseEntity<List<PushablePackageDto>> queryAllPullablePackagesV2(int limit, int offset, String appName,
        String sortType, String sortItem) {
        return ResponseEntity
            .ok(pullablePackageService
                .queryAllPullablePackagesV2(limit, offset, appName, sortType, sortItem).getResults());
    }

    /**
     * get pullable packages by id.
     *
     * @param platformId appstore id
     * @param userId user id
     * @return dto
     */
    public ResponseEntity<Page<PushablePackageDto>> getPullablePackagesV2(String platformId, int limit, long offset,
        String sortType, String sortItem, String appName, String userId) {
        return  pullablePackageService.getPullablePackagesV2(platformId, limit, offset,
            sortType, sortItem, appName, userId);
    }

    /**
     * query all pullable packages.
     *
     * @return list
     */
    public ResponseEntity<List<PushablePackageDto>> queryAllPullablePackages() {
        return ResponseEntity.ok(pullablePackageService.queryAllPullablePackages());
    }

    /**
     * get pullable packages by id.
     *
     * @param platformId appstore id
     * @param userId user id
     * @return dto
     */
    public ResponseEntity<List<PushablePackageDto>> getPullablePackages(String platformId, String userId) {
        return ResponseEntity.ok(pullablePackageService.getPullablePackages(platformId, userId));
    }

    /**
     * get pullable packages by id.
     *
     * @param packageId package id
     * @param dto PullAppReqDto
     * @return bool
     */
    public Boolean pullPackage(String packageId, PullAppReqDto dto) {
        User user = new User(dto.getUserId(), dto.getUserName());
        PushablePackageDto packageDto = new PushablePackageDto(dto);
        return pullablePackageService.pullPackage(packageId, dto.getSourceStoreId(), user, packageDto);
    }
}
