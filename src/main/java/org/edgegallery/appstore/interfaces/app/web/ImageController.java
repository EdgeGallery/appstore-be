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

package org.edgegallery.appstore.interfaces.app.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.appstore.domain.model.app.Chunk;
import org.edgegallery.appstore.domain.model.app.ErrorRespDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RestSchema(schemaId = "image")
@RequestMapping("/mec/appstore/v1/image")
@Api(tags = "image")
@Validated
public class ImageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);

    @Value("${appstore-be.temp-path}")
    private String filePathTemp;

    @Value("${appstore-be.package-path}")
    private String filePath;

    /**
     * upload image.
     */
    @ApiOperation(value = "upload image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity uploadImage(HttpServletRequest request, Chunk chunk) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            MultipartFile file = chunk.getFile();

            if (file == null) {
                LOGGER.error("can not find any needed file");
                return ResponseEntity.badRequest().build();
            }
            File uploadDirTmp = new File(filePathTemp);
            if (!uploadDirTmp.exists()) {
                boolean rt = uploadDirTmp.mkdirs();
                if (rt == false) {
                    throw new Exception("create folder failed");
                }
            }

            Integer chunkNumber = chunk.getChunkNumber();
            if (chunkNumber == null) {
                chunkNumber = 0;
            }
            File outFile = new File(filePathTemp + File.separator + chunk.getIdentifier(), chunkNumber + ".part");
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * merge image.
     */
    @ApiOperation(value = "merge image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/merge", method = RequestMethod.GET)
    @PreAuthorize("hasRole('DEVELOPER_TENANT') || hasRole('DEVELOPER_ADMIN')")
    public ResponseEntity merge(@RequestParam(value = "fileName", required = false) String fileName,
        @RequestParam(value = "guid", required = false) String guid) throws Exception {
        File uploadDir = new File(filePath);
        if (!uploadDir.exists()) {
            boolean rt = uploadDir.mkdirs();
            if (rt == false) {
                throw new Exception("create folder failed");
            }

        }
        File file = new File(filePathTemp + File.separator + guid);
        String newFileAddress = "";
        String newFileName = "";
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                newFileAddress = filePath + File.separator + UUID.randomUUID().toString().replace("-", "");
                File partFiles = new File(newFileAddress);
                if (!partFiles.exists()) {
                    boolean rt = partFiles.mkdirs();
                    if (rt == false) {
                        throw new Exception("create folder failed");
                    }
                }
                newFileName = partFiles + File.separator + fileName;
                File partFile = new File(newFileName);
                for (int i = 1; i <= files.length; i++) {
                    File s = new File(filePathTemp + File.separator + guid, i + ".part");
                    FileOutputStream destTempfos = new FileOutputStream(partFile, true);
                    FileUtils.copyFile(s, destTempfos);
                    destTempfos.close();
                }
                FileUtils.deleteDirectory(file);
            }
        }

        return ResponseEntity.ok(newFileName);
    }

}
