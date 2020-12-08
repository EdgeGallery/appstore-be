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
     * @return
     */
    public static EnumPackageStatus fromString(String text) {
        for (EnumPackageStatus b : EnumPackageStatus.values()) {
            if (b.text.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
