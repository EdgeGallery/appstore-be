package org.edgegallery.appstore.infrastructure.persistence.appstore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_store_table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppStorePo {
    @Id
    @Column(name = "APPSTOREID")
    private String appStoreId;

    @Column(name = "APPSTORENAME")
    private String appStoreName;

    @Column(name = "APPSTOREVERSION")
    private String appStoreVersion;

    @Column(name = "COMPANY")
    private String company;

    @Column(name = "URL")
    private String url;

    @Column(name = "SCHEMA")
    private String schema;

    @Column(name = "APPPUSHINTF")
    private String appPushIntf;

    @Column(name = "APPDTRANSID")
    private String appdTransId;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ADDEDTIME")
    private Date addedTime;

    @Column(name = "MODIFIEDTIME")
    private Date modifiedTime;

    public Date getAddedTime() {
        return addedTime == null ? null : (Date)addedTime.clone();
    }

    public void setAddedTime(Date addedTime) {
        this.addedTime = addedTime == null ? null : (Date)addedTime.clone();
    }

    public Date getModifiedTime() {
        return modifiedTime == null ? null : (Date)modifiedTime.clone();
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime == null ? null : (Date)modifiedTime.clone();
    }
}
