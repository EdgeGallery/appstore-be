package org.edgegallery.appstore.infrastructure.persistence.meao;

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

    @Column(name = "version")
    private String version;

    @Column(name = "url")
    private String url;

    @Column(name = "ip")
    private String ip;

    @Column(name = "port")
    private int port;

    @Column(name = "region")
    private String region;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "product")
    private String product;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "token_type")
    private String tokenType;

    @Column(name = "status")
    private String status;
}