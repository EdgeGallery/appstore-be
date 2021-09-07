package org.edgegallery.appstore.infrastructure.persistence.apackage;

import javax.persistence.Column;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThirdSystem {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "system_name")
    private String systemName;

    @Column(name = "system_type")
    private String systemType;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private String port;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "vendor")
    private String vendor;
}