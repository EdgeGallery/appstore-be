package org.edgegallery.appstore.infrastructure.persistence.apackage;

import javax.persistence.Column;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageSubscribe {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "caller_id")
    private String callerId;

    @Column(name = "meao_id")
    private String meaoId;

    @Column(name = "notification_uri")
    private String notificationUri;
}