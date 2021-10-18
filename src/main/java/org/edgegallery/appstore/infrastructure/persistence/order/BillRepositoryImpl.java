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

package org.edgegallery.appstore.infrastructure.persistence.order;

import java.util.List;
import java.util.Map;
import org.edgegallery.appstore.domain.model.order.AppOrderStatInfo;
import org.edgegallery.appstore.domain.model.order.AppSaleStatInfo;
import org.edgegallery.appstore.domain.model.order.Bill;
import org.edgegallery.appstore.domain.model.order.BillExtendEntity;
import org.edgegallery.appstore.domain.model.order.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BillRepositoryImpl implements BillRepository {

    @Autowired
    private BillMapper billMapper;

    @Override
    public void addBill(Bill billData) {
        BillPo billPo = BillPo.of(billData);
        billMapper.insert(billPo);
    }

    @Override
    public List<BillExtendEntity> queryBillList(Map<String, Object> queryParams) {
        return billMapper.queryBillList(queryParams);
    }

    @Override
    public long queryBillCount(Map<String, Object> queryParams) {
        return billMapper.queryBillCount(queryParams).longValue();
    }

    @Override
    public double statOverallAmount(Map<String, Object> statParams) {
        Double overallAmount = billMapper.statOverallAmount(statParams);
        return overallAmount != null ? overallAmount.doubleValue() : 0.0;
    }

    @Override
    public List<AppSaleStatInfo> statAppSaleAmount(Map<String, Object> statParams) {
        return billMapper.statAppSaleAmount(statParams);
    }

    @Override
    public List<AppSaleStatInfo> statAppSaleCount(Map<String, Object> statParams) {
        return billMapper.statAppSaleCount(statParams);
    }

    @Override
    public List<AppOrderStatInfo> statAppOrderAmount(Map<String, Object> statParams) {
        return billMapper.statAppOrderAmount(statParams);
    }
}
