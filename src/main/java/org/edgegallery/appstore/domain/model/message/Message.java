/* Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.domain.model.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

    private String messageId;

    private String result;

    private boolean readed;

    private BasicMessageInfo basicInfo;

    private EnumMessageType messageType;

    private String sourceAppStore;

    private String targetAppStore;

    private String time;

    private String description;

    private String atpTestStatus;

    private String atpTestTaskId;

    private String atpTestReportUrl;

    private String packageDownloadUrl;

    private String iconDownloadUrl;

    private String demoVideoDownloadUrl;
}
