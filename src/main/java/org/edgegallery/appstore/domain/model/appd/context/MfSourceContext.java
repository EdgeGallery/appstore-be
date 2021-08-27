package org.edgegallery.appstore.domain.model.appd.context;

import lombok.Getter;
import org.edgegallery.appstore.domain.model.appd.IAppdContextDef;

@Getter
public enum MfSourceContext implements IAppdContextDef {
    Source("Source", true),
    Algorithm("Algorithm", true),
    Hash("Hash", true);

    private String name;

    private boolean isNotNull;

    MfSourceContext(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    public IAppdContextDef of(String name) {
        for (MfSourceContext type : MfSourceContext.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
