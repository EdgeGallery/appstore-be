package org.edgegallery.appstore.interfaces.appstore.facade.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AppStoreDto {
    private String appStoreId;

    private String appStoreName;

    private String appStoreVersion;

    private String company;

    private String url;

    private String schema;

    private String appPushIntf;

    private String appdTransId;

    private String description;

    private String addedTime;

    private String modifiedTime;
}
