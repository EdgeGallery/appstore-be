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

package org.edgegallery.appstore.interfaces.apackage.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.app.facade.dto.QueryAppCtrlDto;
import org.edgegallery.appstore.interfaces.message.facade.dto.MessageRespDto;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class GetPackageByIdTest extends AppTest {

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/apps/%s/packages/%s", appId, packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_with_wrong_appId() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af3";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/apps/%s/packages/%s", appId, packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_with_wrong_packageId() throws Exception {
        String packageId = "30ec10f4a43041e6a6198ba824311af3";
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v1/apps/%s/packages/%s", appId, packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_packageId_v2() throws Exception {
        MvcResult result = mvc.perform(
            MockMvcRequestBuilders.get(String.format("/mec/appstore/v2/apps/%s/packages/%s", appId, packageId))
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_packages() throws Exception {
        Optional.ofNullable(packageMapper.findReleaseById(unPublishedPackageId)).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Test_failed.toString());
            packageMapper.updateRelease(r);
        });
        Mockito.when(atpService.getAtpTaskResult(Mockito.any(), Mockito.nullable(String.class))).thenReturn("created");
        MvcResult result = mvc.perform(MockMvcRequestBuilders
            .get("/mec/appstore/v1/packages")
            .param("userId", userId).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_packages_v2() throws Exception {
        Optional.ofNullable(packageMapper.findReleaseById(unPublishedPackageId)).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Test_failed.toString());
            packageMapper.updateRelease(r);
        });
        Mockito.when(atpService.getAtpTaskResult(Mockito.any(), Mockito.nullable(String.class))).thenReturn("created");
        MvcResult result = mvc.perform(MockMvcRequestBuilders
            .get("/mec/appstore/v2/packages")
            .param("limit", String.valueOf(10))
            .param("offset", String.valueOf(0))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_GUEST")
    public void should_success_ByCreateTime() throws Exception {
        Optional.ofNullable(packageMapper.findReleaseById(unPublishedPackageId)).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Test_failed.toString());
            packageMapper.updateRelease(r);
        });
        Mockito.when(atpService.getAtpTaskResult(Mockito.any(), Mockito.nullable(String.class))).thenReturn("created");
        MvcResult result = mvc.perform(MockMvcRequestBuilders
            .get("/mec/appstore/v2/packages")
            .param("limit", String.valueOf(10))
            .param("offset", String.valueOf(0))
            .param("startTime", "2021-11-1")
            .param("endTime", "2121-11-1")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_queryPackagesByCond() throws Exception {
        QueryAppCtrlDto ctrDto = new QueryAppCtrlDto();
        ctrDto.setLimit(15);
        ctrDto.setOffset(0);
        ctrDto.setSortItem("createTime");
        ctrDto.setSortType("desc");
        ctrDto.setAppName("");
        List<String> status = new ArrayList<>();
        status.add("Published");
        ctrDto.setStatus(status);

        String body = new Gson().toJson(ctrDto);
        MvcResult actions = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v2/packages/action/query").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(body).with(csrf())).andDo(MockMvcResultHandlers.print())
            .andReturn();
        String page = actions.getResponse().getContentAsString();
        JSONObject jsonObject1 = JSONObject.parseObject(page);
        JSONArray listObject = jsonObject1.getJSONArray("results");
        int listCount = JSONObject.parseArray(listObject.toJSONString(), MessageRespDto.class).size();
        Assert.assertTrue(listCount > 0);
    }
}
