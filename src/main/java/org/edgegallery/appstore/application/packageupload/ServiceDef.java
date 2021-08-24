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
}
