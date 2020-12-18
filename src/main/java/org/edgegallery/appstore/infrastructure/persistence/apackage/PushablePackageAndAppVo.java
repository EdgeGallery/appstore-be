package org.edgegallery.appstore.infrastructure.persistence.apackage;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushablePackageAndAppVo extends AppReleasePo {

    @Column(name = "atpTestReportUrl")
    private String atpTestReportUrl;

    @Column(name = "latestPushTime")
    private String latestPushTime;

    @Column(name = "pushTimes")
    private Integer pushTimes;

    @Column(name = "targetPlatform")
    private String targetPlatform;

    @Column(name = "sourcePlatform")
    private String sourcePlatform;
}
