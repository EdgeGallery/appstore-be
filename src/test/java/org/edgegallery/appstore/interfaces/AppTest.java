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

package org.edgegallery.appstore.interfaces;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.application.external.atp.AtpService;
import org.edgegallery.appstore.domain.model.app.AppPageCriteria;
import org.edgegallery.appstore.domain.model.app.EnumAppStatus;
import org.edgegallery.appstore.domain.model.releases.EnumPackageStatus;
import org.edgegallery.appstore.infrastructure.persistence.apackage.PackageMapper;
import org.edgegallery.appstore.infrastructure.persistence.app.AppMapper;
import org.edgegallery.appstore.infrastructure.persistence.comment.CommentMapper;
import org.edgegallery.appstore.interfaces.AppstoreApplicationTest;
import org.edgegallery.appstore.interfaces.app.facade.dto.RegisterRespDto;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class AppTest {

    @Value("${appstore-be.package-path}")
    private String testDir;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected AppMapper appMapper;

    @Autowired
    protected PackageMapper packageMapper;

    @Autowired
    protected CommentMapper commentMapper;

    @MockBean
    protected AtpService atpService;

    protected String userId = "5abdd29d-b281-4f96-8339-b5621a67d217";

    protected String userName = "username";

    protected String appId;

    protected String packageId;

    protected String unPublishedPackageId;

    protected Gson gson = new Gson();

    protected static final String POSITIONING_EG_1_CSAR = "testfile/positioning_eg_1.0.csar";

    protected static final String POSITIONING_EG_2_CSAR = "testfile/positioning_eg_2.0.csar";

    protected static final String POSITIONING_EG_UNIQUE_CSAR = "testfile/positioning_eg_unique.csar";

    protected static final String LOGO_PNG = "testfile/logo.png";

    public MvcResult registerApp(String iconAddr, String csarAddr, String userId, String userName, String testTaskId)
        throws Exception {
        return registerApp(iconAddr, csarAddr, userId, userName, "Video", "test", "GPU", "Smart City", testTaskId);
    }

    public MvcResult registerApp(String iconAddr, String csarAddr, String userId, String userName) throws Exception {
        return registerApp(iconAddr, csarAddr, userId, userName, "Video", "test", "GPU", "Smart City", null);
    }

    public MvcResult registerApp(String iconAddr, String csarAddr, String userId, String userName, String type,
        String shortDesc, String affinity, String industry, String testTaskId) throws Exception {
        File iconFile = Resources.getResourceAsFile(iconAddr);
        File csarFile = Resources.getResourceAsFile(csarAddr);
        byte[] taskBytes = testTaskId == null ? null : testTaskId.getBytes();
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/mec/appstore/v1/apps").file(
            new MockMultipartFile("file", csarFile.getName(), MediaType.MULTIPART_FORM_DATA_VALUE,
                FileUtils.openInputStream(csarFile))).file(
            new MockMultipartFile("icon", iconFile.getName(), MediaType.MULTIPART_FORM_DATA_VALUE,
                FileUtils.openInputStream(iconFile)))
            .file(new MockMultipartFile("type", "", MediaType.TEXT_PLAIN_VALUE, type.getBytes()))
            .file(new MockMultipartFile("shortDesc", "", MediaType.TEXT_PLAIN_VALUE, shortDesc.getBytes()))
            .file(new MockMultipartFile("affinity", "", MediaType.TEXT_PLAIN_VALUE, affinity.getBytes()))
            .file(new MockMultipartFile("industry", "", MediaType.TEXT_PLAIN_VALUE, industry.getBytes()))
            .file(new MockMultipartFile("testTaskId", "", MediaType.TEXT_PLAIN_VALUE, taskBytes)).with(csrf())
            .param("userId", userId).param("userName", userName));
        return resultActions.andReturn();
    }

    public void clearDb() {
        appMapper.findAllWithAppPagination(new AppPageCriteria(100, 0, null, null, null, null, null)).forEach(po -> {
            appMapper.remove(po.getAppId());
            packageMapper.removeReleasesByAppId(po.getAppId());
            commentMapper.removeAll(po.getAppId());
        });
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void test() {
        // empty test
    }

    @After
    public void clear() {
        try {
            FileUtils.cleanDirectory(new File(testDir));
            clearDb();
        } catch (IOException e) {
            Assert.assertNull(e);
        }
    }


    @Before
    public void registerAnApp() throws Exception {
        MvcResult mvcResult = registerApp(LOGO_PNG, POSITIONING_EG_1_CSAR, userId, userName);
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        RegisterRespDto dto = gson.fromJson(mvcResult.getResponse().getContentAsString(), RegisterRespDto.class);
        appMapper.findByAppId(dto.getAppId()).ifPresent(appPo -> {
            appPo.setStatus(EnumAppStatus.Published);
            appMapper.update(appPo);
        });
        Optional.ofNullable(packageMapper.findReleaseById(dto.getPackageId())).ifPresent(r -> {
            r.setStatus(EnumPackageStatus.Published.toString());
            packageMapper.updateRelease(r);
        });
        appId = dto.getAppId();
        packageId = dto.getPackageId();
        mvcResult = registerApp(LOGO_PNG, POSITIONING_EG_2_CSAR, userId, userName);
        Assert.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        dto = gson.fromJson(mvcResult.getResponse().getContentAsString(), RegisterRespDto.class);
        unPublishedPackageId = dto.getPackageId();
    }

}
