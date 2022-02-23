/*
 *    Copyright 2021-2022 Huawei Technologies Co., Ltd.
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
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
    public ResponseEntity<ResponseObject> getAllMecHosts(String token, String appId, String packageId) {
        List<Map<String, Object>> mecmHosts = mecmService.getAllMecHosts(token);
        LOGGER.info("get all mec hosts, size is {}.", mecmHosts.size());
        List<MecmHostDto> respDataDto;
        if (!StringUtils.isEmpty(appId) && !StringUtil.isEmpty(packageId)) {
            Release release = appService.getRelease(appId, packageId);
            String vim = release.getDeployMode().equalsIgnoreCase(Consts.APP_CONTAINER) ? Consts.OS_K8S : Consts.OS_OPENSTACK;
            List<Map<String, Object>> resMecHosts = mecmHosts.stream()
                .filter(r -> String.valueOf(r.get("vim")).equalsIgnoreCase(vim)).collect(Collectors.toList());
            LOGGER.info("after filtering by deploy mode {}, size is {}", release.getDeployMode(), resMecHosts.size());
            respDataDto = resMecHosts.stream().map(MecmHostDto::fromMap).collect(Collectors.toList());
        } else {
            respDataDto = mecmHosts.stream().map(MecmHostDto::fromMap).collect(Collectors.toList());
        }
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(respDataDto, resultMsg, "query mecm host success."));
    }
}
