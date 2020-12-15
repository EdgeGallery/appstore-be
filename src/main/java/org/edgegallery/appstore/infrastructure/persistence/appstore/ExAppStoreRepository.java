package org.edgegallery.appstore.infrastructure.persistence.appstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ExAppStoreRepository {
    @Autowired
    private AppStoreMapper appStoreMapper;

    public ExAppStorePo findAppStoreById(String id) {
        return appStoreMapper.findExAppStoreById(id);
    }
}
