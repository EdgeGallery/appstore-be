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

package org.edgegallery.appstore.interfaces.order.facade;

import com.github.pagehelper.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.interfaces.order.facade.dto.MecmHostDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("MecmWrapperServiceFacade")
public class MecmWrapperServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(MecmWrapperServiceFacade.class);

    @Autowired
    private MecmService mecmService;

    @Autowired
    private AppService appService;

    /**
     * get all mecm hosts.
     *
     * @param token Access Token
     * @return ResponseEntity
     */
    public ResponseEntity<ResponseObject> getAllMecmHosts(String token, String appId, String packageId) {
        LOGGER.info("[Get all mecm hosts].");
        List<Map<String, Object>> mecmHosts = mecmService.getAllMecmHosts(token);
        LOGGER.error("[Get all mecm hosts] Utilize mecm service success. Start to filter the hsots.");
        List<Map<String, Object>> resMecmHosts = new ArrayList<>();
        if (!StringUtils.isEmpty(appId) && !StringUtil.isEmpty(packageId)) {
            LOGGER.info("[Get all mecm hosts]. Filter mecm hosts by depoly mode.");
            Release release = appService.getRelease(appId, packageId);
            LOGGER.info("[Get All MecmHost] Deploymode:{}", release.getDeployMode());
            String deployMode = release.getDeployMode().equalsIgnoreCase("container") ? "K8s" : "OpenStack";
            for (Map<String, Object> mecmHost : mecmHosts) {
                LOGGER.info("[Get All MecmHost], current mecm host deploy mode:{}",
                    String.valueOf(mecmHost.get("vim")));
                if (String.valueOf(mecmHost.get("vim")).equalsIgnoreCase(deployMode)) {
                    resMecmHosts.add(mecmHost);
                    LOGGER.info("[Get All MecmHost], successfully add a mecm host:{}",
                        String.valueOf(mecmHost.get("vim")));
                }
            }
        } else {
            resMecmHosts = mecmHosts;
        }
        List<MecmHostDto> respDataDto = resMecmHosts.stream().map(MecmHostDto::fromMap).collect(Collectors.toList());
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(respDataDto, resultMsg, "query mecm host success."));
    }
}
