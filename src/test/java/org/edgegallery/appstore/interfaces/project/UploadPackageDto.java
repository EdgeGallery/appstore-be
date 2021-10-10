package org.edgegallery.appstore.interfaces.project;

import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.system.lcm.UploadResponse;

@Getter
@Setter
public class UploadPackageDto {
    private UploadResponse data;
}
