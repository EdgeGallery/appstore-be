/*
 *    Copyright 2020-2022 Huawei Technologies Co., Ltd.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.application.inner.PackageService;
import org.edgegallery.appstore.application.packageupload.UploadPackageService;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.releases.AbstractFileChecker;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.files.LocalFileServiceImpl;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;
import org.edgegallery.appstore.infrastructure.util.AppUtil;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PublishAppReqDto;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppCtrlDto;
import org.edgegallery.appstore.interfaces.meao.facade.ProgressFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String TEMP_EXPIRE_PREFIX = "tempExpire";

    @Value("${appstore-be.package-path}")
    private String packageDir;

    /**
     * scheduled clean up tempPackage more than 24 hours.
     */
    private static final long CLEAN_ENV_WAIT_TIME = 1000L * 60 * 60 * 24;

    @Autowired
    private AppService appService;

    @Autowired
    private LocalFileServiceImpl fileService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppUtil appUtil;

    @Autowired
    private UploadPackageService uploadPackageService;

    @Autowired
    ProgressFacade progressFacade;

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
        filePath = AbstractFileChecker.checkByPath(filePath);
        return fileService.get(release.getPackageFile().getStorageAddress(), filePath);
    }

    /**
     * delete package.
     *
     * @param appId app id.
     * @param packageId package id.
     * @param user User object.
     * @param isAdmin admin delete permission.
     */
    public void deletePackage(String appId, String packageId, User user, boolean isAdmin) {
        appService.deletePackage(appId, packageId, user, isAdmin);
    }

    /**
     * download package by package id.
     *
     * @param appId app id.
     * @param packageId package id.
     */
    public ResponseEntity<InputStreamResource> downloadPackage(String appId, String packageId,
        boolean isDownloadImage) throws IOException {
        Release release = appService.download(appId, packageId);
        InputStream ins;
        if (isDownloadImage) {
            String storageAddress = release.getPackageFile().getStorageAddress();
            String fileParent = storageAddress.substring(0, storageAddress.lastIndexOf(ZIP_POINT));
            appUtil.loadZipIntoPackage(storageAddress, fileParent);
            String fileZipName = new File(storageAddress).getParentFile().getCanonicalFile() + File.separator
                + TEMP_EXPIRE_PREFIX + release.getAppBasicInfo().getAppName();
            String fileAddress = appUtil.compressAndDeleteFile(fileParent, fileZipName, ZIP_EXTENSION);
            ins = fileService.get(fileAddress);
        } else {
            ins = fileService.get(release.getPackageFile());
        }
        String fileName = release.getAppBasicInfo().getAppName() + "_download" + ZIP_EXTENSION;
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + encodeFileName);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * download icon by package id.
     *
     * @param appId app id.
     * @param packageId package id.
     */
    public ResponseEntity<InputStreamResource> downloadIcon(String appId, String packageId) throws IOException {
        Release release = appService.getRelease(appId, packageId);
        String fileName = appUtil.getFileName(release, release.getIcon());
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
        InputStream ins = fileService.get(release.getIcon());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/octet-stream");
        headers.add("Content-Disposition", "attachment; filename=" + encodeFileName);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(ins));
    }

    /**
     * sysc package to meao.
     *
     * @param appId appId
     * @param packageId packageId
     * @param token token
     * @return ResponseEntity
     * @throws IOException IOException
     */
    public ResponseEntity<ResponseObject> syncPackage(String appId, String packageId, String meaoId, String token)
        throws IOException {
        Release release = appService.download(appId, packageId);
        if ("container".equalsIgnoreCase(release.getDeployMode())) {
            throw new AppException("can not support container app.", ResponseConst.RET_CONTAINER_NOT_SUPPORT);
        }
        // build upload progress data
        String progressId = UUID.randomUUID().toString();
        Date createTime = Timestamp.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        PackageUploadProgress progress = new PackageUploadProgress(progressId, packageId, meaoId, createTime);
        progressFacade.createProgress(progress);
        LOGGER.info("progressId create: {}}", progressId);

        String storageAddress = release.getPackageFile().getStorageAddress();
        String fileParent = storageAddress.substring(0, storageAddress.lastIndexOf(ZIP_POINT));
        String fileZipName = new File(storageAddress).getParentFile().getCanonicalFile() + File.separator
            + TEMP_EXPIRE_PREFIX + release.getAppBasicInfo().getAppName();
        if (!new File(fileZipName + ZIP_EXTENSION).exists()) {
            appUtil.loadZipIntoPackage(storageAddress, fileParent);
            appUtil.compressAndDeleteFile(fileParent, fileZipName, ZIP_EXTENSION);
        }

        // start a thread to upload package to meao
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        new Thread(() -> uploadPackageService
            .uploadPackage(fileZipName + ZIP_EXTENSION, packageId, meaoId, token, progressId).toString()).start();
        return ResponseEntity.ok(new ResponseObject("Uploading", errMsg, "Uploading package takes a long time."));
    }

    /**
     * publish package.
     */
    public ResponseEntity<String> publishPackage(String appId, String packageId, PublishAppReqDto publishAppReq) {
        packageService.publishPackage(appId, packageId, publishAppReq);
        return ResponseEntity.ok("Publish Success");
    }

    /**
     * publish package v2.
     */
    public ResponseEntity<ResponseObject> publishPackageV2(String appId, String packageId,
        PublishAppReqDto publishAppReq) {
        packageService.publishPackage(appId, packageId, publishAppReq);
        ErrorMessage errMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject("Publish Success", errMsg, "Publish Success."));
    }

    /**
     * modify app attributes.
     *
     * @param iconFile app icon.
     * @param demoVideo app demo video.
     * @param docFile app detail md file.
     * @param packageDto packageDto.
     * @param request HttpServletRequest.
     */
    public ResponseEntity<PackageDto> updateAppById(MultipartFile iconFile, MultipartFile demoVideo,
        MultipartFile docFile, PackageDto packageDto, HttpServletRequest request) {
        boolean isAdmin = false;
        String authorities = (String) request.getAttribute(Consts.AUTHORITIES);
        if (!StringUtils.isEmpty(authorities) && authorities.contains("ROLE_APPSTORE_ADMIN")) {
            isAdmin = true;
        }
        Release releasePermission = appService.getRelease(packageDto.getAppId(), packageDto.getPackageId());
        releasePermission.checkPermission(
            new User((String) request.getAttribute("userId"), (String) request.getAttribute("userName")), isAdmin,
            ResponseConst.RET_NO_ACCESS_MODIFY_PACKAGE);

        packageService.updateAppById(iconFile, demoVideo, docFile, packageDto);
        Release release = packageRepository.findReleaseById(packageDto.getAppId(), packageDto.getPackageId());
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
     * query all published packages.
     *
     */
    public Page<PackageDto> getPackages(int limit, int offset, String startTime, String endTime) {
        try {
            Date startDate = null;
            Date endDate = null;
            if (!StringUtils.isEmpty(startTime)) {
                startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startTime);
            }
            if (!StringUtils.isEmpty(endTime)) {
                endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endTime);
            }
            long total = packageService.countTotalForCreateTime(limit, offset, startDate, endDate);
            return new Page<>(
                packageService.getPackageByCreateTime(limit, offset, startDate, endDate).stream().map(PackageDto::of)
                    .collect(Collectors.toList()), limit, offset, total);
        } catch (ParseException e) {
            throw new AppException("The time parameter format is incorrect.", ResponseConst.RET_PARAM_INVALID);
        }
    }

    /**
     * query all the package owned by the user, and sorted by create time.
     *
     * @param userId user id
     * @return packages
     */
    public Page<PackageDto> getPackageByUserIdV2(String userId, QueryAppCtrlDto queryCtrl, String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("createTime", "createTime");
        params.put("sortItem", queryCtrl.getSortItem());
        params.put("userId", userId);
        params.put("limit", queryCtrl.getLimit());
        params.put("offset", queryCtrl.getOffset());
        params.put("appName", queryCtrl.getAppName());
        params.put("status", queryCtrl.getStatus());
        params.put("sortType", queryCtrl.getSortType());
        packageService.getPackageByUserIdV2(params).stream()
            .filter(s -> s.getTestTaskId() != null && EnumPackageStatus.needRefresh(s.getStatus())).forEach(
                s -> appService.loadTestTask(s.getAppId(), s.getPackageId(),
                    new AtpMetadata(s.getTestTaskId(), token)));
        long total = packageService.countTotalForUserId(params);
        return new Page<>(
            packageService.getPackageByUserIdV2(params).stream().map(PackageDto::of).collect(Collectors.toList()),
            queryCtrl.getLimit(), queryCtrl.getOffset(), total);
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

    /**
     * get expire time for pacakge.
     *
     * @param tempZip tempZip file.
     */
    public long getExpireTime(File tempZip) {
        long startTime = tempZip.lastModified();
        long endTime = new Date().getTime();
        return endTime - startTime;
    }

    /**
     * Search for the file containing the keyword in the specified directory.
     *
     * @param folder resource folder.
     * @param keyword keyword.
     */
    public static List<File> searchTempFiles(File folder, String keyword) {
        List<File> result = new ArrayList<>();
        if (folder.isFile()) {
            result.add(folder);
        }
        File[] subFolders = folder.listFiles(file -> file.isDirectory() || file.getName().startsWith(keyword));
        if (subFolders != null) {
            for (File file : subFolders) {
                if (file.isFile()) {
                    // add result list if  it is file
                    result.add(file);
                } else {
                    // If it is a folder, call this method recursively, and then add all files to the result list
                    result.addAll(searchTempFiles(file, keyword));
                }
            }
        }
        return result;
    }

    /**
     * schedule delete compressed temporary directory files.
     */
    public void scheduledDeletePackage() {
        LOGGER.info("Start schedule delete temp file path {}", packageDir);
        List<File> files = searchTempFiles(new File(packageDir), TEMP_EXPIRE_PREFIX);
        LOGGER.info("schedule find temp file count is {}", files.size());
        for (File tempFile : files) {
            long expireTime = getExpireTime(tempFile);
            if (expireTime >= CLEAN_ENV_WAIT_TIME) {
                LOGGER.info("Start schedule delete temp file is {}", tempFile);
                FileUtils.deleteQuietly(tempFile);
            }
        }
        LOGGER.info("End schedule delete temp file.");
    }

    /**
     * offShelf package.
     *
     * @param appId app id
     * @param packageId package id
     * @param user user info
     * @param isAdmin if admin role
     */
    public ResponseEntity<String> offShelfPackage(String appId, String packageId, User user, boolean isAdmin) {
        packageService.offShelfPackage(appId, packageId, user, isAdmin);
        return ResponseEntity.ok("OffShelf Success");
    }

}
