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
