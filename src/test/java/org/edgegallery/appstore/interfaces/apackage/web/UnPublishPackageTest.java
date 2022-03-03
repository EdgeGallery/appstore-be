/* Copyright 2022 Huawei Technologies Co., Ltd.
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


import java.util.Optional;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PublishAppReqDto;
import org.edgegallery.appstore.interfaces.controlleradvice.RestReturn;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

public class UnPublishPackageTest extends AppTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success() throws Exception {
        MvcResult result = mvc.perform(MockMvcRequestBuilders
            .post(String.format("/mec/appstore/v1/apps/%s/packages/%s/action/unPublish", appId, packageId)).with(csrf())
            .content(gson.toJson(new PublishAppReqDto()))
            .param("userId", userId).param("userName", userName)
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        result = mvc.perform(MockMvcRequestBuilders
            .post(String.format("/mec/appstore/v1/apps/%s/packages/%s/action/publish", appId, packageId)).with(csrf())
            .content(gson.toJson(new PublishAppReqDto()))
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed() throws Exception {
        String testAppId = "3993111199fe4cd8881f04ca8c4fe09e";
        String testPackageId = "3993111199fe4cd8881f04ca8c4fe09e";
        MvcResult result = mvc.perform(MockMvcRequestBuilders
            .post(String.format("/mec/appstore/v1/apps/%s/packages/%s/action/unPublish", testAppId, testPackageId)).with(csrf())
            .content(gson.toJson(new PublishAppReqDto()))
            .param("userId", userId).param("userName", userName)
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        RestReturn restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_APP_NOT_FOUND, restReturn.getRetCode());

        result = mvc.perform(MockMvcRequestBuilders
            .post(String.format("/mec/appstore/v1/apps/%s/packages/%s/action/unPublish", appId, testPackageId)).with(csrf())
            .content(gson.toJson(new PublishAppReqDto()))
            .param("userId", userId).param("userName", userName)
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_PACKAGE_NOT_FOUND, restReturn.getRetCode());

        Optional.ofNullable(packageMapper.findReleaseById(unPublishedPackageId)).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Test_success.toString());
            packageMapper.updateRelease(r);
        });
        result = mvc.perform(MockMvcRequestBuilders
            .post(String.format("/mec/appstore/v1/apps/%s/packages/%s/action/unPublish", appId, unPublishedPackageId)).with(csrf())
            .content(gson.toJson(new PublishAppReqDto()))
            .param("userId", userId).param("userName", userName)
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
        restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_OFFSHELF_NO_PUBLISH, restReturn.getRetCode());

        String testUserId = "39937079-99fe-4cd8-881f-04ca8c4fe09d";
        String testUserName = "test-username-unPublish";
        result = mvc.perform(MockMvcRequestBuilders
            .post(String.format("/mec/appstore/v1/apps/%s/packages/%s/action/unPublish", appId, packageId)).with(csrf())
            .content(gson.toJson(new PublishAppReqDto()))
            .param("userId", testUserId).param("userName", testUserName)
            .contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn();

        Assert.assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
        restReturn = gson.fromJson(result.getResponse().getContentAsString(), RestReturn.class);
        Assert.assertEquals(ResponseConst.RET_NO_ACCESS_OFFSHELF_PACKAGE, restReturn.getRetCode());
    }
}
