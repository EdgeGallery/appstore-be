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
    Uploading(5,"uploading"),

    // package is uploaded
    Uploaded(10, "uploaded"),

    // package is upload failed
    UploadFailed(0, "uploadFailed"),

    // package is Distributing
    Distributing(25, "distributing"),

    // package is Distributed
    Distributed(35, "distributed"),

    // package is Distributed
    DistributeFailed(0, "distributedFailed"),

    //package is Instantiating
    Instantiating(45, "instantiating"),

    // package is Instantiated
    Instantiated(60, "instantiated"),

    // package is Instantiated
    InstantiateFailed(0, "instantiateFailed"),

    // getStatusSuccess
    GetStatusSuccess(100, "getStatusSuccess"),

    // cleanEnvSuccess
    CleanEnvSuccess(0, "cleanEnvSuccess");

    // enum content
    private String text;

    private int index;

    EnumExperienceStatus(int index, String text) {
        this.text = text;
        this.index = index;
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
        return index;
    }

}
