package org.edgegallery.appstore.infrastructure.persistence.appstore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.appstore.AppStore;

@Getter
@Setter
@Entity
@Table(name = "app_store_table")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
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

    /**
     * from AppStore.
     */
    public static AppStorePo of(AppStore appStore) {
        if (appStore == null) {
            return null;
        }

        AppStorePo appStorePo = new AppStorePo();
        appStorePo.setAppStoreId(appStore.getAppStoreId());
        appStorePo.setAppStoreName(appStore.getAppStoreName());
        appStorePo.setAppStoreVersion(appStore.getAppStoreVersion());
        appStorePo.setCompany(appStore.getCompany());
        appStorePo.setUrl(appStore.getUrl());
        appStorePo.setSchema(appStore.getSchema());
        appStorePo.setAppPushIntf(appStore.getAppPushIntf());
        appStorePo.setAppdTransId(appStore.getAppdTransId());
        appStorePo.setDescription(appStore.getDescription());
        appStorePo.setAddedTime(appStore.getAddedTime());
        appStorePo.setModifiedTime(appStore.getModifiedTime());

        return appStorePo;
    }

    /**
     * to AppStore.
     */
    public AppStore toAppStore() {
        AppStore appStore = new AppStore(this.getAppStoreName(), this.getCompany());
        appStore.setAppStoreId(appStoreId);
        appStore.setAppStoreVersion(appStoreVersion);
        appStore.setUrl(url);
        appStore.setSchema(schema);
        appStore.setAppPushIntf(appPushIntf);
        appStore.setAppdTransId(appdTransId);
        appStore.setDescription(description);
        appStore.setAddedTime(addedTime);
        appStore.setModifiedTime(modifiedTime);

        return appStore;
    }

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
