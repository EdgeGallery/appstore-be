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

package org.edgegallery.appstore.domain.model.releases;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.ValueObject;
import org.edgegallery.appstore.domain.shared.exceptions.PermissionNotAllowedException;
import org.edgegallery.appstore.interfaces.app.facade.AppParam;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Release implements ValueObject<Release> {

    private String versionId;

    private String appId;

    private AFile packageFile;

    private AFile icon;

    private String createTime;

    private String shortDesc;

    private String affinity;

    private String industry;

    private String applicationType;

    private User user;

    private BasicInfo appBasicInfo;

    public void setAppIdValue(String appId) {
        this.appId = appId;
    }

    /**
     * Constructor of Release.
     */
    public Release(AFile packageFile, AFile icon, User user, AppParam appParam) {
        String random = UUID.randomUUID().toString();
        this.versionId = random.replace("-", "");
        this.packageFile = packageFile;
        this.icon = icon;
        this.user = user;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.createTime = simpleDateFormat.format(new Date());
        this.shortDesc = appParam.getShortDesc();
        this.applicationType = appParam.getApplicationType();
        this.industry = appParam.getIndustry();
        this.affinity = appParam.getAffinity();
        appBasicInfo = new BasicInfo().load(packageFile.getStorageAddress());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Release release = (Release) o;
        return Objects.equals(versionId, release.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionId);
    }

    @Override
    public boolean sameValueAs(Release other) {
        return this.equals(other);
    }

    /**
     * check operator permission.
     *
     * @param operatorId operator id
     */
    public void checkPermission(String operatorId) {
        if (!this.getUser().getUserId().equals(operatorId)) {
            throw new PermissionNotAllowedException("operator do not have permission");
        }
    }
}
