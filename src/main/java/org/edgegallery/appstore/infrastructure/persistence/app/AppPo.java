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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.infrastructure.persistence.PersistenceObject;

@Getter
@Setter
@Entity
@Table(name = "app_table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppPo implements PersistenceObject<App> {

    public static final String APP_NAME = "appName";

    @Id
    @Column(name = "APPID")
    private String appId;

    @Column(name = "APPNAME")
    private String appName;

    @Column(name = "APPLICATIONTYPE")
    private String applicationType; //applicationType

    @Column(name = "SHORTDESC")
    private String shortDesc; //

    @Column(name = "PROVIDER")
    private String provider; //

    @Column(name = "APPINTRODUCTION")
    private String appIntroduction;

    @Column(name = "DOWNLOADCOUNT")
    private int downloadCount;

    @Column(name = "AFFINITY")
    private String affinity;

    @Column(name = "INDUSTRY")
    private String industry;

    @Column(name = "CONTACT")
    private String contact;

    @Column(name = "USERID")
    private String userId;

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "CREATETIME")
    private String createTime;

    @Column(name = "MODIFYTIME")
    private String modifyTime;

    @Column(name = "SCORE")
    private double score;

    @Column(name = "STATUS")
    private EnumAppStatus status;

    @Override
    public App toDomainModel() {
        return App.builder().appId(appId).appName(appName).provider(provider).createTime(createTime)
            .updateTime(modifyTime).downloadCount(downloadCount).score(score).shortDesc(shortDesc).affinity(affinity)
            .industry(industry).contact(contact).applicationType(applicationType).appIntroduction(appIntroduction)
            .user(new User(userId, userName)).numOfcomment(0).releases(null).status(status).build();
    }

    public AppPo() {
        // empty construct
    }

    static AppPo of(App app) {
        AppPo po = new AppPo();
        po.appId = app.getAppId();
        po.appName = app.getAppName();
        po.applicationType = app.getApplicationType();
        po.shortDesc = app.getShortDesc();
        po.provider = app.getProvider();
        po.appIntroduction = app.getAppIntroduction();
        po.downloadCount = app.getDownloadCount();
        po.userId = app.getUser().getUserId();
        po.userName = app.getUser().getUserName();
        po.createTime = app.getCreateTime();
        po.modifyTime = app.getUpdateTime();
        po.affinity = app.getAffinity();
        po.industry = app.getIndustry();
        po.contact = app.getContact();
        po.score = app.getScore();
        po.status = app.getStatus();
        return po;
    }
}
