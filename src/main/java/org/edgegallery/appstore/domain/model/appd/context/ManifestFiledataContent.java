package org.edgegallery.appstore.domain.model.appd.context;

import lombok.Getter;
import org.edgegallery.appstore.domain.model.appd.IAppdContentEnum;

/**
 * manifest-file: source file
 */
@Getter
public enum ManifestFiledataContent implements IAppdContentEnum {
    Source("Source", true),
    Algorithm("Algorithm", true),
    Hash("Hash", true);

    private final String name;

    private final boolean isNotNull;

    ManifestFiledataContent(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    public IAppdContentEnum of(String name) {
        for (ManifestFiledataContent type : ManifestFiledataContent.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
