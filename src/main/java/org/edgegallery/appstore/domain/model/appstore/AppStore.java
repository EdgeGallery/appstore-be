package org.edgegallery.appstore.domain.model.appstore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.edgegallery.appstore.interfaces.appstore.facade.dto.AppStoreDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
@NoArgsConstructor
public class AppStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppStore.class);

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private String appStoreId;

    private String appStoreName;

    private String appStoreVersion;

    private String company;

    private String url;

    private String schema;

    private String appPushIntf;

    private String appdTransId;

    private String description;

    private Date addedTime;

    private Date modifiedTime;

    public AppStore(String appStoreName, String company) {
        this.appStoreName = appStoreName;
        this.company = company;
    }

    /**
     * from dto.
     */
    public static AppStore of(AppStoreDto appStoreDto) {
        if (appStoreDto == null) {
            return null;
        }

        AppStore appStore = new AppStore(appStoreDto.getAppStoreName(), appStoreDto.getCompany());
        appStore.setAppStoreId(appStoreDto.getAppStoreId());
        appStore.setAppStoreVersion(appStoreDto.getAppStoreVersion());
        appStore.setUrl(appStoreDto.getUrl());
        appStore.setSchema(appStoreDto.getSchema());
        appStore.setAppPushIntf(appStoreDto.getAppPushIntf());
        appStore.setAppdTransId(appStoreDto.getAppdTransId());
        appStore.setDescription(appStoreDto.getDescription());
        appStore.setAddedTime(transDateFormat(appStoreDto.getAddedTime()));
        appStore.setModifiedTime(transDateFormat(appStoreDto.getModifiedTime()));

        return appStore;
    }

    /**
     * to DTO.
     */
    public AppStoreDto toAppStoreDto() {
        AppStoreDto appStoreDto = new AppStoreDto(this.getAppStoreId(), this.getAppStoreName(),
                this.getAppStoreVersion(), this.getCompany(), this.getUrl(),
                this.getSchema(), this.getAppPushIntf(), this.getAppdTransId(),
                this.getDescription(), null, null);
        appStoreDto.setAddedTime(transDateToString(this.getAddedTime()));
        appStoreDto.setModifiedTime(transDateToString(this.getModifiedTime()));

        return appStoreDto;
    }

    /**
     * trans string to date.
     */
    public static Date transDateFormat(String strDate) {
        if (StringUtils.isBlank(strDate)) {
            return null;
        }

        DateFormat df = new SimpleDateFormat(DATE_PATTERN);
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse Date String.", e);
            return null;
        }
    }

    /**
     * trans date to string.
     */
    public static String transDateToString(Date date) {
        if (date == null) {
            return null;
        }

        DateFormat df = new SimpleDateFormat(DATE_PATTERN);
        return df.format(date);
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
