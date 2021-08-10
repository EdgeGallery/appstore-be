package org.edgegallery.appstore.application.packageupload;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UploadPackageEntity {
    private String fileName;

    private long totalSie;

    private int shardCount;

    private String csrfToken;

    private String cookie;

}
