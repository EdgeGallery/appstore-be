/* Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.interfaces.order.web;

import org.edgegallery.appstore.application.inner.BillService;
import org.edgegallery.appstore.interfaces.TestApplicationWithAdmin;
import org.edgegallery.appstore.interfaces.order.facade.OrderServiceFacade;
import org.edgegallery.appstore.interfaces.order.facade.dto.CreateOrderReqDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplicationWithAdmin.class)
@AutoConfigureMockMvc
public class BillServiceTest {
    @Autowired
    private OrderServiceFacade orderServiceFacade;

    @Autowired
    protected BillService billService;

    @Test
    public void generateBills() throws Exception {
        createOrder();
        billService.generateBill();
    }

    private void createOrder() {
        CreateOrderReqDto createOrderReqDto = new CreateOrderReqDto();
        createOrderReqDto.setAppId("appid-test-0001");
        createOrderReqDto.setAppPackageId("packageid-0003");
        createOrderReqDto.setMecHostIp("127.0.0.1");
        orderServiceFacade.createOrder("d0f8fa57-2f4c-4182-be33-0a508964d04a", "test-username-fororder", createOrderReqDto);
    }
}
