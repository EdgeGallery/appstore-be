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

package org.edgegallery.appstore.infrastructure.persistence.app;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppPageCriteria;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.edgegallery.appstore.domain.shared.exceptions.MaxRecordLimitException;
import org.edgegallery.appstore.infrastructure.persistence.apackage.AppReleasePO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AppRepositoryImpl implements AppRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppRepositoryImpl.class);

    public static final int MAX_ENTRY_PER_USER_PER_MODEL = 1000;

    @Autowired
    private AppMapper appMapper;

    @Override
    public Optional<App> find(String appId) {
        Optional<App> app = appMapper.findByAppId(appId).map(AppPO::toDomainModel);
        if (app.isPresent()) {
            List<Release> releases = appMapper.findAllByAppId(appId)
                .stream()
                .map(AppReleasePO::toDomainModel)
                .collect(Collectors.toList());
            app.get().setReleases(releases);
        }
        return app;
    }

    @Override
    public void store(App app) {
        AppPO appPO = AppPO.of(app);
        Optional<AppPO> existed = appMapper.findByAppId(app.getAppId());
        if (existed.isPresent()) {
            appMapper.update(appPO);
        } else {
            if (appMapper.countTotalAppForUser(app.getUserId()) >= MAX_ENTRY_PER_USER_PER_MODEL) {
                LOGGER.error("maximum app limit has reached for user " + app.getUserId());
                throw new MaxRecordLimitException("maximum app limit has reached for user " + app.getUserId());
            }
            appMapper.insert(appPO);
        }
        updateReleases(app.getAppId(), app.getReleases());
    }

    private void updateReleases(String appId, List<Release> releases) {
        List<Release> releaseList = appMapper.findAllByAppId(appId)
            .stream()
            .map(AppReleasePO::toDomainModel)
            .collect(Collectors.toList());

        releases.forEach(it -> {
            if (!releaseList.contains(it)) {
                if (releaseList.size() >= MAX_ENTRY_PER_USER_PER_MODEL) {
                    LOGGER.error("maximum release limit has reached for app " + appId);
                    throw new MaxRecordLimitException("maximum release limit has reached for app " + appId);
                }
                appMapper.insertRelease(AppReleasePO.of(it));
            }
        });
        releaseList.forEach(it -> {
            if (!releases.contains(it)) {
                appMapper.removeByPackageId(it.getPackageId());
            }
        });
    }

    @Override
    public String generateAppId() {
        String random = UUID.randomUUID().toString();
        return random.replace("-", "");
    }

    /**
     * Find App by app name.
     * @param appName app name.
     * @return
     */
    public Optional<App> findByAppNameAndProvider(String appName, String provider) {
        Optional<App> app = appMapper.findByAppNameAndProvider(appName, provider).map(AppPO::toDomainModel);
        if (app.isPresent()) {
            List<Release> releases = appMapper.findAllByAppId(app.get().getAppId())
                .stream()
                .map(AppReleasePO::toDomainModel)
                .collect(Collectors.toList());
            app.get().setReleases(releases);
        }
        return app;
    }

    @Override
    public void remove(String appId) {
        appMapper.remove(appId);
        appMapper.removeReleasesByAppId(appId);
    }

    @Override
    public Page<App> query(AppPageCriteria appPageCriteria) {
        long total = appMapper.countTotal(appPageCriteria).longValue();
        List<App> releases = appMapper.findAllWithAppPagination(appPageCriteria)
            .stream()
            .map(AppPO::toDomainModel)
            .collect(Collectors.toList());
        return new Page<>(releases, appPageCriteria.getLimit(), appPageCriteria.getOffset(), total);
    }

    @Override
    public Page<Release> findAllWithPagination(PageCriteria pageCriteria) {
        long total = appMapper.countTotalForReleases(pageCriteria).longValue();
        List<Release> releases = appMapper.findAllWithPagination(pageCriteria)
            .stream()
            .map(AppReleasePO::toDomainModel)
            .collect(Collectors.toList());
        return new Page<>(releases, pageCriteria.getLimit(), pageCriteria.getOffset(), total);
    }


}
