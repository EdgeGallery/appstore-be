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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.atp.model.AtpMetadata;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.SwImgDesc;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final String SWIMAGE_PATH_EXTENSION = ".qcow2";

    private static final Gson gson = new Gson();

    private static final String ZIP_PACKAGE_ERR_MESSAGES = "failed to zip application package";

    private static final String ZIP_PACKAGE_ERR_GET = "failed to get application package image";

    private static final String ZIP_EXTENSION = ".zip";

    private static final String JSON_EXTENSION = "Image/SwImageDesc.json";

    private static final String VM = "vm";

    private static final String CONTAINER = "container";

    private static final String COLON = ":";

    private static final String IMAGE = "Image";

    private static final String QUERY_PATH = "image?imageId=";

    @Autowired
    private AppService appService;

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
     * @param release app package
     * @param file original file
     * @return file name
     */
    public String getFileName(Release release, AFile file) {
        StringBuffer fileName = new StringBuffer(release.getAppBasicInfo().getAppName());
        fileName.append(".");
        fileName.append(Files.getFileExtension(file.getOriginalFileName().toLowerCase()));
        return fileName.toString();
    }

    /**
     * append image path.
     *
     * @param str append args list.
     * @return StringBuilder.
     */
    public static StringBuilder stringBuilder(String... str) {

        StringBuilder stringBuilder = new StringBuilder();
        if (str == null || str.length <= 0) {
            return stringBuilder;
        }

        for (int i = 0; i < str.length; i++) {
            stringBuilder.append(str[i]);
        }

        return stringBuilder;
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
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            LOGGER.info("res: {}", response);
            return HttpStatus.OK.equals(response.getStatusCode());
        } catch (RestClientException | NullPointerException e) {
            LOGGER.error("image not exist from fileSystem which imageUrl is {} exception {}", url, e.getMessage());
        }
        return true;
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
                throw new AppException("download file exception", ResponseConst.RET_GET_IMAGE_DESC_FAILED);
            }
            result = response.getBody();

            if (result == null) {
                LOGGER.error("download file error, response is {}", response.getBody());
                throw new AppException("download file exception", ResponseConst.RET_GET_IMAGE_DESC_FAILED);
            }

        } catch (RestClientException e) {

            LOGGER.error("Failed to get image status which imageId is {} exception {}", url, e.getMessage());
            return null;
        }

        return result;
    }

    /**
     * load file and analyse file list.
     *
     * @param fileAddress file storage path.
     */
    public void checkImage(String fileAddress, AtpMetadata atpMetadata, String fileParent, String appClass) {
        if (!StringUtils.isEmpty(appClass) && appClass.equals(CONTAINER)) {
            return;
        }
        try {
            File file = new File(fileParent);
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File fl : files) {
                    if (fl.isDirectory() && fl.getName().equals(IMAGE)) {
                        File[] filezipArrays = fl.listFiles();
                        if (filezipArrays != null && filezipArrays.length > 0) {
                            boolean presentZip = Arrays.asList(filezipArrays).stream()
                                .filter(m1 -> m1.toString().contains(ZIP_EXTENSION)).findAny().isPresent();
                            if (!presentZip) {
                                List<SwImgDesc> imgDecsList = getPkgFile(fileParent);
                                for (SwImgDesc imageDesc : imgDecsList) {
                                    String pathName = imageDesc.getSwImage();
                                    String imageId = imageDesc.getId();
                                    pathName = pathName.substring(0, pathName.lastIndexOf(File.separator));
                                    StringBuilder newUrl = stringBuilder(pathName, File.separator, QUERY_PATH, imageId);
                                    if (!isImageExist(newUrl.toString(), atpMetadata.getToken())) {
                                        throw new AppException(ZIP_PACKAGE_ERR_GET,
                                            ResponseConst.RET_GET_IMAGE_DESC_FAILED);
                                    }
                                }
                            }
                        } else {
                            throw new AppException(ZIP_PACKAGE_ERR_GET, ResponseConst.RET_GET_IMAGE_DESC_FAILED);
                        }

                    }
                }
            }
        } catch (Exception e1) {
            LOGGER.error("judge package type error {} ", e1.getMessage());
        }

    }

    private List<SwImgDesc> getPkgFile(String parentDir) {
        File swImageDesc = appService.getFileFromPackage(parentDir, "SwImageDesc.json");
        if (swImageDesc == null) {
            return Collections.emptyList();
        }
        try {
            return appService.getSwImageDescrInfo(FileUtils.readFileToString(swImageDesc, StandardCharsets.UTF_8));
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
     * @param imageName imageName.
     */
    public void updateJsonFile(SwImgDesc imageDesc, List<SwImgDesc> imgDecsLists, String fileParent,
        String imageName) {
        int index = imgDecsLists.indexOf(imageDesc);
        StringBuilder newPathName = stringBuilder(IMAGE, File.separator, imageName, ZIP_EXTENSION, File.separator,
            imageName, File.separator, imageName, SWIMAGE_PATH_EXTENSION);
        imageDesc.setSwImage(newPathName.toString());
        imgDecsLists.set(index, imageDesc);
        String jsonFile = fileParent + File.separator + JSON_EXTENSION;
        File swImageDesc = new File(jsonFile);
        writeFile(swImageDesc, gson.toJson(imgDecsLists));
    }

    /**
     * write json file.
     *
     * @param file file.
     * @param content content.
     */
    private void writeFile(File file, String content) {
        try {
            Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            LOGGER.error("write data into SwImageDesc.json failed, {}", e.getMessage());
        }
    }

    /**
     * load file and analyse file list.
     *
     * @param fileAddress file storage object url.
     */
    public boolean loadZipIntoCsar(String fileAddress, String token, String fileParent) {
        File tempFolder = new File(fileParent);
        if (!tempFolder.exists()) {
            if (!tempFolder.mkdirs()) {
                LOGGER.error("create upload path failed");
                throw new AppException("create download file error");
            }
        }
        appService.unzipApplicationPacakge(fileAddress, fileParent);
        //get unzip  temp folder under csar folder
        try {
            File file = new File(fileParent);
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    if (f.isDirectory() && f.getName().equals(IMAGE)) {
                        File[] filezipArrays = f.listFiles();
                        if (filezipArrays != null && filezipArrays.length > 0) {
                            boolean presentZip = Arrays.asList(filezipArrays).stream()
                                .filter(m1 -> m1.toString().contains(ZIP_EXTENSION)).findAny().isPresent();
                            if (!presentZip) {
                                String outPath = f.getCanonicalPath();
                                List<SwImgDesc> imgDecsLists = getPkgFile(outPath);
                                for (SwImgDesc imageDesc : imgDecsLists) {
                                    String pathname = imageDesc.getSwImage();
                                    byte[] result = downloadImageFromFileSystem(token, pathname);
                                    String imageName = imageDesc.getName();
                                    if (imageName.contains(COLON)) {
                                        imageName = imageName.substring(0, imageName.lastIndexOf(":"));
                                    }
                                    LOGGER.info("output image path:{}", outPath);
                                    File imageDir = new File(outPath);
                                    if (!imageDir.exists()) {
                                        if (!imageDir.mkdirs()) {
                                            LOGGER.error("create upload path failed");
                                            return false;
                                        }
                                    }
                                    File fileImage = new File(outPath + File.separator + imageName + ZIP_EXTENSION);
                                    if (!fileImage.exists() && !fileImage.createNewFile()) {
                                        LOGGER.error("create download file error");
                                        throw new AppException("create download file error");
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
                                    updateJsonFile(imageDesc, imgDecsLists, fileParent, imageName);
                                }
                            } else {
                                return true;
                            }
                        }

                    }
                }
            }

        } catch (IOException e1) {
            LOGGER.error("judge package type error {} ", e1.getMessage());
        }

        return false;
    }

    /**
     * ZIP application package.
     *
     * @param intendedDir application package ID
     */
    public String compressAppPackage(String intendedDir) throws IOException {
        final Path srcDir = Paths.get(intendedDir);
        String zipFileName = intendedDir.concat(ZIP_EXTENSION);
        try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            java.nio.file.Files.walkFileTree(srcDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = srcDir.relativize(file);
                        os.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = java.nio.file.Files.readAllBytes(file);
                        os.write(bytes, 0, bytes.length);
                        os.closeEntry();
                    } catch (IOException e) {
                        throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AppException(ZIP_PACKAGE_ERR_MESSAGES, ResponseConst.RET_COMPRESS_FAILED);
        }

        return zipFileName;
    }

}
