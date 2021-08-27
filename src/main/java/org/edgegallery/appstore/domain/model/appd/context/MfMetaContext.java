package org.edgegallery.appstore.domain.model.appd.context;

import lombok.Getter;
import org.edgegallery.appstore.domain.model.appd.IAppdContextDef;

@Getter
public enum MfMetaContext implements IAppdContextDef {
    metadata("metadata", true),
    app_product_name("app_product_name", true),
    app_provider_id("app_provider_id", true),
    app_package_version("app_package_version", true),
    app_release_data_time("app_release_data_time", true),
    app_type("app_type", true),
    app_class("app_class", true),
    app_package_description("app_package_description", true);

    private String name;

    private boolean isNotNull;

    MfMetaContext(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    public IAppdContextDef of(String name) {
        for (MfMetaContext type : MfMetaContext.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
