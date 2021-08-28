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

import java.io.File;
import java.io.IOException;
import org.apache.ibatis.io.Resources;
import org.edgegallery.appstore.domain.model.appd.AppdFileHandlerFactory;
import org.edgegallery.appstore.domain.model.appd.IAppdFile;
import org.edgegallery.appstore.domain.model.appd.context.ManifestCmsContent;
import org.edgegallery.appstore.domain.model.appd.context.ManifestFiledataContent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ParsingCMSTest {

    private IAppdFile fileHandler;

    @Before
    public void before() throws IOException {
        fileHandler = AppdFileHandlerFactory.createFileHandler(AppdFileHandlerFactory.MF_FILE);
        File toscaFile = Resources.getResourceAsFile("appd/include_cms.mf");
        assert fileHandler != null;
        fileHandler.load(toscaFile);
    }

    @Test
    public void should_successfully_when_load_cms_from_mf() throws IOException {
        String ret = fileHandler.toString();
        System.out.println(ret);
        Assert.assertTrue(ret.contains("-----BEGIN CMS-----"));
        Assert.assertTrue(ret.contains("MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgEFADCABgkqhkiG9w0BBwGggCSABIIBClNvdXJjZTogQVBQRC9NYWluU2VydmljZVRlbXBsYXRlLnppcApBbGdvcml0aG06IFNIQS0yNTYKSGFzaDogNjIzNjE4MjVlZjc4ZDM5YTYzYjlmYzE4NTc5NTFjYjdkMjIxM2JhZjkzNGI0YmQ5ZDVkOTlkMzQxY2M3ZDk1NgoKU291cmNlOiBBcnRpZmFjdHMvRGVwbG95bWVudC9DaGFydHMvdGVzdDIwNDgudGd6CkFsZ29yaXRobTogU0hBLTI1NgpIYXNoOiA0MTdlMWQ4ZGI2ZWViODM1ODE3NWRkNTgzYTg1MmRkYmFiYTU2YzViZDdiMDQ3NmUzNTg0M2ZiNjVjYWQwMjk5AAAAAAAAoIAwggN"));
        Assert.assertTrue(ret.contains("-----END CMS-----"));
    }

    @Test
    public void should_successfully_when_delete_cms_from_mf() throws IOException {
        String ret = fileHandler.toString();
        Assert.assertTrue(ret.contains("-----BEGIN CMS-----"));
        Assert.assertTrue(ret.contains("MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgEFADCABgkqhkiG9w0BBwGggCSABIIBClNvdXJjZTogQVBQRC9NYWluU2VydmljZVRlbXBsYXRlLnppcApBbGdvcml0aG06IFNIQS0yNTYKSGFzaDogNjIzNjE4MjVlZjc4ZDM5YTYzYjlmYzE4NTc5NTFjYjdkMjIxM2JhZjkzNGI0YmQ5ZDVkOTlkMzQxY2M3ZDk1NgoKU291cmNlOiBBcnRpZmFjdHMvRGVwbG95bWVudC9DaGFydHMvdGVzdDIwNDgudGd6CkFsZ29yaXRobTogU0hBLTI1NgpIYXNoOiA0MTdlMWQ4ZGI2ZWViODM1ODE3NWRkNTgzYTg1MmRkYmFiYTU2YzViZDdiMDQ3NmUzNTg0M2ZiNjVjYWQwMjk5AAAAAAAAoIAwggN"));
        Assert.assertTrue(ret.contains("-----END CMS-----"));
        fileHandler.delContentByTypeAndValue(ManifestCmsContent.BEGIN_CMS, "-----BEGIN CMS-----");
        ret = fileHandler.toString();
        Assert.assertFalse(ret.contains("-----BEGIN CMS-----"));
        Assert.assertFalse(ret.contains("MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgEFADCABgkqhkiG9w0BBwGggCSABIIBClNvdXJjZTogQVBQRC9NYWluU2VydmljZVRlbXBsYXRlLnppcApBbGdvcml0aG06IFNIQS0yNTYKSGFzaDogNjIzNjE4MjVlZjc4ZDM5YTYzYjlmYzE4NTc5NTFjYjdkMjIxM2JhZjkzNGI0YmQ5ZDVkOTlkMzQxY2M3ZDk1NgoKU291cmNlOiBBcnRpZmFjdHMvRGVwbG95bWVudC9DaGFydHMvdGVzdDIwNDgudGd6CkFsZ29yaXRobTogU0hBLTI1NgpIYXNoOiA0MTdlMWQ4ZGI2ZWViODM1ODE3NWRkNTgzYTg1MmRkYmFiYTU2YzViZDdiMDQ3NmUzNTg0M2ZiNjVjYWQwMjk5AAAAAAAAoIAwggN"));
        Assert.assertFalse(ret.contains("-----END CMS-----"));
    }
}
