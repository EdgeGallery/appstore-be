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

package org.edgegallery.appstore.interfaces.app.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppPageCriteria;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.app.Chunk;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.domain.model.app.SwImgDesc;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.releases.IconChecker;
import org.edgegallery.appstore.domain.model.releases.PackageChecker;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.releases.VideoChecker;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.domain.shared.exceptions.PermissionNotAllowedException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.infrastructure.util.AppUtil;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.AppDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("AppServiceFacade")
public class AppServiceFacade {

    public static final Logger LOGGER = LoggerFactory.getLogger(AppServiceFacade.class);

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    public static final String HEADER_VALUE = "attachment; filename=";

    private static final String ROLE_APPSTORE_ADMIN = "ROLE_APPSTORE_ADMIN";

    private static final String VM = "vm";

    @Autowired
    private AppService appService;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppUtil appUtil;

    @Value("${appstore-be.package-path}")
    private String dir;

    @Value("${appstore-be.temp-path}")
    private String filePathTemp;

    public AppServiceFacade(AppService appService) {
        this.appService = appService;
    }

    /**
     * upload image.
     */
    public ResponseEntity<String> uploadImage(boolean isMultipart, Chunk chunk) throws IOException {
        if (isMultipart) {
            MultipartFile file = chunk.getFile();

            if (file == null) {
                LOGGER.error("can not find any needed file");
                return ResponseEntity.badRequest().build();
            }
            File uploadDirTmp = new File(filePathTemp);
            checkDir(uploadDirTmp);

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                chunkNumber = 0;
            }
            File outFile = new File(filePathTemp + File.separator + chunk.getIdentifier(), chunkNumber + ".part");
            try (InputStream inputStream = file.getInputStream()) {
                FileUtils.copyInputStreamToFile(inputStream, outFile);
            }
        }

