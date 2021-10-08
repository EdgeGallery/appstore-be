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

public class AppConfig {
    private AppConfig() {
    }

    public static final int FILE_SIZE = 9437980;

    public static final String MEAO_HOST = "meao_host";

    public static final String MEAO_USER = "meao_user";

    public static final String MEAO_PASSWORD = "meao_password";

    public static final String UPLOAD_PATH = "/rest/nfv/v1/vnfcatalogwebsite/apppackage/action/upload/vnfpackage?"
        + "name=${taskName}&unlimitRetryNum=0&fileCurrentIndex=";
}
