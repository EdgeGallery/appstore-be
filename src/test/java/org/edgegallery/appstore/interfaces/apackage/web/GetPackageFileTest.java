/* Copyright 2020 Huawei Technologies Co., Ltd.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class GetPackageFileTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_get_package_file_success() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549337";
        String filePath = "AR_app:MainServiceTemplate.mf";
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.post(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "/files?filePath=" + filePath).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        System.out.println(obj.getContentAsString());
        Assert.assertTrue(obj.getContentAsString().length() > 100);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_get_package_file_failed_with_blank_in_file_name() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549337";
        String filePath = "ab cd.md";
        try {
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.post(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "/files?filePath=" + filePath).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        } catch (NestedServletException e) {
            Assert.assertEquals("ab cd.md :filepath contain blank", e.getRootCause().getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_get_package_file_failed_with_empty_file_name() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549337";
        String filePath = "";
        try {
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.post(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "/files?filePath=" + filePath).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        } catch (NestedServletException e) {
            Assert.assertEquals(" :filepath is empty", e.getRootCause().getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_get_package_file_failed_with_filetype_not_support() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549337";
        String filePath = "AR_app:MainServiceTemplate.doc";
        try {
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.post(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "/files?filePath=" + filePath).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        } catch (NestedServletException e) {
            Assert.assertEquals(null, e.getRootCause().getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_get_package_file_failed_with_file_not_exist() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549337";
        String filePath = "AR_app:Artifacts:ChangeLog1.txt";
        try {
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.post(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "/files?filePath=" + filePath).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        } catch (FileNotFoundException e) {
            Assert.assertTrue(!StringUtils.isEmpty(e.getMessage()));
        }
    }
}
