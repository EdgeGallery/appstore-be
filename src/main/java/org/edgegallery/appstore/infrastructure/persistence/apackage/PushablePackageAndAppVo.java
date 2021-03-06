/* Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.infrastructure.persistence.apackage;

import javax.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushablePackageAndAppVo extends AppReleasePo {

    @Column(name = "atpTestReportUrl")
    private String atpTestReportUrl;

    @Column(name = "latestPushTime")
    private String latestPushTime;

    @Column(name = "pushTimes")
    private int pushTimes;

    @Column(name = "targetPlatform")
    private String targetPlatform;

    @Column(name = "sourcePlatform")
    private String sourcePlatform;
}
