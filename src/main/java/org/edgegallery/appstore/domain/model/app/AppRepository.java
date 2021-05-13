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

package org.edgegallery.appstore.domain.model.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.PageCriteria;

public interface AppRepository {

    void store(App app);

    Optional<App> find(String appId);

    String generateAppId();

    Optional<App> findByAppNameAndProvider(String appName, String provider);

    void remove(String appId);

    List<App> queryV2(Map<String, Object> params);

    Page<App> query(AppPageCriteria appPageCriteria);

    Page<Release> findAllWithPagination(PageCriteria pageCriteria);

    long countTotalV2(Map<String, Object> params);
}
