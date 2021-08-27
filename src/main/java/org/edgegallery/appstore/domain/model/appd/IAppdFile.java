package org.edgegallery.appstore.domain.model.appd;

import java.io.File;

public interface IAppdFile {

    void load(File file);

    String toString();

    boolean delFileDescByName(String name);

    boolean formatCheck();
}
