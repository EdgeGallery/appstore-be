/* Copyright 2020-2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.infrastructure.persistence.apackage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.edgegallery.appstore.config.ApplicationContext;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PushablePackageRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(PushablePackageRepository.class);

    @Autowired
    private PushablePackageMapper pushablePackageMapper;

    @Autowired
    private ApplicationContext context;

    /**
     * query all of the pushable packages.
     *
     * @return pushable packages list
     */
    public Page<PushablePackageDto> queryAllPushablePackagesV2(int limit, int offset, String appName, String sortType,
        String sortItem, String shareType) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", limit);
        params.put("offset", offset);
        params.put("appName", appName);
        if ("push".equals(shareType)) {
            params.put("latestPushTime", "latestPushTime");
        } else {
            params.put("createTime", "createTime");
        }
        params.put("sortItem", sortItem);
        params.put("sortType", sortType);
        long total = pushablePackageMapper.getAllPushablePackagesCount(params);
        List<PushablePackageAndAppVo> apps = pushablePackageMapper.getAllPushablePackagesV2(params);
        List<PushablePackageDto> packages = new ArrayList<>();
        apps.forEach(app -> packages.add(new PushablePackageDto(app, context.atpReportUrl)));
        return new Page<>(packages, limit, offset, total);
    }

    /**
     * query all of the pushable packages.
     *
     * @return pushable packages list
     */
    public List<PushablePackageDto> queryAllPushablePackages(String appName, String sortType,
        String sortItem, String shareType) {
        Map<String, Object> params = new HashMap<>();
        params.put("appName", appName);
        if ("push".equals(shareType)) {
            params.put("latestPushTime", "latestPushTime");
        } else {
            params.put("createTime", "createTime");
        }
        params.put("sortItem", sortItem);
        params.put("sortType", sortType);
        List<PushablePackageAndAppVo> apps = pushablePackageMapper.getAllPushablePackages(params);
        List<PushablePackageDto> packages = new ArrayList<>();
        apps.forEach(app -> packages.add(new PushablePackageDto(app, context.atpReportUrl)));
        return packages;
    }


    /**
     * find one package by id.
     *
     * @return PushablePackageDto
     */
    public PushablePackageDto getPushablePackages(String packageId) {
        PushablePackageAndAppVo appReleasePo = pushablePackageMapper.getPushablePackages(packageId)
            .orElseThrow(() -> new EntityNotFoundException(PushablePackageDto.class, packageId,
                ResponseConst.RET_PACKAGE_NOT_FOUND));
        LOGGER.warn("sourcePlatform is:", appReleasePo.getSourcePlatform());
        return new PushablePackageDto(appReleasePo, context.atpReportUrl);
    }

    /**
     * to save or update push log.
     */
    public void updateOrSavePushLog(PushablePackageDto packagePo) {
        PushablePackagePo po = pushablePackageMapper.findPushTableByPackageId(packagePo.getPackageId());
        if (po == null) {
            po = new PushablePackagePo(packagePo.getPackageId(), packagePo.getAtpTestReportUrl(), new Date(),
                packagePo.getPushTimes() + 1, packagePo.getSourcePlatform());
            pushablePackageMapper.savePushTable(po);
        } else {
            po.setPushTimes(po.getPushTimes() + 1);
            po.setLatestPushTime(Timestamp.valueOf(
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE).format(new Date())));
            po.setSourcePlatform(packagePo.getSourcePlatform());
            pushablePackageMapper.updatePushTable(po);
        }
    }

    /**
     * delete one package by id.
     *
     * @param packageId package id
     */
    public void deletePushablePackages(String packageId) {
        pushablePackageMapper.deletePushablePackages(packageId);
    }
}
