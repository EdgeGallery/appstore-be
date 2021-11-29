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
import org.edgegallery.appstore.domain.model.releases.EnumExperienceStatus;
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
    private String packageAddress;

    @Column(name = "iconAddress")
    private String iconAddress;

    @Column(name = "SIZE")
    private String size;

    @Column(name = "fileStructure")
    private String fileStructure;

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
    private String applicationType;

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

    @Column(name = "APPINSTANCEID")
    private String appInstanceId;

    @Column(name = "INSTANCETENENTID")
    private String instanceTenentId;

    @Column(name = "INSTANCEPACKAGEID")
    private String instancePackageId;

    @Column(name = "EXPERIENCEABLE")
    private boolean experienceAble;

    @Column(name = "STARTEXPTIME")
    private String startExpTime;

    @Column(name = "EXPERIENCEABLEIP")
    private String experienceableIp;

    @Column(name = "MECHOST")
    private String mecHost;

    @Column(name = "EXPERIENCESTATUS")
    private int experienceStatus;


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
     * @return AppReleasePo
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
        po.shortDesc = pack.getAppBasicInfo().getAppDesc();
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
        po.deployMode = pack.getDeployMode();
        po.appInstanceId = pack.getAppInstanceId();
        po.instanceTenentId = pack.getInstanceTenentId();
        po.instancePackageId = pack.getInstancePackageId();
        po.experienceAble = pack.isExperienceAble();
        po.startExpTime = pack.getStartExpTime();
        po.setExperienceableIp(pack.getExperienceableIp());
        po.setMecHost(pack.getMecHost());
        return po;
    }

    /**
     * transfer to Release.
     *
     * @return Release
     */
    public Release toDomainModel() {
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setAppName(appName);
        basicInfo.setProvider(provider);
        basicInfo.setVersion(version);
        basicInfo.setContact(contact);
        basicInfo.setFileStructure(fileStructure);
        basicInfo.setMarkDownContent(markDownContent);
        basicInfo.setAppDesc(shortDesc);
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
        release.setShowType(showType);
        release.setAffinity(affinity);
        release.setApplicationType(applicationType);
        release.setIndustry(industry);
        release.setUser(new User(userId, userName));
        release.setAppBasicInfo(basicInfo);
        release.setStatus(EnumPackageStatus.valueOf(status));
        release.setTestTaskId(testTaskId);
        release.setDeployMode(deployMode);
        release.setAppInstanceId(appInstanceId);
        release.setExperienceAble(experienceAble);
        return release;
    }

    /**
     * init experience param.
     *
     */
    public void initialConfig() {
        this.appInstanceId = null;
        this.instanceTenentId = null;
        this.startExpTime = null;
    }

}

