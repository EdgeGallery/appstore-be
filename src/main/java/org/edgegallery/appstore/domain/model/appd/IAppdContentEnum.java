package org.edgegallery.appstore.domain.model.appd;

public interface IAppdContentEnum {

    IAppdContentEnum of(String name);

    boolean isNotNull();

    String getName();

}

