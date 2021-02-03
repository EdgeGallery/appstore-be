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

package org.edgegallery.appstore.interfaces.appstore.facade;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.domain.model.appstore.AppStore;
import org.edgegallery.appstore.domain.model.appstore.AppStoreRepository;
import org.edgegallery.appstore.interfaces.appstore.facade.dto.AppStoreDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("AppStoreServiceFacade")
public class AppStoreServiceFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStoreServiceFacade.class);

    private static final String COMMON_PATTERN = "\\*";

    private static final String SQL_COMMON_PATTERN = "%";

    @Autowired
    private AppStoreRepository appStoreRepository;

    /**
     * add app store.
     */
    public ResponseEntity<AppStoreDto> addAppStore(AppStoreDto appStoreDto, HttpServletRequest request) {
        if (appStoreDto.getUrl().indexOf(request.getLocalAddr()) != -1) {
            LOGGER.error("can not add itself appstore : {}", appStoreDto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(appStoreDto);
        }
        String uuid = appStoreRepository.addAppStore(AppStore.of(appStoreDto));
        AppStore appStore = appStoreRepository.queryAppStoreById(uuid);
        if (appStore == null) {
            LOGGER.error("failed to add app store : {}", appStoreDto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(appStoreDto);
        }
        return ResponseEntity.ok(appStore.toAppStoreDto());
    }

    /**
     * delete app store.
     */
    public ResponseEntity<String> deleteAppStore(String appStoreId) {
        appStoreRepository.deleteAppStoreById(appStoreId);
        return ResponseEntity.ok("");
    }

    /**
     * edit app store.
     */
    public ResponseEntity<AppStoreDto> editAppStore(AppStoreDto appStoreDto) {
        if (appStoreRepository.updateAppStoreById(AppStore.of(appStoreDto)) != 1) {
            LOGGER.error("failed to edit app store : {}", appStoreDto);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        AppStore appStore = appStoreRepository.queryAppStoreById(appStoreDto.getAppStoreId());
        return ResponseEntity.ok(appStore.toAppStoreDto());
    }

    /**
     * query app stores.
     */
    public ResponseEntity<List<AppStoreDto>> queryAppStores(String name, String company) {
        AppStore appStore = new AppStore(replaceSqlPattern(name), replaceSqlPattern(company));
        return ResponseEntity.ok(appStoreRepository.queryAppStores(appStore).stream().map(AppStore::toAppStoreDto)
            .collect(Collectors.toList()));
    }

    /**
     * replace * to %.
     */
    private String replaceSqlPattern(String param) {
        if (StringUtils.isBlank(param)) {
            return null;
        }

        return param.replaceAll(COMMON_PATTERN, SQL_COMMON_PATTERN);
    }

    /**
     * query app store.
     */
    public ResponseEntity<AppStoreDto> queryAppStore(String appStoreId) {
        AppStore appStore = appStoreRepository.queryAppStoreById(appStoreId);
        return ResponseEntity.status(HttpStatus.OK).body(
                appStore == null ? null : appStore.toAppStoreDto());
    }
}
