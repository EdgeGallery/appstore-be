package org.edgegallery.appstore.infrastructure.persistence.appstore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_store_table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExAppStorePo {

    @Column(name = "appstoreid")
    private String appStoreId;

    private String name;

    private String url;

    private String version;

    private String company;

    private String schema;

    private String appPushintf;

    private String appTransId;

    private String addedTime;

    private String modifiedTime;

    private String description;
}
