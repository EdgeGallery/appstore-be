/*
 * Copyright 2021 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.edgegallery.appstore.interfaces.appd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.appd.AppdFileHandlerFactory;
import org.edgegallery.appstore.domain.model.appd.IAppdFile;
import org.edgegallery.appstore.domain.model.appd.AppdFileContentHandler;
import org.edgegallery.appstore.domain.model.appd.context.ManifestFiledataContent;
import org.edgegallery.appstore.domain.model.appd.context.ToscaSourceContent;
import org.junit.Assert;
import org.junit.Test;

public class ParasAppFileTest {

    @Test
    public void should_successfully_when_load_mf_file() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.MF_FILE);
        File mfFile = Resources.getResourceAsFile("appd/loactionAppTest.mf");
        fileHandler.load(mfFile);
    }

    @Test
    public void should_successfully_when_load_tosca_file() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.TOSCA_META_FILE);
        File mfFile = Resources.getResourceAsFile("appd/TOSCA.meta");
        fileHandler.load(mfFile);
        IAppdFile handler = (IAppdFile) fileHandler;
        Assert.assertNotNull(handler.getParamsHandlerList());
    }

    @Test
    public void should_successfully_when_paras_tosca_file() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.TOSCA_META_FILE);
        File mfFile = Resources.getResourceAsFile("appd/TOSCA.meta");
        fileHandler.load(mfFile);
        IAppdFile handler = (IAppdFile) fileHandler;
        Assert.assertEquals(4, handler.getParamsHandlerList().size());
        Assert.assertTrue(handler.getParamsHandlerList().get(0) instanceof AppdFileContentHandler);
    }

    @Test
    public void should_successfully_when_tosca_toString() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.TOSCA_META_FILE);
        File toscaFile = Resources.getResourceAsFile("appd/TOSCA.meta");
        fileHandler.load(toscaFile);
        String ret = fileHandler.toString();
        Assert.assertNotNull(ret);
        String data = readFileToList(toscaFile);
        Assert.assertTrue(data.trim().equals(ret));
    }

    @Test
    public void should_successfully_when_paras_mf_file() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.MF_FILE);
        File mfFile = Resources.getResourceAsFile("appd/loactionAppTest.mf");
        fileHandler.load(mfFile);
        IAppdFile handler = (IAppdFile) fileHandler;
        Assert.assertEquals(3, handler.getParamsHandlerList().size());
        Assert.assertTrue(handler.getParamsHandlerList().get(0) instanceof AppdFileContentHandler);
    }

    @Test
    public void should_successfully_when_mf_toString() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.MF_FILE);
        File mfFile = Resources.getResourceAsFile("appd/loactionAppTest.mf");
        String data = readFileToList(mfFile);
        fileHandler.load(mfFile);
        String ret = fileHandler.toString();
        Assert.assertNotNull(ret);
        Assert.assertTrue(data.trim().equals(ret));
    }

    @Test
    public void should_successfully_when_delete_name_from_tosca() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.TOSCA_META_FILE);
        File toscaFile = Resources.getResourceAsFile("appd/TOSCA.meta");
        assert fileHandler != null;
        fileHandler.load(toscaFile);
        String ret = fileHandler.toString();
        Assert.assertTrue(ret.contains("Name: Image/ubuntu_test.zip\nContent-Type: image"));
        Assert.assertTrue(fileHandler.delFileDescByName(ToscaSourceContent.Name, "Image/ubuntu_test.zip"));
        ret = fileHandler.toString();
        Assert.assertFalse(ret.contains("Name: Image/ubuntu_test.zip\nContent-Type: image"));
    }

    @Test
    public void should_successfully_when_delete_name_from_mf() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.MF_FILE);
        File toscaFile = Resources.getResourceAsFile("appd/loactionAppTest.mf");
        assert fileHandler != null;
        fileHandler.load(toscaFile);
        String ret = fileHandler.toString();
        Assert.assertTrue(ret.contains("Source: APPD/loactionAppTest.zip\nAlgorithm: SHA-256\nHash: 556a62edb8a15a457152c6c9d02607c28ebc69d9bdcab9e9f8c411eac75b3924"));
        Assert.assertTrue(fileHandler.delFileDescByName(ManifestFiledataContent.Source, "APPD/loactionAppTest.zip"));
        ret = fileHandler.toString();
        Assert.assertFalse(ret.contains("Source: APPD/loactionAppTest.zip\nAlgorithm: SHA-256\nHash: 556a62edb8a15a457152c6c9d02607c28ebc69d9bdcab9e9f8c411eac75b3924"));
    }

    @Test
    public void should_failed_when_check_error_mf_file() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.MF_FILE);
        File toscaFile = Resources.getResourceAsFile("appd/include_error_data.mf");
        assert fileHandler != null;
        fileHandler.load(toscaFile);
        Assert.assertFalse(fileHandler.formatCheck());
    }

    private String readFileToList(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return StringUtils.join(lines, "\n");
    }


}
