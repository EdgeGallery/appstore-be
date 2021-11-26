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

package org.edgegallery.appstore.domain.model.app;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.Entity;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class App implements Entity {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final int MAX_SAME_RELEASES = 5;

    private String appId;

    private String appName;

    private String provider;

    private String createTime;

    private String updateTime;

    private int downloadCount;

    private double score;

    private String shortDesc;

    private String showType;

    private String affinity;

    private String industry;

    private String contact;

    private String applicationType;

    private String appIntroduction;

    private User user;

    private int numOfcomment;

    private EnumAppStatus status;

    private List<Release> releases;

    private String deployMode;

    private boolean isHotApp;

    private boolean experienceAble;

    private boolean isFree = true;

    private double price;

    /**
     * Constructor of App.
     *
     * @param appId id of app.
     * @param release release of app.
     */
    public App(String appId, Release release) {
        this.appId = appId;
        this.appName = release.getAppBasicInfo().getAppName();
        this.shortDesc = release.getAppBasicInfo().getAppDesc();
        this.showType = release.getShowType();
        this.provider = release.getAppBasicInfo().getProvider();
        this.user = release.getUser();
        this.affinity = release.getAffinity();
        this.applicationType = release.getApplicationType();
        this.appIntroduction = release.getAppBasicInfo().getMarkDownContent();
        this.industry = release.getIndustry();
        this.contact = release.getAppBasicInfo().getContact();
        this.status = EnumAppStatus.UnPublish;
        this.releases = Collections.singletonList(release);
        this.deployMode = release.getDeployMode();
        this.experienceAble = release.isExperienceAble();

    }

    public String getUserId() {
        return user.getUserId();
    }

    /**
     * upload function.
     *
     * @param release app release.
     */
    public void upload(Release release) {
        this.shortDesc = release.getAppBasicInfo().getAppDesc();
        this.showType = release.getShowType();
        this.affinity = release.getAffinity();
        this.contact = release.getAppBasicInfo().getContact();
        this.applicationType = release.getApplicationType();
        this.appIntroduction = release.getAppBasicInfo().getMarkDownContent();
        this.deployMode = release.getDeployMode();
        releases.add(release);
    }

    /**
     * check release unique.
     *
     * @param release app release.
     */
    public void checkReleases(Release release) {
        releases.stream().filter(r -> r.getStatus() == EnumPackageStatus.Published).forEach(r1 -> {
            if (release.getAppBasicInfo().getVersion().equals(r1.getAppBasicInfo().getVersion())) {
                try {
                    FileUtils.deleteDirectory(new File(release.getPackageFile().getStorageAddress()).getParentFile());
                } catch (IOException e) {
                    LOGGER.error("Delete the package directory exception: {}", e.getMessage());
                }
                throw new AppException("The same app has existed.", ResponseConst.RET_SAME_APP_EXIST);
            }
        });
        AtomicInteger sameSize = new AtomicInteger();
        releases.stream().filter(r -> r.getStatus() != EnumPackageStatus.Published).forEach(r1 -> {
            if (release.getAppBasicInfo().getVersion().equals(r1.getAppBasicInfo().getVersion())
                && release.getUser().getUserId().equals(r1.getUser().getUserId())
                && sameSize.incrementAndGet() >= MAX_SAME_RELEASES) {
                try {
                    FileUtils.deleteDirectory(new File(release.getPackageFile().getStorageAddress()).getParentFile());
                } catch (IOException e) {
                    LOGGER.error("Delete the package directory exception: {}", e.getMessage());
                }
                throw new AppException("The same unPublished apps have reach the limit.",
                    ResponseConst.RET_SAME_APP_REACH_LIMIT, MAX_SAME_RELEASES);
            }
        });
    }

    public void downLoad() {
        downloadCount = downloadCount + 1;
    }

    // default score is 5, numberOfComment should be 1.
    public void comment(Comment comment) {
        score = (numOfcomment * score + comment.getScore()) / (numOfcomment + 1);
    }

    public Optional<Release> findByPackageId(String packageId) {
        return releases.stream().filter(it -> it.getPackageId().equals(packageId)).findAny();
    }

    public void unPublish(Release release) {
        releases.remove(release);
    }

    public Optional<Release> findLastRelease() {
        return releases.stream().filter(r -> r.getStatus() == EnumPackageStatus.Published)
            .max(Comparator.comparing(Release::getCreateTime));
    }

    public Optional<Release> findLatestRelease() {
        return releases.stream().max(Comparator.comparing(Release::getCreateTime));
    }

    public boolean hasPublishedRelease() {
        return releases.stream().anyMatch(r -> r.getStatus() == EnumPackageStatus.Published);
    }

}
