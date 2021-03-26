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

package org.edgegallery.appstore.application.external.atp;

import org.edgegallery.appstore.application.external.atp.model.AtpTestDto;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AtpService")
public class AtpService implements AtpServiceInterface {

    @Autowired
    private AtpUtil atpUtil;

    @Override
    public String getAtpTaskResult(String token, String taskId) {
        return atpUtil.getTaskStatusFromAtp(taskId, token);
    }

    @Override
    public AtpTestDto createTestTask(Release release, String token) {
        return atpUtil.sendCreatTask2Atp(release.getPackageFile().getStorageAddress(), token);
    }

}
