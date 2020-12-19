package org.edgegallery.appstore.domain.model.appstore;

import java.util.List;

public interface AppStoreRepository {
    String addAppStore(AppStore appStore);

    int deleteAppStoreById(String appStoreId);

    int updateAppStoreById(AppStore appStore);

    AppStore queryAppStoreById(String appStoreId);

    List<AppStore> queryAppStores(AppStore appStore);
}
