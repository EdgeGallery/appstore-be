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
import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PackageMapper {

    AppReleasePo findReleaseById(String packageId);

    void updateRelease(AppReleasePo releasePo);

    void insertRelease(AppReleasePo releasePo);

    void removeByPackageId(String packageId);

    void removeReleasesByAppId(String appId);

    List<AppReleasePo> findAllByAppId(String appId);

    List<AppReleasePo> findAllWithPagination(PageCriteria pageCriteria);

    Integer countTotalForReleases(PageCriteria pageCriteria);

    List<AppReleasePo> findReleaseByUserIdV2(Map<String, Object> params);

    List<AppReleasePo> findReleaseByUserId(String userId);

    Integer countTotalForUserId(Map<String, Object> params);

    void updateAppInstanceApp(AppReleasePo releasePo);

}
