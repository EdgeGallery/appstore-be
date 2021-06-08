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

import com.google.gson.Gson;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class BasicInfo {

    public static final String PACKAGE_XML_FORMAT = ".xml";

    public static final String PACKAGE_YAML_FORMAT = ".yaml";

    public static final String MANIFEST = ".mf";

    public static final String MARKDOWN = ".md";

    public static final String MF_META = "metadata:";

    public static final String MF_VERSION_META = "app_package_version";

    public static final String MF_PRODUCT_NAME = "app_product_name";

    public static final String MF_PROVIDER_META = "app_provider_id";

    public static final String MF_TIME_META = "app_release_data_time";

    public static final String MF_TYPE_META = "app_type";

    public static final String MF_CLASS_META = "app_class";

    public static final String MF_DESC_META = "app_package_description";

    public static final String MF_SOURCE_CHECK = "Source";

    public static final String MF_ALGORITHM_CHECK = "Algorithm";

    public static final String MF_HASH_CHECK = "Hash";

    public static final String MF_SEPARATOR = ": ";

    public static final String MF_NEWLINE = "\n";

    public static final String MF_APP_CONTACT = "app_contact";

    public static final String CSAR_EXTENSION = ".csar";

    private static final String ZIP_EXTENSION = ".zip";

    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private static final int BUFFER_READER_SIZE = 2 * 1024;

    private static final int BOUNDED_INPUTSTREAM_SIZE = 8 * 1024;

    private static final int READ_MAX_LONG = 10;

    private static final int LINE_MAX_LEN = 4096;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicInfo.class);

    private String appName;

    private String provider;

    private String contact;

    private String version;

    private String appReleaseTime;

    private String appType;

    private String appClass;

    private String appDesc;

    private List<String> sources;

    private String hashAlgorithm;

    private String fileType;

    private String fileStructure;

    private String markDownContent;

    public BasicInfo() {
        // empty construct function
    }

    /**
     * create dir.
     *
     * @param dir dir to create
     * @return boolean
     */
    public static boolean createDirectory(String dir) {
        File folder = new File(dir);
        return folder.exists() || folder.mkdirs();
    }

    /**
     * unzip zip file.
     *
     * @param zipFileName file name to zip
     * @param extPlace extPlace
     * @return unzip file name
     * @throws IOException e1
     */
    public static List<String> unzip(String zipFileName, String extPlace) throws IOException {
        List<String> unzipFileNams = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(zipFileName)) {
            Enumeration<ZipArchiveEntry> fileEn = zipFile.getEntries();
            byte[] buffer = new byte[BUFFER_SIZE];
            while (fileEn.hasMoreElements()) {
                ZipArchiveEntry entry = fileEn.nextElement();
                if (entry.isDirectory()) {
                    createDirectory(new File(extPlace, entry.getName()).getCanonicalPath());
                    continue;
                }
                File file = new File(extPlace, entry.getName());
                if (!file.getParentFile().exists()) {
                    createDirectory(file.getParentFile().getCanonicalPath());
                }
                try (InputStream input = zipFile.getInputStream(entry);
                     FileOutputStream out = FileUtils.openOutputStream(file);
                     BufferedOutputStream bos = new BufferedOutputStream(out)) {
                    int length = 0;
                    while ((length = input.read(buffer)) != -1) {
                        bos.write(buffer, 0, length);
                    }
                    unzipFileNams.add(file.getCanonicalPath());
                }
            }
        }
        return unzipFileNams;
    }

    /**
     * judge the file's format is yaml or not.
     *
     * @param file file to judge
     * @return boolean
     */
    public static boolean isYamlFile(File file) {
        return !file.isDirectory() && file.getName().indexOf(PACKAGE_YAML_FORMAT) != -1;
    }

    private static String getUnzipDir(String dirName) {
        File tmpDir = new File(File.separator + dirName);
        return tmpDir.getAbsolutePath().replace(CSAR_EXTENSION, "").replace(ZIP_EXTENSION, "");
    }

    private static FileRelationResponse buildFileStructure(String root, String base) {
        File rootfile = new File(root);
        if (!rootfile.isDirectory()) {
            return new FileRelationResponse(rootfile.getName());
        } else {
            FileRelationResponse current = new FileRelationResponse(rootfile.getName());
            File[] list = rootfile.listFiles();
            if (list != null && list.length > 0) {
                for (File subFile : list) {
                    current.addChild(
                        buildFileStructure(subFile.getAbsolutePath(), base + File.separator + rootfile.getName()));
                }
            }
            return current;
        }
    }

    /**
     * load file and analyse file list.
     *
     * @param fileAddress file storage path.
     */
    public BasicInfo load(String fileAddress) {
        String unzipDir = getUnzipDir(fileAddress);
        boolean isXmlCsar = false;
        try {
            List<String> unzipFiles = unzip(fileAddress, unzipDir);
            if (unzipFiles.isEmpty()) {
                isXmlCsar = true;
            }
            FileRelationResponse treeStruct = buildFileStructure(unzipDir, unzipDir);
            Gson gson = new Gson();
            fileStructure = gson.toJson(treeStruct);
            for (String unzipFile : unzipFiles) {
                boolean isRegular = isRegularFile(Paths.get(unzipFile));
                if (!isRegular) {
                    break;
                }
                File f = new File(unzipFile);
                String canonicalPath = f.getCanonicalPath();
                if (canonicalPath.endsWith(MANIFEST)) {
                    readManifest(f);
                }
                if (canonicalPath.endsWith(MARKDOWN)) {
                    readMarkDown(f);
                }
                if (isYamlFile(f)) {
                    isXmlCsar = false;
                }
            }
        } catch (IOException e1) {
            LOGGER.error("judge package type error {} ", e1.getMessage());
        }
        if (appName == null || provider == null || version == null || appName.length() < 1 || provider.length() < 1
            || version.length() < 1) {
            throw new IllegalArgumentException(
                MF_PRODUCT_NAME + ", " + MF_PROVIDER_META + " or " + MF_VERSION_META + " is empty.");
        }

        if (isXmlCsar) {
            fileType = PACKAGE_XML_FORMAT;
        } else {
            fileType = PACKAGE_YAML_FORMAT;
        }

        return this;
    }

    private void readMarkDown(File file) {
        try (InputStream in = FileUtils.openInputStream(file);
             BoundedInputStream boundedInput = new BoundedInputStream(in, BOUNDED_INPUTSTREAM_SIZE);
             InputStreamReader reader = new InputStreamReader(boundedInput, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader)) {
            String temp = readLine(br);
            StringBuilder sb = new StringBuilder();
            while (temp != null) {
                sb.append(temp).append("\n");
                temp = readLine(br);
            }
            markDownContent = sb.toString();
        } catch (IOException ex) {
            LOGGER.error("Exception occurs when open markdown file.");
        }
    }

    private String readLine(BufferedReader br) throws IOException {
        return read(br, LINE_MAX_LEN);
    }

    private String read(BufferedReader br, int lineMaxLen) throws IOException {
        int intC = br.read();
        if (-1 == intC) {
            return null;
        }
        StringBuilder sb = new StringBuilder(READ_MAX_LONG);
        while (intC != -1) {
            char c = (char) intC;
            if (c == '\n') {
                break;
            }
            if (sb.length() >= lineMaxLen) {
                throw new IOException("line too long");
            }
            sb.append(c);
            intC = br.read();
        }
        String str = sb.toString();
        if (!str.isEmpty() && str.endsWith("\r")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private void checkLines(String tempString) {
        try {
            int count1 = tempString.indexOf(':');
            String meta = tempString.substring(0, count1).trim();
            if (meta.equalsIgnoreCase(MF_PRODUCT_NAME)) {
                int count = tempString.indexOf(':') + 1;
                appName = tempString.substring(count).trim();
            }
            // Check for the package provider name
            if (meta.equalsIgnoreCase(MF_PROVIDER_META)) {
                int count = tempString.indexOf(':') + 1;
                provider = tempString.substring(count).trim();
            }
            // Check for package version
            if (meta.equalsIgnoreCase(MF_VERSION_META)) {
                int count = tempString.indexOf(':') + 1;
                version = tempString.substring(count).trim();
            }
            // Check for package release time
            if (meta.equalsIgnoreCase(MF_TIME_META)) {
                int count = tempString.indexOf(':') + 1;
                appReleaseTime = tempString.substring(count).trim();
            }
            // Check for package type
            if (meta.equalsIgnoreCase(MF_TYPE_META)) {
                int count = tempString.indexOf(':') + 1;
                appType = tempString.substring(count).trim();
            }
            // Check for package class
            if (meta.equalsIgnoreCase(MF_CLASS_META)) {
                int count = tempString.indexOf(':') + 1;
                appClass = tempString.substring(count).trim();
            }
            // Check for package description
            if (meta.equalsIgnoreCase(MF_DESC_META)) {
                int count = tempString.indexOf(':') + 1;
                appDesc = tempString.substring(count).trim();
            }
            // Check for package source file
            if (meta.equalsIgnoreCase(MF_SOURCE_CHECK)) {
                int count = tempString.indexOf(':') + 1;
                sources.add(tempString.substring(count).trim());
            }
            // Check for package hash algorithm
            if (meta.equalsIgnoreCase(MF_ALGORITHM_CHECK)) {
                int count = tempString.indexOf(':') + 1;
                hashAlgorithm = tempString.substring(count).trim();
            }
            // Check for package contact
            if (meta.equalsIgnoreCase(MF_APP_CONTACT)) {
                int count = tempString.indexOf(':') + 1;
                contact = tempString.substring(count).trim();
            }
        } catch (StringIndexOutOfBoundsException e) {
            LOGGER.error("Nonstandard format: {}", e.getMessage());
        }

    }

    private void readManifest(File file) {
        // Fix the package type to CSAR, temporary
        try (BoundedInputStream boundedInput = new BoundedInputStream(FileUtils.openInputStream(file),
            BOUNDED_INPUTSTREAM_SIZE);
             InputStreamReader isr = new InputStreamReader(boundedInput, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr, BUFFER_READER_SIZE);) {
            for (String tempString; (tempString = readLine(reader)) != null; ) {
                // If line is empty, ignore
                if ("".equals(tempString) || !tempString.contains(":")) {
                    continue;
                }
                checkLines(tempString);
            }
        } catch (IOException e) {
            LOGGER.error("Exception while parsing manifest file: {}", e.getMessage());
        }
    }

    /**
     * rewrite manifest file with image zip file.
     *
     */
    public void rewriteManifestWithImage(File mfFile, String imgZipPath) {
        try {
            readManifest(mfFile);
            String mfFilePath = mfFile.getCanonicalPath();
            String parentDir = mfFilePath.substring(0, mfFilePath.lastIndexOf(File.separator));
            String content = buildManifestContent(parentDir, imgZipPath);
            writeFile(mfFile, content);
        } catch (Exception e) {
            LOGGER.error("Exception while parse manifest file: {}", e.getMessage());
        }
    }

    private String buildManifestContent(String parentDir, String imageFullPath) {
        StringBuilder content = new StringBuilder().append(MF_META).append(MF_NEWLINE);
        content.append(MF_PRODUCT_NAME).append(MF_SEPARATOR).append(appName).append(MF_NEWLINE)
            .append(MF_PROVIDER_META).append(MF_SEPARATOR).append(provider).append(MF_NEWLINE)
            .append(MF_VERSION_META).append(MF_SEPARATOR).append(version).append(MF_NEWLINE)
            .append(MF_TIME_META).append(MF_SEPARATOR).append(appReleaseTime).append(MF_NEWLINE)
            .append(MF_TYPE_META).append(MF_SEPARATOR).append(appType).append(MF_NEWLINE)
            .append(MF_CLASS_META).append(MF_SEPARATOR).append(appClass).append(MF_NEWLINE)
            .append(MF_DESC_META).append(MF_SEPARATOR).append(appDesc).append(MF_NEWLINE)
            .append(MF_NEWLINE);
        for (String source : sources) {
            String sourceFilePath = parentDir + File.separator + source;
            content.append(MF_SOURCE_CHECK).append(MF_SEPARATOR).append(source).append(MF_NEWLINE)
                .append(MF_ALGORITHM_CHECK).append(MF_SEPARATOR).append(hashAlgorithm).append(MF_NEWLINE)
                .append(MF_HASH_CHECK).append(MF_SEPARATOR).append(getHashValue(sourceFilePath)).append(MF_NEWLINE)
                .append(MF_NEWLINE);
        }
        if (!StringUtils.isEmpty(imageFullPath)) {
            String imageSourceFile = imageFullPath.substring(imageFullPath.indexOf(parentDir) + 1);
            if (!sources.contains(imageSourceFile)) {
                content.append(MF_SOURCE_CHECK).append(MF_SEPARATOR).append(imageSourceFile).append(MF_NEWLINE)
                    .append(MF_ALGORITHM_CHECK).append(MF_SEPARATOR).append(hashAlgorithm).append(MF_NEWLINE)
                    .append(MF_HASH_CHECK).append(MF_SEPARATOR).append(getHashValue(imageFullPath)).append(MF_NEWLINE)
                    .append(MF_NEWLINE);
            }
        }
        return content.toString();
    }

    private String getHashValue(String sourceFilePath) {
        try (FileInputStream fis = new FileInputStream(sourceFilePath)) {
            return DigestUtils.sha256Hex(fis);
        } catch (IOException e) {
            throw new AppException("get hash value of source file failed", ResponseConst.RET_PARSE_FILE_EXCEPTION);
        }
    }

    private void writeFile(File file, String content) {
        try {
            Writer fw = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            LOGGER.error("write manifest file error.");
        }
    }

    private boolean isRegularFile(Path filePath) {
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(filePath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return attr.isRegularFile();
        } catch (IOException e) {
            LOGGER.error("Not a reqgular file with IOException.");
            return false;
        } catch (Exception e) {
            LOGGER.error("Not a reqgular file with Exception.");
            return false;
        }
    }
}
