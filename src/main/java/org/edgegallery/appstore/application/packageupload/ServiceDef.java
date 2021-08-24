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
     * getVims
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
     * setVims
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
