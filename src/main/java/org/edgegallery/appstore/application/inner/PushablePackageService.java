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

package org.edgegallery.appstore.application.inner;

import java.util.List;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PushablePackageRepository;
import org.edgegallery.appstore.infrastructure.persistence.appstore.ExAppStorePo;
import org.edgegallery.appstore.infrastructure.persistence.appstore.ExAppStoreRepository;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("PushablePackageService")
public class PushablePackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushablePackageService.class);

    private static final String NOTICE_API = "";

    @Autowired
    private PushablePackageRepository pushablePackageRepository;

    @Autowired
    private ExAppStoreRepository exAppStoreRepository;

    public List<PushablePackageDto> queryAllPushablePackages() {
        return pushablePackageRepository.queryAllPushablePackages();
    }

    public PushablePackageDto getPushablePackage(String packageId) {
        return pushablePackageRepository.getPushablePackages(packageId)
            .orElseThrow(() -> new EntityNotFoundException(PushablePackageDto.class, packageId));
    }

    /**
     * push package to target appstore.
     *
     * @param packageId package id
     * @param targetAppStore target appstore
     */
    public void pushPackage(String packageId, PushTargetAppStoreDto targetAppStore) {
        final PushablePackageDto packageDto = pushablePackageRepository.getPushablePackages(packageId)
            .orElseThrow(() -> new EntityNotFoundException(PushablePackageDto.class, packageId));
        LOGGER.info("push package {}", packageDto.getPackageId());
        targetAppStore.getTargetPlatform().forEach(platformId -> {
            // TODO:  platform by Id
            ExAppStorePo appStorePo = exAppStoreRepository.findAppStoreById(platformId);
            String url = appStorePo.getUrl() + NOTICE_API;
            LOGGER.info(url);
        });
    }
}
