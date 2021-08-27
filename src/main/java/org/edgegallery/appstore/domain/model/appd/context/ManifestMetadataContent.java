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
 * manifest-file: metadata info.
 */
@Getter
public enum ManifestMetadataContent implements IAppdContentEnum {
    metadata("metadata", true),
    app_product_name("app_product_name", true),
    app_provider_id("app_provider_id", true),
    app_package_version("app_package_version", true),
    app_release_data_time("app_release_data_time", true),
    app_type("app_type", false),
    app_class("app_class", true),
    app_package_description("app_package_description", false);

    private final String name;

    private final boolean isNotNull;

    ManifestMetadataContent(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    /**
     * create enum from name.
     */
    public IAppdContentEnum of(String name) {
        for (ManifestMetadataContent type : ManifestMetadataContent.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
