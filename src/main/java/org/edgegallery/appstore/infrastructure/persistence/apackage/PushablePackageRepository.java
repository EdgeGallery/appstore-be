/* Copyright 2020 Huawei Technologies Co., Ltd.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.edgegallery.appstore.config.ApplicationContext;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PushablePackageRepository {

    @Autowired
    private PushablePackageMapper pushablePackageMapper;

    @Autowired
    private ApplicationContext context;

    /**
     * query all of the pushable packages.
     *
     * @return
     */
    public List<PushablePackageDto> queryAllPushablePackages() {
        List<PushablePackageAndAppVo> apps = pushablePackageMapper.getAllPushablePackages(0, 1000);
        List<PushablePackageDto> packages = new ArrayList<>();
        apps.forEach(app -> packages.add(new PushablePackageDto(app, context.atpReportUrl)));
        return packages;
    }

    /**
     * find one package by id.
     *
     * @return
     */
    public PushablePackageDto getPushablePackages(String packageId) {
        PushablePackageAndAppVo appReleasePo = pushablePackageMapper.getPushablePackages(packageId)
            .orElseThrow(() -> new EntityNotFoundException(PushablePackageDto.class, packageId));
        return new PushablePackageDto(appReleasePo, context.atpReportUrl);
    }

    /**
     * to save or update push log.
     */
    @Transactional
    public void updateOrSavePushLog(PushablePackageDto packagePo) {
        PushablePackagePo po = pushablePackageMapper.findPushTableByPackageId(packagePo.getPackageId());
        if (po == null) {
            po = new PushablePackagePo(packagePo.getPackageId(), packagePo.getAtpTestReportUrl(), new Date(),
                packagePo.getPushTimes() + 1, packagePo.getSourcePlatform());
            pushablePackageMapper.savePushTable(po);
        } else {
            po.setPushTimes(po.getPushTimes() + 1);
            po.setLatestPushTime(new Date());
            po.setSourcePlatform(packagePo.getSourcePlatform());
            pushablePackageMapper.updatePushTable(po);
        }
    }
}
