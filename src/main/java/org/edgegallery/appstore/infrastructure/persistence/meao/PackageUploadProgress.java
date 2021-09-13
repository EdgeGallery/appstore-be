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

    /**
     * PackageUploadProgress.
     *
     * @param id id
     * @param packageId packageId
     * @param meaoId meaoId
     * @param createTime createTime
     */
    public PackageUploadProgress(String id, String packageId, String meaoId, Date createTime) {
        this.id = id;
        this.packageId = packageId;
        this.meaoId = meaoId;
        this.status = "uploading";
        this.progress = "0";
        this.createTime = createTime == null ? null : (Date) createTime.clone();
    }

    public Date getCreateTime() {
        return createTime == null ? null : (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime == null ? null : (Date) createTime.clone();
    }
}