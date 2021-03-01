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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.edgegallery.appstore.application.external.atp.AtpService;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
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

@Service("AppRegisterService")
public class AppService {

    static final int TOO_MANY = 1024;

    static final int TOO_BIG = 104857600;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppService.class);

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
    public static void unzipApplicationPacakge(String localFilePath, String intendedDir) {

        try (ZipFile zipFile = new ZipFile(localFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int entriesCount = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entriesCount > TOO_MANY) {
                    throw new IllegalStateException("too many files to unzip");
                }
                entriesCount++;
                // sanitize file path
                String fileName = LocalFileService.sanitizeFileName(entry.getName(), intendedDir);
                if (!entry.isDirectory()) {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        if (inputStream.available() > TOO_BIG) {
                            throw new IllegalStateException("file being unzipped is too big");
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
            throw new AppException("Failed to unzip");
        }
    }

    /**
     * Returns list of image details.
     *
     * @param swImageDescr software image descriptor file content
     * @return list of image details
     */
    public static List<SwImgDesc> getSwImageDescrInfo(String swImageDescr) {

        List<SwImgDesc> swImgDescrs = new LinkedList<>();
        JsonArray swImgDescrArray = new JsonParser().parse(swImageDescr).getAsJsonArray();
        SwImgDesc swDescr;
        for (JsonElement descr : swImgDescrArray) {
            swDescr = new Gson().fromJson(descr.getAsJsonObject().toString(), SwImgDesc.class);
            swImgDescrs.add(swDescr);
        }
        LOGGER.info("sw image descriptors: {}", swImgDescrs);
        return swImgDescrs;
    }

    public Release getRelease(String appId, String packageId) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        return app.findByPackageId(packageId).orElseThrow(() -> new UnknownReleaseExecption(packageId));
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
        unzipApplicationPacakge(localFilePath, parentDir);

        File swImageDesc = getFileFromPackage(parentDir, "Image/SwImageDesc.json");
        if (swImageDesc == null) {
            return null;
        }

        try {
            FileUtils.forceDelete(new File(localFilePath));
        } catch (IOException ex) {
            LOGGER.debug("failed to delete csar package {}", ex.getMessage());
        }

        try {
            return getSwImageDescrInfo(FileUtils.readFileToString(swImageDesc, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("failed to get sw image descriptor file {}", e.getMessage());
            throw new AppException("failed to get sw image descriptor file");
        }
    }

    /**
     * Updates software image descriptor with docker repo info.
     *
     * @param swImageDescr software image descriptor file content
     */
    public void updateRepoInfoInSwImageDescr(File swImageDescr) {

        try {
            String descrStr = FileUtils.readFileToString(swImageDescr, StandardCharsets.UTF_8);
            JsonArray swImgDescrArray = new JsonParser().parse(descrStr).getAsJsonArray();

            for (JsonElement descr : swImgDescrArray) {
                JsonObject jsonObject = descr.getAsJsonObject();
                String swImage = jsonObject.get("swImage").getAsString();
                String[] image = swImage.split("/");

                if (image.length > 1) {
                    jsonObject.addProperty("swImage", appstoreRepoEndpoint + "/appstore/" + image[image.length - 1]);
                } else {
                    jsonObject.addProperty("swImage", appstoreRepoEndpoint + "/appstore/" + image[0]);
                }
            }
            FileUtils.writeStringToFile(swImageDescr, swImgDescrArray.toString(), StandardCharsets.UTF_8.name());
            LOGGER.info("Updated swImages : {}", swImgDescrArray);
        } catch (IOException e) {
            LOGGER.info("failed to update sw image descriptor");
            throw new AppException("Failed to update repo info to image descriptor file");
        }
    }

    /**
     * Update application package with apstore repo info.
     *
     * @param parentDir        parent Dir
     */
    public void updateAppPackageWithRepoInfo(String parentDir) {

        File swImageDesc = getFileFromPackage(parentDir, "Image/SwImageDesc.json");
        updateRepoInfoInSwImageDescr(swImageDesc);

        File chartsTar = getFileFromPackage(parentDir, "/Artifacts/Deployment/Charts/");
        if (chartsTar == null) {
            throw new AppException("failed to find values yaml");
        }

        try {
            deCompress(chartsTar.getCanonicalFile().toString(),
                    new File(chartsTar.getCanonicalFile().getParent()));

            FileUtils.forceDelete(chartsTar);
            File valuesYaml = getFileFromPackage(parentDir, "/values.yaml");
            if (valuesYaml == null) {
                throw new AppException("failed to find values yaml");
            }

            //update values.yaml
            Map<String, Object> values = loadvaluesYaml(valuesYaml);
            ImgLoc imageLocn = null;
            for (String key : values.keySet()) {
                if (key.equals("imagelocation")) {
                    ModelMapper mapper = new ModelMapper();
                    imageLocn = mapper.map(values.get("imagelocation"), ImgLoc.class);
                    imageLocn.setDomainame(appstoreRepoEndpoint);
                    imageLocn.setProject("appstore");
                    break;
                }
            }
            if (imageLocn != null) {
                values.put("imagelocation", imageLocn);
            } else {
                LOGGER.error("missing image location parameters ");
                throw new AppException("failed to update values yaml, missing image location parameters");
            }
            String json = new Gson().toJson(values);
            FileUtils.writeStringToFile(valuesYaml, json, StandardCharsets.UTF_8.name());
            LOGGER.info("imageLocation updated in values yaml {}", json);

            compress(valuesYaml.getParent());

            FileUtils.deleteDirectory(new File(valuesYaml.getParent()));
        } catch (IOException e) {
            throw new AppException("failed to find charts directory");
        }
    }

    /**
     * Update helm chart values.
     *
     * @param valuesYaml       values file
     */
    private Map<String, Object> loadvaluesYaml(File valuesYaml) {

        Map<String, Object> valuesYamlMap;
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(valuesYaml)) {
            valuesYamlMap = yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new AppException("failed to load value yaml form charts");
        } catch (IOException e) {
            throw new AppException("failed to load value yaml form charts");
        }
        return valuesYamlMap;
    }

    /**
     * ZIP application package.
     *
     * @param intendedDir application package ID
     */
    public String compressAppPackage(String intendedDir) {
        final Path srcDir = Paths.get(intendedDir);
        String zipFileName = intendedDir.concat(".csar");
        String[] fileName = zipFileName.split("/");
        String fileStorageAdd = srcDir + "/" + fileName[fileName.length - 1];
        try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = srcDir.relativize(file);
                        os.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        os.write(bytes, 0, bytes.length);
                        os.closeEntry();
                    } catch (IOException e) {
                        throw new AppException("failed to zip application package");
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AppException("failed to zip application package");
        }
        try {
            FileUtils.deleteDirectory(new File(intendedDir));
            FileUtils.moveFileToDirectory(new File(zipFileName), new File(intendedDir), true);
        } catch (IOException e) {
            throw new AppException("failed to zip application package");
        }
        return fileStorageAdd;
    }

    /**
     * update image in appstore repo.
     *
     * @param imageInfoList image list
     */
    public void updateImgInRepo(List<SwImgDesc> imageInfoList) {
        Set<String> downloadedImgs = null;
        Set<String> uploadedImgs = null;

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
                //.withDockerTlsVerify(true)
                //.withDockerCertPath("/usr/app/ssl")
                .withRegistryUrl("http://" + repo)
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
                throw new AppException("failed to download image");
            } catch (NotFoundException e) {
                LOGGER.error("failed to download image {}, image not found in repository, {}", imageInfo.getSwImage(),
                        e.getMessage());
                throw new AppException("failed to push image to edge repo");
            } catch (InternalServerErrorException e) {
                LOGGER.error("internal server error while downloading image {},{}", imageInfo.getSwImage(),
                        e.getMessage());
                throw new AppException("failed to push image to edge repo");
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
                        .append("/appstore/").append(dockerImageNames[dockerImageNames.length - 1]).toString();
            } else {
                uploadImgName = new StringBuilder(appstoreRepoEndpoint)
                        .append("/appstore/").append(dockerImageNames[0]).toString();
            }

            String id = dockerClient.inspectImageCmd(imageInfo.getSwImage()).exec().getId();
            dockerClient.tagImageCmd(id, uploadImgName, imageInfo.getVersion()).withForce().exec();

            LOGGER.info("Upload tagged docker image: {}", uploadImgName);
            try {
                dockerClient.pushImageCmd(uploadImgName)
                        .exec(new PushImageResultCallback()).awaitCompletion();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AppException("failed to download image");
            } catch (NotFoundException e) {
                LOGGER.error("failed to download image {}, image not found in repository, {}", uploadImgName,
                        e.getMessage());
                throw new AppException("failed to push image to edge repo");
            } catch (InternalServerErrorException e) {
                LOGGER.error("internal server error while downloading image {},{}", uploadImgName, e.getMessage());
                throw new AppException("failed to push image to edge repo");
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
                if (tarEntry.isDirectory()) {
                    continue;
                } else {
                    File outputFile = new File(destFile + File.separator + tarEntry.getName());
                    LOGGER.info("deCompressing... {}", outputFile.getName());
                    boolean result = outputFile.getParentFile().mkdirs();
                    LOGGER.debug("create directory result {}", result);
                    IOUtils.copy(tis, new FileOutputStream(outputFile));
                }
            }
        } catch (IOException ex) {
            throw new AppException("failed to decompress, IO exception " + ex.getMessage());
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

    private void compress(String sourceDir) {
        if (sourceDir == null || sourceDir.isEmpty()) {
            return;
        }

        File destination = new File(sourceDir);
        try (FileOutputStream destOutStream = new FileOutputStream(destination.getCanonicalPath().concat(".tgz"));
                GZIPOutputStream gipOutStream = new GZIPOutputStream(new BufferedOutputStream(destOutStream));
                TarArchiveOutputStream outStream = new TarArchiveOutputStream(gipOutStream)) {

            addFileToTar(sourceDir, "", outStream);

        } catch (IOException e) {
            throw new AppException("failed to compress " + e.getMessage());
        }
    }

    private void addFileToTar(String filePath, String parent, TarArchiveOutputStream tarArchive) {

        File file = new File(filePath);
        LOGGER.info("compressing... {}", file.getName());

        String entry = parent + file.getName();
        try {
            tarArchive.putArchiveEntry(new TarArchiveEntry(file, entry));
            if (file.isFile()) {
                FileInputStream inputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                IOUtils.copy(bufferedInputStream, tarArchive);
                tarArchive.closeArchiveEntry();
                bufferedInputStream.close();
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
            throw new AppException("failed to compress " + e.getMessage());
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
            throw new AppException(file + e.getMessage());
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
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findByPackageId(packageId).orElseThrow(() -> new UnknownReleaseExecption(packageId));
        release.checkPermission(user.getUserId());

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
     * @return
     */
    public Release download(String appId, String packageId) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Release release = app.findByPackageId(packageId).orElseThrow(() -> new UnknownReleaseExecption(packageId));
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
