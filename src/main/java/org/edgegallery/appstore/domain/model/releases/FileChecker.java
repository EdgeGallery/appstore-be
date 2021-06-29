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

package org.edgegallery.appstore.domain.model.releases;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.IllegalRequestException;
import org.springframework.web.multipart.MultipartFile;

public abstract class FileChecker {

    public static final String BLANK_REG = "\\s";

    public static final String PACKAGE_XML_FORMAT = ".xml";

    public static final String PACKAGE_YAML_FORMAT = ".yaml";

    public static final String PACKAGE_CSH_FORMAT = ".csh";

    public static final String PACKAGE_META_FORMAT = ".meta";

    public static final String PACKAGE_TXT_FORMAT = ".txt";

    public static final String MANIFEST = ".mf";

    public static final String MARKDOWN = ".md";

    private static final String REG = "[^\\s\\\\/:*?\"<>|](\\x20|[^\\s\\\\/:*?\"<>|])*[^\\s\\\\/:*?\"<>|.]$";

    private static final int MAX_LENGTH_FILE_NAME = 255;

    private static List<String> extensions = Arrays
        .asList(PACKAGE_XML_FORMAT, PACKAGE_YAML_FORMAT, PACKAGE_CSH_FORMAT, PACKAGE_META_FORMAT, PACKAGE_TXT_FORMAT,
            MANIFEST, MARKDOWN);

    private String dir;

    /**
     * Constructor to create FileChecker.
     *
     * @param dir package path
     */
    protected FileChecker(String dir) {
        this.dir = dir;
    }

    /**
     * check if file path is valid.
     *
     * @param filePath file path.
     *
     */
    public static String checkByPath(String filePath) {
        filePath = Normalizer.normalize(filePath, Normalizer.Form.NFKC);

        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalRequestException(filePath + " :filepath is empty",
                ResponseConst.RET_FILE_PATH_INVALID);
        }

        // file name should not contains blank.
        if (filePath.split(BLANK_REG).length > 1) {
            throw new IllegalRequestException(filePath + " :filepath contain blank",
                ResponseConst.RET_FILE_PATH_INVALID);
        }

        String name = filePath.toLowerCase();
        if (!extensions.contains(name.substring(name.lastIndexOf(".")))) {
            throw new IllegalRequestException(filePath + " :filepath doesn't have file extension",
                ResponseConst.RET_FILE_PATH_INVALID);
        }

        String[] dirs = filePath.split(":");
        for (String dir : dirs) {
            Matcher matcher = Pattern.compile(FileChecker.REG).matcher(dir);
            if (!matcher.matches()) {
                throw new IllegalRequestException(filePath + " :filepath isn't regular",
                    ResponseConst.RET_FILE_PATH_INVALID);
            }
        }
        return filePath.replace(":", File.separator);
    }

    /**
     * check if file name if it's invalid.
     *
     * @param fileName file name
     */
    public static boolean isValid(String fileName) {
        if (StringUtils.isEmpty(fileName) || fileName.length() > MAX_LENGTH_FILE_NAME) {
            return false;
        }
        fileName = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        Matcher matcher = Pattern.compile(REG).matcher(fileName);
        return matcher.matches();
    }

    protected abstract long getMaxFileSize();

    protected abstract List<String> getFileExtensions();

    /**
     * check file if is invalid.
     *
     * @param file object.
     */
    public File check(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        // file name should not contains blank.
        if (originalFilename != null && originalFilename.split("\\s").length > 1) {
            throw new IllegalRequestException(originalFilename + " :fileName contain blank",
                ResponseConst.RET_FILE_NAME_CONTAIN_BLANK, originalFilename);
        }

        if (originalFilename != null && !isAllowedFileName(originalFilename)) {
            List<String> validExtensions = getFileExtensions();
            String extensions = "[" + String.join(",", validExtensions) + "]";
            throw new IllegalRequestException(originalFilename + " :fileName is Illegal",
                ResponseConst.RET_FILE_NAME_POSTFIX_INVALID, originalFilename, extensions);
        }

        if (file.getSize() > getMaxFileSize()) {
            throw new IllegalRequestException(originalFilename + " :fileSize is too big",
                ResponseConst.RET_FILE_TOO_BIG, originalFilename, getMaxFileSize() / 1024 / 1024L);
        }
        return null;
    }

    private boolean isAllowedFileName(String originalFilename) {
        return isValid(originalFilename) && getFileExtensions()
            .contains(Files.getFileExtension(originalFilename.toLowerCase()));
    }

    protected void createFile(String filePath) throws IOException {
        File tempFile = new File(filePath);
        boolean result = false;

        if (!tempFile.getParentFile().exists() && !tempFile.isDirectory()) {
            result = tempFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists() && !tempFile.isDirectory() && !tempFile.createNewFile() && !result) {
            throw new IllegalArgumentException("create temp file failed");
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
}



