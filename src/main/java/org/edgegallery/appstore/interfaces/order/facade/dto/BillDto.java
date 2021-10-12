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

package org.edgegallery.appstore.interfaces.order.facade.dto;

import java.text.SimpleDateFormat;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.order.BillExtendEntity;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class BillDto {
    private String billId;

    private String orderId;

    private String orderNum;

    private String orderUserId;

    private String orderUserName;

    private String billUserId;

    private String billUserName;

    private String appId;

    private String appName;

    private String provider;

    private String createTime;

    private String billType;

    private String billSubType;

    private double billAmount;

    private double operatorFee;

    private double supplierFee;

    /**
     * convert bill extend entity to dto object.
     *
     * @param billExtendEntity bill extend entity
     * @return dto object
     */
    public static BillDto of(BillExtendEntity billExtendEntity) {
        BillDto billDto = new BillDto();
        BeanUtils.copyProperties(billExtendEntity, billDto);
        billDto.createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(billExtendEntity.getCreateTime());
        return billDto;
    }
}
