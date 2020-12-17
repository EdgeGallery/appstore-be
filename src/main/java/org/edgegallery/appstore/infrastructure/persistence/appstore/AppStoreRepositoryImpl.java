package org.edgegallery.appstore.infrastructure.persistence.appstore;

import java.util.ArrayList;
import java.util.List;
import org.edgegallery.appstore.domain.model.appstore.AppStoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AppStoreRepositoryImpl implements AppStoreRepository {
    public int addAppStore(AppStorePo appStorePo) {
        return 1;
    }

    public int deleteAppStoreById(String appStoreId) {
        return 1;
    }

    public int updateAppStoreById(AppStorePo appStorePo) {
        return 1;
    }

    public AppStorePo queryAppStoreById(String appStoreId) {
        return null;
    }

    public List<AppStorePo> queryAppStores(AppStorePo appStorePo) {
        return new ArrayList<>();
    }
}
