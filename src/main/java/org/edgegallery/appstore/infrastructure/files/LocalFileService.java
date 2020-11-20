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

package org.edgegallery.appstore.infrastructure.files;

import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.service.FileService;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalFileService implements FileService {

    public static final Logger LOGGER = LoggerFactory.getLogger(LocalFileService.class);

    private String dir;

    /**
     * Constructor to create LocalFileService.
     *
     * @param dir package path
     */
    public LocalFileService(String dir) {
        this.dir = dir;
    }

    private String generateFileName() {
        String random = UUID.randomUUID().toString();
        return random.replace("-", "");
    }

    @Override
    public String saveTo(File file) {
        if (file == null || file.getName() == null) {
            throw new IllegalArgumentException("file is null");
        }
        String fileName = generateFileName();
        String fileAddress = "";
        String originalFileName = file.getName();
        if (originalFileName != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(dir).append(File.separator).append(fileName);
            String fileLoaction = sb.toString();
            sb.append(File.separator).append(fileName).append(".").append(Files.getFileExtension(originalFileName));
            fileAddress = sb.toString();
            File f = new File(fileLoaction);
            boolean isSuccess = f.mkdirs();
            if (!isSuccess) {
                return "";
            }
        }
        try {
            FileUtils.moveFile(file, new File(fileAddress));
        } catch (IOException e) {
            LOGGER.error("move file exception : {}", e.getMessage());
            throw new FileOperateException("move file exception.");
        }
        return fileAddress;
    }

    public InputStream get(AFile afile) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(afile.getStorageAddress()));
    }

    @Override
    public String get(String fileAddress, String filePath) throws IOException {
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
     * @return
     */
    public static String getCsarFileContentByName(String filePath, String file) throws IOException {
        return readFileContent(filePath, file);
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
                throw new FileNotFoundException(target + " not found");
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
     * @return
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
     * Returns dir.
     *
     * @return dir
     */
    public String getDir() {
        return dir;
    }

    /**
     * Sets dir.
     *
     * @param dir package dir
     */
    public void setDir(String dir) {
        this.dir = dir;
    }
}
