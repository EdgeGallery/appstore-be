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

package org.edgegallery.appstore.domain.model.appstore;

import java.util.List;
import java.util.Map;

public interface AppStoreRepository {
    String addAppStore(AppStore appStore);

    int deleteAppStoreById(String appStoreId);

    int updateAppStoreById(AppStore appStore);

    AppStore queryAppStoreById(String appStoreId);

    List<AppStore> queryAppStoresV2(Map<String, Object> params);

    Integer getAllAppstoreCount(String appStoreName);

    List<AppStore> queryAppStores(AppStore appStore);
}
