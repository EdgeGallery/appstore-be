/* Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.order.facade.dto;

import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.order.AppOrderStatInfo;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class TopOrderAppResultDto {
    private String appId;

    private String appName;

    private double orderAmount;

    /**
     * convert entity to dto object.
     *
     * @param appOrderStatInfo entity object
     * @return dto object
     */
    public static TopOrderAppResultDto of(AppOrderStatInfo appOrderStatInfo) {
        TopOrderAppResultDto dto = new TopOrderAppResultDto();
        BeanUtils.copyProperties(appOrderStatInfo, dto);
        return dto;
    }
}
