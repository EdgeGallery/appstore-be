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

package org.edgegallery.appstore.interfaces.app.web;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.github.dockerjava.api.exception.DockerClientException;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.application.inner.AppService;
import org.edgegallery.appstore.domain.model.app.Chunk;
import org.edgegallery.appstore.domain.model.app.SwImgDesc;
import org.edgegallery.appstore.domain.shared.exceptions.FileOperateException;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.app.facade.AppServiceFacade;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

public class AppRegisterTest extends AppTest {

    @Autowired
    private AppServiceFacade appServiceFacade;

    @Autowired
    private AppService appService;

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success() {
        try {
            MvcResult mvcResult = registerApp(LOGO_PNG, TEST2048_UNIQUE_CSAR, userId, userName);
            Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
            new Gson().fromJson(mvcResult.getResponse().getContentAsString(), RegisterRespDto.class);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_with_testTaskId() {
        String testTaskId = "test task id";

        try {
            Mockito.when(atpService.getAtpTaskResult(Mockito.any(), Mockito.any())).thenReturn("success");
            MvcResult mvcResult = registerApp(LOGO_PNG, TEST2048_UNIQUE_CSAR, userId, userName, testTaskId);
            Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
            new Gson().fromJson(mvcResult.getResponse().getContentAsString(), RegisterRespDto.class);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_failed_with_wrong_signature() {
        try {
            MvcResult mvcResult = registerApp(LOGO_PNG, BATTLE_CITY_CSAR, userId, userName);
            Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), mvcResult.getResponse().getStatus());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_fail_with_same_app() {
        try {
            MvcResult mvcResult = registerApp(LOGO_PNG, TEST2048_1_CSAR, userId, userName);
            Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), mvcResult.getResponse().getStatus());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_fail_with_no_image() {
        try {
            MvcResult mvcResult = registerApp(LOGO_PNG, NEW_CSAR, userId, userName);
            Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), mvcResult.getResponse().getStatus());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_container_with_image() {
        try {
            MvcResult mvcResult = registerApp(LOGO_PNG, CONTAINER_IMAGE_CSAR, userId, userName);
            Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
            new Gson().fromJson(mvcResult.getResponse().getContentAsString(), RegisterRespDto.class);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_fail_with_no_csarFile() {
        try {
            File iconFile = Resources.getResourceAsFile(LOGO_PNG);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("icon", "logo.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video Application".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "X86".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Park".getBytes()))
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
    public void should_fail_with_no_iconFile() {
        try {
            File csarFile = Resources.getResourceAsFile(TEST2048_1_CSAR);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "positioning_eg_1.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video Application".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "X86".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Park".getBytes()))
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
    public void should_fail_with_no_typeField() {
        try {
            File iconFile = Resources.getResourceAsFile(LOGO_PNG);
            File csarFile = Resources.getResourceAsFile(TEST2048_1_CSAR);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "positioning_eg_1.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "logo.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "X86".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Park".getBytes()))
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
    public void should_with_no_affinityField() {
        try {
            File iconFile = Resources.getResourceAsFile(LOGO_PNG);
            File csarFile = Resources.getResourceAsFile(TEST2048_1_CSAR);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "positioning_eg_1.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "logo.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video Application".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Park".getBytes()))
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
        try {
            File iconFile = Resources.getResourceAsFile(LOGO_PNG);
            File csarFile = Resources.getResourceAsFile(TEST2048_1_CSAR);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "positioning_eg_1.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "logo.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video Application".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "X86".getBytes()))
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
    public void should_success_with_VM() {
        try {
            File csarFile = Resources.getResourceAsFile(TEST2048_1_CSAR);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps/upload")
                .file(new MockMultipartFile("file", "positioning_eg_1.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile))).with(csrf()));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.OK.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_with_Chun() {
        try {
            File csarFile = Resources.getResourceAsFile(TEST2048_1_CSAR);
            FileInputStream fileInputStream = new FileInputStream(csarFile);
            MultipartFile multipartFile = new MockMultipartFile("file", csarFile.getName(), "text/plain",
                IOUtils.toByteArray(fileInputStream));
            Chunk chunk = new Chunk();
            chunk.setFile(multipartFile);
            chunk.setChunkSize(8 * 1024 * 1024L);
            MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post("/mec/appstore/v1/apps/upload")
                .contentType(MediaType.APPLICATION_JSON_VALUE).with(csrf()).content(gson.toJson(chunk))
                .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
            int result = mvcResult.getResponse().getStatus();
            assertEquals(200, result);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_fail_with_no_vm() {
        try {
            ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps/upload").with(csrf())
                    .param("fileName", fileName).param("guid", guid));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.BAD_REQUEST.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_fail_with_merge() {
        try {
            ResultActions resultActions = mvc.perform(
                MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps/merge").with(csrf()).param("fileName", fileName)
                    .param("guid", guid));
            MvcResult mvcResult = resultActions.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andReturn();
            int result = mvcResult.getResponse().getStatus();
            Assert.assertEquals(result, HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success_with_video() {
        try {
            File csarFile = Resources.getResourceAsFile(TEST2048_2_CSAR);
            File iconFile = Resources.getResourceAsFile(LOGO_PNG);
            File videoFile = Resources.getResourceAsFile(DEMO_VIDEO);
            ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps")
                .file(new MockMultipartFile("file", "positioning_eg_2.0.csar", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(csarFile)))
                .file(new MockMultipartFile("icon", "logo.png", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(iconFile)))
                .file(new MockMultipartFile("demoVideo", "demo_video.mp4", MediaType.TEXT_PLAIN_VALUE,
                    FileUtils.openInputStream(videoFile)))
                .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, "Video Application".getBytes()))
                .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, "Desc".getBytes()))
                .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, "X86".getBytes()))
                .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE,
                    "Smart Park".getBytes()))
                .file(new MockMultipartFile("showType", "", MediaType.TEXT_PLAIN_VALUE, "public".getBytes()))
                .with(csrf())
                .param("userId", userId)
                .param("userName", userName));
            MvcResult mvcResult = resultActions.andReturn();
            Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        } catch (Exception e) {
            Assert.assertNull(e);
        }
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void test_merge() {
        ResponseEntity<String> res  = appServiceFacade.merge("fileName","test_guid_01");
        HttpStatus ss = res.getStatusCode();
        Assert.assertEquals("200 OK", res.getStatusCode().toString());
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void test_merge_should_falied_no_guid() {
        try {
            appServiceFacade.merge("", "").getBody();
        } catch (FileOperateException e) {
            Assert.assertThrows("can not merge parts to file", NullPointerException.class, null);
        }

    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void test_updateImgInRepo_should_failed_error_info() {
        List<SwImgDesc> imageInfoList = new ArrayList<>();
        SwImgDesc swImgDesc = new SwImgDesc();
        swImgDesc.setId("2b490b5d8a45460ebe1a19892578eab8");
        swImgDesc.setName("ubuntu_test");
        swImgDesc.setVersion("18.04");
        swImgDesc.setChecksum("36fcf66940532088b6081512557528b3");
        swImgDesc.setContainerFormat("bare");
        swImgDesc.setDiskFormat("qcow2");
        swImgDesc.setMinDisk(6);
        swImgDesc.setMinRam(3);
        swImgDesc.setArchitecture("x86_64");
        swImgDesc.setSize(688390);
        swImgDesc.setSwImage(
            "http://192.168.100.106:80/image-management/v1/images/2b490b5d8a45460ebe1a19892578eab8/action/download");
        swImgDesc.setHwDiskBus("scsi");
        swImgDesc.setHwScsiModel("virtio-scsi");
        swImgDesc.setOperatingSystem("ubuntu");
        swImgDesc.setSupportedVirtualisationEnvironment("linux");
        imageInfoList.add(swImgDesc);
        try {
            appService.updateImgInRepo(imageInfoList);
        } catch (DockerClientException e) {
            Assert.assertThrows("can not merge parts to file", NullPointerException.class, null);
        }

    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void test_uploadAppImage_should_failed_error_info() {
        List<SwImgDesc> imageInfoList = new ArrayList<>();
        SwImgDesc swImgDesc = new SwImgDesc();
        swImgDesc.setId("2b490b5d8a45460ebe1a19892578eab8");
        swImgDesc.setName("ubuntu_test");
        swImgDesc.setVersion("18.04");
        swImgDesc.setChecksum("36fcf66940532088b6081512557528b3");
        swImgDesc.setContainerFormat("bare");
        swImgDesc.setDiskFormat("qcow2");
        swImgDesc.setMinDisk(6);
        swImgDesc.setMinRam(3);
        swImgDesc.setArchitecture("x86_64");
        swImgDesc.setSize(688390);
        swImgDesc.setSwImage(
            "http://192.168.100.106:80/image-management/v1/images/2b490b5d8a45460ebe1a19892578eab8/action/download");
        swImgDesc.setHwDiskBus("scsi");
        swImgDesc.setHwScsiModel("virtio-scsi");
        swImgDesc.setOperatingSystem("ubuntu");
        swImgDesc.setSupportedVirtualisationEnvironment("linux");
        imageInfoList.add(swImgDesc);
        try {
            appService.uploadAppImage(imageInfoList);
        } catch (DockerClientException e) {
            Assert.assertThrows("can not merge parts to file", NullPointerException.class, null);
        }

    }

}
