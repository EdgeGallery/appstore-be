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

package org.edgegallery.appstore.interfaces.comment.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import org.edgegallery.appstore.interfaces.AppInterfacesTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.NestedServletException;

public class LeaveCommentsTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_comment_leave_success() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af3";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";
        CommentBody body = new CommentBody("good app", 4.5);
        Gson gson = new Gson();
        String requestJson = gson.toJson(body);
        ResultActions resultActions = mvc.perform(
            MockMvcRequestBuilders.post("/mec/appstore/v1/apps/" + appId + "/comments"
            +"?userId=" + userID +"&userName=" + userName)
                    .content(requestJson)
                    .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertEquals("comments success.", obj.getContentAsString());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_comment_leave_failed_with_wrong_body() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af3";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";
        TestBody body = new TestBody("good app", 4.5);
        Gson gson = new Gson();
        String requestJson = gson.toJson(body);
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.post("/mec/appstore/v1/apps/" + appId + "/comments"
                        +"?userId=" + userID +"&userName=" + userName)
                        .content(requestJson)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertEquals("", obj.getContentAsString());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_comment_leave_failed_with_empty_body() throws Exception {
        String appId = "30ec10f4a43041e6a6198ba824311af3";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";
        ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.post("/mec/appstore/v1/apps/" + appId + "/comments"
                        +"?userId=" + userID +"&userName=" + userName)
                        .content("")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isBadRequest());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertEquals("", obj.getContentAsString());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_comment_leave_failed_with_wrong_appid() throws Exception {
        String appId = "appid_test_wrong";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";

        CommentBody body = new CommentBody("good app", 4.5);
        Gson gson = new Gson();
        String requestJson = gson.toJson(body);
        try{
            ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.post("/mec/appstore/v1/apps/" + appId + "/comments"
                    +"?userId=" + userID +"&userName=" + userName)
                    .content(requestJson)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
        } catch (NestedServletException e) {
            Assert.assertEquals("addComments.appId: must match \"[0-9a-f]{32}\"", e.getRootCause().getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_comment_leave_failed_with_not_exist_appid() throws Exception {
        String appId = "63c79ce8551143609ebf615f4ada48cb";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";
        CommentBody body = new CommentBody("good app", 4.5);
        Gson gson = new Gson();
        String requestJson = gson.toJson(body);
        try{
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.post("/mec/appstore/v1/apps/" + appId + "/comments"
                            +"?userId=" + userID +"&userName=" + userName)
                            .content(requestJson)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)).andExpect(MockMvcResultMatchers.status().isNotFound());
        } catch (NestedServletException e) {
            Assert.assertEquals("cannot find the app with id 63c79ce8551143609ebf615f4ada48cb", e.getRootCause().getMessage());
        }
    }


    public class CommentBody{
        private String body;
        private double score;

        public CommentBody(String body, double score) {
            this.body = body;
            this.score = score;
        }
    }

    public class TestBody{
        private String content;
        private double number;

        public TestBody(String content, double number) {
            this.content = content;
            this.number = number;
        }
    }
}
