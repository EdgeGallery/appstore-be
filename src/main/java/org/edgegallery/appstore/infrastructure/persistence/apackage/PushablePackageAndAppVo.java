package org.edgegallery.appstore.infrastructure.persistence.apackage;

import javax.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushablePackageAndAppVo extends AppReleasePo {

    @Column(name = "atpTestReportUrl")
    private String atpTestReportUrl;

    @Column(name = "latestPushTime")
    private String latestPushTime;

    @Column(name = "pushTimes")
    private int pushTimes;

    @Column(name = "targetPlatform")
    private String targetPlatform;

    @Column(name = "sourcePlatform")
    private String sourcePlatform;
}
