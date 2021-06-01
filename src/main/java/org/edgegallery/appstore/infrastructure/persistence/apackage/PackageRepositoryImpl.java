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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
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
    public void updateRelease(Release release) {

        AppReleasePo releasePO = packageMapper.findReleaseById(release.getPackageId());
        if (releasePO == null) {
            LOGGER.error("update status error: can not find package by {}", release.getPackageId());
            throw new AppException("update status error: can not find package", ResponseConst.RET_PACKAGE_NOT_FOUND);
        }
        packageMapper.updateRelease(AppReleasePo.of(release));
    }

    @Override
    public Release findReleaseById(String appId, String packageId) {
        AppReleasePo releasePO = packageMapper.findReleaseById(packageId);
        if (releasePO == null || !releasePO.getAppId().equals(appId)) {
            LOGGER.error("find release error: can not find package by {}", packageId);
            throw new AppException("find release error: can not find package", ResponseConst.RET_PACKAGE_NOT_FOUND);
        }
        return releasePO.toDomainModel();
    }

    @Override
    public void storeRelease(Release release) {
        AppReleasePo releasePO = packageMapper.findReleaseById(release.getPackageId());
        if (releasePO != null) {
            LOGGER.error("release {} has existed.", release.getPackageId());
            throw new AppException("release has existed.", ResponseConst.RET_RELEASE_EXIST);
        }
        packageMapper.insertRelease(AppReleasePo.of(release));
    }

    @Override
    public void removeRelease(Release release) {
        AppReleasePo releasePO = packageMapper.findReleaseById(release.getPackageId());
        if (releasePO == null) {
            LOGGER.error("find release error: can not find package by {}", release.getPackageId());
            throw new AppException("find release error: can not find package", ResponseConst.RET_PACKAGE_NOT_FOUND);
        }
        packageMapper.removeByPackageId(release.getPackageId());
    }

    @Override
    public List<Release> findReleaseByUserIdV2(Map<String, Object> params) {
        return packageMapper.findReleaseByUserIdV2(params).stream().map(AppReleasePo::toDomainModel)
            .collect(Collectors.toList());
    }

    @Override
    public List<Release> findReleaseByUserId(String userId) {
        return packageMapper.findReleaseByUserId(userId).stream().map(AppReleasePo::toDomainModel)
            .collect(Collectors.toList());
    }

    @Override
    public Integer countTotalForUserId(Map<String, Object> params) {
        return packageMapper.countTotalForUserId(params);
    }
}
