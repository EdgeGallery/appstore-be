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

package org.edgegallery.appstore.interfaces.app.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.interfaces.AppInterfacesTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class AppRegisteringTest extends AppInterfacesTest {

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_app_register_success() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File iconFile = Resources.getResourceAsFile(AR_PNG);
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "GPU".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Campus".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    public void should_app_register_fail_with_no_permission() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File iconFile = Resources.getResourceAsFile(AR_PNG);
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "GPU".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Campus".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andReturn();
            int statusResult = mvcResult.getResponse().getStatus();
            Assert.assertEquals(HttpStatus.UNAUTHORIZED.value(), statusResult);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_app_register_fail_with_no_csarFile() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File iconFile = Resources.getResourceAsFile(AR_PNG);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("icon", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "GPU".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Campus".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_app_register_fail_with_no_iconFile() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "GPU".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Campus".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_app_register_fail_with_no_typeField() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File iconFile = Resources.getResourceAsFile(AR_PNG);
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "GPU".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Campus".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_app_register_fail_with_no_shortDescField() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File iconFile = Resources.getResourceAsFile(AR_PNG);
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "GPU".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Campus".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_app_register_fail_with_no_affinityField() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File iconFile = Resources.getResourceAsFile(AR_PNG);
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Campus".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_app_register_fail_with_no_industryField() {
        String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";
        String userName = "username";

        try {
            File iconFile = Resources.getResourceAsFile(AR_PNG);
            File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "GPU".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

}
