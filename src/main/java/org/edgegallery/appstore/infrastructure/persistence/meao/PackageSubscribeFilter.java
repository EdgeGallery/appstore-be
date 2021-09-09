package org.edgegallery.appstore.infrastructure.persistence.meao;

import javax.persistence.Column;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageSubscribeFilter {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "subscribe_id")
    private String subscribeId;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "type_package")
    private String typePackage;

    @Column(name = "type_ne")
    private String typeNe;
}