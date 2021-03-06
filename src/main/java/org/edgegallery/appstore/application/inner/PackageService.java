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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.edgegallery.appstore.application.external.atp.AtpService;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.releases.IconChecker;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.releases.VideoChecker;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("PackageService")
public class PackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageService.class);

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AtpService atpService;

    @Autowired
    private LocalFileService fileService;

    @Value("${appstore-be.package-path}")
    private String dir;

    /**
     * publish a package.
     *
     * @param appId app id
     * @param packageId package id
     */
    public void publishPackage(String appId, String packageId) {
        Release release = packageRepository.findReleaseById(appId, packageId);
        if (release.getStatus() != EnumPackageStatus.Test_success) {
            LOGGER.error("Test status is {}, publish failed", release.getStatus());
            throw new AppException("Test status is not success, publish failed", ResponseConst.RET_PUBLISH_NO_TESTED);
        }
        appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND))
            .checkReleases(release);
        release.setStatus(EnumPackageStatus.Published);
        publishAppAndPackage(appId, release);
    }

    /**
     * update app and package to published status.
     *
     * @param appId app id
     * @param release a package
     */
    @Transactional
    public void publishAppAndPackage(String appId, Release release) {
        App app = appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND));
        if (app.getStatus() != EnumAppStatus.Published) {
            app.setStatus(EnumAppStatus.Published);
            appRepository.store(app);
        }
        release.setStatus(EnumPackageStatus.Published);
        packageRepository.updateRelease(release);
    }

    /**
     * test a package from atp.
     *
     * @param release a package
     * @param token token
     * @return dto
     */
    public AtpTestDto testPackage(Release release, String token) {
        AtpTestDto dto = atpService.createTestTask(release, token);
        if (dto != null) {
            release.setStatus(EnumPackageStatus.fromString(dto.getStatus()));
            release.setTestTaskId(dto.getAtpTaskId());
            packageRepository.updateRelease(release);
        }
        return dto;
    }

    /**
     * query all the packages by user id.
     * @return releases
     */
    public List<Release> getPackageByUserIdV2(Map<String, Object> params) {
        return packageRepository.findReleaseByUserIdV2(params);
    }

    /**
     * query all the packages by user id.
     *
     * @param userId user id
     * @return releases
     */
    public List<Release> getPackageByUserId(String userId) {
        return packageRepository.findReleaseByUserId(userId);
    }

    /**
     * query all the packages total by user id.
     *
     * @param params search condition
     * @return releases
     */
    public Integer countTotalForUserId(Map<String, Object> params) {
        return packageRepository.countTotalForUserId(params);
    }

    /**
     * update app package information.
     *
     * @param appId app Id
     * @param packageId package Id
     * @param packageDto packageDto
     */
    public void updateAppById(String appId, String packageId, MultipartFile iconFile, MultipartFile demoVideo,
        PackageDto packageDto) {
        Release release = packageRepository.findReleaseById(appId, packageId);
        App app = appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND));
        String fileParent = dir + File.separator + UUID.randomUUID().toString().replace("-", "");
        if (iconFile != null) {
            AFile icon = getFile(iconFile, new IconChecker(dir), fileParent);
            release.setIcon(icon);
        }
        if (demoVideo != null) {
            AFile demoVideoFile = getFile(demoVideo, new VideoChecker(dir), fileParent);
            release.setDemoVideo(demoVideoFile);
        }
        if (packageDto.getIndustry() != null) {
            release.setIndustry(packageDto.getIndustry());
            app.setIndustry(packageDto.getIndustry());
        }
        if (packageDto.getType() != null) {
            release.setApplicationType(packageDto.getType());
            app.setApplicationType(packageDto.getType());
        }
        if (packageDto.getAffinity() != null) {
            release.setAffinity(packageDto.getAffinity());
            app.setAffinity(packageDto.getAffinity());
        }
        if (packageDto.getShortDesc() != null) {
            release.setShortDesc(packageDto.getShortDesc());
            app.setShortDesc(packageDto.getShortDesc());
        }
        if (packageDto.getShowType() != null) {
            release.setShowType(packageDto.getShowType());
            app.setShowType(packageDto.getShowType());
        }
        release.setExperienceAble(packageDto.isExperienceAble());
        appRepository.store(app);
        packageRepository.updateRelease(release);
    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker, String fileParent) {
        File tempFile = fileChecker.check(file);
        String fileStorageAddress = fileService.saveTo(tempFile, fileParent);
        return new AFile(file.getOriginalFilename(), fileStorageAddress);
    }
}
