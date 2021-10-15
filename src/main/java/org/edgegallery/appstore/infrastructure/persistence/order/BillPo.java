/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.order.Bill;
import org.edgegallery.appstore.infrastructure.persistence.PersistenceObject;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@Entity
@Table(name = "app_bill")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillPo implements PersistenceObject<Bill> {

    @Id
    @Column(name = "BILLID")
    private String billId;

    @Column(name = "ORDERID")
    private String orderId;

    @Column(name = "CREATETIME")
    private String createTime;

    @Column(name = "USERID")
    private String userId;

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "BILLTYPE")
    private String billType;

    @Column(name = "BILLSUBTYPE")
    private String billSubType;

    @Column(name = "BILLAMOUNT")
    private double billAmount;

    @Column(name = "OPERATORFEE")
    private double operatorFee;

    @Column(name = "SUPPLIERFEE")
    private double supplierFee;

    public BillPo() {
        // empty construct
    }

    static BillPo of(Bill bill) {
        BillPo po = new BillPo();
        BeanUtils.copyProperties(bill, po);
        return po;
    }

    @Override
    public Bill toDomainModel() {
        return Bill.builder()
            .billId(billId)
            .orderId(orderId)
            .createTime(createTime)
            .userId(userId)
            .userName(userName)
            .billType(billType)
            .billSubType(billSubType)
            .billAmount(billAmount)
            .operatorFee(operatorFee)
            .supplierFee(supplierFee)
            .build();
    }
}
