/*
 *    Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.ValueObject;
import org.edgegallery.appstore.domain.shared.exceptions.PermissionNotAllowedException;
import org.edgegallery.appstore.interfaces.app.facade.AppParam;

@Getter
@Setter
@NoArgsConstructor
public class Release implements ValueObject<Release> {

    private String packageId;

    private String appId;

    private AFile packageFile;

    private AFile icon;

    private Date createTime;

    private String shortDesc;

    private String showType;

    private boolean experienceAble;

    private String affinity;

    private String industry;

    private String applicationType;

    private EnumPackageStatus status;

    private String testTaskId;

    private User user;

    private BasicInfo appBasicInfo;

    private AFile demoVideo;

    private String deployMode;

    private String appInstanceId;

    private String instanceTenentId;

    private String instancePackageId;

    private String errorLog;

    private String startExpTime;

    private String experienceableIp;

    private String mecHost;

    /**
     * Constructor of Release.
     */
    public Release(AFile packageFile, AFile icon, AFile demoVideo, User user, AppParam appParam, String appClass) {
        String random = UUID.randomUUID().toString();
        this.packageId = random.replace("-", "");
        this.packageFile = packageFile;
        this.demoVideo = demoVideo;
        this.icon = icon;
        this.user = user;
        this.createTime = new Date();
        this.shortDesc = appParam.getShortDesc();
        this.showType = appParam.getShowType();
        this.applicationType = appParam.getApplicationType();
        this.industry = appParam.getIndustry();
        this.affinity = appParam.getAffinity();
        this.status = EnumPackageStatus.Upload;
        this.deployMode = appClass;
        this.experienceAble = appParam.isExperienceAble();
        appBasicInfo = new BasicInfo().load(packageFile.getStorageAddress());
    }

    public void setAppIdValue(String appId) {
        this.appId = appId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageId);
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
        return Objects.equals(packageId, release.packageId);
    }

    @Override
    public boolean sameValueAs(Release other) {
        return this.equals(other);
    }

    /**
     * check operator permission.
     *
     * @param user the user info of delete package.
     */
    public void checkPermission(User user) {
        if (!this.getUser().getUserId().equals(user.getUserId())) {
            throw new PermissionNotAllowedException("operator do not have permission",
                ResponseConst.RET_NO_ACCESS_DELETE_PACKAGE, user.getUserName());

        }
    }

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }
}
