package org.edgegallery.appstore.interfaces.meao.facade.dto;

import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;

@Getter
@Setter
@NoArgsConstructor
public class PackageProgressDto {
    private String id;

    private String packageId;

    private String meaoId;

    private String status;

    private String progress;

    private Date createTime;

    private String systemName;

    private String url;

    public Date getCreateTime() {
        return createTime == null ? null : (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime == null ? null : (Date) createTime.clone();
    }

    public PackageProgressDto transferTo(PackageUploadProgress progress) {
        PackageProgressDto dto = new PackageProgressDto();
        dto.setId(progress.getId());
        dto.setPackageId(progress.getPackageId());
        dto.setMeaoId(progress.getMeaoId());
        dto.setStatus(progress.getStatus());
        dto.setProgress(progress.getProgress());
        dto.setCreateTime(progress.getCreateTime());
        return dto;
    }
}
