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

package org.edgegallery.appstore.interfaces.apackage.facade.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.domain.shared.Entity;
import org.edgegallery.appstore.infrastructure.persistence.apackage.AppReleasePo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PushablePackageDto implements Entity {

    private String appId;

    private String packageId;

    private String name;

    private String provider;

    private String version;

    private String atpTestStatus;

    private String atpTestTaskId;

    private String atpTestReportUrl;

    private String latestPushTime;

    private Integer pushTimes;

    private List<String> targetPlatform;

    private String affinity;

    private String shortDesc;

    private String industry;

    private String type;

    /**
     * init this object by AppReleasePo.
     *
     * @param appReleasePo
     */
    public PushablePackageDto(AppReleasePo appReleasePo) {
        this.appId = appReleasePo.getAppId();
        this.affinity = appReleasePo.getAffinity();
        this.atpTestTaskId = appReleasePo.getTestTaskId();
        this.shortDesc = appReleasePo.getShortDesc();
        this.industry = appReleasePo.getIndustry();
        this.name = appReleasePo.getAppName();
        this.packageId = appReleasePo.getPackageId();
        this.provider = appReleasePo.getProvider();
        this.type = appReleasePo.getApplicationType();
        this.version = appReleasePo.getVersion();
    }

}
