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

import java.util.List;
import org.edgegallery.appstore.application.external.atp.AtpService;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.domain.shared.exceptions.OperateAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("PackageService")
public class PackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageService.class);

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AtpService atpService;

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
            throw new OperateAvailableException("Test status is not success, publish failed");
        }
        appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId))
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
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
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
     *
     * @param userId user id
     * @return releases
     */
    public List<Release> getPackageByUserId(String userId) {
        return packageRepository.findReleaseByUserId(userId);
    }
}
