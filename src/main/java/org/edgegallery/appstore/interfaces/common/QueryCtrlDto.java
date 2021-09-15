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

package org.edgegallery.appstore.interfaces.common;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class QueryCtrlDto {
    private static final String DEFAULT_SORTITEM = "createTime";

    private static final String DEFAULT_SORTTYPE = "DESC";

    @Min(value = 0)
    private int offset;

    @Min(value = 1)
    @Max(value = 500)
    private int limit;

    @ApiModelProperty(example = "createTime")
    private String sortItem;

    @ApiModelProperty(example = "ASC")
    @Pattern(regexp = "(?i)DESC|(?i)ASC")
    private String sortType;

    /**
     * check basic data by trim.
     */
    public void stringTrim() {
        this.sortItem = StringUtils.trimWhitespace(this.sortItem);
        if (StringUtils.isEmpty(this.sortItem)) {
            this.sortItem = DEFAULT_SORTITEM;
        }

        this.sortType = StringUtils.trimWhitespace(this.sortType);
        if (StringUtils.isEmpty(this.sortType)) {
            this.sortType = DEFAULT_SORTTYPE;
        }

    }
}
