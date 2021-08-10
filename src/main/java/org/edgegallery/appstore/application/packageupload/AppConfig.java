package org.edgegallery.appstore.application.packageupload;

public class AppConfig {
    public static final String CHARSET = "UTF-8";

    public static String SERVER_IP;

    public static int SERVER_PORT = 31943;

    public static int FILE_SIZE = 9437980;

    public static String UPLOAD_PATH
        = "/rest/nfv/v1/vnfcatalogwebsite/apppackage/action/upload/vnfpackage?name=${taskName}&unlimitRetryNum=0&fileCurrentIndex=";

}
