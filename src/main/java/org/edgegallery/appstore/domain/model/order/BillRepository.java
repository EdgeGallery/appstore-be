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

package org.edgegallery.appstore.domain.model.order;

import java.util.List;
import java.util.Map;

public interface BillRepository {

    void addBill(Bill billData);

    List<BillExtendEntity> queryBillList(Map<String, Object> queryParams);

    long queryBillCount(Map<String, Object> queryParams);

    double statOverallAmount(Map<String, Object> statParams);

    List<AppSaleStatInfo> statAppSaleAmount(Map<String, Object> statParams);

    List<AppSaleStatInfo> statAppSaleCount(Map<String, Object> statParams);

    List<AppOrderStatInfo> statAppOrderAmount(Map<String, Object> statParams);

}