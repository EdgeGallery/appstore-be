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

import java.util.Calendar;
import java.util.Date;
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
import org.edgegallery.appstore.domain.model.order.EnumOrderStatus;
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
     * generate bill on deactivate action.
     *
     * @param order order object
     * @param activateDateTime activate time
     */
    public void generateBillOnDeactivate(Order order, Date activateDateTime) {
        LOGGER.info("generate bill on deactivate order, orderNum = {}", order.getOrderNum());
        if (activateDateTime == null) {
            LOGGER.error("activate time is invalid.");
            return;
        }

        generateBillByOrder(order, activateDateTime, true);
    }

    /**
     * generate bill.
     */
    public void generateBill() {
        LOGGER.info("generate bills begin.");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("status", EnumOrderStatus.ACTIVATED.toString());
        List<Order> activeOrderList = orderRepository.queryOrders(queryParams);

        LOGGER.info("generate bills for {} active order(s).", activeOrderList.size());
        for (Order activeOrder : activeOrderList) {
            generateBillByOrder(activeOrder, activeOrder.getOperateTime(), false);
        }

        LOGGER.info("generate bills end.");
    }

    private void generateBillByOrder(Order order, Date activateDateTime, boolean isDeactivating) {
        LOGGER.info("generate bills for order begin, orderNum = {}", order.getOrderNum());
        Optional<App> appOptional = appRepository.find(order.getAppId());
        if (!appOptional.isPresent()) {
            LOGGER.warn("the app of order had been deleted! appId = {}", order.getAppId());
            return;
        }

        App app = appOptional.get();
        if (app.isFree() || app.getPrice() == 0) {
            LOGGER.debug("the app of order is free, appId = {}", order.getAppId());
            return;
        }

        double splitRatio = getSplitConfig(app.getAppId());
        double totalAmount = calcTotalAmount(app.getPrice(), activateDateTime, isDeactivating);
        if (totalAmount <= 0) {
            LOGGER.debug("total amount is zero this calc time, orderNum = {}", order.getOrderNum());
            return;
        }

        double operatorFee = totalAmount * splitRatio;
        double supplierFee = totalAmount - operatorFee;
        billRepository.addBill(geneBillForConsumer(order.getOrderId(), order.getUserId(),
            order.getUserName(), operatorFee, supplierFee));
        billRepository.addBill(geneBillForOperator(order.getOrderId(), Consts.SUPER_ADMIN_ID,
            Consts.SUPER_ADMIN_NAME, operatorFee));
        billRepository.addBill(geneBillForSupplier(order.getOrderId(), app.getUserId(),
            app.getUser().getUserName(), supplierFee));
        LOGGER.info("generate bills for order end, orderNum = {}", order.getOrderNum());
    }

    private double calcTotalAmount(double price, Date activateDateTime, boolean isDeactivating) {
        long currTime = new Date().getTime();
        long activateOperTime = activateDateTime.getTime();
        long activeKeepTime = currTime - activateOperTime;
        long calcHour = 0;
        if (isDeactivating) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long todayZeroTime = calendar.getTimeInMillis();

            long keepActiveHour = activeKeepTime / (1000 * 60 * 60);
            calcHour = Math.min(keepActiveHour, (currTime - todayZeroTime) / (1000 * 60 * 60));
        } else {
            calcHour = 24;
            if (activeKeepTime < 1 * 24 * 60 * 60 * 1000) {
                long keepActiveHour = activeKeepTime / (1000 * 60 * 60);
                calcHour = keepActiveHour;
            }
        }

        return price * calcHour;
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
