package org.edgegallery.appstore.domain.model.appd;

import org.edgegallery.appstore.domain.model.appd.context.ManifestMetadataContent;
import org.edgegallery.appstore.domain.model.appd.context.ManifestFiledataContent;
import org.edgegallery.appstore.domain.model.appd.context.ToscaSourceContent;
import org.edgegallery.appstore.domain.model.appd.context.ToscaMatedataContent;

public class AppdFileHandlerFactory {

    public static final int TOSCA_META_FILE = 1;

    public static final int MF_FILE = 2;

    public static IAppdFile createFileHandler(int fileType) {
        switch (fileType) {
            case TOSCA_META_FILE:
                return new ToscaFileHandler(ToscaMatedataContent.class, ToscaSourceContent.class);
            case MF_FILE:
                return new ToscaFileHandler(ManifestMetadataContent.class, ManifestFiledataContent.class);
            default:
                return null;
        }
    }
}
