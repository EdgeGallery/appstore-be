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

package org.edgegallery.appstore.domain.model.app;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.Entity;

public class App implements Entity {

    private String appId;

    private String appName;

    private String provider;

    private String createTime;

    private String updateTime;

    private int downloadCount;

    private double score;

    private String shortDesc;

    private String affinity;

    private String industry;

    private String contact;

    private String applicationType;

    private String appIntroduction;

    private User user;

    private int numOfcomment;

    private List<Release> releases;

    /**
     * Constructor of App.
     *
     * @param appId id of app.
     * @param release release of app.
     */
    public App(String appId, Release release) {
        this.appId = appId;
        this.appName = release.getAppBasicInfo().getAppName();
        this.shortDesc = release.getShortDesc();
        this.provider = release.getAppBasicInfo().getProvider();
        this.user = release.getUser();
        this.affinity = release.getAffinity();
        this.applicationType = release.getApplicationType();
        this.appIntroduction = release.getAppBasicInfo().getMarkDownContent();
        this.industry = release.getIndustry();
        this.contact = release.getAppBasicInfo().getContact();
        this.releases = Collections.singletonList(release);
    }

    /**
     * Constructor of App.
     *
     * @param builder builder of app
     */
    public App(Builder builder) {
        this.appId = builder.appId;
        this.appName = builder.appName;
        this.provider = builder.provider;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
        this.downloadCount = builder.downloadCount;
        this.score = builder.score;
        this.shortDesc = builder.shortDesc;
        this.affinity = builder.affinity;
        this.industry = builder.industry;
        this.contact = builder.contact;
        this.applicationType = builder.applicationType;
        this.appIntroduction = builder.appIntroduction;
        this.user = builder.user;
    }

    public String getAppName() {
        return appName;
    }

    public String getProvider() {
        return provider;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public double getScore() {
        return score;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public String getAffinity() {
        return affinity;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getAppIntroduction() {
        return appIntroduction;
    }

    public String getUserId() {
        return user.getUserId();
    }

    public List<Release> getReleases() {
        return releases;
    }

    public void setReleases(List<Release> releases) {
        this.releases = releases;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setNumOfcomment(int numOfcomment) {
        this.numOfcomment = numOfcomment;
    }

    /**
     * upload function.
     *
     * @param release app release.
     */
    public void upload(Release release) {
        if (!provider.equals(release.getAppBasicInfo().getProvider())) {
            throw new IllegalArgumentException();
        }
        this.shortDesc = release.getShortDesc();
        this.affinity = release.getAffinity();
        this.contact = release.getAppBasicInfo().getContact();
        this.applicationType = release.getApplicationType();
        this.appIntroduction = release.getAppBasicInfo().getMarkDownContent();
        releases.add(release);
    }

    public String getAppId() {
        return appId;
    }

    public void downLoad() {
        downloadCount = downloadCount + 1;
    }

    // default score is 5, numberOfComment should be 1.
    public void comment(Comment comment) {
        score = (numOfcomment * score + comment.getScore()) / (numOfcomment + 1);
    }

    public Optional<Release> findByVersion(String packageId) {
        return releases.stream().filter(it -> it.getVersionId().equals(packageId)).findAny();
    }

    public void unPublish(Release release) {
        releases.remove(release);
    }

    public Optional<Release> findLastRelease() {
        return releases.stream().max(Comparator.comparing(Release::getCreateTime));
    }

    public User getUser() {
        return user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String appId;
        private String appName;
        private String provider;
        private String createTime;
        private String updateTime;
        private int downloadCount;
        private double score;
        private String shortDesc;
        private String affinity;
        private String industry;
        private String contact;
        private String applicationType;
        private String appIntroduction;
        private User user;

        private Builder() {
            // private construct
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder setProvider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder setCreateTime(String createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder setDownloadCount(int downloadCount) {
            this.downloadCount = downloadCount;
            return this;
        }

        public Builder setScore(double score) {
            this.score = score;
            return this;
        }

        public Builder setShortDesc(String shortDesc) {
            this.shortDesc = shortDesc;
            return this;
        }

        public Builder setAffinity(String affinity) {
            this.affinity = affinity;
            return this;
        }

        public Builder setIndustry(String industry) {
            this.industry = industry;
            return this;
        }

        public Builder setContact(String contact) {
            this.contact = contact;
            return this;
        }

        public Builder setApplicationType(String applicationType) {
            this.applicationType = applicationType;
            return this;
        }

        public Builder setAppIntroduction(String appIntroduction) {
            this.appIntroduction = appIntroduction;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public App build() {
            return new App(this);
        }
    }

}
