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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.edgegallery.appstore.domain.shared.exceptions.IllegalRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class PackageChecker extends FileChecker {

    private static final int BUFFER = 512;

    private static final Long TOOBIG = 0x280000000L; // max size of unzipped data, 100MB 0x6400000

    private static final int TOOMANY = 1024; // max number of files

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageChecker.class);

    /**
     * Constructor to create PackageChecker.
     *
     * @param dir package path
     */
    public PackageChecker(String dir) {
        super(dir);
    }

    @Override
    protected long getMaxFileSize() {
        return 5 * 1024 * 1024 * 1024L;
    }

    @Override
    protected List<String> getFileExtensions() {
        return Arrays.asList("csar", "zip");
    }

    @Override
    public File check(MultipartFile file) {
        File result = null;
        super.check(file);
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null) {
            throw new IllegalRequestException("Package File name is null.", ResponseConst.RET_FILE_NAME_NULL);
        }

        String tempFileAddress = new StringBuilder().append(getDir()).append(File.separator).append("temp")
            .append(File.separator).append(file.getOriginalFilename()).toString();
        try {
            createFile(tempFileAddress);
            result = new File(tempFileAddress);
            file.transferTo(result);
            unzip(tempFileAddress);
        } catch (IOException e) {
            LOGGER.error("create temp file failed: {}", e.getMessage());
            throw new FileOperateException("create temp file with IOException",
                ResponseConst.RET_PACKAGE_CHECK_EXCEPTION);
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException: {}", e.getMessage());
            throw new FileOperateException(e.getMessage(), ResponseConst.RET_PACKAGE_CHECK_EXCEPTION);
        }
        return result;
    }

    private String sanitzeFileName(String entryName, String intendedDir) throws IOException {
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
            throw new IllegalStateException("File is outside extraction target directory.");
        }
    }

    /**
     * Prevent bomb attacks.
     *
     * @param fileName file name.
     * @throws java.io.IOException throw IOException
     */
    public final void unzip(String fileName) throws IOException {
        ZipArchiveEntry entry;
        int entries = 0;
        long total = 0;
        byte[] data = new byte[BUFFER];
        List<File> tempFiles = new ArrayList<>();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(new File(fileName)))) {
            while ((entry = zis.getNextZipEntry()) != null) {
                int count;
                // Write the files to the disk, but ensure that the entryName is valid,
                // and that the file is not insanely big
                String name = sanitzeFileName(entry.getName(), getDir() + File.separator + "temp");
                File f = new File(name);
                tempFiles.add(f);
                if (isDir(entry, f)) {
                    continue;
                }
                FileOutputStream fos = FileUtils.openOutputStream(f);
                try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {
                    while (total <= TOOBIG && (count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                        total += count;
                    }
                    dest.flush();
                }
                entries++;
                if (entries > TOOMANY) {
                    throw new IllegalStateException("Too many files to unzip.");
                }
                if (total > TOOBIG) {
                    throw new IllegalStateException("File being unzipped is too big.");
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("unzip csar with exception.");
        } finally {
            deleteTempFiles(tempFiles);
        }
    }

    /**
     * check if entry is directory, if then create dir.
     *
     * @param entry entry of next element.
     * @param f File
     */
    private boolean isDir(ZipEntry entry, File f) {
        if (entry.isDirectory()) {
            boolean isSuccess = f.mkdirs();
            if (isSuccess) {
                return true;
            } else {
                return f.exists();
            }
        }
        return false;
    }

    // delete temp files
    private void deleteTempFiles(List<File> tempFiles) throws IOException {
        for (File f : tempFiles) {
            if (f.exists()) {
                FileUtils.forceDelete(f);
            }
        }
    }
}
