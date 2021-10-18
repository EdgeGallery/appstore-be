/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.model.order.SplitConfig;
import org.edgegallery.appstore.domain.model.order.SplitConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SplitConfigRepositoryImpl implements SplitConfigRepository {

    @Autowired
    private SplitConfigMapper splitConfigMapper;

    @Override
    public void addSplitConfig(SplitConfig splitConfig) {
        SplitConfigPo splitConfigPo = SplitConfigPo.of(splitConfig);
        splitConfigMapper.insert(splitConfigPo);
    }

    @Override
    public int updateSplitConfig(SplitConfig splitConfig) {
        SplitConfigPo splitConfigPo = SplitConfigPo.of(splitConfig);
        return splitConfigMapper.update(splitConfigPo);
    }

    @Override
    public void deleteSplitConfig(String appId) {
        splitConfigMapper.delete(appId);
    }

    @Override
    public List<SplitConfig> getAllSplitConfigs() {
        return splitConfigMapper.findAll().stream().map(SplitConfigPo::toDomainModel).collect(Collectors.toList());
    }

    @Override
    public Optional<SplitConfig> findByAppId(String appId) {
        return splitConfigMapper.findByAppId(appId).map(SplitConfigPo::toDomainModel);
    }
}
