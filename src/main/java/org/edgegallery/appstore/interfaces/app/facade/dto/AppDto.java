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

package org.edgegallery.appstore.interfaces.app.facade.dto;

import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;

@Getter
@Setter
public class AppDto {
    private String appId;

    private String iconUrl;

    private String name;

    private String provider;

    private String type;

    private String shortDesc;

    private String createTime;

    private String details;

    private int downloadCount;

    private String affinity;

    private String industry;

    private String contact;

    private double score;

    private String userId;

    private String userName;

    private EnumAppStatus status;


    public AppDto() {
        // empty construct
    }

    /**
     * transfer App to AppDto object..
     * @param app is an App object.
     * @return
     */
    public static AppDto of(App app) {
        AppDto dto = new AppDto();
        dto.appId = app.getAppId();
        dto.name = app.getAppName();
        dto.provider = app.getProvider();
        dto.type = app.getApplicationType();
        dto.shortDesc = app.getShortDesc();
        dto.createTime = app.getCreateTime();
        dto.details = app.getAppIntroduction();
        dto.downloadCount = app.getDownloadCount();
        dto.affinity = app.getAffinity();
        dto.industry = app.getIndustry();
        dto.contact = app.getContact();
        dto.score = app.getScore();
        dto.userId = app.getUser().getUserId();
        dto.userName = app.getUser().getUserName();
        dto.status = app.getStatus();
        return dto;
    }

}
