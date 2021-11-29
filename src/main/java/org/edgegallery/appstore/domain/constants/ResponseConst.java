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

public class ResponseConst {
    /**
     * the success code.
     */
    public static final int RET_SUCCESS = 0;

    /**
     * the fail code.
     */
    public static final int RET_FAIL = 1;

    /**
     * the pok code.
     */
    public static final int RET_PART_SUCCESS = 5000;

    /**
     * APP error code.
     */
    public static final int RET_APP_BASE = 10000;

    /**
     * app param is invalid.
     */
    public static final int RET_PARAM_INVALID = 10001;

    /**
     * file name contain blank.
     */
    public static final int RET_FILE_NAME_CONTAIN_BLANK = 10002;

    /**
     * file name postfix is invalid.
     */
    public static final int RET_FILE_NAME_POSTFIX_INVALID = 10003;

    /**
     * file size is too big.
     */
    public static final int RET_FILE_TOO_BIG = 10004;

    /**
     * file name is null.
     */
    public static final int RET_FILE_NAME_NULL = 10005;

    /**
     * package check exception.
     */
    public static final int RET_PACKAGE_CHECK_EXCEPTION = 10006;

    /**
     * save file exception.
     */
    public static final int RET_SAVE_FILE_EXCEPTION = 10007;

    /**
     * fail to get app class.
     */
    public static final int RET_GET_APP_CLASS_FAILED = 10008;

    /**
     * get image description failed.
     */
    public static final int RET_GET_IMAGE_DESC_FAILED = 10009;

    /**
     * file not found.
     */
    public static final int RET_FILE_NOT_FOUND = 10010;

    /**
     * miss image location info.
     */
    public static final int RET_MISS_IMAGE_LOCATION = 10011;

    /**
     * update image desc failed.
     */
    public static final int RET_UPDATE_IMAGE_FAILED = 10012;

    /**
     * decompress file failed.
     */
    public static final int RET_DECOMPRESS_FAILED = 10013;

    /**
     * compress failed.
     */
    public static final int RET_COMPRESS_FAILED = 10014;

    /**
     * too many files to unzip.
     */
    public static final int RET_UNZIP_TOO_MANY_FILES = 10015;

    /**
     * An exception occurred while getting the file from application package.
     */
    public static final int RET_PARSE_FILE_EXCEPTION = 10016;

    /**
     * load yaml failed.
     */
    public static final int RET_LOAD_YAML_FAILED = 10017;

    /**
     * pull image failed.
     */
    public static final int RET_PULL_IMAGE_FAILED = 10018;

    /**
     * push image failed.
     */
    public static final int RET_PUSH_IMAGE_FAILED = 10019;

    /**
     * icon file check exception.
     */
    public static final int RET_ICON_CHECK_EXCEPTION = 10020;

    /**
     * video file check exception.
     */
    public static final int RET_VIDEO_CHECK_EXCEPTION = 10021;

    /**
     * the same app has existed.
     */
    public static final int RET_SAME_APP_EXIST = 10022;

    /**
     * the same unPublished apps have reach the limit.
     */
    public static final int RET_SAME_APP_REACH_LIMIT = 10023;

    /**
     * the user's apps have reach the limit.
     */
    public static final int RET_USER_APPS_REACH_LIMIT = 10024;

    /**
     * release has existed.
     */
    public static final int RET_RELEASE_EXIST = 10025;

    /**
     * get task status from atp response failed.
     */
    public static final int RET_GET_TEST_STATUS_FAILED = 10026;

    /**
     * app not found with id.
     */
    public static final int RET_APP_NOT_FOUND = 10027;

    /**
     * the image of application is not exist.
     */
    public static final int RET_IMAGE_NOT_EXIST = 10028;

    /**
     * the content of manifest file is incorrect.
     */
    public static final int RET_MF_CONTENT_INVALID = 10029;

    /**
     * sign package failed.
     */
    public static final int RET_SIGN_PACKAGE_FAILED = 10030;

    /**
     * The app package is illegal.
     */
    public static final int RET_PACKAGE_ILLEGAL = 10031;

    /**
     * package base error code.
     */
    public static final int RET_PACKAGE_BASE = 11000;

    /**
     * Application package not found.
     */
    public static final int RET_PACKAGE_NOT_FOUND = 11001;

