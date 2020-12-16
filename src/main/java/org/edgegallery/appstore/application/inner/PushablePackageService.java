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

import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.config.ApplicationContext;
import org.edgegallery.appstore.domain.model.message.BasicMessageInfo;
import org.edgegallery.appstore.domain.model.message.EnumMessageType;
import org.edgegallery.appstore.domain.model.message.Message;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PushablePackageRepository;
import org.edgegallery.appstore.infrastructure.persistence.appstore.ExAppStorePo;
import org.edgegallery.appstore.infrastructure.persistence.appstore.ExAppStoreRepository;
import org.edgegallery.appstore.infrastructure.persistence.message.MessageRepository;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageReqDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("PushablePackageService")
public class PushablePackageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushablePackageService.class);

    private static final String NOTICE_API = "/mec/appstore/poke/messages";

    @Autowired
    private PushablePackageRepository pushablePackageRepository;

    @Autowired
    private ExAppStoreRepository exAppStoreRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ApplicationContext context;

    public List<PushablePackageDto> queryAllPushablePackages() {
        return pushablePackageRepository.queryAllPushablePackages();
    }

    public PushablePackageDto getPushablePackage(String packageId) {
        return pushablePackageRepository.getPushablePackages(packageId);
    }

    /**
     * push package to target appstore.
     *
     * @param packageId package id
     * @param targetAppStore target appstore
     */
    public List<Boolean> pushPackage(String packageId, PushTargetAppStoreDto targetAppStore) {
        final PushablePackageDto packagePo = pushablePackageRepository.getPushablePackages(packageId);
        final List<Boolean> results = new ArrayList<>();
        LOGGER.info("push package {}", packagePo.getPackageId());
        targetAppStore.getTargetPlatform().forEach(platformId -> {
            ExAppStorePo appStorePo = exAppStoreRepository.findAppStoreById(platformId);
            if (appStorePo == null) {
                results.add(false);
                return;
            }
            String url = appStorePo.getUrl() + NOTICE_API;
            LOGGER.info(url);

            MessageReqDto messageReqDto = pushNotice(url, packagePo);
            if (messageReqDto == null) {
                results.add(false);
                return;
            }
            Message message = messageReqDto.toMessage(appStorePo.getName(), EnumMessageType.PUSH);

            // store message to the db
            messageRepository.addMessage(message);
            results.add(true);
        });
        return results;
    }

    private MessageReqDto pushNotice(String url, PushablePackageDto packageDto) {

        try {
            MessageReqDto requestDto = new MessageReqDto();
            BasicMessageInfo basicMessageInfo = new BasicMessageInfo(packageDto);
            requestDto.setBasicInfo(basicMessageInfo);
            requestDto.setSourceAppStore(context.platformName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<MessageReqDto> requestEntity = new HttpEntity<>(requestDto, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (!HttpStatus.OK.equals(response.getStatusCode())) {
                LOGGER.error("failed to send app {} to the app store {}", packageDto.getAppId(),
                    packageDto.getTargetPlatform(), response.getStatusCode());
                return null;
            }
            return requestDto;
        } catch (RestClientException e) {
            LOGGER.error("failed to send notice to {}", url);
        }
        return null;
    }
}
