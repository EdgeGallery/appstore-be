package org.edgegallery.appstore.domain.model.appd;

public interface IAppdContextDef {

    IAppdContextDef of(String name);

    boolean isNotNull();

    String getName();

}

