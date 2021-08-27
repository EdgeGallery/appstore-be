package org.edgegallery.appstore.interfaces.appd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.appd.AToscaFileHandler;
import org.edgegallery.appstore.domain.model.appd.AppdFileHandlerFactory;
import org.edgegallery.appstore.domain.model.appd.IAppdFile;
import org.edgegallery.appstore.domain.model.appd.ToscaFileContextDef;
import org.edgegallery.appstore.domain.model.appd.ToscaFileHandler;
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
        ToscaFileHandler handler = (ToscaFileHandler) fileHandler;
        Assert.assertNotNull(handler.getParamsHandlerList());
    }

    @Test
    public void should_successfully_when_paras_tosca_file() throws IOException {
        IAppdFile fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.TOSCA_META_FILE);
        File mfFile = Resources.getResourceAsFile("appd/TOSCA.meta");
        fileHandler.load(mfFile);
        AToscaFileHandler handler = (AToscaFileHandler) fileHandler;
        Assert.assertEquals(4, handler.getParamsHandlerList().size());
        Assert.assertTrue(handler.getParamsHandlerList().get(0) instanceof ToscaFileContextDef);
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
        AToscaFileHandler handler = (AToscaFileHandler) fileHandler;
        Assert.assertEquals(3, handler.getParamsHandlerList().size());
        Assert.assertTrue(handler.getParamsHandlerList().get(0) instanceof ToscaFileContextDef);
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

    private String readFileToList(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return StringUtils.join(lines, "\n");
    }


}
