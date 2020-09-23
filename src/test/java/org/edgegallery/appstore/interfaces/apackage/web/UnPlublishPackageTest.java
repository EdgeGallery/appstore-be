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
import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class UnPlublishPackageTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_package_unPublish_success() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = REAL_PACKAGE_ID_WILL_DELETE;
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";
        try {
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            File storeFile = new File(
                "" + File.separator + "home" + File.separator + APPSTORE_ROOT + File.separator + appId
                    + File.separator + packageId + CSAR_EXTENSION);
            createFile(storeFile.getCanonicalPath());
            FileUtils.copyFile(csarFile, storeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResultActions resultActions = mvc.perform(
            MockMvcRequestBuilders.delete(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                    "?userId=" + userID +"&userName=" + userName)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());

        MvcResult result = resultActions.andReturn();
        MockHttpServletResponse obj = result.getResponse();
        Assert.assertEquals(obj.getContentAsString(), "delete App success.");
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_package_unPublish_failed_with_not_exist_packageid() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549348";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";
        try {
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.delete(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "?userId=" + userID +"&userName=" + userName)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isNotFound());
        } catch (NestedServletException e) {
            Assert.assertEquals("No release with versionId 44a00b12c13b43318d21840793549348 exists in the system", e.getRootCause().getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_package_unPublish_failed_with_wrong_packageid() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "wrongId";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada48cb";
        String userName = "testuser001";
        try {
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.delete(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "?userId=" + userID +"&userName=" + userName)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        } catch (NestedServletException e) {
            Assert.assertEquals("unPublishPackage.packageId: must match \"[0-9a-f]{32}\"", e.getRootCause().getMessage());
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_package_unPublish_failed_with_wrong_userid() throws Exception {
        String appId = REAL_APP_ID;
        String packageId = "44a00b12c13b43318d21840793549339";
        String userID = "63c79ce8-5511-4360-9ebf-615f4ada12";
        String userName = "testuser001";
        try {
            ResultActions resultActions = mvc.perform(
                    MockMvcRequestBuilders.delete(REST_API_ROOT + appId + REST_API_PACKAGES + packageId +
                            "?userId=" + userID +"&userName=" + userName)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        } catch (NestedServletException e) {
            Assert.assertEquals("unPublishPackage.userId: must match \"[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\"", e.getRootCause().getMessage());
        }
    }
}
