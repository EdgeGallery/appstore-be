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

package org.edgegallery.appstore.domain.constants;

public final class Consts {

    public static final String REG_USER_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    public static final String REG_APP_ID = "[0-9a-f]{32}";

    public static final String USERID = "userId";

    public static final String USERNAME = "userName";

    public static final String AUTHORITIES = "authorities";

    public static final String SUPER_ADMIN_ID = "39937079-99fe-4cd8-881f-04ca8c4fe09d";

    public static final String SUPER_ADMIN_NAME = "admin";

    public static final String SPLITCONFIG_APPID_GLOBAL = "all";

    public static final double SPLITCONFIG_SPLITRATIO_GLOBAL = 0.15;

    public static final String DOWNLOAD_FILE_URL_V1 = "/mec/developer/v1/files/";

    public static final String APP_LCM_INSTANTIATE_APP_URL
        = "/lcmcontroller/v2/tenants/tenantId/app_instances/appInstanceId/instantiate";

    public static final String APP_LCM_UPLOAD_APPPKG_URL = "/lcmcontroller/v2/tenants/tenantId/packages";

    public static final String APP_LCM_DISTRIBUTE_APPPKG_URL = "/lcmcontroller/v2/tenants/tenantId/packages/packageId";

    public static final String APP_LCM_DELETE_HOST_URL
        = "/lcmcontroller/v2/tenants/tenantId/packages/packageId/hosts/hostIp";

    public static final String APP_LCM_DELETE_APPPKG_URL = "/lcmcontroller/v2/tenants/tenantId/packages/packageId";

    public static final String APP_LCM_TERMINATE_APP_URL
        = "/lcmcontroller/v2/tenants/tenantId/app_instances/appInstanceId/terminate";

    public static final String APP_LCM_GET_WORKLOAD_STATUS_URL
        = "/lcmcontroller/v2/tenants/tenantId/app_instances/appInstanceId";

    public static final String APP_LCM_GET_WORKLOAD_EVENTS_URL
        = "/lcmcontroller/v2/tenants/tenantId/app_instances/appInstanceId/workload/events";

    public static final String APP_LCM_GET_HEALTH = "/lcmcontroller/v1/health";

    /**
     * add MEC host.
     */
    public static final String APP_LCM_ADD_MECHOST = "/lcmcontroller/v1/tenants/tenantId/hosts";

    /**
     * delete MEC host.
     */
    public static final String APP_LCM_DELETE_MECHOST = "/lcmcontroller/v1/tenants/tenantId/hosts/hostIp";

    /**
     * Upload Config file to lcm.
     */
    public static final String APP_LCM_UPLOAD_FILE = "/lcmcontroller/v2/tenants/tenantId/configuration";

    /**
     * MEAO package upload path.
     */
    public static final String MEAO_UPLOAD_URL = "/mec/third-system/v1/meao/%s/action/upload";

    /**
     * MEAO session path.
     */
    public static final String MEAO_SESSION_URL = "/mec/third-system/v1/meao/%s/session";

    /**
     * Third system path.
     */
    public static final String THIRD_SYSTEM_URL = "/mec/third-system/v1";

    public static final long HOUR_IN_MS = 60 * 60 * 1000L;

    public static final String ACCESS_TOKEN_STR = "access_token";

    public static final int MAX_DETAILS_STRING_LENGTH = 1024;

    private Consts() {
    }
}
