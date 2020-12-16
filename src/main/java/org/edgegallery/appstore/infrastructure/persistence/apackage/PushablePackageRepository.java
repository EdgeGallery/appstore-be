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
import java.util.List;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PushablePackageRepository {

    @Autowired
    private PushablePackageMapper pushablePackageMapper;

    public List<PushablePackageDto> queryAllPushablePackages() {
        List<AppReleasePo> apps = pushablePackageMapper.getAllPushablePackages(0, 1000);
        List<PushablePackageDto> packages = new ArrayList<>();
        apps.forEach(app -> packages.add(new PushablePackageDto(app)));
        return packages;
    }

    public PushablePackageDto getPushablePackages(String packageId) {
        AppReleasePo appReleasePo = pushablePackageMapper.getPushablePackages(packageId)
            .orElseThrow(() -> new EntityNotFoundException(PushablePackageDto.class, packageId));
        return new PushablePackageDto(appReleasePo);
    }
}