    /**
     * The application can be published only after testing successfully.
     */
    public static final int RET_PUBLISH_NO_TESTED = 11002;

    /**
     * Application package file not found.
     */
    public static final int RET_PACKAGE_FILE_NOT_FOUND = 11003;

    /**
     * get csar package file failed.
     */
    public static final int RET_GET_PACKAGE_FILE_FAILED = 11004;

    /**
     * The package status is not allowed to test again.
     */
    public static final int RET_NOT_ALLOWED_TO_TEST = 11005;

    /**
     * create test package task failed.
     */
    public static final int RET_CREATE_TEST_TASK_FAILED = 11006;

    /**
     * add image zip to package failed.
     */
    public static final int RET_IMAGE_TO_PACKAGE_FAILED = 11007;

    /**
     * download image from file system failed.
     */
    public static final int RET_DOWNLOAD_IMAGE_FAILED = 11008;

    /**
     * add image info to file failed.
     */
    public static final int RET_ADD_IMAGE_INFO_FAILED = 11009;

    /**
     * pull package exception.
     */
    public static final int RET_PULL_PACKAGE_FAILED = 11010;

    /**
     * comment base error code.
     */
    public static final int RET_COMMENT_BASE = 12000;

    /**
     * user can not comment own app.
     */
    public static final int RET_COMMENT_OWN_APP = 12001;

    /**
     * appstore base error code.
     */
    public static final int RET_APPSTORE_BASE = 13000;

    /**
     * can not add local appstore.
     */
    public static final int RET_ADD_SELF_APPSTORE = 13001;

    /**
     * add appstore failed.
     */
    public static final int RET_ADD_APPSTORE_FAILED = 13002;

    /**
     * update appstore failed.
     */
    public static final int RET_UPDATE_APPSTORE_FAILED = 13003;

    /**
     * message base error code.
     */
    public static final int RET_MESSAGE_BASE = 14000;

    /**
     * message not found.
     */
    public static final int RET_MESSAGE_NOT_FOUND = 14001;

    /**
     * message has existed.
     */
    public static final int RET_MESSAGE_EXISTED = 14002;

    /**
     * the download url of package or icon is null.
     */
    public static final int RET_MESSAGE_DOWNLOAD_URL_NULL = 14003;

    /**
     * download file from message failed.
     */
    public static final int RET_DOWNLOAD_FROM_MESSAGE_FAILED = 14004;

    /**
     * download file from source appstore failed.
     */
    public static final int RET_DOWNLOAD_FROM_APPSTORE_FAILED = 14005;

    /**
     * system base error code.
     */
    public static final int RET_SYSTEM_BASE = 15000;

    /**
     * Upload config file error.
     */
    public static final int UPLOAD_CONFIG_FILE_ERROR = 15001;

    /**
     * please register host.
     */
    public static final int HOST_EMPTY_ERROR = 15002;

    /**
     * get app nodeport url failed.
     */
    public static final int GET_NODEPORT_FAILED = 15003;

    /**
     * this pacakge not instantiate.
     */
    public static final int NOT_INSTATIATE_ERROR = 15004;

    /**
     * Can not create a host.
     */
    public static final int CREATE_HOST_ERROR = 15005;

    /**
     * add mec host to lcm fail.
     */
    public static final int ADD_HOST_TO_LCM_FAILED = 15006;

    /**
     * userId is empty.
     */
    public static final int USERID_IS_EMPTY = 15007;

    /**
     * Can not find the host.
     */
    public static final int NOT_GET_HOST_ERROR = 15008;

    /**
     * delete host failed.
     */
    public static final int DELETE_HOST_FAILED = 15009;

    /**
     * health check faild,current ip or port cann't be used.
     */
    public static final int HEALTH_CHECK_FAILED = 15010;

    /**
     * file name is invalid.
     */
    public static final int RET_FILE_NAME_INVALID = 15011;

    /**
     * Can not update the host.
     */
    public static final int RET_UPDATE_HOST_FAILED = 15012;

    /**
     * Get ip number error.
     */
    public static final int RET_GET_IP_NUMBER_ERROR = 15013;

    /**
     * get current node error.
     */
    public static final int RET_GET_NODE_ERROR = 15014;

