package org.edgegallery.appstore.domain.model.appd.context;

import lombok.Getter;
import org.edgegallery.appstore.domain.model.appd.IAppdContextDef;

@Getter
public enum ToscaLinkContext implements IAppdContextDef {
    Name("Name", true),
    Content_Type("Content-Type", true);

    private String name;

    private boolean isNotNull;

    ToscaLinkContext(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }
    public IAppdContextDef of(String name){
        for (ToscaLinkContext type : ToscaLinkContext.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
