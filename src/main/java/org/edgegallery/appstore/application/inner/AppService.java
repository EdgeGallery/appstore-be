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

package org.edgegallery.appstore.application.inner;

import java.util.Optional;
import org.edgegallery.appstore.application.external.AtpService;
import org.edgegallery.appstore.application.external.model.AtpMetadata;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.comment.CommentRepository;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.releases.UnknownReleaseExecption;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.service.FileService;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("AppRegisterService")
public class AppService {

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private AtpService atpService;

    public Release getRelease(String appId, String packageId) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        return app.findByVersion(packageId).orElseThrow(() -> new UnknownReleaseExecption(packageId));
    }

    /**
     * register app.
     *
     * @param release use object of release to register.
     */
    @Transactional
    public RegisterRespDto registerApp(Release release) {

        Optional<App> existedApp = appRepository
            .findByAppNameAndProvider(release.getAppBasicInfo().getAppName(), release.getAppBasicInfo().getProvider());
        App app = null;
        if (existedApp.isPresent()) {
            app = existedApp.get();
            app.checkReleases(release);
            app.upload(release);
        } else {
            String appId = appRepository.generateAppId();
            app = new App(appId, release);
        }
        release.setAppIdValue(app.getAppId());
        appRepository.store(app);
        return RegisterRespDto.builder().appName(release.getAppBasicInfo().getAppName()).appId(app.getAppId())
            .packageId(release.getPackageId()).provider(app.getProvider())
            .version(release.getAppBasicInfo().getVersion()).build();
    }

    /**
     * delete package by app id and package id.
     *
     * @param appId app id
     * @param packageId package id
     * @param user obj of User
     */
    @Transactional
    public void unPublishPackage(String appId, String packageId, User user) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findByVersion(packageId).orElseThrow(() -> new UnknownReleaseExecption(packageId));
        release.checkPermission(user.getUserId());
        deleteReleaseFile(release);
        app.unPublish(release);
        if (app.getReleases().isEmpty()) {
            unPublish(app);
        } else {
            appRepository.store(app);
        }
    }

    /**
     * download package by app id and package id.
     *
     * @param appId app id.
     * @param packageId package id.
     * @return
     */
    public Release download(String appId, String packageId) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findByVersion(packageId).orElseThrow(() -> new UnknownReleaseExecption(packageId));
        app.downLoad();
        appRepository.store(app);
        return release;
    }

    /**
     * unPublish app.
     *
     * @param app app object.
     */
    @Transactional
    public void unPublish(App app) {
        app.getReleases().forEach(this::deleteReleaseFile);
        appRepository.remove(app.getAppId());
        commentRepository.removeByAppId(app.getAppId());
    }

    // delete release file
    private void deleteReleaseFile(Release release) {
        fileService.delete(release.getIcon());
        fileService.delete(release.getPackageFile());
    }

    public void loadTestTask(String packageId, AtpMetadata atpMetadata) {
        String status = atpService.getAtpTaskResult(atpMetadata.getToken(), atpMetadata.getTestTaskId());
        if (status != null) {
            EnumPackageStatus packageStatus = EnumPackageStatus.fromString(status);
            packageRepository.updateStatus(packageId, packageStatus);
        }
    }
}
