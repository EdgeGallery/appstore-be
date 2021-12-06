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

package org.edgegallery.appstore.domain.model.releases;

/**
 * package status.
 *
 */
public enum EnumExperienceStatus {

    // package is Uploading
    UPLOADING(5,"uploading"),

    // package is uploaded
    UPLOADED(10, "uploaded"),

    // package is upload failed
    UPLOAD_FAILED(0, "uploadFailed"),

    // package is Distributing
    DISTRIBUTING(25, "distributing"),

    // package is Distributed
    DISTRIBUTED(35, "distributed"),

    // package is Distributed
    DISTRIBUTE_FAILED(0, "distributedFailed"),

    //package is Instantiating
    INSTANTIATING(50, "instantiating"),

    //check distribute
    CHECK_INSTANTIATE(60, "checkInstantiate"),

    // package is Instantiated
    INSTANTIATED(70, "running"),

    // package is Instantiated
    VM_INSTANTIATED(70, "Instantiated"),

    // package is Instantiated
    INSTANTIATE_FAILED(0, "instantiateFailed"),

    // getStatusSuccess
    GET_STATUS_SUCCESS(100, "getStatusSuccess"),

    // cleanEnvSuccess
    CLEAN_ENV_SUCCESS(0, "cleanEnvSuccess");

    // enum content
    private String text;

    private int progress;

    EnumExperienceStatus(int progress, String text) {
        this.text = text;
        this.progress = progress;
    }

    /**
     * transform string to enum.
     *
     * @param text input string
     * @return status
     */
    public static EnumExperienceStatus fromString(String text) {
        for (EnumExperienceStatus b : EnumExperienceStatus.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public int getProgress() {
        return progress;
    }

}
