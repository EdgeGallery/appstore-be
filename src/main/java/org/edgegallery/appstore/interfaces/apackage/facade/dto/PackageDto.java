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

package org.edgegallery.appstore.interfaces.apackage.facade.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.Release;

@Getter
@Setter
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageDto {

    private String packageId;

    private String size;

    private String format;

    private Date createTime;

    private String name;

    private String version;

    private String type;

    private String details;

    private String affinity;

    private String industry;

    private String contact;

    private String appId;

    private String userId;

    private String userName;

    private EnumPackageStatus status;

    private String shortDesc;

    private String testTaskId;

    private String provider;

    public PackageDto() {
        // empty construct function
    }

    /**
     * Transfer Release object to PackageDto object.
     *
     * @param release Release object.
     * @return
     */
    public static PackageDto of(Release release) {
        PackageDto dto = new PackageDto();
        dto.packageId = release.getPackageId();
        dto.size = release.getPackageFile().getSize();
        dto.format = release.getAppBasicInfo().getFileStructure();
        dto.createTime = release.getCreateTime();
        dto.name = release.getAppBasicInfo().getAppName();
        dto.version = release.getAppBasicInfo().getVersion();
        dto.type = release.getApplicationType();
        dto.details = release.getAppBasicInfo().getMarkDownContent();
        dto.affinity = release.getAffinity();
        dto.industry = release.getIndustry();
        dto.contact = release.getAppBasicInfo().getContact();
        dto.appId = release.getAppId();
        dto.userId = release.getUser().getUserId();
        dto.userName = release.getUser().getUserName();
        dto.status = release.getStatus();
        dto.shortDesc = release.getShortDesc();
        dto.testTaskId = release.getTestTaskId();
        dto.provider = release.getAppBasicInfo().getProvider();
        return dto;
    }
}
