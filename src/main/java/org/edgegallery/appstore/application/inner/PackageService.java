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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.atp.AtpService;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.AbstractFileChecker;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.IconChecker;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.releases.VideoChecker;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.infrastructure.files.LocalFileServiceImpl;
import org.edgegallery.appstore.infrastructure.util.AppUtil;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PublishAppReqDto;
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
    private LocalFileServiceImpl fileService;

    @Autowired
    private AppUtil appUtil;

    @Value("${appstore-be.package-path}")
    private String dir;

    /**
     * publish a package.
     *
     * @param appId app id
     * @param packageId package id
     * @param publishAppReq publish request body
     */
    public void publishPackage(String appId, String packageId, PublishAppReqDto publishAppReq) {
        Release release = packageRepository.findReleaseById(appId, packageId);
        if (release.getStatus() != EnumPackageStatus.Test_success) {
            LOGGER.error("Test status is {}, publish failed", release.getStatus());
            throw new AppException("Test status is not success, publish failed", ResponseConst.RET_PUBLISH_NO_TESTED);
        }
        if (!appRepository.find(appId).isPresent()) {
            throw new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND);
        }
        Optional<App> existedApp = appRepository
            .findByAppNameAndProvider(release.getAppBasicInfo().getAppName(),
                release.getAppBasicInfo().getProvider());
        if (existedApp.isPresent()) {
            App app = existedApp.get();
            app.checkReleases(release);
        }
        release.setStatus(EnumPackageStatus.Published);
        publishAppAndPackage(appId, release, publishAppReq);
    }

    /**
     * update app and package to published status.
     *
     * @param appId app id
     * @param release a package
     * @param publishAppReq publish request body
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishAppAndPackage(String appId, Release release, PublishAppReqDto publishAppReq) {
        App app = appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND));
        if (app.getStatus() != EnumAppStatus.Published) {
            app.setStatus(EnumAppStatus.Published);
            app.setFree(publishAppReq.isFree());
            app.setPrice(publishAppReq.getPrice());
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
     * @param iconFile app icon
     * @param demoVideo app demo video
     * @param docFile app detail md file.
     * @param packageDto packageDto
     */
    public void updateAppById(MultipartFile iconFile, MultipartFile demoVideo, MultipartFile docFile,
        PackageDto packageDto) {
        String appId = packageDto.getAppId();
        Release release = packageRepository.findReleaseById(appId, packageDto.getPackageId());
        App app = appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND));
        modifyPackageFile(docFile, packageDto.getShortDesc(), release);
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
            release.getAppBasicInfo().setAppDesc(packageDto.getShortDesc());
            app.setShortDesc(packageDto.getShortDesc());
        }
        if (packageDto.getShowType() != null) {
            release.setShowType(packageDto.getShowType());
            app.setShowType(packageDto.getShowType());
        }
        if (docFile != null) {
            String mdContent = modifyAppDetail(docFile, release);
            if (mdContent != null) { // app detail may be empty content
                release.getAppBasicInfo().setMarkDownContent(mdContent);
            }
        }
        release.setExperienceAble(packageDto.isExperienceAble());
        appRepository.store(app);
        packageRepository.updateRelease(release);
    }

    private String modifyAppDetail(MultipartFile docFile, Release release) {
        String pkgPath = release.getPackageFile().getStorageAddress();
        File tempFile = transMultiFileToFile(docFile, pkgPath);
        if (tempFile == null) {
            return null;
        }
        try {
            return FileUtils.readFileToString(tempFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("read file error, {}", e.getMessage());
            return null;
        }
    }

    private File transMultiFileToFile(MultipartFile multipartFile, String filePath) {
        if (multipartFile == null) {
            return null;
        }
        String tempPath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1)
            + multipartFile.getOriginalFilename();
        File tempFile = new File(tempPath);
        try {
            multipartFile.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            LOGGER.error("transfer multipartFile to file error, {}", e.getMessage());
            return null;
        }
    }

    private void modifyPackageFile(MultipartFile docFile, String shortDesc, Release release) {
        if (docFile == null && StringUtils.isEmpty(shortDesc)) {
            return;
        }
        String packagePath = release.getPackageFile().getStorageAddress();
        File packageFullFile = new File(packagePath.substring(0, packagePath.lastIndexOf(".")));
        String fileParent = packageFullFile.getPath();
        if (!packageFullFile.exists() || !packageFullFile.isDirectory()) {
            appUtil.unzipApplicationPackage(packagePath, fileParent);
        }
        try {
            if (docFile != null) {
                File tempFile = transMultiFileToFile(docFile, packagePath);
                File srcFile = appUtil.getFile(fileParent + "/Artifacts/Docs", "md");
                if (srcFile != null) {
                    FileUtils.copyFile(tempFile, srcFile);
                } else {
                    FileUtils.copyFileToDirectory(tempFile, new File(fileParent + "/Artifacts/Docs"));
                }
            }
            if (!StringUtils.isEmpty(shortDesc)) {
                File mfFile = appUtil.getFile(fileParent, "mf");
                String oldValue = "app_package_description: " + release.getAppBasicInfo().getAppDesc();
                String newValue = "app_package_description: " + shortDesc;
                FileUtils.writeStringToFile(mfFile, FileUtils.readFileToString(mfFile, StandardCharsets.UTF_8)
                        .replace(oldValue, newValue), StandardCharsets.UTF_8, false);
            }
            String fileExtension = packagePath.substring(packagePath.lastIndexOf("."));
            appUtil.compressAndDeleteFile(fileParent, fileParent, fileExtension);
        } catch (IOException e) {
            LOGGER.error("modifyPackageFile catch exception {}.", e.getMessage());
        }
    }

    private AFile getFile(MultipartFile file, AbstractFileChecker fileChecker, String fileParent) {
        File tempFile = fileChecker.check(file);
        String fileStorageAddress = fileService.saveTo(tempFile, fileParent);
        return new AFile(file.getOriginalFilename(), fileStorageAddress);
    }

    /**
     * query all the packages by create time.
     *
     * @return releases
     */
    public List<Release> getPackageByCreateTime(int limit, int offset, Date startDate, Date endDate) {
        return packageRepository.getPackageByCreateTime(limit, offset, startDate, endDate);
    }

    /**
     * query all the packages total by create time.
     *
     */
    public Integer countTotalForCreateTime(int limit, int offset, Date startDate, Date endDate) {
        return packageRepository.countTotalForCreateTime(limit, offset, startDate, endDate);
    }
}
