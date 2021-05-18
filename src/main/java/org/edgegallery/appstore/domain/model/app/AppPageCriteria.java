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

import lombok.Getter;
import org.edgegallery.appstore.domain.shared.PageCriteria;

@Getter
public class AppPageCriteria extends PageCriteria {

    private String appName;

    private String provider;

    private String applicationType;

    private String affinity;

    private String userId;

    /**
     * Constructor of AppPageCriteria.
     */
    public AppPageCriteria(int limit, long offset, String appName, String provider, String applicationType,
        String affinity, String userId) {
        super(limit, offset, null, null, null);
        this.appName = appName;
        this.provider = provider;
        this.applicationType = applicationType;
        this.affinity = affinity;
        this.userId = userId;
    }

}
