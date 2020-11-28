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

package org.edgegallery.appstore.infrastructure.persistence.app;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.BasicInfo;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;

@Getter
@Setter
@Entity
@Table(name = "catalog_package_table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppReleasePO {
    @Id
    @Column(name = "versionID")
    private String versionID;

    @Column(name = "packageAddress")
    private String packageAddress; //packageAddress

    @Column(name = "iconAddress")
    private String iconAddress; //iconAddress

    @Column(name = "SIZE")
    private String size;

    @Column(name = "fileStructure")
    private String fileStructure; //Tree

    @Column(name = "CREATETIME")
    private String createTime;

    @Column(name = "SHORTDESC")
    private String shortDesc;

    @Column(name = "appName")
    private String appName;

    @Column(name = "VERSION")
    private String version;

    @Column(name = "PROVIDER")
    private String provider;

    @Column(name = "applicationType")
    private String applicationType; //applicationType

    @Column(name = "markdownContent")
    private String markDownContent;

    @Column(name = "AFFINITY")
    private String affinity;

    @Column(name = "INDUSTRY")
    private String industry;

    @Column(name = "CONTACT")
    private String contact;

    @Column(name = "APPID")
    private String appId;

    @Column(name = "USERID")
    private String userId;

    @Column(name = "USERNAME")
    private String userName;

    public AppReleasePO() {
        // empty constructor of AppReleasePO
    }

    /**
     * transfer Release to AppRelease.
     *
     * @param pack object of Release.
     * @return
     */
    public static AppReleasePO of(Release pack) {
        AppReleasePO po = new AppReleasePO();
        po.versionID = pack.getVersionId();
        po.packageAddress = pack.getPackageFile().getStorageAddress();
        po.iconAddress = pack.getIcon().getStorageAddress();
        po.size = pack.getPackageFile().getSize();
        po.fileStructure = pack.getAppBasicInfo().getFileStructure();
        po.createTime = pack.getCreateTime();
        po.shortDesc = pack.getShortDesc();
        po.appName = pack.getAppBasicInfo().getAppName();
        po.version = pack.getAppBasicInfo().getVersion();
        po.applicationType = pack.getApplicationType();
        po.markDownContent = pack.getAppBasicInfo().getMarkDownContent();
        po.affinity = pack.getAffinity();
        po.industry = pack.getIndustry();
        po.contact = pack.getAppBasicInfo().getContact();
        po.appId = pack.getAppId();
        po.userId = pack.getUser().getUserId();
        po.userName = pack.getUser().getUserName();
        po.provider = pack.getAppBasicInfo().getProvider();
        return po;
    }

    /**
     * transfer to Release.
     *
     * @return
     */
    public Release toDomainModel() {
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setAppName(appName);
        basicInfo.setProvider(provider);
        basicInfo.setVersion(version);
        basicInfo.setContact(contact);
        basicInfo.setFileStructure(fileStructure);
        basicInfo.setMarkDownContent(markDownContent);
        return Release.builder()
                .packageFile(new AFile(new File(packageAddress).getName(), packageAddress))
                .appId(appId)
                .versionId(versionID)
                .icon(new AFile(new File(iconAddress).getName(), iconAddress))
                .createTime(createTime)
                .shortDesc(shortDesc)
                .affinity(affinity)
                .applicationType(applicationType)
                .industry(industry)
                .user(new User(userId, userName))
                .appBasicInfo(basicInfo)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppReleasePO that = (AppReleasePO) o;
        return Objects.equals(versionID, that.versionID) && Objects.equals(packageAddress, that.packageAddress)
            && Objects.equals(iconAddress, that.iconAddress) && Objects.equals(size, that.size) && Objects.equals(
            fileStructure, that.fileStructure) && Objects.equals(createTime, that.createTime) && Objects.equals(
            shortDesc, that.shortDesc) && Objects.equals(appName, that.appName) && Objects.equals(version, that.version)
            && Objects.equals(applicationType, that.applicationType) && Objects.equals(markDownContent,
            that.markDownContent) && Objects.equals(affinity, that.affinity) && Objects.equals(appId, that.appId)
            && Objects.equals(userId, that.userId) && Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionID, packageAddress, iconAddress, size, fileStructure, createTime, shortDesc, appName,
            version, applicationType, markDownContent, affinity, appId, userId, userName);
    }
}

