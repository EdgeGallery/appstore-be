package org.edgegallery.appstore.domain.model.releases;

public enum EnumPackageStatus {
    Upload("upload"),
    Test_created("created"),
    Test_create_failed("create failed"),
    Test_running("running"),
    Test_waiting("waiting"),
    Test_failed("failed"),
    Test_success("success"),
    Published("publish");

    private String text;

    EnumPackageStatus(String text) {
        this.text = text;
    }

    EnumPackageStatus() {
    }

    /**
     * transform string to enum.
     *
     * @param text input string
     * @return status
     */
    public static EnumPackageStatus fromString(String text) {
        for (EnumPackageStatus b : EnumPackageStatus.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    /**
     * check the status if can be create the test task.
     *
     * @param status status
     * @return true or false
     */
    public static boolean testAllowed(EnumPackageStatus status) {
        return status == Upload || status == Test_failed || status == Test_create_failed || status == Test_success;
    }

    public static boolean needRefresh(EnumPackageStatus status) {
        return status == Test_created || status == Test_running || status == Test_waiting;
    }
}
