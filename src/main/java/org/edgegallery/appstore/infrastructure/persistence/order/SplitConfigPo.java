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

package org.edgegallery.appstore.infrastructure.persistence.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.order.SplitConfig;
import org.edgegallery.appstore.infrastructure.persistence.PersistenceObject;

@Getter
@Setter
@Entity
@Table(name = "app_split_config")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SplitConfigPo implements PersistenceObject<SplitConfig> {

    @Id
    @Column(name = "APPID")
    private String appId;

    @Column(name = "SPLITRATIO")
    private double splitRatio;

    public SplitConfigPo() {
        // empty construct
    }

    static SplitConfigPo of(SplitConfig splitConfig) {
        SplitConfigPo po = new SplitConfigPo();
        po.appId = splitConfig.getAppId();
        po.splitRatio = splitConfig.getSplitRatio();

        return po;
    }

    @Override
    public SplitConfig toDomainModel() {
        return SplitConfig.builder()
            .appId(appId)
            .splitRatio(splitRatio)
            .build();
    }
}
