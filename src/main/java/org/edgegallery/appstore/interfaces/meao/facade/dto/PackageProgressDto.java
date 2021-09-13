/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.meao.facade.dto;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;

@Getter
@Setter
@NoArgsConstructor
public class PackageProgressDto {
    private String id;

    private String packageId;

    private String meaoId;

    private String status;

    private String progress;

    private Date createTime;

    private String systemName;

    private String url;

    public Date getCreateTime() {
        return createTime == null ? null : (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime == null ? null : (Date) createTime.clone();
    }

    /**
     * transfer PackageUploadProgress to PackageProgressDto.
     *
     * @param progress PackageUploadProgress
     * @return PackageProgressDto
     */
    public PackageProgressDto transferTo(PackageUploadProgress progress) {
        PackageProgressDto dto = new PackageProgressDto();
        dto.setId(progress.getId());
        dto.setPackageId(progress.getPackageId());
        dto.setMeaoId(progress.getMeaoId());
        dto.setStatus(progress.getStatus());
        dto.setProgress(progress.getProgress());
        dto.setCreateTime(progress.getCreateTime());
        return dto;
    }
}
