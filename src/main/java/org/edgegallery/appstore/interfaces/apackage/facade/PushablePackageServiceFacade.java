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

package org.edgegallery.appstore.interfaces.apackage.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.edgegallery.appstore.application.inner.PushablePackageService;
import org.edgegallery.appstore.domain.model.releases.UnknownReleaseExecption;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("PushablePackageServiceFacade")
public class PushablePackageServiceFacade {

    @Autowired
    private PushablePackageService pushablePackageService;

    public ResponseEntity<List<PushablePackageDto>> queryAllPushablePackages() {
        List<PushablePackageDto> list = pushablePackageService.queryAllPushablePackages();
        if (list == null) {
            return ResponseEntity.ok(new ArrayList<>());
        } else {
            return ResponseEntity.ok(list);
        }
    }

    public ResponseEntity<PushablePackageDto> getPushablePackage(String packageId) {
        PushablePackageDto dto = pushablePackageService.getPushablePackage(packageId);
        if (dto == null) {
            new UnknownReleaseExecption(packageId);
        }
        return ResponseEntity.ok(dto);
    }

    public void pushPackage(String packageId, PushTargetAppStoreDto targetAppStore) {
        // find the package
        pushablePackageService.pushPackage(packageId, targetAppStore);
    }
}
