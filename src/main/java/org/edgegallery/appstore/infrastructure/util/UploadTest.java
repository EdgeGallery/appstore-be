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

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.CustomException;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PackageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("UploadTest")
public class UploadTest {
    public static final Logger LOGGER = LoggerFactory.getLogger(UploadTest.class);

    private static final int chunkSize = 50 * 1024 * 1024;

    private static final RestTemplate restTemplate = new RestTemplate();

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    @Autowired
    private PackageMapper packageMapper;

    @Value("${appstore-be.filesystem-address:}")
    private String fileSystemAddress;

    /**
     * encrypted identifier.
     * @param originString identifier.
     * @return
     */
    public static String encryptedByMD5(String originString) {
        try {
            //Create information digest with MD5 algorithm.
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Use the specified byte array to make the final update to the summary, complete the summary calculation.
            byte[] bytes = md.digest(originString.getBytes(Charset.forName("UTF-8")));
            //Turn the resulting byte array into a string and return.
            String s = byteArrayToHex(bytes);
            return s.toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert the byte array to hexadecimal and return it as a string.
     * 128 bits refer to binary bits. Binary is too long, so it is generally rewritten into hexadecimal.
     * Each hexadecimal number can replace a 4-bit binary number, so if a 128-bit binary number is.
     * written as a hexadecimal number, it becomes 128/4=32 bits.
     *
     * @param b bytes.
     * @return
     */
    private static String byteArrayToHex(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(byteToHex(b[i]));
        }
        return sb.toString();
    }

    /**
     *Convert a byte to hexadecimal and return it as a string.
     * @param b bytes.
     * @return
     */
    public static String byteToHex(byte b) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex;
    }


    /**
     *  upload file to file server.
     * @param userId userId.
     * @param absolutionFilePath absolutionFilePath.
     * @return
     */
    public String uploadFile(String userId, String absolutionFilePath) throws IOException {
        String imageId = "";
        File sourceFile = new File(absolutionFilePath);
        long fileLength = sourceFile.length();
        RandomAccessFile readFile = new RandomAccessFile(sourceFile, "rw");
        long chunkTotal = fileLength / chunkSize;
        if (fileLength % chunkSize != 0) {
            chunkTotal++;
        }

        byte[] buf = new byte[chunkSize];
        int chunkCount = 0;
        int currentChunkSize = -1;
        String identifier = UUID.randomUUID().toString().replace("-", "");
        while ((currentChunkSize = readFile.read(buf)) != -1) {
            chunkCount++;
            String targetFile = chunkCount + ".part";
            RandomAccessFile writeFile = new RandomAccessFile(new File(targetFile), "rw");
            writeFile.write(buf, 0, currentChunkSize);
            writeFile.close();

            FileBody bin = new FileBody(new File(targetFile));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart(targetFile, bin);
            if (!sliceUploadFile(identifier, targetFile)) {
                LOGGER.error("upload to remote file server failed.");
                FileUtils.deleteDirectory(new File(targetFile));
                throw new AppException("upload to remote file server failed.",
                    ResponseConst.RET_UPLOAD_FILE_FAILED);
            }
        }
        if (chunkTotal == chunkCount) {
            String uploadResult = sliceMergeFile(identifier, absolutionFilePath, userId);
            Gson gson = new Gson();
            Map<String, String> uploadResultModel = gson.fromJson(uploadResult, Map.class);
            imageId = uploadResultModel.get("imageId");
            deleteTempPartFile(absolutionFilePath);

        }
        return imageId;
    }

    /**
     * delete temp .part file.
     * @param absolutionFilePath temp file folder.
     */
    public void deleteTempPartFile(String absolutionFilePath) throws IOException {
        File tempFolder = new File(absolutionFilePath).getParentFile().getCanonicalFile();
        if (!tempFolder.exists() && !tempFolder.mkdirs()) {
            LOGGER.error("temp file folder not exist.");
            throw new FileOperateException(".emp file folder not exist", ResponseConst.RET_MAKE_DIR_FAILED);
        }
        File[] files = tempFolder.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.getName().endsWith(".part")) {
                    FileUtils.deleteQuietly(file);
                }
            }
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

        ResponseEntity<String> response;
        try {
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("slice upload file exception {}", errorLog);
            return false;
        } catch (RestClientException e) {
            LOGGER.error("slice upload file exception {}", e.getMessage());
            return false;
        }

        if (response == null || response.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("slice upload file failed!");
            return false;
        }

        return true;
    }


    /**
     * slice merge file.
     *
     * @param fileName File name.
     * @param identifier File Identifier.
     * @param userId User ID.
     * @return
     */
    public String sliceMergeFile(String identifier, String fileName, String userId) {
        LOGGER.info("slice merge file, identifier = {}, filename = {}", identifier, fileName);
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
            REST_TEMPLATE.setErrorHandler(new CustomResponseErrorHandler());
            response = REST_TEMPLATE.exchange(url, HttpMethod.POST, requestEntity, String.class);
            LOGGER.info("slice merge file success, resp = {}", response);
        } catch (CustomException e) {
            String errorLog = e.getBody();
            LOGGER.error("slice merge file exception", errorLog);
            return null;
        } catch (RestClientException e) {
            LOGGER.error("slice merge file exception", e.getMessage());
            return null;
        }

        if (response == null || response.getStatusCode() != HttpStatus.OK) {
            LOGGER.error("slice merge file failed");
            return null;

        }

        LOGGER.info("slice merge file success, resp = {}", response);
        return response.getBody();
    }

}
