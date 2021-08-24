package org.edgegallery.appstore.application.packageupload;

public class AppConfig {

    public static final int FILE_SIZE = 9437980;

    public static final String MEAO_HOST = "meao_host";

    public static final String MEAO_USER = "meao_user";

    public static final String MEAO_PASSWORD = "meao_password";

    public static final String UPLOAD_PATH = "/rest/nfv/v1/vnfcatalogwebsite/apppackage/action/upload/vnfpackage?"
        + "name=${taskName}&unlimitRetryNum=0&fileCurrentIndex=";
}
