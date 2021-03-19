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

    @Autowired
    private AppService appService;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private AppRepository appRepository;

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
    public ResponseEntity<RegisterRespDto> uploadImage(boolean isMultipart, Chunk chunk) throws Exception {
        if (isMultipart) {
            MultipartFile file = chunk.getFile();

            if (file == null) {
                LOGGER.error("can not find any needed file");
                return ResponseEntity.badRequest().build();
            }
            File uploadDirTmp = new File(filePathTemp);
            if (!uploadDirTmp.exists()) {
                boolean rt = uploadDirTmp.mkdirs();
                if (rt == false) {
                    throw new Exception("create folder failed");
                }
            }

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                chunkNumber = 0;
            }
            File outFile = new File(filePathTemp + File.separator + chunk.getIdentifier(), chunkNumber + ".part");
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * merge image.
     */
    public ResponseEntity merge(String fileName, String guid) throws Exception {
        File uploadDir = new File(dir);
        if (!uploadDir.exists()) {
            boolean rt = uploadDir.mkdirs();
            if (rt == false) {
                throw new Exception("create folder failed");
            }

        }
        File file = new File(filePathTemp + File.separator + guid);
        String newFileAddress = "";
        String newFileName = "";
        String temfolder = "";
        String randomPath = "";
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                temfolder = UUID.randomUUID().toString().replace("-", "");
                newFileAddress = dir + File.separator + temfolder;
                File partFiles = new File(newFileAddress);
                if (!partFiles.exists()) {
                    boolean rt = partFiles.mkdirs();
                    if (rt == false) {
                        throw new Exception("create folder failed");
                    }
                }
                randomPath = temfolder + File.separator + fileName;
                newFileName = partFiles + File.separator + fileName;
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


    /**
     * appRegistering.
     */
    public ResponseEntity<RegisterRespDto> appRegistering(User user, MultipartFile packageFile, AppParam appParam,
        MultipartFile iconFile, MultipartFile demoVideo, AtpMetadata atpMetadata) {
        if (!appParam.checkValidParam(appParam)) {
            throw new AppException("app param is invalid!");
        }

        String fileParent = dir + File.separator + UUID.randomUUID().toString().replace("-", "");
        AFile packageAFile = getPkgFile(packageFile, new PackageChecker(dir), fileParent);
        AFile icon = getFile(iconFile, new IconChecker(dir), fileParent);
        Release release;
        AFile demoVideoFile = null;
        if (demoVideo != null) {
            demoVideoFile = getFile(demoVideo, new VideoChecker(dir), fileParent);
        }
        release = new Release(packageAFile, icon, demoVideoFile, user, appParam);
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
        AFile packageAFile = getPkgFileNew(fileAddress, new PackageChecker(fileParent), fileParent);
        AFile icon = getFile(iconFile, new IconChecker(dir), fileParent);
        Release release;
        AFile demoVideoFile = null;
        if (demoVideo != null) {
            demoVideoFile = getFile(demoVideo, new VideoChecker(dir), fileParent);
        }
        release = new Release(packageAFile, icon, demoVideoFile, user, appParam);
        RegisterRespDto dto = appService.registerApp(release);
        if (atpMetadata.getTestTaskId() != null) {
            appService.loadTestTask(dto.getAppId(), dto.getPackageId(), atpMetadata);
        }
        return ResponseEntity.ok(dto);
    }

    private AFile getPkgFileNew(String fileAddress, FileChecker fileChecker, String fileDir) throws IOException {
        File packageFile  = new File(fileAddress);
        FileInputStream fileInputStream = new FileInputStream(packageFile);
        MultipartFile multipartFile = new MockMultipartFile("file", packageFile.getName(), "text/plain", IOUtils.toByteArray(fileInputStream));
        File file = fileChecker.check(multipartFile);
        if(!file.exists()){
            LOGGER.error("Package File  is Illegal.");
            throw new IllegalArgumentException("Package File name is Illegal.");
        }
        List<SwImgDesc> imgDecsList;
        boolean isImgZipExist = false;
        String fileDirName = fileAddress.substring(fileAddress.lastIndexOf(File.separator) + 1);
        try {

            imgDecsList = appService.getAppImageInfo(fileAddress, fileDir);
            if (imgDecsList.isEmpty()) {
                return new AFile(fileDirName, fileAddress);
            }

            for (SwImgDesc imageDescr : imgDecsList) {
                if (imageDescr.getSwImage().contains(".zip")) {
                    isImgZipExist = true;
                }
            }

            if (!isImgZipExist) {
                FileUtils.forceDelete(new File(fileAddress));
                appService.updateAppPackageWithRepoInfo(fileDir);
                appService.updateImgInRepo(imgDecsList);
                fileAddress = appService.compressAppPackage(fileDir);
            }
        } catch (AppException | IllegalArgumentException ex) {
            throw new AppException(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.debug("failed to delete csar package {}", ex.getMessage());
        }
        return new AFile(fileDirName, fileAddress);
    }

    private AFile getFile(MultipartFile file, FileChecker fileChecker, String fileParent) {
        File tempfile = fileChecker.check(file);
        String fileStoreageAddress = fileService.saveTo(tempfile, fileParent);
        return new AFile(file.getOriginalFilename(), fileStoreageAddress);
    }

    private AFile getPkgFile(MultipartFile file, FileChecker fileChecker, String fileParent) {
        File tempfile = fileChecker.check(file);
        String fileStoreageAddress = fileService.saveTo(tempfile, fileParent);

        List<SwImgDesc> imgDecsList;
        boolean isImgZipExist = false;

        try {
            imgDecsList = appService.getAppImageInfo(fileStoreageAddress, fileParent);
            if (imgDecsList.isEmpty()) {
                return new AFile(file.getOriginalFilename(), fileStoreageAddress);
            }

            for (SwImgDesc imageDescr : imgDecsList) {
                if (imageDescr.getSwImage().contains(".zip")) {
                    isImgZipExist = true;
                }
            }

            if (!isImgZipExist) {
                FileUtils.forceDelete(new File(fileStoreageAddress));
                appService.updateAppPackageWithRepoInfo(fileParent);
                appService.updateImgInRepo(imgDecsList);
                fileStoreageAddress = appService.compressAppPackage(fileParent);
            }
        } catch (AppException | IllegalArgumentException ex) {
            throw new AppException(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.debug("failed to delete csar package {}", ex.getMessage());
        }

        return new AFile(file.getOriginalFilename(), fileStoreageAddress);
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
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + release.getPackageFile().getOriginalFileName());
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
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + release.getIcon().getOriginalFileName());
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * Download demo video by app id.
     *
     * @param appId app id.
     * @return video entity
     */
    public ResponseEntity<byte[]> downloadDemoVideo(String appId) throws FileNotFoundException {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findLatestRelease().orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        byte[] image = new byte[0];
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "video/mp4");
        if (release.getDemoVideo() != null && release.getDemoVideo().getStorageAddress() != null) {
            try {
                image = Files.readAllBytes(new File(release.getDemoVideo().getStorageAddress()).toPath());
            } catch (IOException e) {
                LOGGER.error("get download video error: {}", e.getMessage());
            }
            headers.setContentLength(image.length);
            headers.add("Content-Disposition", "attachment; filename=" + release.getDemoVideo().getOriginalFileName());
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
    public void unPublishApp(String appId, User user) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        if ("admin".equals(user.getUserName()) || user.getUserId().equals(app.getUserId())) {
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
            appStream = appStream.filter(a -> a.getStatus() == EnumAppStatus.Published);
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
