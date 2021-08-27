package org.edgegallery.appstore.domain.model.appd.context;

import lombok.Getter;
import org.edgegallery.appstore.domain.model.appd.IAppdContextDef;

@Getter
public enum ToscaMateContext implements IAppdContextDef {
    TOSCA_Meta_File_Version("TOSCA-Meta-File-Version", true),
    CSAR_Version("CSAR-Version", true),
    Created_by("Created-by", true),
    Entry_Definitions("Entry-Definitions", true);

    private String name;

    private boolean isNotNull;

    ToscaMateContext(String name, boolean isNotNull) {
        this.name = name;
        this.isNotNull = isNotNull;
    }

    public IAppdContextDef of(String name) {
        for (ToscaMateContext type : ToscaMateContext.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
}
