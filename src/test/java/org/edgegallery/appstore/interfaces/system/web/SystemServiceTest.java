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

package org.edgegallery.appstore.interfaces.system.web;

import com.spencerwi.either.Either;
import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.domain.model.system.EnumHostStatus;
import org.edgegallery.appstore.domain.model.system.MepCreateHost;
import org.edgegallery.appstore.domain.model.system.MepHost;
import org.edgegallery.appstore.domain.shared.ResponseObject;
import org.edgegallery.appstore.infrastructure.util.FormatRespDto;
import org.edgegallery.appstore.interfaces.system.facade.SystemService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = DeveloperApplicationTests.class)
@RunWith(SpringRunner.class)
public class SystemServiceTest {

    @Autowired
    private SystemService systemService;

    @Before
    public void init() {
        System.out.println("start to test");
    }

    @After
    public void after() {
        System.out.println("test over");
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testGetAll() {
        List<MepHost> res = systemService.getAllHosts("host", "10.1.12.1");
        Assert.assertNotNull(res);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testCreateHostWithNullUserName() {
        Either<ResponseObject, Boolean> res = systemService.createHost(new MepCreateHost(), "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testCreateHostWithNullPwd() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        Either<ResponseObject, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testCreateHostWithNullUserId() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        Either<ResponseObject, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testCreateHostWithErrorLcmIp() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("10.2.3.1");
        host.setPort(30200);
        host.setUserId(UUID.randomUUID().toString());
        Either<ResponseObject, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testCreateHostWithErrorConfId() {
        MepCreateHost host = new MepCreateHost();
        host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<ResponseObject, Boolean> res = systemService.createHost(host, "");
        // Assert.assertNull(res);
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testDeleteHostWithErrorId() {
        Either<ResponseObject, Boolean> res = systemService.deleteHost("hostId");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "DEVELOPER_TENANT")
    public void testDeleteHostSuccess() {
        Either<ResponseObject, Boolean> res = systemService.deleteHost("c8aac2b2-4162-40fe-9d99-0630e3245cf7");
        Assert.assertTrue(res.isRight());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testUpdateHost() {
        MepCreateHost host = new MepCreateHost();
        // host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<ResponseObject, Boolean> res = systemService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf7", host,"");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testUpdateHostError() {
        MepCreateHost host = new MepCreateHost();
        // host.setHostId(UUID.randomUUID().toString());
        host.setName("onlineever");
        host.setAddress("address");
        host.setArchitecture("x86");
        host.setStatus(EnumHostStatus.NORMAL);
        host.setLcmIp("127.0.0.1");
        host.setPort(30204);
        host.setConfigId("errorId");
        host.setUserId(UUID.randomUUID().toString());
        Either<ResponseObject, Boolean> res = systemService.updateHost("c8aac2b2-4162-40fe-9d99-0630e3245cf789", host,"");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testGetHostError() {
        Either<ResponseObject, MepHost> res = systemService.getHost("c8aac2b2-4162-40fe-9d99-0630e3245cf789");
        Assert.assertTrue(res.isLeft());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void testGetHostSuccess() {
        Either<ResponseObject, MepHost> res = systemService.getHost("c8aac2b2-4162-40fe-9d99-0630e3245cdd");
        Assert.assertTrue(res.isRight());
    }





}
