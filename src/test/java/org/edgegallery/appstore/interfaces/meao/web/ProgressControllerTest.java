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

package org.edgegallery.appstore.interfaces.meao.web;

import com.google.gson.Gson;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class ProgressControllerTest extends AppTest {
    @Autowired
    protected MockMvc mvc;

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_create_progress() throws Exception {
        PackageUploadProgress progress = new PackageUploadProgress();
        progress.setProgress("progress");
        progress.setPackageId("packageId");
        progress.setStatus("start");
        progress.setId("id");
        progress.setMeaoId("meaoId");
        String body = new Gson().toJson(progress);
        MvcResult actions = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/upload_progress").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(body).with(csrf())).andDo(MockMvcResultHandlers.print())
            .andReturn();
        String page = actions.getResponse().getContentAsString();
        Assert.assertEquals("create process success.", page);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_get_progress() throws Exception {
        String progressId = "c8aac2b2-4162-40fe-9d99-0630e3245bbb";
        MvcResult actions = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/upload_progress/%s", progressId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Integer status = actions.getResponse().getStatus();
        Assert.assertEquals(java.util.Optional.of(HttpStatus.OK.value()), java.util.Optional.of(status));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_update_progress() throws Exception {
        PackageUploadProgress progress = new PackageUploadProgress();
        progress.setPackageId("packageId");
        progress.setStatus("start");
        progress.setId("c8aac2b2-4162-40fe-9d99-0630e3245aaa");
        progress.setMeaoId("meaoId-6");
        String body = new Gson().toJson(progress);
        MvcResult actions = mvc.perform(
            MockMvcRequestBuilders.put("/mec/appstore/v1/upload_progress").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(body).with(csrf())).andDo(MockMvcResultHandlers.print())
            .andReturn();
        Integer status = actions.getResponse().getStatus();
        Assert.assertEquals(java.util.Optional.of(HttpStatus.OK.value()), java.util.Optional.of(status));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_delete_progress() throws Exception {
        String progressId = "c8aac2b2-4162-40fe-9d99-0630e3245fff";
        MvcResult actions = mvc.perform(
            MockMvcRequestBuilders.delete(String.format("/mec/appstore/v1/upload_progress/%s", progressId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Integer status = actions.getResponse().getStatus();
        Assert.assertEquals(java.util.Optional.of(HttpStatus.OK.value()), java.util.Optional.of(status));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_success_get_progress_by_meao() throws Exception {
        String packageId = "package-1";
        String meaoId = "meao-1";
        MvcResult actions = mvc.perform(MockMvcRequestBuilders
            .get(String.format("/mec/appstore/v1/upload_progress/package/%s/meao/%s", packageId, meaoId)).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Integer status = actions.getResponse().getStatus();
        Assert.assertEquals(java.util.Optional.of(HttpStatus.OK.value()), java.util.Optional.of(status));
    }

    @Test
    @WithMockUser(roles = "APPSTORE_ADMIN")
    public void should_failed_get_progress_by_packageId() throws Exception {
        String packageId = "package-1";
        MvcResult actions = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/upload_progress/package/%s", packageId))
                .with(csrf()).contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Integer status = actions.getResponse().getStatus();
        Assert.assertNotSame(java.util.Optional.of(HttpStatus.OK.value()), java.util.Optional.of(status));
    }
}
