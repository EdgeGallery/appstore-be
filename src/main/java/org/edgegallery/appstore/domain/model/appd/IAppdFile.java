package org.edgegallery.appstore.domain.model.appd;

import java.io.File;
import java.util.List;

public interface IAppdFile {

    void load(File file);

    List<IParamsHandler> getParamsHandlerList();

    String toString();

    boolean delFileDescByName(IAppdContentEnum type, String name);

    boolean formatCheck();
}
