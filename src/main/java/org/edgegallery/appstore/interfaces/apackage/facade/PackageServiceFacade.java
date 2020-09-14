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
import org.edgegallery.appstore.application.AppService;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.service.FileService;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("PackageServiceFacade")
public class PackageServiceFacade {

    @Autowired
    private AppService appService;

    @Autowired
    private FileService fileService;

    /**
     * Query package by package id.
     *
     * @param appId app id.
     * @param packageId package id.
     * @return PackageDto object.
     */
    public PackageDto queryPackageById(String appId, String packageId) {
        Release release = appService.getRelease(appId, packageId);
        return PackageDto.of(release);
    }



    /**
     * Get Csar file content by package id and file path.
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
    public ResponseEntity<InputStreamResource> downloadPackage(String appId, String packageId) throws
                                                                                               FileNotFoundException {
        Release release = appService.download(appId,packageId);
        InputStream ins = fileService.get(release.getPackageFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename="
            + release.getPackageFile().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }
}
