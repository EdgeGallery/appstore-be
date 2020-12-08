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

import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PackageRepositoryImpl implements PackageRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageRepositoryImpl.class);

    @Autowired
    private PackageMapper packageMapper;

    @Override
    public void updateStatus(String packageId, EnumPackageStatus status) {

        AppReleasePO releasePO = packageMapper.findReleaseById(packageId);
        if (releasePO == null) {
            LOGGER.error("update status error: can not find package by {}", packageId);
            throw new EntityNotFoundException("update status error: can not find package");
        }
        releasePO.setStatus(status.toString());
        packageMapper.updateRelease(releasePO);
    }

    @Override
    public Release findReleaseById(String appId, String packageId) {
        AppReleasePO releasePO = packageMapper.findReleaseById(packageId);
        if (releasePO == null || !releasePO.getAppId().equals(appId)) {
            LOGGER.error("find release error: can not find package by {}", packageId);
            throw new EntityNotFoundException("find release error: can not find package");
        }
        return releasePO.toDomainModel();
    }
}
