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

package org.edgegallery.appstore.interfaces.app.facade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.edgegallery.appstore.application.AppService;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppPageCriteria;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.releases.IconChecker;
import org.edgegallery.appstore.domain.model.releases.PackageChecker;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.service.FileService;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.AppDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("AppServiceFacade")
public class AppServiceFacade {

    @Autowired
    private AppService appService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AppRepository appRepository;

    @Value("${appstore-be.package-path}")
    private String dir;

    public AppServiceFacade(AppService appService) {
        this.appService = appService;
    }

    /**
     * appRegistering.
     */
    public void appRegistering(User user, MultipartFile packageFile, AppParam appParam, MultipartFile iconFile)
        throws IOException {

        AFile packageAFile = getFile(packageFile, new PackageChecker(dir));
        AFile icon = getFile(iconFile, new IconChecker(dir));

        Release release = new Release(packageAFile, icon, user, appParam);

        appService.registerApp(release);
    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker) throws IOException {
        File tempfile = fileChecker.check(file);
        String fileStoreageAddress = fileService.saveTo(tempfile);
        return new AFile(file.getOriginalFilename(), fileStoreageAddress);
    }

    /**
     * download APP.
     *
     * @param appId download package by app id, return latest version.
     * @return
     */
    public ResponseEntity<InputStreamResource> downloadApp(String appId) throws FileNotFoundException {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findLastRelease().orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        app.downLoad();
        appRepository.store(app);
        InputStream ins = fileService.get(release.getPackageFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename="
            + release.getPackageFile().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * download icon by app id.
     *
     * @param appId app id.
     * @return
     */
    public ResponseEntity<InputStreamResource> downloadIcon(String appId) throws FileNotFoundException {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findLastRelease().orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        InputStream ins = fileService.get(release.getIcon());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename="
            + release.getIcon().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    public App queryByAppId(String appId) {
        return appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
    }

    /**
     * delete app by app id and user.
     *
     * @param appId app id.
     * @param user User object.
     */
    public void unPublishApp(String appId, User user) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));

        if (user.getUserId().equals(app.getUserId())) {
            appService.unPublish(app);
        }
    }

    /**
     * Query app list by parameters follows.
     * @param name app name.
     * @param provider app provider.
     * @param type app type.
     * @param affinity app affinity.
     * @param userId user id.
     * @param limit limit of single page.
     * @param offset offset of pages.
     * @return
     */
    public ResponseEntity<List<AppDto>> queryAppsByCond(String name, String provider, String type, String affinity,
        String userId, int limit, long offset) {
        return ResponseEntity.ok(
            appRepository.query(new AppPageCriteria(limit, offset, name, provider, type, affinity, userId))
                .map(AppDto::of)
                .getResults());
    }

    /**
     * Find all package list by parameters follows.
     * @param appId app id.
     * @param limit limit of single page.
     * @param offset offset of pages.
     * @return
     */
    public ResponseEntity<List<PackageDto>> findAllPackages(String appId, int limit, long offset) {
        return ResponseEntity.ok(appRepository.findAllWithPagination(new PageCriteria(limit, offset, appId))
            .map(PackageDto::of)
            .getResults());
    }

}
