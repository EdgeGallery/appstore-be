package org.edgegallery.appstore.infrastructure.persistence.meao;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PackageUploadProgress {

    private String id;

    private String packageId;

    private String meaoId;

    private String status;

    private String progress;

    private Date createTime;

    public PackageUploadProgress(String id, String packageId, String meaoId, Date createTime) {
        this.id = id;
        this.packageId = packageId;
        this.meaoId = meaoId;
        this.status = "uploading";
        this.progress = "0";
        this.createTime = (Date) createTime.clone();
    }
}