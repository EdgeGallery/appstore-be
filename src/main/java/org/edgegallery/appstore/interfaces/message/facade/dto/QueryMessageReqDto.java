/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.edgegallery.appstore.interfaces.message.facade.dto;

import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageDateEnum;

@Setter
@Getter
public class QueryMessageReqDto {

    private String appName;

    private MessageDateEnum timeFlag;

    private String messageType;

    private int limit;

    private int offset;

    private String sortType;

    private String sortItem;

    private boolean readable;

    private boolean allMessage;

}