        return ResponseEntity.ok("upload package block success.");
    }

    /**
     * merge image.
     */
    public ResponseEntity merge(String fileName, String guid) throws IOException {
        File uploadDir = new File(dir);
        checkDir(uploadDir);
        File file = new File(filePathTemp + File.separator + guid);
        String randomPath = "";
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                String temp = UUID.randomUUID().toString().replace("-", "");
                String newFileAddress = dir + File.separator + temp;
                File partFiles = new File(newFileAddress);
                checkDir(partFiles);
                randomPath = temp + File.separator + fileName;
                String newFileName = partFiles + File.separator + fileName;
                File partFile = new File(newFileName);
                for (int i = 1; i <= files.length; i++) {
                    File s = new File(filePathTemp + File.separator + guid, i + ".part");
                    FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                    FileUtils.copyFile(s, destTempfos);
                    destTempfos.close();
                }
                FileUtils.deleteDirectory(file);
            }
        }

        return ResponseEntity.ok(randomPath);
    }

    private void checkDir(File fileDir) throws IOException {
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                throw new IOException("create folder failed");
            }
        }
    }

    /**
     * appRegistering.
     */
    public ResponseEntity<RegisterRespDto> appRegistering(User user, MultipartFile packageFile, AppParam appParam,
        MultipartFile iconFile, MultipartFile demoVideo, AtpMetadata atpMetadata) {
        if (!appParam.checkValidParam(appParam)) {
            throw new AppException("app param is invalid!");
        }

        String fileParent = dir + File.separator + UUID.randomUUID().toString().replace("-", "");
        File tempfile = new PackageChecker(dir).check(packageFile);
        String fileStoreageAddress = fileService.saveTo(tempfile, fileParent);
        AFile packageAFile;
        String appClass = appUtil.getAppClass(fileStoreageAddress);
        if (!appClass.isEmpty() && appClass.equals(VM)) {
            packageAFile = new AFile(packageFile.getOriginalFilename(), fileStoreageAddress);
        } else {
            packageAFile = getPkgFile(packageFile.getOriginalFilename(), fileStoreageAddress, fileParent);
        }
        packageAFile.setFileSize(packageFile.getSize());
        AFile icon = getFile(iconFile, new IconChecker(dir), fileParent);
        Release release;
        AFile demoVideoFile = null;
        if (demoVideo != null) {
            demoVideoFile = getFile(demoVideo, new VideoChecker(dir), fileParent);
        }
        release = new Release(packageAFile, icon, demoVideoFile, user, appParam, appClass);
        RegisterRespDto dto = appService.registerApp(release);
        if (atpMetadata.getTestTaskId() != null) {
            appService.loadTestTask(dto.getAppId(), dto.getPackageId(), atpMetadata);
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * appRegistering big file.
     */
    public ResponseEntity<RegisterRespDto> appRegister(User user, AppParam appParam, MultipartFile iconFile,
        MultipartFile demoVideo, AtpMetadata atpMetadata, String fileAddress) throws IOException {
        if (!appParam.checkValidParam(appParam)) {
            throw new AppException("app param is invalid!");
        }
        String fileDir = fileAddress.substring(0, fileAddress.lastIndexOf(File.separator));
        String fileParent = dir + File.separator + fileDir;
        fileAddress =  dir + File.separator + fileAddress;
        File packageFile  = new File(fileAddress);
        FileInputStream fileInputStream = new FileInputStream(packageFile);
        MultipartFile multipartFile = new MockMultipartFile("file", packageFile.getName(), "text/plain",
            IOUtils.toByteArray(fileInputStream));
        File file = new PackageChecker(fileParent).check(multipartFile);
        if (!file.exists()) {
            LOGGER.error("Package File  is Illegal.");
            throw new IllegalArgumentException("Package File name is Illegal.");
        }
        AFile packageAFile;
        String appClass = appUtil.getAppClass(fileAddress);
        if (!appClass.isEmpty() && appClass.equals(VM)) {
            packageAFile = new AFile(multipartFile.getOriginalFilename(), fileAddress);
        } else {
            packageAFile = getPkgFile(multipartFile.getOriginalFilename(), fileAddress, fileParent);
        }
        packageAFile.setFileSize(multipartFile.getSize());
        AFile icon = getFile(iconFile, new IconChecker(dir), fileParent);
        Release release;
        AFile demoVideoFile = null;
        if (demoVideo != null) {
            demoVideoFile = getFile(demoVideo, new VideoChecker(dir), fileParent);
        }
        release = new Release(packageAFile, icon, demoVideoFile, user, appParam, appClass);
        RegisterRespDto dto = appService.registerApp(release);
        if (atpMetadata.getTestTaskId() != null) {
            appService.loadTestTask(dto.getAppId(), dto.getPackageId(), atpMetadata);
        }
        return ResponseEntity.ok(dto);
    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker, String fileParent) {
        File tempfile = fileChecker.check(file);
        String fileStoreageAddress = fileService.saveTo(tempfile, fileParent);
        return new AFile(file.getOriginalFilename(), fileStoreageAddress);
    }

    private AFile getPkgFile(String fileName, String fileAddress, String fileParent) {
        List<SwImgDesc> imgDecsList;
        boolean isImgZipExist = false;

        try {
            imgDecsList = appService.getAppImageInfo(fileAddress, fileParent);
            if (imgDecsList.isEmpty()) {
                return new AFile(fileName, fileAddress);
            }

            for (SwImgDesc imageDescr : imgDecsList) {
                if (imageDescr.getSwImage().contains(".zip")) {
                    isImgZipExist = true;
                }
            }

            if (!isImgZipExist) {
                FileUtils.forceDelete(new File(fileAddress));
                appService.updateAppPackageWithRepoInfo(fileParent);
                appService.updateImgInRepo(imgDecsList);
                fileAddress = appService.compressAppPackage(fileParent);
            }
        } catch (AppException | IllegalArgumentException ex) {
            throw new AppException(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.debug("failed to delete csar package {}", ex.getMessage());
        }

        return new AFile(fileName, fileAddress);
    }

    /**
     * download APP.
     *
     * @param appId download package by app id, return latest version.
     * @return file
     */
    public ResponseEntity<InputStreamResource> downloadApp(String appId) throws FileNotFoundException {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findLastRelease().orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        app.downLoad();
        appRepository.store(app);
        InputStream ins = fileService.get(release.getPackageFile());
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, "application/octet-stream");
        headers.add(CONTENT_DISPOSITION, HEADER_VALUE + release.getPackageFile().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * download icon by app id.
     *
     * @param appId app id.
     * @return file
     */
    public ResponseEntity<InputStreamResource> downloadIcon(String appId) throws FileNotFoundException {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findLatestRelease().orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        InputStream ins = fileService.get(release.getIcon());
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, "application/octet-stream");
        headers.add(CONTENT_DISPOSITION, HEADER_VALUE + release.getIcon().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * Download demo video by app id.
     *
     * @param appId app id.
     * @return video entity
     */
    public ResponseEntity<byte[]> downloadDemoVideo(String appId) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findLatestRelease().orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        byte[] image = new byte[0];
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, "video/mp4");
        if (release.getDemoVideo() != null && release.getDemoVideo().getStorageAddress() != null) {
            try {
                image = Files.readAllBytes(new File(release.getDemoVideo().getStorageAddress()).toPath());
            } catch (IOException e) {
                LOGGER.error("get download video error: {}", e.getMessage());
            }
            headers.setContentLength(image.length);
            headers.add(CONTENT_DISPOSITION, HEADER_VALUE + release.getDemoVideo().getOriginalFileName());
        }
        return ResponseEntity.ok().headers(headers).body(image);
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
    public void unPublishApp(String appId, User user, String authorities) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        if (user.getUserId().equals(app.getUserId()) || authorities.contains(ROLE_APPSTORE_ADMIN)) {
            appService.unPublish(app);
        } else {
            throw new PermissionNotAllowedException("can not delete app");
        }
    }

    /**
     * Query app list by parameters follows.
     *
     * @param name app name.
     * @param provider app provider.
     * @param type app type.
     * @param affinity app affinity.
     * @param userId user id.
     * @param limit limit of single page.
     * @param offset offset of pages.
     * @return List<AppDto></AppDto>
     */
    public ResponseEntity<List<AppDto>> queryAppsByCond(String name, String provider, String type, String affinity,
        String userId, int limit, long offset) {
        Stream<AppDto> appStream = appRepository
            .query(new AppPageCriteria(limit, offset, name, provider, type, affinity, userId)).map(AppDto::of)
            .getResults().stream();
        if (userId == null) {
            appStream = appStream.filter(a -> a.getStatus() == EnumAppStatus.Published && a.getShowType() != "private");
        }
        return ResponseEntity.ok(appStream.collect(Collectors.toList()));
    }

    /**
     * Find all package list by parameters follows.
     *
     * @param appId app id.
     * @param limit limit of single page.
     * @param offset offset of pages.
     * @return List<PackageDto></PackageDto>
     */
    public ResponseEntity<List<PackageDto>> findAllPackages(String appId, String userId, int limit, long offset,
        String token) {
        Stream<Release> releaseStream = appRepository.findAllWithPagination(new PageCriteria(limit, offset, appId))
            .getResults().stream();
        if (userId == null) {
            releaseStream = releaseStream.filter(p -> p.getStatus() == EnumPackageStatus.Published);
        } else {
            releaseStream.filter(r -> r.getUser().getUserId().equals(userId))
                    .filter(s -> s.getTestTaskId() != null && EnumPackageStatus.needRefresh(s.getStatus())).forEach(
                        s -> appService.loadTestTask(
                            s.getAppId(), s.getPackageId(), new AtpMetadata(s.getTestTaskId(), token)));
            releaseStream = appRepository.findAllWithPagination(new PageCriteria(limit, offset, appId)).getResults()
                .stream().filter(r -> r.getUser().getUserId().equals(userId));
        }
        List<PackageDto> packageDtos = releaseStream.map(PackageDto::of).collect(Collectors.toList());
        return ResponseEntity.ok(packageDtos);
    }

}
