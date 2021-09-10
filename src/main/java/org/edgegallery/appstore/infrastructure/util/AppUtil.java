/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.infrastructure.util;

import com.google.common.io.Files;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.SwImgDesc;
import org.edgegallery.appstore.domain.model.appd.AppdFileHandlerFactory;
import org.edgegallery.appstore.domain.model.appd.IAppdFile;
import org.edgegallery.appstore.domain.model.appd.context.ManifestFiledataContent;
import org.edgegallery.appstore.domain.model.appd.context.ToscaSourceContent;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.BasicInfo;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("AppUtil")
public class AppUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(AppUtil.class);

    private static final String ZIP_PACKAGE_ERR_MESSAGES = "failed to zip application package";

    private static final String DOWNLOAD_IMAGE_FAIL = "failed download image from file system";

    private static final String ZIP_EXTENSION = ".zip";

    private static final String CSAR_EXTENSION = ".csar";

    private static final String VIDIO_EXTENSION = ".mp4";

    private static final String PNG_EXTENSION = ".png";

    private static final String JSON_EXTENSION = "Image/SwImageDesc.json";

    private static final String CONTAINER = "container";

    private static final String COLON = ":";

    private static final String IMAGE = "Image";

    /**
     * The maximum size of a package sent over the network.
     */
    private static final int MAX_NET_FILE_SIZE = 8192;

    /**
     * Set whether to save the uploaded file as a temporary file in the data value of the file.
     */
    private static final int FILE_TEMPORARY_VALUE = 16;

    private static final String DOWNLOAD_IMAGE_TAG = "/action/download";

    private static final String DOWNLOAD_ZIP_IMAGE = "?isZip=true";

    @Value("${appstore-be.encrypted-key-path:}")
    private String keyPath;

    @Value("${appstore-be.key-password:}")
    private String keyPwd;

    @Value("${appstore-be.filesystem-address:}")
    private String fileSystemAddress;

    @Autowired
    private AppService appService;

    @Autowired
    private UploadFileUtil uploadFileUtil;

    /**
     * get app_class.
     *
     * @param filePath filePath
     * @return appClass
     */
    public String getAppClass(String filePath) {
        try (ZipFile zipFile = new ZipFile(filePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().split("/").length == 1 && entry.getName().endsWith(".mf")) {
                    try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            // prefix: path
                            if (line.trim().startsWith("app_class")) {
                                return line.split(":")[1].trim();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new AppException("failed to get app class.", ResponseConst.RET_GET_APP_CLASS_FAILED);
        }
        return null;
    }

    /**
     * get file name by release.
     *
     * @param release app package
     * @param file original file
     * @return file name
     */
    public String getFileName(Release release, AFile file) {
        StringBuilder fileName = new StringBuilder().append(release.getAppBasicInfo().getAppName());
        fileName.append(".").append(Files.getFileExtension(file.getOriginalFileName().toLowerCase()));
        return fileName.toString();
    }

    /**
     * get image status by imageUrl from fileSystem.
     *
     * @param url download url
     * @param token token
     * @return boolean
     */
    public boolean isImageExist(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("access_token", token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        LOGGER.info("get images status from fileSystem, url: {}", url);
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            LOGGER.info("get image from file system status: {}", response.getStatusCode());
            return HttpStatus.OK.equals(response.getStatusCode());
        } catch (RestClientException  e) {
            LOGGER.error("get image from file system exception, Url is {}, exception {}", url, e.getMessage());
            throw new AppException("get image from file system exception.", ResponseConst.RET_IMAGE_NOT_EXIST, url);
        }
    }

    /**
     * download image by imageUrl from fileSystem.
     *
     * @param url image url
     * @param token token
     * @return boolean
     */
    public byte[] downloadImageFromFileSystem(String token, String url) {
        LOGGER.info("download images status from fileSystem, url: {}", url);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(600000);// 设置超时
        requestFactory.setReadTimeout(600000);
        RestTemplate restObject = new RestTemplate(requestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setConnection("close");
        headers.set("access_token", token);
        byte[] result = null;
        ResponseEntity<byte[]> response;
        try {
            response = restObject.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("download file error, response is {}", response.getBody());
                throw new AppException(DOWNLOAD_IMAGE_FAIL, ResponseConst.RET_DOWNLOAD_IMAGE_FAILED, url);
            }
            result = response.getBody();
            if (result == null) {
                LOGGER.error("download file error, response is null");
                throw new AppException(DOWNLOAD_IMAGE_FAIL, ResponseConst.RET_DOWNLOAD_IMAGE_FAILED, url);
            }

        } catch (RestClientException e) {
            LOGGER.error("Failed to get image status which imageId exception {}", e.getMessage());
            throw new AppException(DOWNLOAD_IMAGE_FAIL, ResponseConst.RET_DOWNLOAD_IMAGE_FAILED, url);
        }

        return result;
    }

    /**
     * transfer file format to multipartFile.
     *
     * @param filePath filePath.
     * @return
     */
    public static FileItem createFileItem(String filePath) {
        File file = new File(filePath);
        String originalFilename = file.getName();
        FileItemFactory factory = new DiskFileItemFactory(FILE_TEMPORARY_VALUE, null);
        String textFieldName = "textField";
        FileItem item = factory.createItem(textFieldName, "text/plain", true, originalFilename);
        File newfile = new File(filePath);
        int bytesRead = 0;
        byte[] buffer = new byte[MAX_NET_FILE_SIZE];
        try {
            FileInputStream fis = new FileInputStream(newfile);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, MAX_NET_FILE_SIZE)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            throw new AppException("Package File name is Illegal.", ResponseConst.RET_FILE_NOT_FOUND);
        }
        return item;
    }

    /**
     * load file and analyse file list.
     */
    public void checkImage(AtpMetadata atpMetadata, String fileParent, String appClass, String userId) {
        File file = new File(fileParent);
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (File fl : files) {
                if (fl.isDirectory() && fl.getName().equals(IMAGE)) {
                    File[] filezipArrays = fl.listFiles();
                    if (filezipArrays == null || filezipArrays.length == 0) {
                        throw new AppException("there is no file in path /Image", ResponseConst.RET_FILE_NOT_FOUND,
                            "/Image");
                    }
                    checkImageExist(atpMetadata, fileParent, filezipArrays, userId, fl);
                }
            }
        }
    }

    private void checkImageExist(AtpMetadata atpMetadata, String fileParent, File[] filezipArrays, String userId,
        File imageFolder) {
        boolean presentZip = Arrays.asList(filezipArrays).stream()
            .anyMatch(m1 -> m1.toString().contains(ZIP_EXTENSION));
        if (!presentZip) {
            List<SwImgDesc> imgDecsList = getPkgFile(fileParent);
            for (SwImgDesc imageDesc : imgDecsList) {
                String pathUrl = imageDesc.getSwImage();
                pathUrl = pathUrl.substring(0, pathUrl.lastIndexOf(DOWNLOAD_IMAGE_TAG));
                try {
                    if (!isImageExist(pathUrl, atpMetadata.getToken())) {
                        FileUtils.deleteDirectory(new File(fileParent));
                    }
                } catch (IOException e) {
                    LOGGER.error("delete file error {}", e.getMessage());
                    throw new AppException("the image of this application does not exist.",
                        ResponseConst.RET_IMAGE_NOT_EXIST, pathUrl);
                }
            }
        } else {
            try {
                uploadFileToFileServer(userId, fileParent, imageFolder);
                organizedFile(fileParent);
            } catch (IOException e) {
                LOGGER.error("failed to add image zip to fileServer {} ", e.getMessage());
                throw new AppException("failed to add image zip to package.",
                    ResponseConst.RET_IMAGE_TO_FILE_SERVER_FAILED);
            }
        }

    }

    /**
     * delete temp file and folder.
     *
     * @param fileParent fileParent.
     */
    public void deleteTempFolder(String fileParent) {
        File file = new File(fileParent);
        String parent = file.getParent();
        File parentDir = new File(parent);
        File[] files = parentDir.listFiles();
        if (files != null && files.length > 0) {
            for (File tempFile : files) {
                if (tempFile.getName().endsWith(CSAR_EXTENSION) || tempFile.getName().endsWith(PNG_EXTENSION)
                    || tempFile.getName().endsWith(VIDIO_EXTENSION)) {
                    continue;
                }
                FileUtils.deleteQuietly(tempFile);

            }
        }
    }

    /**
     * clean up temp file.
     *
     * @param fileParent fileParent.
     */
    public void organizedFile(String fileParent) {
        String zipFileName = fileParent.concat(CSAR_EXTENSION);
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            createCompressedFile(out, new File(fileParent), "");
        } catch (IOException e) {
            throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        deleteTempFolder(fileParent);

    }

    /**
     * upload file to FileServer.
     *
     * @param userId userId.
     * @param fileParent fileParent.
     * @param imageFolder imageFolder.
     */
    public void uploadFileToFileServer(String userId, String fileParent, File imageFolder) throws IOException {
        String imageId = "";
        String imagePath = "";
        String outPath = imageFolder.getCanonicalPath();
        List<SwImgDesc> imgDecsLists = getPkgFile(outPath);
        for (SwImgDesc imageDesc : imgDecsLists) {
            try {
                String imageName = imageDesc.getName();
                //get image name
                File fileImage = new File(outPath + File.separator + imageName + ZIP_EXTENSION);
                imagePath = fileImage.getCanonicalPath();
                //upload image file
                imageId = uploadFileUtil.uploadFile(userId, imagePath);
                if (StringUtils.isEmpty(imageId)) {
                    LOGGER.error("upload to remote file server failed.");
                    throw new AppException("upload to remote file server failed.",
                        ResponseConst.RET_UPLOAD_FILE_FAILED);
                }
                //update swImageJson file
                String newPathName = fileSystemAddress + "/image-management/v1/images/" + imageId + "/action/download";
                updateJsonFileServer(imageDesc, imgDecsLists, fileParent, newPathName);
                updateRelationalFile(fileParent, imageName);
            } catch (IOException e) {
                LOGGER.error("failed to add image zip to fileServer {} ", e.getMessage());
                throw new AppException("failed to add image zip to package.",
                    ResponseConst.RET_IMAGE_TO_FILE_SERVER_FAILED);
            }
        }
    }

    /**
     * update mf and tosca config file.
     *
     * @param fileParent fileParent folder.
     * @param imageName image Name.
     */
    public void updateRelationalFile(String fileParent, String imageName) throws IOException {
        String target = "Image" + File.separator + imageName + ZIP_EXTENSION;
        File mfFile = getFile(fileParent, "mf");
        IAppdFile fileHandlerMf = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.MF_FILE);
        fileHandlerMf.load(mfFile);
        fileHandlerMf.delContentByTypeAndValue(ManifestFiledataContent.Source, target);
        writeFile(mfFile, fileHandlerMf.toString());
        String toscaMeta = fileParent + "/TOSCA-Metadata/TOSCA.meta";
        File metaFile = new File(toscaMeta);
        IAppdFile fileHandlerTosca = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.TOSCA_META_FILE);
        fileHandlerTosca.load(metaFile);
        fileHandlerTosca.delContentByTypeAndValue(ToscaSourceContent.Name, target);
        writeFile(metaFile, fileHandlerTosca.toString());

    }


    private List<SwImgDesc> getPkgFile(String parentDir) {
        File swImageDesc = appService.getFileFromPackage(parentDir, "SwImageDesc.json");
        if (swImageDesc == null) {
            return Collections.emptyList();
        }
        try {
            return AppService.getSwImageDescInfo(FileUtils.readFileToString(swImageDesc, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error("failed to get sw image descriptor file {}", e.getMessage());
            throw new AppException("failed to get sw image descriptor file", ResponseConst.RET_GET_IMAGE_DESC_FAILED);
        }

    }

    /**
     * update json file.
     *
     * @param imageDesc imageDesc.
     * @param imgDecsLists imgDecsLists.
     * @param fileParent fileParent.
     * @param imgPath imgZipPath.
     */
    public void updateJsonFile(SwImgDesc imageDesc, List<SwImgDesc> imgDecsLists, String fileParent, String imgPath) {
        int index = imgDecsLists.indexOf(imageDesc);
        String newPathName = "";
        try (ZipFile zipFile = new ZipFile(imgPath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                newPathName = "Image/" + imgPath.substring(imgPath.lastIndexOf(File.separator) + 1)
                    + File.separator + entry.getName();
            }
        } catch (IOException e) {
            throw new AppException("failed to get image path from image file.",
                ResponseConst.RET_PARSE_FILE_EXCEPTION);
        }
        imageDesc.setSwImage(newPathName);
        imgDecsLists.set(index, imageDesc);
        String jsonFile = fileParent + File.separator + JSON_EXTENSION;
        File swImageDesc = new File(jsonFile);
        writeFile(swImageDesc, new Gson().toJson(imgDecsLists));
    }

    private void addImageFileInfo(String parentDir, String imgZipPath) {
        if (!StringUtils.isEmpty(imgZipPath)) {
            try {
                // add image zip to mf file
                File mfFile = getFile(parentDir, "mf");
                new BasicInfo().rewriteManifestWithImage(mfFile, imgZipPath, keyPath, keyPwd);

                // add image zip to TOSCA.meta file
                String toscaMeta = parentDir + "/TOSCA-Metadata/TOSCA.meta";
                File metaFile = new File(toscaMeta);
                String contentFile = imgZipPath.substring(parentDir.length() + 1);
                String contentName = "Name: " + contentFile + "\n";
                if (!FileUtils.readFileToString(metaFile, StandardCharsets.UTF_8).contains(contentName)) {
                    FileUtils.writeStringToFile(metaFile, contentName, StandardCharsets.UTF_8, true);
                    FileUtils.writeStringToFile(metaFile,
                        "Content-Type: image\n", StandardCharsets.UTF_8, true);
                }
            } catch (Exception e) {
                LOGGER.error("add image file info to package failed {}", e.getMessage());
                throw new AppException("failed to add image info to package.",
                    ResponseConst.RET_ADD_IMAGE_INFO_FAILED, ".mf or TOSCA-Metadata/TOSCA.meta");
            }
        }
    }

    /**
     * get file by parent directory and file extension.
     */
    public File getFile(String parentDir, String fileExtension) {
        List<File> files = (List<File>) FileUtils.listFiles(new File(parentDir), null, true);
        for (File fileEntry : files) {
            if (Files.getFileExtension(fileEntry.getName().toLowerCase(Locale.ROOT)).equals(fileExtension)) {
                return fileEntry;
            }
        }
        return null;
    }

    /**
     * write json file.
     *
     * @param file file.
     * @param content content.
     */
    private void writeFile(File file, String content) {
        try (Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(content);
        } catch (IOException e) {
            LOGGER.error("write data into SwImageDesc.json failed, {}", e.getMessage());
        }
    }

    /**
     * load file and analyse file list.
     *
     * @param fileAddress file storage object url.
     */
    public void loadZipIntoPackage(String fileAddress, String token, String fileParent) {
        //get unzip  temp folder under csar folder
        try {
            File tempFolder = new File(fileParent);
            FileUtils.deleteDirectory(tempFolder);
            if (!tempFolder.exists() && !tempFolder.mkdirs()) {
                LOGGER.error("create upload path failed");
                throw new FileOperateException("create download file error", ResponseConst.RET_MAKE_DIR_FAILED);
            }
            AppService.unzipApplicationPackage(fileAddress, fileParent);
            File file = new File(fileParent);
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                String imgZipPath = getImgZipPath(token, fileParent, files);
                addImageFileInfo(fileParent, imgZipPath);
            }
        }  catch (IOException e) {
            LOGGER.error("failed to add image zip to package {} ", e.getMessage());
            throw new AppException("failed to add image zip to package.", ResponseConst.RET_IMAGE_TO_PACKAGE_FAILED);
        }
    }

    private String getImgZipPath(String token, String fileParent, File[] files) throws IOException {
        String imgZipPath = null;
        for (File f : files) {
            if (f.isDirectory() && f.getName().equals(IMAGE)) {
                File[] zipFileArrays = f.listFiles();
                if (zipFileArrays != null && zipFileArrays.length > 0) {
                    boolean presentZip = Arrays.stream(zipFileArrays)
                        .anyMatch(m1 -> m1.toString().contains(ZIP_EXTENSION));
                    if (!presentZip) {
                        imgZipPath = addImageFile(token, fileParent, f);
                    }
                }
            }
        }
        return imgZipPath;
    }

    private String addImageFile(String token, String fileParent, File f) throws IOException {
        String imgZipPath = null;
        String outPath = f.getCanonicalPath();
        List<SwImgDesc> imgDecsLists = getPkgFile(outPath);
        for (SwImgDesc imageDesc : imgDecsLists) {
            String pathname = imageDesc.getSwImage() + DOWNLOAD_ZIP_IMAGE;
            byte[] result = downloadImageFromFileSystem(token, pathname);
            String imageName = imageDesc.getName();
            if (imageName.contains(COLON)) {
                imageName = imageName.substring(0, imageName.lastIndexOf(":"));
            }
            LOGGER.info("output image path:{}", outPath);
            File imageDir = new File(outPath);
            if (!imageDir.exists() && !imageDir.mkdirs()) {
                LOGGER.error("create upload path failed");
                throw new AppException("create folder failed", ResponseConst.RET_MAKE_DIR_FAILED);
            }
            File fileImage = new File(outPath + File.separator + imageName + ZIP_EXTENSION);
            if (!fileImage.exists() && !fileImage.createNewFile()) {
                LOGGER.error("create download file error");
                throw new FileOperateException("create file failed", ResponseConst.RET_CREATE_FILE_FAILED);
            }
            try (InputStream inputStream = new ByteArrayInputStream(result);
                 OutputStream outputStream = new FileOutputStream(fileImage)) {
                int len = 0;
                byte[] buf = new byte[1024];
                while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
            }
            imgZipPath = fileImage.getCanonicalPath();
            updateJsonFile(imageDesc, imgDecsLists, fileParent, imgZipPath);
        }
        return imgZipPath;
    }

    /**
     * ZIP application package.
     *
     * @param intendedDir application package ID
     */
    public String compressCsarAppPackage(String intendedDir) {
        final Path srcDir = Paths.get(intendedDir);
        String zipFileName = intendedDir.concat(CSAR_EXTENSION);
        String[] fileName = zipFileName.split("/");
        String fileStorageAdd = srcDir + "/" + fileName[fileName.length - 1];
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            createCompressedFile(out, new File(intendedDir), "");
        } catch (IOException e) {
            throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        try {
            FileUtils.deleteDirectory(new File(intendedDir));
            FileUtils.moveFileToDirectory(new File(zipFileName), new File(intendedDir), true);
        } catch (IOException e) {
            throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        return fileStorageAdd;
    }

    /**
     * compress and delete ZIP application package.
     *
     * @param destinationFile destinationFile.
     * @param fileName compress file name.
     */
    public String compressAndDeleteFile(String destinationFile, String fileName) {
        String zipFileName = fileName.concat(ZIP_EXTENSION);
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            createCompressedFile(out, new File(destinationFile), "");
        } catch (IOException e) {
            throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        try {
            FileUtils.deleteDirectory(new File(destinationFile));
        } catch (IOException e) {
            throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
        return zipFileName;
    }

    private void createCompressedFile(ZipOutputStream out, File file, String dir) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (!dir.equals("")) {
                out.putNextEntry(new ZipEntry(dir + "/"));
            }

            dir = dir.length() == 0 ? "" : dir + "/";
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    createCompressedFile(out, files[i], dir + files[i].getName());
                }
            }
        } else {
            compressFile(out, file, dir);
        }
    }

    private void compressFile(ZipOutputStream out, File file, String dir) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            out.putNextEntry(new ZipEntry(dir));
            int j = 0;
            byte[] buffer = new byte[1024];
            while ((j = fis.read(buffer)) > 0) {
                out.write(buffer, 0, j);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("createCompressedFile: can not find param file, {}", e.getMessage());
            throw new AppException("can not find file", ResponseConst.RET_COMPRESS_FAILED);
        }
    }

    /**
     * zip files.
     * @param srcfile source file list
     * @param zipfile to be zipped file
     */
    public void zipFiles(List<File> srcfile, File zipfile) {
        List<String> entryPaths = new ArrayList<>();
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));) {
            for (File file : srcfile) {
                if (file.isFile()) {
                    addFileToZip(out, file, entryPaths);
                } else if (file.isDirectory()) {
                    entryPaths.add(file.getName());
                    addFolderToZip(out, file, entryPaths);
                    entryPaths.remove(entryPaths.size() - 1);
                }
            }
        } catch (IOException e) {
            throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }
    }

    private static void addFolderToZip(ZipOutputStream out, File file, List<String> entryPaths) throws IOException {
        out.putNextEntry(new ZipEntry(StringUtils.join(entryPaths, "/") + "/"));
        out.closeEntry();
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File subFile : files) {
            if (subFile.isFile()) {
                addFileToZip(out, subFile, entryPaths);
            } else if (subFile.isDirectory()) {
                entryPaths.add(subFile.getName());
                addFolderToZip(out, subFile, entryPaths);
                entryPaths.remove(entryPaths.size() - 1);
            }
        }
    }

    private static void addFileToZip(ZipOutputStream out, File file, List<String> entryPaths) throws IOException {
        byte[] buf = new byte[1024];
        try (FileInputStream in = new FileInputStream(file)) {
            if (!entryPaths.isEmpty()) {
                out.putNextEntry(new ZipEntry(StringUtils.join(entryPaths, "/")
                    + "/" + file.getName()));
            } else {
                out.putNextEntry(new ZipEntry(file.getName()));
            }
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        }
    }

    /**
     * update json file.
     *
     * @param imageDesc imageDesc.
     * @param imgDecsLists imgDecsLists.
     * @param fileParent fileParent.
     * @param newPathName newPathName.
     */
    public void updateJsonFileServer(SwImgDesc imageDesc, List<SwImgDesc> imgDecsLists, String fileParent,
        String newPathName) {
        int index = imgDecsLists.indexOf(imageDesc);
        imageDesc.setSwImage(newPathName);
        imgDecsLists.set(index, imageDesc);
        String jsonFile = fileParent + File.separator + JSON_EXTENSION;
        File swImageDesc = new File(jsonFile);
        writeFile(swImageDesc, new Gson().toJson(imgDecsLists));
    }
}
