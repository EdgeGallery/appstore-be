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

package org.edgegallery.appstore.infrastructure.persistence.app;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppPageCriteria;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.infrastructure.persistence.apackage.AppReleasePo;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PackageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AppRepositoryImpl implements AppRepository {

    public static final int MAX_ENTRY_PER_USER_PER_MODEL = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppRepositoryImpl.class);

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private PackageMapper packageMapper;

    @Override
    public void store(App app) {
        AppPo appPO = AppPo.of(app);
        Optional<AppPo> existed = appMapper.findByAppId(app.getAppId());
        if (existed.isPresent()) {
            appMapper.update(appPO);
        } else {
            if (appMapper.countTotalAppForUser(app.getUserId()) >= MAX_ENTRY_PER_USER_PER_MODEL) {
                LOGGER.error("maximum app limit has reached for user " + app.getUserId());
                throw new AppException("maximum app limit has reached for user " + app.getUserId(),
                    ResponseConst.RET_USER_APPS_REACH_LIMIT, app.getUser().getUserName(), MAX_ENTRY_PER_USER_PER_MODEL);
            }
            appMapper.insert(appPO);
        }
    }

    @Override
    public Optional<App> find(String appId) {
        Optional<App> app = appMapper.findByAppId(appId).map(AppPo::toDomainModel);
        if (app.isPresent()) {
            List<Release> releases = packageMapper.findAllByAppId(appId).stream().map(AppReleasePo::toDomainModel)
                .collect(Collectors.toList());
            app.get().setReleases(releases);
        }
        return app;
    }

    @Override
    public String generateAppId() {
        String random = UUID.randomUUID().toString();
        return random.replace("-", "");
    }

    /**
     * Find App by app name.
     *
     * @param appName app name.
     * @return
     */
    public Optional<App> findByAppNameAndProvider(String appName, String provider) {
        Optional<App> app = appMapper.findByAppNameAndProvider(appName, provider).map(AppPo::toDomainModel);
        if (app.isPresent()) {
            List<Release> releases = packageMapper.findAllByAppId(app.get().getAppId()).stream()
                .map(AppReleasePo::toDomainModel).collect(Collectors.toList());
            app.get().setReleases(releases);
        }
        return app;
    }

    @Override
    public void remove(String appId) {
        appMapper.remove(appId);
        packageMapper.removeReleasesByAppId(appId);
    }

    @Override
    public List<App> queryV2(Map<String, Object> params) {
        return appMapper.findAllWithAppPaginationV2(params).stream().map(AppBasicPo::toDomainModel)
            .collect(Collectors.toList());
    }

    @Override
    public Page<App> query(AppPageCriteria appPageCriteria) {
        long total = appMapper.countTotal(appPageCriteria).longValue();
        List<App> releases = appMapper.findAllWithAppPagination(appPageCriteria).stream().map(AppPo::toDomainModel)
            .collect(Collectors.toList());
        return new Page<>(releases, appPageCriteria.getLimit(), appPageCriteria.getOffset(), total);
    }

    @Override
    public Page<Release> findAllWithPagination(PageCriteria pageCriteria) {
        if (!appMapper.findByAppId(pageCriteria.getAppId()).isPresent()) {
            throw new EntityNotFoundException(App.class, pageCriteria.getAppId(), ResponseConst.RET_APP_NOT_FOUND);
        }
        long total = packageMapper.countTotalForReleases(pageCriteria).longValue();
        List<Release> releases = packageMapper.findAllWithPagination(pageCriteria).stream()
            .map(AppReleasePo::toDomainModel).collect(Collectors.toList());
        return new Page<>(releases, pageCriteria.getLimit(), pageCriteria.getOffset(), total);
    }

    @Override
    public long countTotalV2(Map<String, Object> params) {
        return appMapper.countTotalV2(params).longValue();
    }

}
