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

import java.util.Collections;
import java.util.List;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.order.SplitConfig;
import org.edgegallery.appstore.domain.model.order.SplitConfigRepository;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.order.facade.dto.SplitConfigOperReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.SplitConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("SplitConfigServiceFacade")
public class SplitConfigServiceFacade {

    public static final Logger LOGGER = LoggerFactory.getLogger(SplitConfigServiceFacade.class);

    @Autowired
    private SplitConfigRepository splitConfigRepository;

    /**
     * query all split config.
     *
     * @return all split config
     */
    public ResponseEntity<ResponseObject> queryAllSplitConfigs() {
        List<SplitConfigDto> respDataDto = null;
        // TODO
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(respDataDto, resultMsg, "query split config success."));
    }

    /**
     * add split config.
     *
     * @param splitConfigOperReqDto request dto
     * @return add result
     */
    public ResponseEntity<ResponseObject> addSplitConfig(SplitConfigOperReqDto splitConfigOperReqDto) {
        LOGGER.info("add split config.");
        for (String appId : splitConfigOperReqDto.getAppIds()) {
            SplitConfig splitConfig = new SplitConfig(appId, splitConfigOperReqDto.getSplitRatio());
            splitConfigRepository.addSplitConfig(splitConfig);
        }

        LOGGER.info("add split config success.");
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(null, resultMsg, "add split config success."));
    }

    /**
     * modify split config.
     *
     * @param appId app id
     * @param splitConfigOperReqDto request dto
     * @return modify result
     */
    public ResponseEntity<ResponseObject> modifySplitConfig(String appId, SplitConfigOperReqDto splitConfigOperReqDto) {
        LOGGER.info("modify split config.");
        SplitConfig splitConfig = new SplitConfig(appId, splitConfigOperReqDto.getSplitRatio());
        splitConfigRepository.updateSplitConfig(splitConfig);

        LOGGER.info("modify split config success.");
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(null, resultMsg, "modify split config success."));
    }

    /**
     * delete split config.
     *
     * @param splitConfigOperReqDto request dto
     * @return delete result
     */
    public ResponseEntity<ResponseObject> deleteSplitConfig(SplitConfigOperReqDto splitConfigOperReqDto) {
        LOGGER.info("delete split config.");
        if (CollectionUtils.isEmpty(splitConfigOperReqDto.getAppIds())) {
            LOGGER.error("invalid delete request parameter.");
            ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_PARAM_INVALID, null);
            return ResponseEntity.badRequest()
                .body(new ResponseObject(null, resultMsg, "invalid delete request parameter."));
        }

        splitConfigOperReqDto.getAppIds().forEach(appId -> splitConfigRepository.deleteSplitConfig(appId));

        LOGGER.info("delete split config success.");
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(null, resultMsg, "delete split config success."));
    }

}
