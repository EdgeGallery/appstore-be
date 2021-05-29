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

package org.edgegallery.appstore.infrastructure.persistence.apackage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.BasicInfo;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;

@Getter
@Setter
@Entity
@Table(name = "catalog_package_table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppReleasePo {
    @Id
    @Column(name = "packageid")
    private String packageId;

    @Column(name = "packageAddress")
    private String packageAddress; //packageAddress

    @Column(name = "iconAddress")
    private String iconAddress; //iconAddress

    @Column(name = "SIZE")
    private String size;

    @Column(name = "fileStructure")
    private String fileStructure; //Tree

    @Column(name = "CREATETIME")
    private Date createTime;

    @Column(name = "SHORTDESC")
    private String shortDesc;

    @Column(name = "SHOWTYPE")
    private String showType;

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

    @Column(name = "TESTTASKID")
    private String testTaskId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "DEMOVIDEOADDRESS")
    private String demoVideoAddress;

    @Column(name = "DEPLOYMODE")
    private String deployMode;

    @Column(name = "ISHOTAPP")
    private boolean isHotApp;

    public AppReleasePo() {
        // empty constructor of AppReleasePO
    }

    public Date getCreateTime() {
        return (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = (Date) createTime.clone();
    }
    
    /**
     * transfer Release to AppRelease.
     *
     * @param pack object of Release.
     * @return
     */
    public static AppReleasePo of(Release pack) {
        AppReleasePo po = new AppReleasePo();
        po.packageId = pack.getPackageId();
        po.packageAddress = pack.getPackageFile().getStorageAddress();
        po.iconAddress = pack.getIcon().getStorageAddress();
        if (pack.getDemoVideo() != null) {
            po.demoVideoAddress = pack.getDemoVideo().getStorageAddress();
        }
        po.size = pack.getPackageFile().getSize();
        po.fileStructure = pack.getAppBasicInfo().getFileStructure();
        po.createTime = pack.getCreateTime();
        po.shortDesc = pack.getShortDesc();
        po.showType = pack.getShowType();
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
        po.testTaskId = pack.getTestTaskId();
        po.status = pack.getStatus().toString();
        po.provider = pack.getAppBasicInfo().getProvider();
        po.deployMode = pack.getDeployMode();
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
        Release release = new Release();

        if (demoVideoAddress != null) {
            release.setDemoVideo(new AFile(new File(demoVideoAddress).getName(), demoVideoAddress));
        }
        release.setPackageFile(new AFile(new File(packageAddress).getName(), packageAddress));
        release.getPackageFile().setFileSize(new File(packageAddress).length());
        release.setAppId(appId);
        release.setPackageId(packageId);
        release.setIcon(new AFile(new File(iconAddress).getName(), iconAddress));
        release.setCreateTime(createTime);
        release.setShortDesc(shortDesc);
        release.setShowType(showType);
        release.setAffinity(affinity);
        release.setApplicationType(applicationType);
        release.setIndustry(industry);
        release.setUser(new User(userId, userName));
        release.setAppBasicInfo(basicInfo);
        release.setStatus(EnumPackageStatus.valueOf(status));
        release.setTestTaskId(testTaskId);
        release.setDeployMode(deployMode);
        return release;
    }
}

