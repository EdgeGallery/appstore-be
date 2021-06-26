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

package org.edgegallery.appstore.application.inner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.edgegallery.appstore.application.external.atp.AtpService;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.domain.model.app.ImgLoc;
import org.edgegallery.appstore.domain.model.app.SwImgDesc;
import org.edgegallery.appstore.domain.model.comment.CommentRepository;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.releases.UnknownReleaseExecption;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Service("AppRegisterService")
public class AppService {

    static final int TOO_MANY = 1024;

    static final int TOO_BIG = 536870912;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppService.class);

    private static final String SWIMAGE = "swImage";

    private static final String APPSTORE_URL = "/appstore/";

    private static final String IMAGE_LOCATION = "imagelocation";

    private static final String PULL_IMAGE_ERR_MESSAGES = "failed to pull image to edge repo";

    private static final String PUSH_IMAGE_ERR_MESSAGES = "failed to push image to edge repo";

    @Value("${appstore-be.package-path}")
    private String dir;

    @Value("${appstore-be.appstore-repo-password:}")
    private String appstoreRepoPassword;

    @Value("${appstore-be.appstore-repo-username:}")
    private String appstoreRepoUsername;

    @Value("${appstore-be.appstore-repo-endpoint:}")
    private String appstoreRepoEndpoint;

    @Value("${appstore-be.dev-repo-username:}")
    private String devRepoUsername;

    @Value("${appstore-be.dev-repo-password:}")
    private String devRepoPassword;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private AtpService atpService;

    /**
     * Returns software image descriptor content in string format.
     *
     * @param localFilePath CSAR file path
     * @param intendedDir   intended directory
     */
    public static void unzipApplicationPackage(String localFilePath, String intendedDir) {

        try (ZipFile zipFile = new ZipFile(localFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int entriesCount = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entriesCount > TOO_MANY) {
                    throw new AppException("too many files to unzip", ResponseConst.RET_UNZIP_TOO_MANY_FILES, TOO_MANY);
                }
                entriesCount++;
                // sanitize file path
                String fileName = LocalFileService.sanitizeFileName(entry.getName(), intendedDir);
                if (!entry.isDirectory()) {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        if (inputStream.available() > TOO_BIG) {
                            throw new AppException("file being unzipped is too big",
                                ResponseConst.RET_FILE_TOO_BIG, TOO_BIG);
                        }
                        FileUtils.copyInputStreamToFile(inputStream, new File(fileName));
                        LOGGER.info("unzip package... {}", entry.getName());
                    }
                } else {

                    File dir = new File(fileName);
                    boolean dirStatus = dir.mkdirs();
                    LOGGER.debug("creating dir {}, status {}", fileName, dirStatus);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to unzip");
            throw new AppException("Failed to unzip", ResponseConst.RET_DECOMPRESS_FAILED);
        }
    }

    /**
     * Returns list of image details.
     *
     * @param swImageDesc software image descriptor file content
     * @return list of image details
     */
    public static List<SwImgDesc> getSwImageDescInfo(String swImageDesc) {

        List<SwImgDesc> swImgDesc = new LinkedList<>();
        JsonArray swImgDescArray = new JsonParser().parse(swImageDesc).getAsJsonArray();
        SwImgDesc swDesc;
        for (JsonElement desc : swImgDescArray) {
            swDesc = new Gson().fromJson(desc.getAsJsonObject().toString(), SwImgDesc.class);
            swImgDesc.add(swDesc);
        }
        LOGGER.info("sw image descriptors: {}", swImgDesc);
        return swImgDesc;
    }

    /**
     * get release.
     *
     */
    public Release getRelease(String appId, String packageId) {
        App app = appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND));
        return app.findByPackageId(packageId)
            .orElseThrow(() -> new UnknownReleaseExecption(packageId, ResponseConst.RET_PACKAGE_NOT_FOUND));
    }

    /**
     * register app.
     *
     * @param release use object of release to register.
     */
    @Transactional
    public RegisterRespDto registerApp(Release release) {

        Optional<App> existedApp = appRepository
                .findByAppNameAndProvider(release.getAppBasicInfo().getAppName(),
                        release.getAppBasicInfo().getProvider());
        App app;
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
        packageRepository.storeRelease(release);
        return RegisterRespDto.builder().appName(release.getAppBasicInfo().getAppName()).appId(app.getAppId())
                .packageId(release.getPackageId()).provider(app.getProvider())
                .version(release.getAppBasicInfo().getVersion()).build();
    }

    /**
     * Returns list of image info.
     *
     * @param localFilePath csar file path
     * @param parentDir     parent dir
     * @return list of image info
     */
    public List<SwImgDesc> getAppImageInfo(String localFilePath, String parentDir) {
        unzipApplicationPackage(localFilePath, parentDir);

        File swImageDesc = getFileFromPackage(parentDir, "Image/SwImageDesc.json");
        if (swImageDesc == null) {
            return Collections.emptyList();
        }

        try {
            return getSwImageDescInfo(FileUtils.readFileToString(swImageDesc, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("failed to get sw image descriptor file {}", e.getMessage());
            throw new AppException("failed to get sw image descriptor file", ResponseConst.RET_GET_IMAGE_DESC_FAILED);
        }
    }

    /**
     * Updates software image descriptor with docker repo info.
     *
     * @param swImageDesc software image descriptor file content
     */
    public void updateRepoInfoInSwImageDesc(File swImageDesc) {

        try {
            String descStr = FileUtils.readFileToString(swImageDesc, StandardCharsets.UTF_8);
            JsonArray swImgDescArray = new JsonParser().parse(descStr).getAsJsonArray();

            for (JsonElement desc : swImgDescArray) {
                JsonObject jsonObject = desc.getAsJsonObject();
                String swImage = jsonObject.get(SWIMAGE).getAsString();
                String[] image = swImage.split("/");

                if (image.length > 1) {
                    jsonObject.addProperty(SWIMAGE, appstoreRepoEndpoint + APPSTORE_URL + image[image.length - 1]);
                } else {
                    jsonObject.addProperty(SWIMAGE, appstoreRepoEndpoint + APPSTORE_URL + image[0]);
                }
            }
            FileUtils.writeStringToFile(swImageDesc, swImgDescArray.toString(), StandardCharsets.UTF_8.name());
            LOGGER.info("Updated swImages : {}", swImgDescArray);
        } catch (IOException e) {
            LOGGER.info("failed to update sw image descriptor");
            throw new AppException("Failed to update repo info to image descriptor file",
                ResponseConst.RET_UPDATE_IMAGE_FAILED);
        }
    }

    /**
     * Update application package with appstore repo info.
     *
     * @param parentDir        parent Dir
     */
    public void updateAppPackageWithRepoInfo(String parentDir) {

        File swImageDesc = getFileFromPackage(parentDir, "Image/SwImageDesc.json");
        updateRepoInfoInSwImageDesc(swImageDesc);
        String unZipPath = dir + File.separator + UUID.randomUUID().toString().replace("-", "");

        File chartsTar = getFileFromPackage(parentDir, "/Artifacts/Deployment/Charts/");
        if (chartsTar == null) {
            throw new AppException("failed to find values yaml", ResponseConst.RET_FILE_NOT_FOUND,
                "/Artifacts/Deployment/Charts/");
        }

        try {
            File unZipPathDir = new File(unZipPath);
            if (!unZipPathDir.mkdirs()) {
                LOGGER.info("unZip path has already existed.");
            }

            String chartsTarStr = chartsTar.getCanonicalFile().toString();
            deCompress(chartsTarStr, new File(unZipPath));

            FileUtils.forceDelete(chartsTar);
            File valuesYaml = getFileFromPackage(unZipPath, "/values.yaml");
            if (valuesYaml == null) {
                throw new AppException("failed to find values yaml", ResponseConst.RET_FILE_NOT_FOUND, "/values.yaml");
            }

            //update values.yaml
            Map<String, Object> values = loadvaluesYaml(valuesYaml);
            ImgLoc imageLoc = null;
            for (String key : values.keySet()) {
                if (key.equals(IMAGE_LOCATION)) {
                    ModelMapper mapper = new ModelMapper();
                    imageLoc = mapper.map(values.get(IMAGE_LOCATION), ImgLoc.class);
                    LOGGER.info("image location domain{}, project {}", imageLoc.getDomainname(), imageLoc.getProject());
                    break;
                }
            }
            if (imageLoc != null) {
                FileUtils.writeStringToFile(valuesYaml, FileUtils.readFileToString(valuesYaml, StandardCharsets.UTF_8)
                    .replace(imageLoc.getDomainname(), appstoreRepoEndpoint)
                    .replace(imageLoc.getProject(), "appstore"), StandardCharsets.UTF_8, false);
            } else {
                LOGGER.error("missing image location parameters ");
                throw new AppException("failed to update values yaml, missing image location parameters",
                    ResponseConst.RET_MISS_IMAGE_LOCATION);
            }

            compress(valuesYaml.getParent(), chartsTarStr);
            FileUtils.deleteDirectory(unZipPathDir);
            if (unZipPathDir.exists()) {
                LOGGER.info("delete charts temp folder failed, {}", valuesYaml.getParent());
            }
        } catch (IOException e) {
            LOGGER.info("Delete temporary unzip directory failed {}", e.getMessage());
        }
    }

    /**
     * Update helm chart values.
     *
     * @param valuesYaml       values file
     */
    private Map<String, Object> loadvaluesYaml(File valuesYaml) {

        Map<String, Object> valuesYamlMap;
        Yaml yaml = new Yaml(new SafeConstructor());
        try (InputStream inputStream = new FileInputStream(valuesYaml)) {
            valuesYamlMap = yaml.load(inputStream);
        } catch (IOException e) {
            throw new AppException("failed to load value yaml form charts", ResponseConst.RET_LOAD_YAML_FAILED);
        }
        return valuesYamlMap;
    }

    /**
     * update image in appstore repo.
     *
     * @param imageInfoList image list
     */
    public void updateImgInRepo(List<SwImgDesc> imageInfoList) {
        if (imageInfoList != null) {
            downloadAppImage(imageInfoList);
            uploadAppImage(imageInfoList);
        }
    }

    /**
     * docker client instance.
     *
     * @param repo image repo
     * @param userName repo user name
     * @param password repo password
     */
    private DockerClient getDockerClient(String repo, String userName, String password) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerTlsVerify(true)
                .withDockerCertPath("/usr/app/ssl")
                .withRegistryUrl("https://" + repo)
                .withRegistryUsername(userName)
                .withRegistryPassword(password)
                .build();

        return DockerClientBuilder.getInstance(config).build();
    }

    /**
     * Downloads app image from repo.
     *
     * @param imageInfoList list of images
     */
    public void downloadAppImage(List<SwImgDesc> imageInfoList) {

        String[] sourceRepoHost;
        for (SwImgDesc imageInfo : imageInfoList) {
            LOGGER.info("Download docker image {} ", imageInfo.getSwImage());

            sourceRepoHost = imageInfo.getSwImage().split("/");
            DockerClient dockerClient = getDockerClient(sourceRepoHost[0], devRepoUsername,
                    devRepoPassword);

            try {
                dockerClient.pullImageCmd(imageInfo.getSwImage())
                        .exec(new PullImageResultCallback()).awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AppException(PULL_IMAGE_ERR_MESSAGES, ResponseConst.RET_PULL_IMAGE_FAILED,
                    imageInfo.getSwImage());
            } catch (Exception e) {
                LOGGER.error("failed to download image {}, image not found in repository, {}", imageInfo.getSwImage(),
                        e.getMessage());
                throw new AppException(PULL_IMAGE_ERR_MESSAGES, ResponseConst.RET_PULL_IMAGE_FAILED,
                    imageInfo.getSwImage());
            }
        }

        LOGGER.info("images to edge repo downloaded successfully");
    }

    /**
     * Uploads app image from repo.
     *
     * @param imageInfoList list of images
     */
    public void uploadAppImage(List<SwImgDesc> imageInfoList) {

        for (SwImgDesc imageInfo : imageInfoList) {
            LOGGER.info("Docker image to  upload: {}", imageInfo.getSwImage());

            DockerClient dockerClient = getDockerClient(appstoreRepoEndpoint, appstoreRepoUsername,
                    appstoreRepoPassword);

            String[] dockerImageNames = imageInfo.getSwImage().split("/");
            String uploadImgName;
            if (dockerImageNames.length > 1) {
                uploadImgName = new StringBuilder(appstoreRepoEndpoint)
                        .append(APPSTORE_URL).append(dockerImageNames[dockerImageNames.length - 1]).toString();
            } else {
                uploadImgName = new StringBuilder(appstoreRepoEndpoint)
                        .append(APPSTORE_URL).append(dockerImageNames[0]).toString();
            }

            try {
                LOGGER.info("Upload tagged docker image: {}", uploadImgName);
                String id = dockerClient.inspectImageCmd(imageInfo.getSwImage()).exec().getId();
                dockerClient.tagImageCmd(id, uploadImgName, imageInfo.getVersion()).withForce().exec();
                dockerClient.pushImageCmd(uploadImgName)
                        .exec(new PushImageResultCallback()).awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AppException(PUSH_IMAGE_ERR_MESSAGES, ResponseConst.RET_PUSH_IMAGE_FAILED, uploadImgName);
            } catch (Exception e) {
                LOGGER.error("failed to push image {}, errormsg: {}", uploadImgName, e.getMessage());
                throw new AppException(PUSH_IMAGE_ERR_MESSAGES, ResponseConst.RET_PUSH_IMAGE_FAILED, uploadImgName);
            }
        }
        LOGGER.info("images to appstore repo uploaded successfully");
    }

    /**
     * Decompress tar file.
     *
     * @param tarFile  tar file
     * @param destFile destination folder
     */
    private void deCompress(String tarFile, File destFile) {
        TarArchiveInputStream tis = null;
        try (FileInputStream fis = new FileInputStream(tarFile)) {

            if (tarFile.contains(".tar")) {
                tis = new TarArchiveInputStream(new BufferedInputStream(fis));
            } else {
                GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fis));
                tis = new TarArchiveInputStream(gzipInputStream);
            }

            TarArchiveEntry tarEntry;
            while ((tarEntry = tis.getNextTarEntry()) != null) {
                if (!tarEntry.isDirectory()) {
                    File outputFile = new File(destFile + File.separator + tarEntry.getName());
                    LOGGER.info("deCompressing... {}", outputFile.getName());
                    boolean result = outputFile.getParentFile().mkdirs();
                    LOGGER.debug("create directory result {}", result);
                    IOUtils.copy(tis, new FileOutputStream(outputFile));
                }
            }
        } catch (IOException ex) {
            throw new AppException("failed to decompress, IO exception " + ex.getMessage(),
                ResponseConst.RET_DECOMPRESS_FAILED);
        } finally {
            if (tis != null) {
                try {
                    tis.close();
                } catch (IOException ex) {
                    LOGGER.error("failed to close tar input stream {} ", ex.getMessage());
                }
            }
        }
    }

    private void compress(String sourceDir, String destPath) {
        if (sourceDir == null || sourceDir.isEmpty()) {
            return;
        }

        File destination = new File(destPath);
        try (FileOutputStream destOutStream = new FileOutputStream(destination.getCanonicalPath());
                GZIPOutputStream gipOutStream = new GZIPOutputStream(new BufferedOutputStream(destOutStream));
                TarArchiveOutputStream outStream = new TarArchiveOutputStream(gipOutStream)) {

            addFileToTar(sourceDir, "", outStream);

        } catch (IOException e) {
            throw new AppException("failed to compress " + e.getMessage(), ResponseConst.RET_COMPRESS_FAILED);
        }
    }

    private void addFileToTar(String filePath, String parent, TarArchiveOutputStream tarArchive) throws IOException {

        File file = new File(filePath);
        LOGGER.info("compressing... {}", file.getName());
        String entry = parent + file.getName();
        try {
            tarArchive.putArchiveEntry(new TarArchiveEntry(file, entry));
            if (file.isFile()) {
                try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                    IOUtils.copy(bufferedInputStream, tarArchive);
                    tarArchive.closeArchiveEntry();
                }
            } else if (file.isDirectory()) {
                tarArchive.closeArchiveEntry();
                File[] files = file.listFiles();
                if (files != null) {
                    for (File f : files) {
                        addFileToTar(f.getAbsolutePath(), entry + File.separator, tarArchive);
                    }
                }
            }
        } catch (IOException e) {
            throw new AppException("failed to compress " + e.getMessage(), ResponseConst.RET_COMPRESS_FAILED);
        }
    }

    /**
     * Returns file from the package.
     *
     * @param parentDir parent Dir
     * @param file      file to search
     * @return file,
     */
    public File getFileFromPackage(String parentDir, String file) {

        List<File> files = (List<File>) FileUtils.listFiles(new File(parentDir), null, true);
        try {
            for (File fileEntry : files) {
                if (fileEntry.getCanonicalPath().contains(file)) {
                    return fileEntry;
                }
            }
        } catch (IOException e) {
            throw new AppException(file + e.getMessage(), ResponseConst.RET_PARSE_FILE_EXCEPTION, file);
        }
        return null;
    }


    /**
     * delete package by app id and package id.
     *
     * @param appId     app id
     * @param packageId package id
     * @param user      obj of User
     */
    @Transactional
    public void unPublishPackage(String appId, String packageId, User user) {
        App app = appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND));
        Release release = app.findByPackageId(packageId)
            .orElseThrow(() -> new UnknownReleaseExecption(packageId, ResponseConst.RET_PACKAGE_NOT_FOUND));
        release.checkPermission(user);

        app.unPublish(release);
        if (app.getReleases().isEmpty()) {
            unPublish(app);
        } else {
            packageRepository.removeRelease(release);
            if (!app.hasPublishedRelease()) {
                app.setStatus(EnumAppStatus.UnPublish);
                appRepository.store(app);
            }
        }

        deleteReleaseFile(release);
    }

    /**
     * download package by app id and package id.
     *
     * @param appId     app id.
     * @param packageId package id.
     * @return release
     */
    public Release download(String appId, String packageId) {
        App app = appRepository.find(appId)
            .orElseThrow(() -> new EntityNotFoundException(App.class, appId, ResponseConst.RET_APP_NOT_FOUND));
        Release release = app.findByPackageId(packageId)
            .orElseThrow(() -> new UnknownReleaseExecption(packageId, ResponseConst.RET_PACKAGE_NOT_FOUND));
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

    /**
     * load test task status from atp.
     *
     * @param packageId   package id
     * @param atpMetadata atp data
     */
    public void loadTestTask(String appId, String packageId, AtpMetadata atpMetadata) {
        String status = atpService.getAtpTaskResult(atpMetadata.getToken(), atpMetadata.getTestTaskId());
        if (status != null) {
            Release release = packageRepository.findReleaseById(appId, packageId);
            release.setStatus(EnumPackageStatus.fromString(status));
            release.setTestTaskId(atpMetadata.getTestTaskId());
            packageRepository.updateRelease(release);
        }
    }
}
