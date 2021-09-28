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

package org.edgegallery.appstore.application.inner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.order.Bill;
import org.edgegallery.appstore.domain.model.order.BillRepository;
import org.edgegallery.appstore.domain.model.order.Order;
import org.edgegallery.appstore.domain.model.order.OrderRepository;
import org.edgegallery.appstore.domain.model.order.SplitConfig;
import org.edgegallery.appstore.domain.model.order.SplitConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("BillService")
public class BillService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private SplitConfigRepository splitConfigRepository;

    /**
     * generate bill.
     */
    public void generateBill() {
        LOGGER.info("generate bills begin.");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("status", "ACTIVATED");
        List<Order> activeOrderList = orderRepository.queryOrders(queryParams);

        LOGGER.info("generate bills for {} active order(s).", activeOrderList.size());
        for (Order activeOrder : activeOrderList) {
            generateBill(activeOrder);
        }

        LOGGER.info("generate bills end.");
    }

    private void generateBill(Order activeOrder) {
        LOGGER.info("generate bills for order begin, orderNum = {}", activeOrder.getOrderNum());
        Optional<App> appOptional = appRepository.find(activeOrder.getAppId());
        if (!appOptional.isPresent()) {
            LOGGER.warn("the app of order had been deleted! appId = {}", activeOrder.getAppId());
            return;
        }

        App app = appOptional.get();
        if (app.isFree() || app.getPrice() == 0) {
            LOGGER.debug("the app of order is free, appId = {}", activeOrder.getAppId());
            return;
        }

        double splitRatio = getSplitConfig(app.getAppId());
        double operatorFee = app.getPrice() * splitRatio;
        double supplierFee = app.getPrice() - operatorFee;
        List<Bill> newBillList = new ArrayList<>();
        newBillList.add(geneBillForConsumer(activeOrder.getOrderId(), activeOrder.getUserId(),
            activeOrder.getUserName(), operatorFee, supplierFee));
        newBillList.add(geneBillForOperator(activeOrder.getOrderId(), Consts.SUPER_ADMIN_ID,
            Consts.SUPER_ADMIN_NAME, operatorFee));
        newBillList.add(geneBillForSupplier(activeOrder.getOrderId(), app.getUserId(),
            app.getUser().getUserName(), supplierFee));
        newBillList.forEach(newBill -> billRepository.addBill(newBill));
        LOGGER.info("generate bills for order end, orderNum = {}", activeOrder.getOrderNum());
    }

    private double getSplitConfig(String appId) {
        Optional<SplitConfig> splitConfigOpt = splitConfigRepository.findByAppId(appId);
        if (splitConfigOpt.isPresent()) {
            return splitConfigOpt.get().getSplitRatio();
        }

        splitConfigOpt = splitConfigRepository.findByAppId(Consts.SPLITCONFIG_APPID_GLOBAL);
        return splitConfigOpt.isPresent() ? splitConfigOpt.get().getSplitRatio() : Consts.SPLITCONFIG_SPLITRATIO_GLOBAL;
    }

    private Bill geneBillForConsumer(String orderId, String userId, String userName, double operatorFee,
        double supplierFee) {
        Bill bill = new Bill(UUID.randomUUID().toString());
        bill.setOrderId(orderId);
        bill.setUserId(userId);
        bill.setUserName(userName);
        bill.setBillType("OUT");
        bill.setBillSubType("APPCONSUME");
        bill.setBillAmount(operatorFee + supplierFee);
        bill.setOperatorFee(operatorFee);
        bill.setSupplierFee(supplierFee);
        return bill;
    }

    private Bill geneBillForOperator(String orderId, String userId, String userName, double operatorFee) {
        Bill bill = new Bill(UUID.randomUUID().toString());
        bill.setOrderId(orderId);
        bill.setUserId(userId);
        bill.setUserName(userName);
        bill.setBillType("IN");
        bill.setBillSubType("OPERATOR");
        bill.setBillAmount(operatorFee);
        return bill;
    }

    private Bill geneBillForSupplier(String orderId, String userId, String userName, double supplierFee) {
        Bill bill = new Bill(UUID.randomUUID().toString());
        bill.setOrderId(orderId);
        bill.setUserId(userId);
        bill.setUserName(userName);
        bill.setBillType("IN");
        bill.setBillSubType("APPSUPPLY");
        bill.setBillAmount(supplierFee);
        return bill;
    }
}
