package org.edgegallery.appstore.domain.model.system.vm;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VmUserData {

    private String operateSystem;

    private String flavorExtraSpecs;

    private boolean isTemp;

    private String contents;

    private String params;

}
