/* Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.application.inner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.edgegallery.appstore.config.ApplicationContext;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.appstore.AppStore;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.domain.model.releases.AFile;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.exceptions.DomainException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PushablePackageRepository;
import org.edgegallery.appstore.infrastructure.persistence.appstore.AppStoreRepositoryImpl;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageRepository;
import org.edgegallery.appstore.infrastructure.util.AppUtil;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.edgegallery.appstore.interfaces.app.facade.AppParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("PullablePackageService")
public class PullablePackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PullablePackageService.class);

    private static final String DOWNLOAD_PACKAGE_API = "/mec/appstore/v1/packages/%s/action/download-package";

    private static final String DOWNLOAD_ICON_API = "/mec/appstore/v1/packages/%s/action/download-icon";

    private static final String PULLABLE_API = "/mec/appstore/v1/packages/pullable";

    private static final String PULLABLE_API_V2 = "/mec/appstore/v2/packages/pullable";
    
    @Value("${appstore-be.package-path}")
    private String dir;

    @Autowired
    private PushablePackageRepository pushablePackageRepository;

    @Autowired
    private AppStoreRepositoryImpl appStoreRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private LocalFileService fileService;

    @Autowired
    private AppService appService;

    @Autowired
    private AppUtil appUtil;

    /**
     * query all pullable packages.
     *
     * @return list
     */
    public Page<PushablePackageDto> queryAllPullablePackagesV2(int limit, int offset, String appName, String sortType,
        String sortItem) {
        LOGGER.info("pullablePackageService queryAllPullablePackages come in");
        return pushablePackageRepository.queryAllPushablePackagesV2(limit, offset, appName, sortType, sortItem, "pull");
    }

    /**
     * get pullable packages by id.
     *
     * @param platformId appstore id
     * @param userId user id
     * @return dto
     */
    public ResponseEntity<Page<PushablePackageDto>> getPullablePackagesV2(String platformId, int limit, long offset,
        String sortType, String sortItem, String appName, String userId) {
        AppStore appStore = appStoreRepository.queryAppStoreById(platformId);
        if (appStore == null) {
            LOGGER.error("appstrore is not exist, appstoreId is {}", platformId);
            return ResponseEntity.ok(new Page<PushablePackageDto>(Collections.emptyList(), limit, offset,
                Collections.emptyList().size()));
        }
        String url = appStore.getUrl() + PULLABLE_API_V2 + "?limit=" + limit + "&offset=" + offset + "&appName="
            + appName + "&sortType=" + sortType + "&sortItem=" + sortItem;
        String countUrl = appStore.getUrl() + PULLABLE_API;
        List<PushablePackageDto> countPackages = filterPullabelPackages(commonPackage(countUrl, appStore), userId);
        LOGGER.info(url);
        List<PushablePackageDto> packages = commonPackage(url, appStore);
        return ResponseEntity.ok(new Page<PushablePackageDto>(filterPullabelPackages(packages, userId), limit, offset,
            countPackages.size()));

    }

    /**
     * get pull package list.
     *
     * @param url appstore url.
     * @param appStore appStore.
     * @return PushablePackageDto list.
     */
    public  List<PushablePackageDto> commonPackage(String url, AppStore appStore) {
        List<PushablePackageDto> packages;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("getPullablePackages error, response code is {}", response.getStatusCode());
                return Collections.emptyList();
            }

            String result = response.getBody();
            if (result == null) {
                LOGGER.error("get pullable packages is null");
                return Collections.emptyList();
            }

            Gson g = new Gson();
            packages = g.fromJson(result, new TypeToken<List<PushablePackageDto>>() { }.getType());
            LOGGER.info("get pullable packages from {}, size is {}", appStore.getAppStoreName(), packages.size());
        } catch (RestClientException e) {
            LOGGER.error("failed to get pullable packages from url {}", url);
            return Collections.emptyList();
        }
        return  packages;

    }

    /**
     * query all pullable packages.
     *
     * @return list
     */
    public List<PushablePackageDto> queryAllPullablePackages() {
        LOGGER.info("pullablePackageService queryAllPullablePackages come in");
        return pushablePackageRepository.queryAllPushablePackages();
    }

    /**
     * query push packege count.
     *
     * @param params params.
     * @return count.
     */
    public Integer getAllPushablePackagesCount(Map<String, Object> params) {
        return pushablePackageRepository.getAllPushablePackagesCount(params);
    }

    /**
     * get pullable packages by id.
     *
     * @param platformId appstore id
     * @param userId user id
     * @return dto
     */
    public List<PushablePackageDto> getPullablePackages(String platformId, String userId) {
        AppStore appStore = appStoreRepository.queryAppStoreById(platformId);
        if (appStore == null) {
            LOGGER.error("appstrore is not exist, appstoreId is {}", platformId);
            return Collections.emptyList();
        }
        String url = appStore.getUrl() + PULLABLE_API;
        LOGGER.info(url);
        List<PushablePackageDto> packages = commonPackage(url, appStore);
        return filterPullabelPackages(packages, userId);
    }

    /**
     * get pullable packages by id.
     *
     * @param packageId package id
     * @param sourceStoreId source appstore id
     * @param user user info
     * @param packagePo package info
     * @return dto
     */
    public Boolean pullPackage(String packageId, String sourceStoreId, User user, PushablePackageDto packagePo) {
        LOGGER.info("pullPackage sourceStoreId {}, userName {}", sourceStoreId, user.getUserName());
        AppStore appStore = appStoreRepository.queryAppStoreById(sourceStoreId);
        if (appStore == null) {
            LOGGER.error("appstrore is not exist, appstoreId is {}", sourceStoreId);
            return false;
        }
        String baseUrl = appStore.getUrl();
        String packageDownloadUrl = baseUrl + String.format(DOWNLOAD_PACKAGE_API, packageId);
        String iconDownloadUrl = baseUrl + String.format(DOWNLOAD_ICON_API, packageId);
        LOGGER.info("pullPackage packageDownloadUrl {}, iconDownloadUrl {}", packageDownloadUrl, iconDownloadUrl);

        try {
            String parentPath = dir + File.separator + UUID.randomUUID().toString().replace("-", "");
            String targetAppstore = context.platformName;
            File tempPackage = fileService.downloadFile(packageDownloadUrl, parentPath, targetAppstore);
            File tempIcon = fileService.downloadFile(iconDownloadUrl, parentPath, targetAppstore);
            AFile apackage = new AFile(tempPackage.getName(), tempPackage.getCanonicalPath());
            AFile icon = new AFile(tempIcon.getName(), tempIcon.getCanonicalPath());
            apackage.setFileSize(tempPackage.length());
            String appClass = appUtil.getAppClass(apackage.getStorageAddress());
            String showType = "public";
            AppParam appParam = new AppParam(packagePo.getType(), packagePo.getShortDesc(), showType,
                packagePo.getAffinity(), packagePo.getIndustry());
            Release release = new Release(apackage, icon, null, user, appParam, appClass);
            // the package pulled from third appstore need to be tested by local appstore's atp
            release.setStatus(EnumPackageStatus.Upload);
            appService.registerApp(release);

            addPullMessage(packagePo);
        } catch (IOException e) {
            LOGGER.error("IOException: {}", e.getMessage());
            throw new DomainException("pull package exception");
        }
        return true;
    }

    private List<PushablePackageDto> filterPullabelPackages(List<PushablePackageDto> packages, String userId) {
        List<PushablePackageDto> result = new ArrayList<>();
        for (PushablePackageDto dto : packages) {
            AtomicBoolean bexist = new AtomicBoolean(false);
            Optional<App> existedApp = appRepository.findByAppNameAndProvider(dto.getName(), dto.getProvider());
            if (existedApp.isPresent()) {
                List<Release> releases = existedApp.get().getReleases();
                releases.stream().filter(r -> r.getStatus() == EnumPackageStatus.Published
                    || userId.equals(r.getUser().getUserId())).forEach(r1 -> {
                        if (dto.getVersion().equals(r1.getAppBasicInfo().getVersion())) {
                            bexist.set(true);
                            LOGGER.info("The same app has existed. packages name {}", dto.getName());
                        }
                    });
            }
            if (!bexist.get()) {
                result.add(dto);
            }
        }
        LOGGER.info("the packages size is {} after filter", result.size());
        return result;
    }

    private void addPullMessage(PushablePackageDto packagePo) {
        // add message log for this action
        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setMessageType(EnumMessageType.PULL);
        BasicMessageInfo basicMessageInfo = new BasicMessageInfo(packagePo);
        String sourceAppstore = packagePo.getSourcePlatform();
        message.setSourceAppStore(sourceAppstore);
        message.setTargetAppStore(context.platformName);
        message.setDescription(String.format("%s pull this app from %s.", context.platformName, sourceAppstore));
        message.setAtpTestStatus(packagePo.getAtpTestStatus());
        message.setBasicInfo(basicMessageInfo);
        message.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        // store message to the db
        messageRepository.addMessage(message);
    }
}
