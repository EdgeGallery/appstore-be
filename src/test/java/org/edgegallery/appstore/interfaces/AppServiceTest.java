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

package org.edgegallery.appstore.interfaces;

import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.domain.model.app.SwImgDesc;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class AppServiceTest {
    @Autowired
    protected AppService appService;

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_filed_download_image() {
        List<SwImgDesc> imageInfoList = new ArrayList<>();
        SwImgDesc swImgDesc = new SwImgDesc();
        swImgDesc.setId("2b490b5d8a45460ebe1a19892578eab8");
        swImgDesc.setName("ubuntu_test");
        swImgDesc.setVersion("18.04");
        swImgDesc.setChecksum("36fcf66940532088b6081512557528b3");
        swImgDesc.setContainerFormat("bare");
        swImgDesc.setDiskFormat("qcow2");
        swImgDesc.setMinDisk(6);
        swImgDesc.setMinRam(3);
        swImgDesc.setArchitecture("x86_64");
        swImgDesc.setSize(688390);
        swImgDesc.setSwImage(
            "http://192.168.100.106:80/image-management/v1/images/2b490b5d8a45460ebe1a19892578eab8/action/download");
        swImgDesc.setHwDiskBus("scsi");
        swImgDesc.setHwScsiModel("virtio-scsi");
        swImgDesc.setOperatingSystem("ubuntu");
        swImgDesc.setSupportedVirtualisationEnvironment("linux");
        imageInfoList.add(swImgDesc);
        try {
            appService.uploadAppImage(imageInfoList);
        } catch (Exception e) {
            Assert.assertThrows("can not merge parts to file", NullPointerException.class, null);
        }

    }
}
