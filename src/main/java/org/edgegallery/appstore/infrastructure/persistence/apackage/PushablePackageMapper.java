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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.AppStoreDto;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PushablePackageMapper {

    List<PushablePackageAndAppVo> getAllPushablePackagesV2(Map<String, Object> params);

    List<PushablePackageAndAppVo> getAllPushablePackages(Map<String, Object> params);

    Integer getAllPushablePackagesCount(Map<String, Object> params);

    Optional<PushablePackageAndAppVo> getPushablePackages(String packageId);

    Optional<AppStoreDto> findAppStoreById(String appStoreId);

    void savePushTable(PushablePackagePo pushablePackagePo);

    void updatePushTable(PushablePackagePo pushablePackagePo);

    PushablePackagePo findPushTableByPackageId(String packageId);

}
