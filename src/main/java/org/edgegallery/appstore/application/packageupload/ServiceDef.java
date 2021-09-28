/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.appstore.application.packageupload;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceDef {
    private String name;

    private String serviceType;

    private String specification;

    private String action;

    private String[] vims;

    private String mode;

    private String fileName;

    private String description;

    /**
     * getVims.
     *
     * @return
     */
    public String[] getVims() {
        if (vims != null) {
            return vims.clone();
        }
        return new String[0];
    }

    /**
     * setVims.
     *
     * @param vims vims
     */
    public void setVims(String[] vims) {
        if (vims != null) {
            this.vims = vims.clone();
        } else {
            this.vims = null;
        }
    }
}