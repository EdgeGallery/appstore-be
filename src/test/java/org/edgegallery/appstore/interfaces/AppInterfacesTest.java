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

package org.edgegallery.appstore.interfaces;

import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.releases.FileChecker;
import org.edgegallery.appstore.domain.model.releases.IconChecker;
import org.edgegallery.appstore.domain.model.releases.PackageChecker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppstoreApplicationTest.class)
@AutoConfigureMockMvc
public class AppInterfacesTest {

    private static final int LINE_MAX_LEN = 40960;

    protected static final String AR_PACKAGE = "testfile/AR.csar";

    protected static final String AR_PNG = "testfile/AR.png";

    protected static final String APPSTORE_ROOT = "appstore";

    protected static final String REAL_APP_ID = "30ec10f4a43041e6a6198ba824311af2";

    protected static final String REAL_APP_ID_WILL_DELETE = "30ec10f4a43041e6a6198ba824311af4";

    protected static final String REAL_PACKAGE_ID_WILL_DELETE = "30ec10f4a43041e6a6198ba824311af5";


    protected static final String REST_API_ROOT = "/mec/appstore/v1/apps/";

    protected static final String ACTION_DOWNLOAD = "/action/download";

    protected static final String ICON = "/icon";

    protected static final String REST_API_PACKAGES = "/packages/";

    protected static final String CSAR_EXTENSION = ".csar";

    private static final String GETAPPS_SUCCESS = "getAppsSuccess_with_no_conditions.txt";

    private static final int READ_MAX_LONG = 10;

    @Autowired
    protected MockMvc mvc;

    @SneakyThrows
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        File csarFile = Resources.getResourceAsFile(AR_PACKAGE);
        File storeFile = new File(
                    "home" + File.separator + APPSTORE_ROOT + File.separator + REAL_APP_ID + File.separator
                                + REAL_APP_ID + CSAR_EXTENSION);
        createFile(storeFile.getCanonicalPath());
        FileUtils.copyFile(csarFile, storeFile);

        File storeFile1 = new File(
                "home" + File.separator + APPSTORE_ROOT + File.separator + REAL_APP_ID + File.separator
                        + REAL_APP_ID_WILL_DELETE + CSAR_EXTENSION);
        createFile(storeFile1.getCanonicalPath());
        FileUtils.copyFile(csarFile, storeFile1);

        File iconFile = Resources.getResourceAsFile(AR_PNG);
        File iconStore = new File(
            "home" + File.separator + APPSTORE_ROOT + File.separator + REAL_APP_ID + File.separator
                + REAL_APP_ID + ".png");
        createFile(iconStore.getCanonicalPath());
        FileUtils.copyFile(iconFile, iconStore);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void iconCheckValid() {
        boolean bn = true;

        try {
            File pngFile = Resources.getResourceAsFile(AR_PNG);
            FileChecker checker = new IconChecker();
            MultipartFile mockMutipartFile = new MockMultipartFile("file", "AR.png", MediaType.TEXT_PLAIN_VALUE,
                        FileUtils.openInputStream(pngFile));
            checker.check(mockMutipartFile);
        } catch (IOException e) {
            bn = false;
            e.printStackTrace();
        }
        Assert.assertTrue(bn);

        try {
            File pngFile = Resources.getResourceAsFile(AR_PNG);
            FileChecker checker = new IconChecker();
            MultipartFile mockMutipartFile = new MockMultipartFile("file",
                        "a3d13969-f86f-4c58879f--25df83908db4nginx.png", MediaType.TEXT_PLAIN_VALUE,
                        FileUtils.openInputStream(pngFile));
            checker.check(mockMutipartFile);
        } catch (IOException e) {
            bn = false;
            e.printStackTrace();
        }
        try {
            File txtFile = Resources.getResourceAsFile("testfile" + File.separator + GETAPPS_SUCCESS);
            FileChecker checker = new PackageChecker();
            MultipartFile mockMutipartFile = new MockMultipartFile("file", GETAPPS_SUCCESS, MediaType.TEXT_PLAIN_VALUE,
                        FileUtils.openInputStream(txtFile));
            checker.check(mockMutipartFile);
        } catch (IllegalArgumentException e) {
            bn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(bn);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void txtCheckInValid() {
        boolean bn = false;
        try {
            File txtFile = Resources.getResourceAsFile("testfile/getAppsSuccess_with_no_conditions.txt");
            FileChecker checker = new PackageChecker();
            MultipartFile mockMutipartFile = new MockMultipartFile("file", GETAPPS_SUCCESS, MediaType.TEXT_PLAIN_VALUE,
                        FileUtils.openInputStream(txtFile));
            checker.check(mockMutipartFile);
        } catch (IllegalArgumentException e) {
            bn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(bn);
    }

    @Test
    @WithMockUser(roles = "APPSTORE_TENANT")
    public void fileNameCheckInValid() {
        boolean bn = false;
        try {
            File txtFile = Resources.getResourceAsFile(AR_PACKAGE);
            FileChecker checker = new PackageChecker();
            MultipartFile mockMutipartFile = new MockMultipartFile("file",
                        "//.." + File.separator + "" + File.separator + "AR.csar", MediaType.TEXT_PLAIN_VALUE,
                        FileUtils.openInputStream(txtFile));
            checker.check(mockMutipartFile);
        } catch (IllegalArgumentException e) {
            bn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(bn);
    }

    protected String readFile(File resultFile) {
        if (resultFile.isFile() && resultFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(resultFile);
                        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
                StringBuilder sb = new StringBuilder();
                String text = null;
                while ((text = readLine(bufferedReader)) != null) {
                    sb.append(text);
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String readLine(BufferedReader br) throws IOException {
        return read(br, LINE_MAX_LEN);
    }

    private String read(BufferedReader br, int lineMaxLen) throws IOException {
        int intC = br.read();
        if (-1 == intC) {
            return null;
        }
        StringBuilder sb = new StringBuilder(READ_MAX_LONG);
        while (intC != -1) {
            char c = (char) intC;
            if (c == '\n') {
                break;
            }
            if (sb.length() >= lineMaxLen) {
                throw new IOException("line too long");
            }
            sb.append(c);
            intC = br.read();
        }
        String str = sb.toString();
        if (!str.isEmpty() && str.endsWith("\r")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    protected void createFile(String dir) {
        File f = new File(dir);
        if (!f.getParentFile().exists()) {
            try {
                if (f.getParentFile().mkdirs() && f.createNewFile()) {
                    throw new IOException("create file or dir failed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
