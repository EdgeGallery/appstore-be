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

package org.edgegallery.appstore.infrastructure.persistence.appstore;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.model.appstore.AppStore;
import org.edgegallery.appstore.domain.model.appstore.AppStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AppStoreRepositoryImpl implements AppStoreRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStoreRepositoryImpl.class);

    @Autowired
    private AppStoreMapper appStoreMapper;

    /**
     * add app store.
     */
    public String addAppStore(AppStore appStore) {
        AppStorePo appStorePo = AppStorePo.of(appStore);
        String uuid = UUID.randomUUID().toString();
        appStorePo.setAppStoreId(uuid);
        if (appStoreMapper.addAppStore(appStorePo) == 1) {
            return uuid;
        }

        return null;
    }

    public int deleteAppStoreById(String appStoreId) {
        return appStoreMapper.deleteAppStoreById(appStoreId);
    }

    public int updateAppStoreById(AppStore appStore) {
        return appStoreMapper.updateAppStoreById(AppStorePo.of(appStore));
    }

    /**
     * query app store by ID.
     */
    public AppStore queryAppStoreById(String appStoreId) {
        AppStorePo appStorePo = appStoreMapper.queryAppStoreById(appStoreId);
        if (appStorePo == null) {
            LOGGER.warn("nothing was queried by id : " + appStoreId);
            return null;
        }
        return appStorePo.toAppStore();
    }

    /**
     * query app stores.
     */
    public List<AppStore> queryAppStores(AppStore appStore) {
        AppStorePo appStorePo = new AppStorePo();
        if (appStore != null) {
            appStorePo.setAppStoreName(appStore.getAppStoreName());
            appStorePo.setCompany(appStore.getCompany());
        }

        return appStoreMapper.queryAppStores(appStorePo).stream().map(AppStorePo::toAppStore).collect(
            Collectors.toList());
    }
}
