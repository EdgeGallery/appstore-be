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

package org.edgegallery.appstore.application.inner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.edgegallery.appstore.domain.shared.exceptions.DomainException;
import org.edgegallery.appstore.infrastructure.files.LocalFileService;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PushablePackageRepository;
import org.edgegallery.appstore.infrastructure.persistence.appstore.AppStoreRepositoryImpl;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageRepository;
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

    private static final String DOWNLOAD_PACKAGE_API
        = "/mec/appstore/v1/packages/%s/action/download-package";

    private static final String DOWNLOAD_ICON_API = "/mec/appstore/v1/packages/%s/action/download-icon";

    private static final String PUSHABLE_API = "/mec/appstore/v1/packages/pushable";

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


    /**
     * get pullable packages by id.
     *
     * @param platformId appstore id
     * @return dto
     */
    public List<PushablePackageDto> getPullablePackages(String platformId) {
        AppStore appStore = appStoreRepository.queryAppStoreById(platformId);
        if (appStore == null) {
            LOGGER.error("appstrore is not exist, appstoreId is {}", platformId);
            return null;
        }
        String url = appStore.getUrl() + PUSHABLE_API;
        LOGGER.info(url);

        List<PushablePackageDto> packages;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate
                .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                LOGGER.error("getPullablePackages error, response code is {}", response.getStatusCode());
                return null;
            }

            String result = response.getBody();
            if (result == null) {
                LOGGER.error("get pullable packages is null");
                return null;
            }

            Gson g = new Gson();
            packages = g.fromJson(result, new TypeToken<List<PushablePackageDto>>(){}.getType());
            for (int i = 0; i < packages.size(); i++) {
                PushablePackageDto dto = packages.get(i);
            }
            LOGGER.info("get pushable packages from {}, size is {}", appStore.getAppStoreName(), packages.size());
        } catch (RestClientException e) {
            LOGGER.error("failed to get pullable packages from url {}", url);
            return null;
        }

        return filterPullabelPackages(packages);
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
            String parentPath = dir + File.separator + UUID.randomUUID().toString();
            String targetAppstore = context.platformName;
            File tempPackage = fileService.downloadFile(packageDownloadUrl, parentPath, targetAppstore);
            File tempIcon = fileService.downloadFile(iconDownloadUrl, parentPath, targetAppstore);
            AFile apackage = new AFile(tempPackage.getName(), tempPackage.getCanonicalPath());
            AFile icon = new AFile(tempIcon.getName(), tempIcon.getCanonicalPath());
            AppParam appParam = new AppParam(packagePo.getType(), packagePo.getShortDesc(),
                packagePo.getAffinity(), packagePo.getIndustry());
            Release release = new Release(apackage, icon, user, appParam);
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

    private List<PushablePackageDto> filterPullabelPackages(List<PushablePackageDto> packages) {
        List<PushablePackageDto> result = new ArrayList<>();
        for (PushablePackageDto dto : packages) {
            AtomicBoolean bexist = new AtomicBoolean(false);
            Optional<App> existedApp = appRepository.findByAppNameAndProvider(dto.getName(), dto.getProvider());
            if (existedApp.isPresent()) {
                List<Release> releases = existedApp.get().getReleases();
                releases.stream().filter(r -> r.getStatus() == EnumPackageStatus.Published).forEach(r1 -> {
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