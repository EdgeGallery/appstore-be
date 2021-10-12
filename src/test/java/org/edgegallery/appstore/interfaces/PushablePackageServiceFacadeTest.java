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
import org.edgegallery.appstore.application.inner.PullablePackageService;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.interfaces.apackage.facade.PushablePackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import mockit.Mock;
import mockit.MockUp;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class PushablePackageServiceFacadeTest {
    @Autowired
    public PushablePackageServiceFacade pushablePackageServiceFacade;

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_getPullablePackagesV2() {
        new MockUp<PullablePackageService>() {
            @Mock
            public List<PushablePackageDto> getPullablePackages(String platformId, String userId, String sortType,
                String sortItem, String appName) {
                PushablePackageDto packagePo = new PushablePackageDto();
                packagePo.setAppId("2bc69d567b3740208306ea192a591209");
                packagePo.setPackageId("3bc69d567b3740208306ea192a591209");
                packagePo.setAtpTestReportUrl("127.0.0.1");
                packagePo.setSourcePlatform("127.0.0.1");
                packagePo.setAffinity("affinity");
                packagePo.setAtpTestStatus("success");
                packagePo.setAtpTestTaskId("testId");
                packagePo.setCreateTime("2021-04-13 18:32:09");
                packagePo.setAtpTestStatus("success");
                List<PushablePackageDto> list = new ArrayList<>();
                list.add(packagePo);
                return list;
            }
        };
        String platformId = "";
        int limit = 1;
        long offset = 1;
        String sortType = "";
        String sortItem = "";
        String appName = "";
        String userId = "";
        ResponseEntity<Page<PushablePackageDto>> responseEntity = pushablePackageServiceFacade
            .getPullablePackagesV2(platformId, limit, offset, sortType, sortItem, appName, userId);
        Assert.assertEquals(responseEntity.getStatusCode().value(), 200);
    }
}
