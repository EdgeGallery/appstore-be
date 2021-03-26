/* Copyright 2020-2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.interfaces.pushapp.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class PushPackageTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Gson gson = new Gson();

    @Before
    public void setUp() throws Exception {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).apply(springSecurity()).build();
    }

    @WithMockUser(roles = "APPSTORE_ADMIN")
    @Test
    public void should_success_when_get_all_pushablepackages() throws Exception {
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get("/mec/appstore/v1/packages/pushable")
            .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        assertEquals(200, result);
        String content = mvcResult.getResponse().getContentAsString();
        List<PushablePackageDto> packages = gson
            .fromJson(content, new TypeToken<List<PushablePackageDto>>() { }.getType());
        assertFalse(packages.isEmpty());
    }

    @WithMockUser(roles = "APPSTORE_ADMIN")
    @Test
    public void should_success_when_get_pushablepackages() throws Exception {
        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.get("/mec/appstore/v1/packages/packageid-0002/pushable")
                .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        assertEquals(200, result);
        String content = mvcResult.getResponse().getContentAsString();
        PushablePackageDto packageDto = gson.fromJson(content, PushablePackageDto.class);
        assertEquals("packageid-0002", packageDto.getPackageId());
    }

    @WithMockUser(roles = "APPSTORE_ADMIN")
    @Test
    public void should_success_when_push_package_notice() throws Exception {
        PushTargetAppStoreDto dto = new PushTargetAppStoreDto();
        List<String> targetPlatform = new ArrayList<>();
        targetPlatform.add("appstore-test-0001");
        targetPlatform.add("appstore-test-0002");
        dto.setTargetPlatform(targetPlatform);

        MvcResult mvcResult = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/packages/packageid-0002/action/push")
                .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).content(gson.toJson(dto))
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        int result = mvcResult.getResponse().getStatus();
        assertEquals(200, result);
        String content = mvcResult.getResponse().getContentAsString();

        // because of can not send the message to the other host in the Junit, so there is false
        // but when the return code is 200, this test case is ok
        assertEquals("[false,false]", content);
    }

}
