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

package org.edgegallery.appstore.infrastructure.persistence.app;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.appstore.domain.model.app.AppPageCriteria;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface AppMapper {

    Optional<AppPO> findByAppId(String appId);

    Optional<AppPO> findByAppName(String appName);

    void update(AppPO appPO);

    void insert(AppPO appPO);

    void remove(String appId);

    Integer countTotal(AppPageCriteria appPageCriteria);

    List<AppPO> findAllWithAppPagination(AppPageCriteria appPageCriteria);

    void insertRelease(AppReleasePO packagePo);

    void removeReleasesByAppId(String appId);

    List<AppReleasePO> findAllByAppId(String appId);

    Integer countTotalForReleases(PageCriteria pageCriteria);

    List<AppReleasePO> findAllWithPagination(PageCriteria pageCriteria);

    void removeByVersionId(String versionId);
}
