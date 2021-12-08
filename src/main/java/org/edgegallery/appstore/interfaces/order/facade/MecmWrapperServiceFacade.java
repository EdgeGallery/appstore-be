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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgegallery.appstore.application.external.mecm.MecmService;
import org.edgegallery.appstore.domain.constants.ResponseConst;
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

    /**
     * get all mecm hosts.
     *
     * @param token Access Token
     * @return ResponseEntity
     */
    public ResponseEntity<ResponseObject> getAllMecmHosts(String token, String userId, String appId, String packageId) {
        LOGGER.info("get all mecm hosts.");
        List<Map<String, Object>> mecHostList = mecmService.getAllMecmHosts(token, userId, appId, packageId);
        List<MecmHostDto> respDataDto = mecHostList.stream().map(MecmHostDto::fromMap).collect(Collectors.toList());
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(respDataDto, resultMsg, "query mecm host success."));
    }
}
