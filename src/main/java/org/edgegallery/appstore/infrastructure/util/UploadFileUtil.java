/*
 *    Copyright 2021-2022 Huawei Technologies Co., Ltd.
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.CustomException;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("UploadFileUtil")
public class UploadFileUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(UploadFileUtil.class);

    private static final int CHUNK_SIZE = 50 * 1024 * 1024;

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Value("${appstore-be.filesystem-address:}")
    private String fileSystemAddress;

    /**
     * upload file to file server.
     *
     * @param userId userId.
     * @param absolutionFilePath absolutionFilePath.
     */
    public String uploadFile(String userId, String absolutionFilePath) {
        File sourceFile = new File(absolutionFilePath);
        String tempFolder = new File(absolutionFilePath).getParent();
        long fileLength = sourceFile.length();

        long chunkTotal = fileLength / CHUNK_SIZE;
        if (fileLength % CHUNK_SIZE != 0) {
            chunkTotal++;
        }

        byte[] buf = new byte[CHUNK_SIZE];
        int chunkCount = 0;
        int currentChunkSize;
        String identifier = UUID.randomUUID().toString().replace("-", "");
        try (RandomAccessFile readFile = new RandomAccessFile(sourceFile, "rw");) {
            while ((currentChunkSize = readFile.read(buf)) != -1) {
                chunkCount++;
                String targetFile = tempFolder + File.separator + chunkCount + ".part";
                RandomAccessFile writeFile = new RandomAccessFile(new File(targetFile), "rw");
                writeFile.write(buf, 0, currentChunkSize);
                writeFile.close();

                FileBody bin = new FileBody(new File(targetFile));
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addPart(targetFile, bin);
                if (!sliceUploadFile(identifier, targetFile)) {
                    LOGGER.error("Upload to remote file server failed.");
                    FileUtils.deleteQuietly(new File(targetFile));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Upload to remote file server failed, errorMsg: {}", e.getMessage());
            throw new AppException("Upload to remote file server failed.", ResponseConst.RET_UPLOAD_FILE_FAILED);
        }

        String imageId = "";
        if (chunkTotal == chunkCount) {
            String fileName = absolutionFilePath.substring(absolutionFilePath.lastIndexOf(File.separator) + 1);
            String uploadResult = mergeSegmentFiles(identifier, fileName, userId);
            Map<String, String> uploadResultModel = new Gson()
                .fromJson(uploadResult, new TypeToken<Map<String, String>>() { }.getType());
            imageId = uploadResultModel.get("imageId");
            deleteTempPartFile(tempFolder, fileName);
        }
        return imageId;
    }

    /**
     * delete temp .part file.
     *
     * @param tempPath temp file folder.
     */
    public void deleteTempPartFile(String tempPath, String fileName) {
        try {
            File tempFolder = new File(tempPath).getCanonicalFile();
            if (!tempFolder.exists() && !tempFolder.mkdirs()) {
                LOGGER.error("The temp file folder does not exist.");
                throw new FileOperateException("The temp file folder does not exist",
                    ResponseConst.RET_MAKE_DIR_FAILED);
            }
            File[] files = tempFolder.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().endsWith(".part") || file.getName().endsWith(fileName)) {
                        FileUtils.deleteQuietly(file.getCanonicalFile());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Delete temp part file failed, errorMsg: {}", e.getMessage());
            throw new AppException("Delete temp part file failed.", ResponseConst.RET_DEL_FILE_FAILED);
        }
    }

    /**
     * slice upload file.
     *
     * @param identifier File identifier。
     * @param filePath File Path。
     * @return upload result
     */
    public boolean sliceUploadFile(String identifier, String filePath) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("part", new FileSystemResource(filePath));
        formData.add("priority", 0);
        formData.add("identifier", identifier);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        org.springframework.http.HttpEntity<MultiValueMap<String, Object>> requestEntity
            = new org.springframework.http.HttpEntity<>(formData, headers);
        String url = String.format("%s/image-management/v1/images/upload", fileSystemAddress);

        try {
            ResponseEntity<String> response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("Slice uploaded file failed!");
                return false;
            }
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("Slice uploaded file failed, CustomException: {}", errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("Slice uploaded file failed, RestClientException: {}", e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * merge segment files.
     *
     * @param fileName File name.
     * @param identifier File Identifier.
     * @param userId User ID.
     */
    public String mergeSegmentFiles(String identifier, String fileName, String userId) {
        LOGGER.info("Merge segment files, identifier = {}, filename = {}", identifier, fileName);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("userId", userId);
        formData.add("priority", 0);
        formData.add("identifier", identifier);
        formData.add("filename", fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        org.springframework.http.HttpEntity<MultiValueMap<String, Object>> requestEntity
            = new org.springframework.http.HttpEntity<>(formData, headers);
        String url = String.format("%s/image-management/v1/images/merge", fileSystemAddress);
        LOGGER.warn(url);
        ResponseEntity<String> response;
        try {
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("Merge segment files failed, CustomException: {}", errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("Merge segment files failed, RestClientException: {}", e.getMessage());
            return null;
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("Merge segment files failed, response = {}", response);
            return null;
        }

        LOGGER.info("Merge segment files successfully, resp = {}", response);
        return response.getBody();
    }

}
