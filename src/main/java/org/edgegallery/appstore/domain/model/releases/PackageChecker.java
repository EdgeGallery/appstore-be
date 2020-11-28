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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

public class PackageChecker extends FileChecker {

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
        return 50 * 1024 * 1024L;
    }

    @Override
    protected List<String> getFileExtensions() {
        return Collections.singletonList("csar");
    }

    @Override
    public File check(MultipartFile file) {
        File result = null;
        super.check(file);
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null) {
            throw new IllegalArgumentException("Package File name is null.");
        }

        String tempFileAddress = new StringBuilder().append(getDir())
                .append(File.separator)
                .append("temp")
                .append(File.separator)
                .append(file.getOriginalFilename())
                .toString();
        try {
            createFile(tempFileAddress);
            result = new File(tempFileAddress);
            file.transferTo(result);
            unzip(tempFileAddress);
        } catch (IOException e) {
            LOGGER.error("create temp file failed: {}", e.getMessage());
            throw new IllegalArgumentException("create temp file with IOException");
        } catch (IllegalStateException e) {
            LOGGER.error("IllegalStateException: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        return result;
    }


    static final int BUFFER = 512;

    static final int TOOBIG = 0x6400000; // max size of unzipped data, 100MB

    static final int TOOMANY = 1024; // max number of files

    // ...

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
        FileInputStream fis = FileUtils.openInputStream(new File(fileName));
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        int entries = 0;
        int total = 0;
        byte[] data = new byte[BUFFER];
        try {
            while ((entry = zis.getNextEntry()) != null) {
                int count;
                // Write the files to the disk, but ensure that the entryName is valid,
                // and that the file is not insanely big
                String name = sanitzeFileName(entry.getName(), getDir() + File.separator + "temp");
                File f = new File(name);
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
                zis.closeEntry();
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
            zis.close();
            FileUtils.deleteDirectory(new File(fileName.substring(0, fileName.lastIndexOf("."))));
        }
    }

    /**
     * check if entry is directory, if then create dir.
     * @param entry entry of next element.
     * @param f File
     * @return
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
}