    /**
     * slice merge file exception.
     */
    public static final int RET_MERGE_FILE_FAILED = 15015;

    /**
     * add image zip to file server failed.
     */
    public static final int RET_IMAGE_TO_FILE_SERVER_FAILED = 15016;

    /**
     * clean up zip info failed.
     */
    public static final int RET_CLEAN_ZIP_INFO_FAILED = 15017;

    /**
     * upload to remote file server failed.
     */
    public static final int RET_UPLOAD_FILE_FAILED = 15018;

    /**
     * create third system fail.
     */
    public static final int RET_CREATE_THIRD_SYSTEM_FAILED = 15101;

    /**
     * get third system fail.
     */
    public static final int RET_QUERY_THIRD_SYSTEM_FAILED = 15102;

    /**
     * third system not exist.
     */
    public static final int RET_THIRD_SYSTEM_NOT_FOUND = 15103;

    /**
     * update third system fail.
     */
    public static final int RET_UPDATE_THIRD_SYSTEM_FAILED = 15104;

    /**
     * delete third system fail.
     */
    public static final int RET_DELETE_THIRD_SYSTEM_FAILED = 15105;

    /**
     * distributed package failed.
     */
    public static final int RET_DISTRIBUTE_FAILED = 15106;

    /**
     * instantiate package failed.
     */
    public static final int RET_INSTANTIATE_FAILED = 15107;

    /**
     * order base error code.
     */
    public static final int RET_ORDER_BASE = 16000;

    /**
     * order not found with id.
     */
    public static final int RET_ORDER_NOT_FOUND = 16001;

    /**
     * create order failed.
     */
    public static final int RET_CREATE_ORDER_FAILED = 16002;

    /**
     * deactivate order failed.
     */
    public static final int RET_DEACTIVATE_ORDER_FAILED = 16003;

    /**
     * an inactivated order can't be deactivated.
     */
    public static final int RET_NOT_ALLOWED_DEACTIVATE_ORDER = 16004;

    /**
     * activate order failed.
     */
    public static final int RET_ACTIVATE_ORDER_FAILED = 16005;

    /**
     * unsubscribed orders can't be activated.
     */
    public static final int RET_NOT_ALLOWED_ACTIVATE_ORDER = 16006;

    /**
     * get mec host info failed.
     */
    public static final int RET_GET_MECMHOST_FAILED = 16007;

    /**
     * failed to upload package to apm.
     */
    public static final int RET_UPLOAD_PACKAGE_TO_APM_FAILED = 16008;

    /**
     * failed to get deploy status from mecm.
     */
    public static final int RET_GET_DEPLOY_STATUS_FAILED = 16009;

    /**
     * not allowed to subscribe own app.
     */
    public static final int RET_NOT_ALLOWED_SUBSCRIBE_OWN_APP = 16010;

    /**
     * right base error code.
     */
    public static final int RET_RIGHT_BASE = 18000;

    /**
     * Permission not allowed to delete app.
     */
    public static final int RET_NO_ACCESS_DELETE_APP = 18001;

    /**
     * Permission not allowed to delete application package.
     */
    public static final int RET_NO_ACCESS_DELETE_PACKAGE = 18002;

    /**
     * Permission not allowed to deactivate order.
     */
    public static final int RET_NO_ACCESS_DEACTIVATE_ORDER = 18003;

    /**
     * Permission not allowed to activate order.
     */
    public static final int RET_NO_ACCESS_ACTIVATE_ORDER = 18004;

    /**
     * general base error code.
     */
    public static final int RET_GENERAL_BASE = 19000;

    /**
     * Database operation failed.
     */
    public static final int RET_DB_ERROR = 19001;

    /**
     * make directory failed.
     */
    public static final int RET_MAKE_DIR_FAILED = 19002;

    /**
     * create file failed.
     */
    public static final int RET_CREATE_FILE_FAILED = 19003;

    /**
     * copy file failed.
     */
    public static final int RET_COPY_FILE_FAILED = 19004;

    /**
     * File is outside extraction target directory.
     */
    public static final int RET_FILE_OUT_TARGET = 19005;

    /**
     * the file path is invalid.
     */
    public static final int RET_FILE_PATH_INVALID = 19006;

    private ResponseConst() {
        throw new IllegalStateException("Utility class");
    }

}