package org.edgegallery.appstore.domain.model.appstore;

import java.util.List;
import org.edgegallery.appstore.infrastructure.persistence.appstore.AppStorePo;

public interface AppStoreRepository {
    int addAppStore(AppStorePo appStorePo);

    int deleteAppStoreById(String appStoreId);

    int updateAppStoreById(AppStorePo appStorePo);

    AppStorePo queryAppStoreById(String appStoreId);

    List<AppStorePo> queryAppStores(AppStorePo appStorePo);
}
