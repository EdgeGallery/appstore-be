/* Copyright 2021 Huawei Technologies Co., Ltd.
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

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.application.inner.PackageService;
import org.edgegallery.appstore.domain.model.releases.PackageRepository;
import org.edgegallery.appstore.domain.model.releases.Release;
import org.edgegallery.appstore.interfaces.AppTest;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PackageDto;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.multipart.MultipartFile;

public class ModifyPackageTest extends AppTest {
    @Autowired
    PackageService packageService;

    @Autowired
    PackageRepository packageRepository;

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void should_success() throws Exception {
        File iconFile = Resources.getResourceAsFile("testfile/test2048.jpg");
        File videoFile = Resources.getResourceAsFile(DEMO_VIDEO);
        File docFile = Resources.getResourceAsFile("testfile/template.md");
        MultipartFile iconMultiFile = new MockMultipartFile("icon", "test2048.png", MediaType.TEXT_PLAIN_VALUE,
            FileUtils.openInputStream(iconFile));
        MultipartFile videoMultiFile = new MockMultipartFile("video", "demo_video.mp4", MediaType.TEXT_PLAIN_VALUE,
            FileUtils.openInputStream(videoFile));
        MultipartFile docMultiFile = new MockMultipartFile("doc", "template.md", MediaType.TEXT_PLAIN_VALUE,
            FileUtils.openInputStream(docFile));
        PackageDto packageDto = new PackageDto();
        packageDto.setAppId(appId);
        packageDto.setPackageId(packageId);
        packageDto.setIndustry("Energy");
        packageDto.setType("Big Data");
        packageDto.setAffinity("ARM64");
        packageDto.setShortDesc("2048 game");
        packageDto.setShowType("inner-public");
        packageDto.setExperienceAble(true);
        packageService.updateAppById(iconMultiFile, videoMultiFile, docMultiFile, packageDto);
        Release release = packageRepository.findReleaseById(appId, packageDto.getPackageId());
        Assert.assertEquals("2048 game", release.getAppBasicInfo().getAppDesc());
    }

}
