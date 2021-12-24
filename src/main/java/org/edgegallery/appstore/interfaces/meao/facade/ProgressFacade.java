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

package org.edgegallery.appstore.interfaces.meao.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.application.packageupload.UploadPackageService;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgressMapper;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystem;
import org.edgegallery.appstore.interfaces.meao.facade.dto.PackageProgressDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("ProgressFacade")
public class ProgressFacade {
    @Autowired
    PackageUploadProgressMapper packageUploadProgressMapper;

    @Autowired
    private UploadPackageService uploadPackageService;

    /**
     * create Progress.
     *
     * @param process process
     * @return String
     */
    public ResponseEntity<String> createProgress(PackageUploadProgress process) {
        if (process.getId() == null) {
            process.setId(UUID.randomUUID().toString());
        }
        int ret = packageUploadProgressMapper.insertSelective(process);
        if (ret > 0) {
            return ResponseEntity.ok("create process success.");
        } else {
            throw new AppException("create process fail.");
        }
    }

    /**
     * query Progress.
     *
     * @param id process id
     * @return PackageUploadProgress
     */
    public ResponseEntity<PackageUploadProgress> getProgress(String id) {
        PackageUploadProgress ret = packageUploadProgressMapper.selectByPrimaryKey(id);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException("get process fail.");
        }
    }

    /**
     * query Progress by packageId and meaoId.
     *
     * @param packageId packageId
     * @param meaoId meaoId
     * @return PackageUploadProgress list
     */
    public ResponseEntity<List<PackageUploadProgress>> getProgressByPackageAndMeao(String packageId, String meaoId) {
        List<PackageUploadProgress> ret = packageUploadProgressMapper.selectByPackageAndMeao(packageId, meaoId);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException("get process fail.");
        }
    }

    /**
     * query Progress by packageId.
     *
     * @param packageId packageId
     * @param token token
     * @return PackageUploadProgress list
     */
    public ResponseEntity<List<PackageProgressDto>> getProgressByPackageId(String packageId, String token) {
        List<PackageUploadProgress> list = packageUploadProgressMapper.selectByPackageId(packageId);
        List<PackageProgressDto> dtoList = new ArrayList<>();
        for (PackageUploadProgress progress : list) {
            String meaoId = progress.getMeaoId();
            ThirdSystem meaoInfo = uploadPackageService.getMeaoInfo(meaoId, token);
            PackageProgressDto dto = new PackageProgressDto().transferTo(progress);
            if (meaoInfo != null) {
                dto.setSystemName(meaoInfo.getSystemName());
                dto.setUrl(meaoInfo.getUrl());
            }
            dtoList.add(dto);
        }

        return ResponseEntity.ok(dtoList);
    }

    /**
     * update Progress.
     *
     * @param process process
     * @return String
     */
    public ResponseEntity<String> updateProgress(PackageUploadProgress process) {
        PackageUploadProgress record = packageUploadProgressMapper.selectByPrimaryKey(process.getId());
        if (record == null) {
            throw new AppException("process not exist.");
        }

        int ret = packageUploadProgressMapper.updateByPrimaryKeySelective(process);
        if (ret > 0) {
            return ResponseEntity.ok("update process success.");
        } else {
            throw new AppException("update process fail.");
        }
    }

    /**
     * delete Progress.
     *
     * @param id process id
     * @return String
     */
    public ResponseEntity<String> deleteProgress(String id) {
        int ret = packageUploadProgressMapper.deleteByPrimaryKey(id);

        if (ret < 0) {
            throw new AppException("delete process fail.");
        } else {
            return ResponseEntity.ok("delete process success.");
        }
    }
}
