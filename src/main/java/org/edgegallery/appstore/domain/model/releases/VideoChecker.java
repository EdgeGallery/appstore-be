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

package org.edgegallery.appstore.domain.model.releases;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.edgegallery.appstore.domain.shared.exceptions.IllegalRequestException;
import org.springframework.web.multipart.MultipartFile;

public class VideoChecker extends FileChecker {

    /**
     * Constructor to create VideoChecker.
     *
     * @param dir package path
     */
    public VideoChecker(String dir) {
        super(dir);
    }

    @Override
    protected long getMaxFileSize() {
        return 50 * 1024 * 1024L;
    }

    @Override
    protected List<String> getFileExtensions() {
        return Arrays.asList("mp4");
    }

    @Override
    public File check(MultipartFile file) {
        File result = null;
        super.check(file);
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new IllegalRequestException("Video File name is null.", ResponseConst.RET_FILE_NAME_NULL);
        }

        String tempFileAddress = new StringBuilder().append(getDir()).append(File.separator).append("temp")
                .append(File.separator).append(file.getOriginalFilename()).toString();
        try {
            createFile(tempFileAddress);
            result = new File(tempFileAddress);
            file.transferTo(result);
        } catch (IOException e) {
            throw new FileOperateException("create temp file with IOException",
                ResponseConst.RET_VIDEO_CHECK_EXCEPTION);
        } catch (IllegalStateException e) {
            throw new FileOperateException(e.getMessage(), ResponseConst.RET_VIDEO_CHECK_EXCEPTION);
        }
        return result;
    }
}
