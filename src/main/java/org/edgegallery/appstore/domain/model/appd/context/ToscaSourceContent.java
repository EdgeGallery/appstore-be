package org.edgegallery.appstore.domain.model.appd.context;

import lombok.Getter;
import org.edgegallery.appstore.domain.model.appd.IAppdContextDef;

/**
 * TOSCA-Metadata:
 */
@Getter
public enum ToscaSourceContent implements IAppdContextDef {
    Name("Name", true),
    Content_Type("Content-Type", true);

    private final String name;

    private final boolean isNotNull;

    ToscaSourceContent(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }
    public IAppdContextDef of(String name){
        for (ToscaSourceContent type : ToscaSourceContent.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
