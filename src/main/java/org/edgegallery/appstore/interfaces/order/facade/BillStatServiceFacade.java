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
import org.edgegallery.appstore.domain.model.order.AppSaleStatInfo;
import org.edgegallery.appstore.domain.model.order.BillRepository;
import org.edgegallery.appstore.domain.shared.ErrorMessage;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.interfaces.order.facade.dto.BillDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.QueryBillsReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.StatOverallReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.StatOverallResultDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopOrderAppReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopOrderAppResultDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopSaleAppReqDto;
import org.edgegallery.appstore.interfaces.order.facade.dto.TopSaleAppResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("BillStatServiceFacade")
public class BillStatServiceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillStatServiceFacade.class);

    @Autowired
    private BillRepository billRepository;

    /**
     * query bill list.
     *
     * @param userId user id
     * @param queryBillsReqDto query request dto
     * @return bill list
     */
    public ResponseEntity<Page<BillDto>> queryBillList(String userId, QueryBillsReqDto queryBillsReqDto) {
        LOGGER.info("query bill list.");
        queryBillsReqDto.adjustTimeFormat();
        Map<String, Object> queryParams = new HashMap<>();
        if (!Consts.SUPER_ADMIN_ID.equals(userId)) {
            queryParams.put("userId", userId);
        }
        queryParams.put("startTime", queryBillsReqDto.getStartTime());
        queryParams.put("endTime", queryBillsReqDto.getEndTime());
        queryParams.put("queryCtrl", queryBillsReqDto.getQueryCtrl());
        List<BillDto> billDtos = billRepository.queryBillList(queryParams).stream().map(BillDto::of)
            .collect(Collectors.toList());
        long totalCount = billRepository.queryBillCount(queryParams);

        LOGGER.info("query bill list succeed.");
        return ResponseEntity.ok(new Page<>(billDtos, queryBillsReqDto.getQueryCtrl().getLimit(),
            queryBillsReqDto.getQueryCtrl().getOffset(), totalCount));
    }

    /**
     * statistic overall income and expenditure.
     *
     * @param userId user id
     * @param statOverallReqDto stat request dto
     * @return stat result
     */
    public ResponseEntity<ResponseObject> statOverall(String userId, StatOverallReqDto statOverallReqDto) {
        LOGGER.info("statistic overall income and expenditure.");
        statOverallReqDto.adjustTimeFormat();
        Map<String, Object> statParams = new HashMap<>();
        statParams.put("userId", userId);
        statParams.put("startTime", statOverallReqDto.getStartTime());
        statParams.put("endTime", statOverallReqDto.getEndTime());

        statParams.put("billType", "IN");
        double incomeNum = billRepository.statOverallAmount(statParams);

        statParams.put("billType", "OUT");
        double expendNum = billRepository.statOverallAmount(statParams);

        LOGGER.info("statistic overall income and expenditure succeed.");
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(new StatOverallResultDto(incomeNum, expendNum), resultMsg,
            "statistic overall income and expenditure succeed."));
    }

    /**
     * statistic top sale app.
     *
     * @param userId user id
     * @param topSaleAppReqDto stat request dto
     * @return stat result
     */
    public ResponseEntity<ResponseObject> statTopSaleApp(String userId, TopSaleAppReqDto topSaleAppReqDto) {
        LOGGER.info("statistic top sale app.");
        topSaleAppReqDto.adjustTimeFormat();
        Map<String, Object> statParams = new HashMap<>();
        if (!Consts.SUPER_ADMIN_ID.equalsIgnoreCase(userId)) {
            statParams.put("userIdOfApp", userId);
        }
        statParams.put("startTime", topSaleAppReqDto.getStartTime());
        statParams.put("endTime", topSaleAppReqDto.getEndTime());
        statParams.put("sortType", topSaleAppReqDto.getSortType());
        statParams.put("topNum", topSaleAppReqDto.getTopNum());
        List<AppSaleStatInfo> appSaleStatInfoList = "SaleAmount".equalsIgnoreCase(topSaleAppReqDto.getTopCriterion())
            ? billRepository.statAppSaleAmount(statParams)
            : billRepository.statAppSaleCount(statParams);
        List<TopSaleAppResultDto> respDataDto = appSaleStatInfoList.stream()
            .map(TopSaleAppResultDto::of).collect(Collectors.toList());

        LOGGER.info("statistic top sale app succeed.");
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(respDataDto, resultMsg, "statistic top sale app succeed."));
    }

    /**
     * statistic top order app.
     *
     * @param userId user id
     * @param topOrderAppReqDto stat request dto
     * @return stat result
     */
    public ResponseEntity<ResponseObject> statTopOrderApp(String userId, TopOrderAppReqDto topOrderAppReqDto) {
        LOGGER.info("statistic top order app.");
        topOrderAppReqDto.adjustTimeFormat();
        Map<String, Object> statParams = new HashMap<>();
        if (!Consts.SUPER_ADMIN_ID.equalsIgnoreCase(userId)) {
            statParams.put("userIdOfApp", userId);
        }
        statParams.put("startTime", topOrderAppReqDto.getStartTime());
        statParams.put("endTime", topOrderAppReqDto.getEndTime());
        statParams.put("topNum", 5);
        List<TopOrderAppResultDto> respDataDto = billRepository.statAppOrderAmount(statParams).stream()
            .map(TopOrderAppResultDto::of).collect(Collectors.toList());

        LOGGER.info("statistic top order app succeed.");
        ErrorMessage resultMsg = new ErrorMessage(ResponseConst.RET_SUCCESS, null);
        return ResponseEntity.ok(new ResponseObject(respDataDto, resultMsg, "statistic top order app succeed."));
    }

}
