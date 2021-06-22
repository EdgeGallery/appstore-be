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

package org.edgegallery.appstore.interfaces.system.facade;

import com.spencerwi.either.Either;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response.Status;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.system.lcm.UploadedFile;
import org.edgegallery.appstore.infrastructure.persistence.system.UploadedFileMapper;
import org.edgegallery.appstore.infrastructure.util.AppStoreFileUtils;
import org.edgegallery.appstore.infrastructure.util.BusinessConfigUtil;
import org.edgegallery.appstore.infrastructure.util.FormatRespDto;
import org.edgegallery.appstore.infrastructure.util.InitConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("uploadFileService")
public class UploadFileService {

    public static final String REGEX_START = Pattern.quote("{{");

    public static final String REGEX_END = Pattern.quote("}}");

    public static final Pattern REPLACE_PATTERN = Pattern.compile(REGEX_START + "(.*?)" + REGEX_END);

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadFileService.class);

    private static final String REGEX_UUID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private String sampleCodePath;

    @Autowired
    private UploadedFileMapper uploadedFileMapper;

    @Value("${imagelocation.domainname:}")
    private String devRepoEndpoint;

    @Value("${imagelocation.username:}")
    private String devRepoUsername;

    @Value("${imagelocation.password:}")
    private String devRepoPassword;

    @Value("${imagelocation.project:}")
    private String devRepoProject;

    @Value("${imagelocation.port:}")
    private String port;

    @Value("${imagelocation.protocol:}")
    private String protocol;

    /**
     * uploadFile.
     *
     * @return
     */
    public Either<FormatRespDto, UploadedFile> uploadFile(String userId, MultipartFile uploadFile) {
        LOGGER.info("Begin upload file");
        UploadedFile result = new UploadedFile();
        String fileName = uploadFile.getOriginalFilename();
        if (!FileChecker.isValid(fileName)) {
            LOGGER.error("File Name is invalid.");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "File Name is invalid."));
        }
        String fileId = UUID.randomUUID().toString();
        String upLoadDir = InitConfigUtil.getWorkSpaceBaseDir() + BusinessConfigUtil.getUploadfilesPath();
        String fileRealPath = upLoadDir + fileId;
        File dir = new File(upLoadDir);
        if (!dir.isDirectory()) {
            boolean isSuccess = dir.mkdirs();
            if (!isSuccess) {
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "make file dir failed"));
            }
        }

        File newFile = new File(fileRealPath);
        try {
            uploadFile.transferTo(newFile);
            result.setFileName(fileName);
            result.setFileId(fileId);
            result.setUserId(userId);
            result.setUploadDate(new Date());
            result.setTemp(true);
            result.setFilePath(BusinessConfigUtil.getUploadfilesPath() + fileId);
            uploadedFileMapper.saveFile(result);
        } catch (IOException e) {
            LOGGER.error("Failed to save file with IOException. {}", e.getMessage());
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Failed to save file."));
        }
        LOGGER.info("upload file success {}", fileName);
        //upload success
        result.setFilePath("");
        return Either.right(result);
    }

    /**
     * delete template files. If uploaded files have not been used over 30min, should be deleted.
     */
    public void deleteTempFile() {
        LOGGER.info("Begin delete temp file.");
        Date now = new Date();
        List<String> tempIds = uploadedFileMapper.getAllTempFiles();
        if (tempIds == null) {
            return;
        }
        for (String tempId : tempIds) {
            UploadedFile tempFile = uploadedFileMapper.getFileById(tempId);
            Date uploadDate = tempFile.getUploadDate();
            if ((int) ((now.getTime() - uploadDate.getTime()) / Consts.MINUTE) < Consts.TEMP_FILE_TIMEOUT) {
                continue;
            }

            String realPath = InitConfigUtil.getWorkSpaceBaseDir() + tempFile.getFilePath();
            File temp = new File(realPath);
            if (temp.exists()) {
                AppStoreFileUtils.deleteTempFile(temp);
                uploadedFileMapper.deleteFile(tempId);
                LOGGER.info("Delete temp file {} success.", tempFile.getFileName());
            }
        }
    }

}
