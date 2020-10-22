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
import org.springframework.web.multipart.MultipartFile;



public abstract class FileChecker {

    private static final String REG
            = "[^\\s\\\\/:*?\"<>|](\\x20|[^\\s\\\\/:*?\"<>|])*[^\\s\\\\/:*?\"<>|.]$";

    private static final int MAX_LENGTH_FILE_NAME = 255;

    public static final String BLANK_REG = "\\s";

    public static final String PACKAGE_XML_FORMAT = ".xml";

    public static final String PACKAGE_YAML_FORMAT = ".yaml";

    public static final String PACKAGE_CSH_FORMAT = ".csh";

    public static final String PACKAGE_META_FORMAT = ".meta";

    public static final String PACKAGE_TXT_FORMAT = ".txt";

    public static final String MANIFEST = ".mf";

    public static final String MARKDOWN = ".md";

    private static List<String> extensions = Arrays.asList(PACKAGE_XML_FORMAT, PACKAGE_YAML_FORMAT, PACKAGE_CSH_FORMAT,
                PACKAGE_META_FORMAT, PACKAGE_TXT_FORMAT, MANIFEST, MARKDOWN);

    private String dir;

    protected abstract long getMaxFileSize();

    protected abstract List<String> getFileExtensions();

    /**
     * Constructor to create FileChecker.
     *
     * @param dir package path
     */
    public FileChecker(String dir) {
        this.dir = dir;
    }

    /**
     * check if file path is valid.
     * @param filePath file path.
     * @return
     */
    public static String checkByPath(String filePath) {
        filePath = Normalizer.normalize(filePath, Normalizer.Form.NFKC);

        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException(filePath + " :filepath is empty");
        }

        // file name should not contains blank.
        if (filePath.split(BLANK_REG).length > 1) {
            throw new IllegalArgumentException(filePath + " :filepath contain blank");
        }

        String name = filePath.toLowerCase();
        if (!extensions.contains(name.substring(name.lastIndexOf(".")))) {
            throw new IllegalArgumentException();
        }

        String[] dirs = filePath.split(":");
        for (String dir : dirs) {
            Matcher matcher = Pattern.compile(FileChecker.REG).matcher(dir);
            if (!matcher.matches()) {
                throw new IllegalArgumentException();
            }
        }
        return filePath.replace(":", File.separator);
    }

    /**
     * check file if is invalid.
     * @param file object.
     */
    public File check(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        // file name should not contains blank.
        if (originalFilename != null && originalFilename.split("\\s").length > 1) {
            throw new IllegalArgumentException(originalFilename + " :fileName contain blank");
        }

        if (originalFilename != null && !isAllowedFileName(originalFilename)) {
            throw new IllegalArgumentException(originalFilename + " :fileName is Illegal");
        }

        if (file.getSize() > getMaxFileSize()) {
            throw new IllegalArgumentException(originalFilename + " :fileSize is too big");
        }
        return null;
    }

    private boolean isAllowedFileName(String originalFilename) {
        return isValid(originalFilename)
                && getFileExtensions().contains(Files.getFileExtension(originalFilename.toLowerCase()));
    }

    /**
     * check if file name if it's invalid.
     * @param fileName file name
     * @return
     */
    static boolean isValid(String fileName) {
        if (StringUtils.isEmpty(fileName) || fileName.length() > MAX_LENGTH_FILE_NAME) {
            return false;
        }
        fileName = Normalizer.normalize(fileName, Normalizer.Form.NFKC);
        Matcher matcher = Pattern.compile(REG).matcher(fileName);
        return matcher.matches();
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



