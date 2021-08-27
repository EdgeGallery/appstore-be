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

package org.edgegallery.appstore.domain.model.appd.context;

import lombok.Getter;
import org.edgegallery.appstore.domain.model.appd.IAppdContentEnum;

/**
 * TOSCA-Metadata: metadata.
 */
@Getter
public enum ToscaMetadataContent implements IAppdContentEnum {
    TOSCA_Meta_File_Version("TOSCA-Meta-File-Version", true),
    CSAR_Version("CSAR-Version", true),
    Created_by("Created-by", true),
    Entry_Definitions("Entry-Definitions", true);

    private final String name;

    private final boolean isNotNull;

    ToscaMetadataContent(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    /**
     * create enum from name.
     */
    public IAppdContentEnum of(String name) {
        for (ToscaMetadataContent type : ToscaMetadataContent.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
