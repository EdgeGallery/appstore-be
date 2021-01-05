/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.PackageService;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.exceptions.OperateAvailableException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("PackageServiceFacade")
public class PackageServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageServiceFacade.class);

    @Autowired
    private AppService appService;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private PackageService packageService;

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
     * Get Csar file content by package id and file path.
     *
     * @param packageId package id.
     * @param filePath file path.
     */
    public String getCsarFileByName(String appId, String packageId, String filePath) throws IOException {
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
     * @return
     */
    public ResponseEntity<InputStreamResource> downloadPackage(String appId, String packageId)
        throws FileNotFoundException {
        Release release = appService.download(appId, packageId);
        InputStream ins = fileService.get(release.getPackageFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + release.getPackageFile().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    public ResponseEntity<String> publishPackage(String appId, String packageId) {
        packageService.publishPackage(appId, packageId);
        return ResponseEntity.ok("Publish Success");
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
            throw new OperateAvailableException("The package status is not allowed to test again.");
        }
        return ResponseEntity.ok(packageService.testPackage(release, token));
    }

    /**
     * query all the package owned by the user, and sorted by create time.
     *
     * @param userId user id
     * @return packages
     */
    public ResponseEntity<List<PackageDto>> getPackageByUserId(String userId) {
        return ResponseEntity.ok(packageService.getPackageByUserId(userId).stream().map(PackageDto::of)
            .sorted(Comparator.comparing(PackageDto::getCreateTime).reversed()).collect(Collectors.toList()));
    }
}
