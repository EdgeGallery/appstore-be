/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.edgegallery.appstore.interfaces.apackage.facade;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.PackageService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.infrastructure.util.AppUtil;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("PackageServiceFacade")
public class PackageServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageServiceFacade.class);

    private static final String ZIP_EXTENSION = ".zip";

    private static final String ZIP_POINT = ".";

    @Autowired
    private AppService appService;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private AppUtil appUtil;

    /**
     * Query package by package id.
     *
     * @param appId app id.
     * @param packageId package id.
     * @return PackageDto object.
     */
    public PackageDto queryPackageById(String appId, String packageId, String token) {
        Release release = appService.getRelease(appId, packageId);
        if (EnumPackageStatus.needRefresh(release.getStatus())) {
            appService.loadTestTask(appId, packageId, new AtpMetadata(release.getTestTaskId(), token));
            release = appService.getRelease(appId, packageId);
        }
        return PackageDto.of(release);
    }

    /**
     * Query package by package id.
     *
     * @param appId app id.
     * @param packageId package id.
     * @return PackageDto object.
     */
    public ResponseEntity<ResponseObject> queryPackageByIdV2(String appId, String packageId, String token) {
        PackageDto dto = queryPackageById(appId, packageId, token);
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(dto, errMsg, "query package by packageId success."));
    }

    /**
     * Get Csar file content by package id and file path.
     *
     * @param packageId package id.
     * @param filePath file path.
     */
    public String getCsarFileByName(String appId, String packageId, String filePath) {
        Release release = appService.getRelease(appId, packageId);
        filePath = FileChecker.checkByPath(filePath);
        return fileService.get(release.getPackageFile().getStorageAddress(), filePath);
    }

    /**
     * delete package.
     *
     * @param appId app id.
     * @param packageId package id.
     * @param user User object.
     */
    public void unPublishPackage(String appId, String packageId, User user) {
        appService.unPublishPackage(appId, packageId, user);
    }

    /**
     * download package by package id.
     *
     * @param appId app id.
     * @param packageId package id.
     */
    public ResponseEntity<InputStreamResource> downloadPackage(String appId, String packageId, boolean isDownloadImage,
        String token) throws IOException {
        Release release = appService.download(appId, packageId);
        String fileName = release.getAppBasicInfo().getAppName() + ZIP_EXTENSION;
        InputStream ins;
        if (isDownloadImage) {
            String storageAddress = release.getPackageFile().getStorageAddress();
            String fileParent = storageAddress.substring(0, storageAddress.lastIndexOf(ZIP_POINT));
            appUtil.loadZipIntoPackage(storageAddress, token, fileParent);
            String fileAddress = appUtil.compressAppPackage(fileParent);

            ins = fileService.get(fileAddress);
        } else {
            ins = fileService.get(release.getPackageFile());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * publish package.
     *
     */
    public ResponseEntity<String> publishPackage(String appId, String packageId) {
        packageService.publishPackage(appId, packageId);
        return ResponseEntity.ok("Publish Success");
    }

    /**
     * publish package v2.
     *
     */
    public ResponseEntity<ResponseObject> publishPackageV2(String appId, String packageId) {
        packageService.publishPackage(appId, packageId);
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject("Publish Success", errMsg, "Publish Success."));
    }

    /**
     * modify app attributes.
     *
     * @param appId app id.
     * @param packageId package id.
     * @param packageDto packageDto.
     */
    public ResponseEntity<PackageDto> updateAppById(String appId, String packageId, MultipartFile iconFile,
        MultipartFile demoVideo, PackageDto packageDto) {
        packageService.updateAppById(appId, packageId, iconFile, demoVideo, packageDto);
        Release release = packageRepository.findReleaseById(appId, packageId);
        return ResponseEntity.ok(PackageDto.of(release));
    }

    /**
     * test a package from atp.
     *
     * @param appId app id
     * @param packageId package id
     * @param token token
     * @return dto
     */
    public ResponseEntity<AtpTestDto> testPackage(String appId, String packageId, String token) {
        Release release = appService.getRelease(appId, packageId);
        if (!EnumPackageStatus.testAllowed(release.getStatus())) {
            LOGGER.error("The package status {} is not allowed to test again.", release.getStatus());
            throw new AppException("The package status is not allowed to test again.",
                ResponseConst.RET_NOT_ALLOWED_TO_TEST, release.getStatus());
        }
        return ResponseEntity.ok(packageService.testPackage(release, token));
    }

    /**
     * query all the package owned by the user, and sorted by create time.
     *
     * @param userId user id
     * @return packages
     */
    public Page<PackageDto> getPackageByUserIdV2(String userId, int limit, long offset, String appName, String status,
        String sortItem, String sortType, String token) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("createTime", "createTime");
        params.put("sortItem", sortItem);
        params.put("userId", userId);
        params.put("limit", limit);
        params.put("offset", offset);
        params.put("appName", appName);
        params.put("status", status);
        params.put("sortType", sortType);

        packageService.getPackageByUserIdV2(params).stream()
            .filter(s -> s.getTestTaskId() != null && EnumPackageStatus.needRefresh(s.getStatus())).forEach(
                s -> appService.loadTestTask(s.getAppId(), s.getPackageId(),
                    new AtpMetadata(s.getTestTaskId(), token)));
        long total = packageService.countTotalForUserId(params);
        return new Page<>(packageService.getPackageByUserIdV2(params).stream().map(PackageDto::of)
            .sorted(Comparator.comparing(PackageDto::getCreateTime).reversed()).collect(Collectors.toList()), limit,
            offset, total);
    }

    /**
     * query all the package owned by the user, and sorted by create time.
     *
     * @param userId user id
     * @return packages
     */
    public ResponseEntity<List<PackageDto>> getPackageByUserId(String userId, String token) {
        // refresh package status
        packageService.getPackageByUserId(userId).stream()
            .filter(s -> s.getTestTaskId() != null && EnumPackageStatus.needRefresh(s.getStatus())).forEach(
                s -> appService.loadTestTask(s.getAppId(), s.getPackageId(),
                    new AtpMetadata(s.getTestTaskId(), token)));

        return ResponseEntity.ok(packageService.getPackageByUserId(userId).stream().map(PackageDto::of)
            .sorted(Comparator.comparing(PackageDto::getCreateTime).reversed()).collect(Collectors.toList()));
    }
}
