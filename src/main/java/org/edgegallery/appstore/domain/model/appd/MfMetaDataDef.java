package org.edgegallery.appstore.domain.model.appd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MfMetaDataDef {
    private static final Logger LOGGER = LoggerFactory.getLogger(MfMetaDataDef.class);

    private final Map<String, String> params = new LinkedHashMap<>();

    public final static String startFlag = ParamType.values()[0].name;

    public void setData(Map.Entry<String, String> data) {
        if (ParamType.of(data.getKey()) != null) {
            params.put(data.getKey(), data.getValue());
        }
    }

    public boolean checkParams() {
        for (ParamType type : ParamType.values()) {
            if (type.isNotNull && !params.containsKey(type.name)) {
                LOGGER.info("not include param {} in the MF file.", type.name);
                return false;
            }
        }
        return true;
    }

    public String toString() {
        List<String> lines = new ArrayList<>();
        params.forEach((key, value) -> lines.add(String.format("%s: %s", key, value).trim()));
        return StringUtils.join(lines, "\n");
    }

    private enum ParamType {
        metadata("metadata", true),
        app_product_name("app_product_name", true),
        app_provider_id("app_provider_id", true),
        app_package_version("app_package_version", true),
        app_release_data_time("", true),
        app_type("app_type", true),
        app_class("app_class", true),
        app_package_description("app_package_description", true);

        private String name;

        private boolean isNotNull;

        ParamType(String name, boolean isNotNull) {
            this.name = name;
            this.isNotNull = isNotNull;
        }

        static ParamType of(String name) {
            for (ParamType type : ParamType.values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}
