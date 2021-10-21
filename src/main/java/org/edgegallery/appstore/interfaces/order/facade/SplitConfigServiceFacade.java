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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.order.SplitConfig;
import org.edgegallery.appstore.domain.model.order.SplitConfigRepository;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.order.facade.dto.SplitConfigDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.SplitConfigOperReqDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service("SplitConfigServiceFacade")
public class SplitConfigServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitConfigServiceFacade.class);

    @Autowired
    private SplitConfigRepository splitConfigRepository;

    @Autowired
    private AppRepository appRepository;

    /**
     * query all split config.
     *
     * @return all split config
     */
    public ResponseEntity<ResponseObject> queryAllSplitConfigs() {
        LOGGER.info("query all split configs.");
        List<SplitConfig> splitConfigList = splitConfigRepository.getAllSplitConfigs();
        if (splitConfigList.stream()
            .noneMatch(item -> Consts.SPLITCONFIG_APPID_GLOBAL.equalsIgnoreCase(item.getAppId()))) {
            splitConfigList
                .add(0, new SplitConfig(Consts.SPLITCONFIG_APPID_GLOBAL, Consts.SPLITCONFIG_SPLITRATIO_GLOBAL));
        }

        LOGGER.info("query all apps.");
        List<App> allApps = appRepository.queryV2(new HashMap<>());
        Map<String, App> appFinder = allApps.stream().collect(Collectors.toMap(App::getAppId, app -> app));

        LOGGER.info("convert split config data result.");
        List<SplitConfigDto> respDataDto = splitConfigList.stream().map(
            splitConfig -> new SplitConfigDto(splitConfig.getAppId(),
                appFinder.get(splitConfig.getAppId()) != null ? appFinder.get(splitConfig.getAppId()).getAppName() : "",
                appFinder.get(splitConfig.getAppId()) != null
                    ? appFinder.get(splitConfig.getAppId()).getProvider()
                    : "", splitConfig.getSplitRatio())).collect(Collectors.toList());

        LOGGER.info("query all split configs success.");
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
        if (CollectionUtils.isEmpty(splitConfigOperReqDto.getAppIds())) {
            LOGGER.error("invalid add request parameter.");
            ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_PARAM_INVALID, null);
            return ResponseEntity.badRequest()
                .body(new ResponseObject(null, resultMsg, "invalid add request parameter."));
        }

        splitConfigOperReqDto.getAppIds().forEach(appId -> splitConfigRepository
            .addSplitConfig(new SplitConfig(appId, splitConfigOperReqDto.getSplitRatio())));

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
        LOGGER.info("modify split config, appId = {}", appId);
        boolean needAddGlobalConfig = false;
        SplitConfig splitConfig = new SplitConfig(appId, splitConfigOperReqDto.getSplitRatio());
        if (splitConfigRepository.updateSplitConfig(splitConfig) <= 0) {
            needAddGlobalConfig = Consts.SPLITCONFIG_APPID_GLOBAL.equalsIgnoreCase(appId);
            if (!needAddGlobalConfig) {
                LOGGER.error("invalid modify request parameter, appId = {}", appId);
                ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_PARAM_INVALID, null);
                return ResponseEntity.badRequest()
                    .body(new ResponseObject(null, resultMsg, "invalid modify request parameter."));
            }
        }

        if (needAddGlobalConfig) {
            LOGGER.info("add global split config.");
            splitConfigRepository.addSplitConfig(
                new SplitConfig(Consts.SPLITCONFIG_APPID_GLOBAL, splitConfigOperReqDto.getSplitRatio()));
        }

        LOGGER.info("modify split config success, appId = {}", appId);
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(null, resultMsg, "modify split config success."));
    }

    /**
     * delete split config.
     *
     * @param appId app id
     * @return delete result
     */
    public ResponseEntity<ResponseObject> deleteSplitConfig(String appId) {
        LOGGER.info("delete split config, appId = {}", appId);
        splitConfigRepository.deleteSplitConfig(appId);

        LOGGER.info("delete split config success.");
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(null, resultMsg, "delete split config success."));
    }

}
