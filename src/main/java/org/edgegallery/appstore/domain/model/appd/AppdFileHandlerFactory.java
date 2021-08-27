package org.edgegallery.appstore.domain.model.appd;

import org.edgegallery.appstore.domain.model.appd.context.MfMetaContext;
import org.edgegallery.appstore.domain.model.appd.context.MfSourceContext;
import org.edgegallery.appstore.domain.model.appd.context.ToscaLinkContext;
import org.edgegallery.appstore.domain.model.appd.context.ToscaMateContext;

public class AppdFileHandlerFactory {

    public static final int TOSCA_META_FILE = 1;

    public static final int MF_FILE = 2;

    public static IAppdFile createFileHandler(int fileType) {
        switch (fileType) {
            case TOSCA_META_FILE:
                return new ToscaFileHandler(ToscaMateContext.class, ToscaLinkContext.class);
            case MF_FILE:
                return new ToscaFileHandler(MfMetaContext.class, MfSourceContext.class);
            default:
                return null;
        }
    }
}
