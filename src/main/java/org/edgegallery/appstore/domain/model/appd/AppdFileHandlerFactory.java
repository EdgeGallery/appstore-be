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

package org.edgegallery.appstore.domain.model.appd;

import org.edgegallery.appstore.domain.model.appd.context.ManifestFiledataContent;
import org.edgegallery.appstore.domain.model.appd.context.ManifestMetadataContent;
import org.edgegallery.appstore.domain.model.appd.context.ToscaMatedataContent;
import org.edgegallery.appstore.domain.model.appd.context.ToscaSourceContent;

public final class AppdFileHandlerFactory {

    public static final int TOSCA_META_FILE = 1;

    public static final int MF_FILE = 2;

    private AppdFileHandlerFactory() {
    }

    /**
     * create handler by file type.
     *
     * @param fileType TOSCA_META_FILE or MF_FILE
     * @return ToscaFileHandler
     */
    public static IAppdFile createFileHandler(int fileType) {
        switch (fileType) {
            case TOSCA_META_FILE:
                return new ToscaFileHandler(ToscaMatedataContent.class, ToscaSourceContent.class);
            case MF_FILE:
                return new ToscaFileHandler(ManifestMetadataContent.class, ManifestFiledataContent.class);
            default:
                return null;
        }
    }
}
