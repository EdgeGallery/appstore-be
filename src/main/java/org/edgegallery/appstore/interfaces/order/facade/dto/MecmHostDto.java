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

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MecmHostDto {
    private String mechostIp;

    private String mechostName;

    private String mechostCity;

    private String vim;

    private String affinity;

    /**
     * convert from data map.
     *
     * @param mecHostInfoMap mec host data map
     * @return dto object
     */
    public static MecmHostDto fromMap(Map<String, Object> mecHostInfoMap) {
        return new MecmHostDto((String) mecHostInfoMap.get("mechostIp"), (String) mecHostInfoMap.get("mechostName"),
            (String) mecHostInfoMap.get("mechostCity"), (String) mecHostInfoMap.get("vim"),
            (String) mecHostInfoMap.get("affinity"));
    }
}
