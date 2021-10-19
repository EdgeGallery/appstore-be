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

package org.edgegallery.appstore.interfaces.app.web;

import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.util.IpCalculateUtil;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.system.facade.ProjectService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

public class DeployAppByIdTest extends AppTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IpCalculateUtil ipCalculateUtil;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_no_appInstanceId() {
        try {
            ResponseEntity<ResponseObject> res = projectService
                .deployAppById("appid-test-0001", "packageid-0002", "e111f3e7-90d8-4a39-9874-ea6ea6752eaa", "host-1",
                    "", "access_token");
            Assert.assertEquals("please register host.", res.getBody().getMessage());
        } catch (NullPointerException e) {
            Assert.assertThrows("please register host.", NullPointerException.class, null);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_get_ip() {
        String segment = "192.168.225.0/24";
        int range = 1;
        String res  = ipCalculateUtil.getStartIp(segment, range);
        Assert.assertEquals("192.168.225.4", res);
    }

}
