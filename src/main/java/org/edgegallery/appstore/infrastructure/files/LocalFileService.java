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

package org.edgegallery.appstore.infrastructure.files;

import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.service.FileService;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.edgegallery.appstore.domain.shared.exceptions.IllegalRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("LocalFileService")
public class LocalFileService implements FileService {

    public static final Logger LOGGER = LoggerFactory.getLogger(LocalFileService.class);


    /**
     *sanitize file name.
     *
     * @param entryName entry name.
     * @parm  intendedDir parent dir
     */
    public static String sanitizeFileName(String entryName, String intendedDir) throws IOException {
        File f = new File(intendedDir, entryName);
        String canonicalPath = f.getCanonicalPath();
        File intendDir = new File(intendedDir);
        if (intendDir.isDirectory() && !intendDir.exists()) {
            createFile(intendedDir);
        }
        String canonicalID = intendDir.getCanonicalPath();
        if (canonicalPath.startsWith(canonicalID)) {
            return canonicalPath;
        } else {
            throw new FileOperateException("file is outside extraction target directory.",
                ResponseConst.RET_FILE_OUT_TARGET);
        }
    }

    /**
     *create file name.
     *
     * @param filePath file name.
     */
    static void createFile(String filePath) throws IOException {
        File tempFile = new File(filePath);
        boolean result = false;

        if (!tempFile.getParentFile().exists() && !tempFile.isDirectory()) {
            result = tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists() && !tempFile.isDirectory() && !tempFile.createNewFile() && !result) {
            throw new FileOperateException("create temp file failed.", ResponseConst.RET_CREATE_FILE_FAILED);
        }
    }

    @Override
    public String saveTo(File file, String fileParent) {
        if (file == null || file.getName() == null) {
            throw new IllegalRequestException("file is null.", ResponseConst.RET_FILE_NAME_NULL);
        }
        String newFileName = UUID.randomUUID().toString().replace("-", "");
        String fileAddress = fileParent + File.separator + newFileName + "." + Files.getFileExtension(file.getName());
        File f = new File(fileParent);
        boolean success = f.mkdirs();
        if (!success) {
            LOGGER.info("parent directory existed.");
        }
        try {
            FileUtils.moveFile(file, new File(fileAddress));
        } catch (IOException e) {
            LOGGER.error("move file exception : {}", e.getMessage());
            throw new FileOperateException("move file exception.", ResponseConst.RET_SAVE_FILE_EXCEPTION);
        }
        return fileAddress;
    }

    public InputStream get(AFile afile) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(afile.getStorageAddress()));
    }

    public InputStream get(String afile) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(afile));
    }

    @Override
    public String get(String fileAddress, String filePath) {
        return getCsarFileContentByName(fileAddress, filePath);
    }

    @Override
    public void delete(AFile afile) {
        try {
            FileUtils.deleteDirectory(new File(afile.getStorageAddress()).getParentFile());
        } catch (IOException e) {
            LOGGER.error("delete file error {}", e.getMessage());
        }
    }

    /**
     * get file content by file path and file.
     *
     * @param filePath csar file address.
     * @param file file path in package.
     * @return file content
     */
    public static String getCsarFileContentByName(String filePath, String file) {
        try {
            return readFileContent(filePath, file);
        } catch (IOException e) {
            throw new AppException("get file content by name failed", ResponseConst.RET_GET_PACKAGE_FILE_FAILED);
        }
    }

    private static String readFileContent(String filePath, String target) throws IOException {
        InputStream inputStream = null;
        try (ZipFile zipFile = new ZipFile(filePath)) {
            ZipEntry result = null;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (checkEquals(entry.getName(), target)) {
                    result = entry;
                    break;
                }
            }
            if (result == null) {
                throw new AppException(target + " not found", ResponseConst.RET_PACKAGE_FILE_NOT_FOUND);
            }
            inputStream = zipFile.getInputStream(result);
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
            return writer.toString();
        } finally {
            closeStreamIgnoringException(inputStream);
        }
    }

    /**
     * check target is or not exist.
     *
     * @param dir file address.
     * @param target target file address.
     * @return boolean
     */
    private static boolean checkEquals(String dir, String target) {
        return dir.replace("/", "").equals(target.replace(File.separator, ""));
    }

    /**
     * close zip file Ignoring Exception.
     *
     * @param stream stream input.
     */
    private static void closeStreamIgnoringException(InputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException var2) {
            LOGGER.info("Close stream with Exception");
        }
    }

    /**
     * download file to parent path directory.
     *
     * @param url url
     * @param parentPath parent path
     * @return download file
     */
    public File downloadFile(String url, String parentPath, String targetAppstore) {
        if (!createParent(parentPath)) {
            LOGGER.error("create file parent fail");
            throw new FileOperateException("create file parent path failed.", ResponseConst.RET_MAKE_DIR_FAILED);
        }
        RestTemplate restTemplate = new RestTemplate();

        List<MediaType> list = new ArrayList<>();
        list.add(MediaType.ALL);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(list);

        try {
            ResponseEntity<byte[]> response = restTemplate
                .exchange(url + "?targetAppstore=" + targetAppstore, HttpMethod.GET, new HttpEntity<byte[]>(headers),
                    byte[].class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("download file error, response is {}", response.getBody());
                throw new AppException("download file from source appstore failed.",
                    ResponseConst.RET_DOWNLOAD_FROM_APPSTORE_FAILED);
            }

            byte[] result = response.getBody();
            if (result == null) {
                throw new AppException("download file from source appstore null.",
                    ResponseConst.RET_DOWNLOAD_FROM_APPSTORE_FAILED);
            }
            String fileName = Optional.ofNullable(response.getHeaders().get("Content-Disposition"))
                .orElseThrow(() -> new AppException("response header Content-Disposition is null",
                    ResponseConst.RET_DOWNLOAD_FROM_APPSTORE_FAILED)).get(0)
                .replace("attachment; filename=", "");
            File file = new File(parentPath + File.separator + fileName);
            if (!file.exists() && !file.createNewFile()) {
                LOGGER.error("create download file error");
                throw new FileOperateException("create download file error.", ResponseConst.RET_CREATE_FILE_FAILED);
            }

            try (InputStream inputStream = new ByteArrayInputStream(result);
                 OutputStream outputStream = new FileOutputStream(file)) {
                int len = 0;
                byte[] buf = new byte[1024];
                while ((len = inputStream.read(buf, 0, 1024)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.flush();
            }

            return file;
        } catch (RuntimeException | IOException e1) {
            LOGGER.error("download file error: {}", e1.getMessage());
            throw new AppException("download file failed.", ResponseConst.RET_DOWNLOAD_FROM_MESSAGE_FAILED);
        }
    }

    private boolean createParent(String parentPath) {
        File parent = new File(parentPath);
        if (!parent.exists() || !parent.isDirectory()) {
            return parent.mkdirs();
        }
        return true;
    }
}
