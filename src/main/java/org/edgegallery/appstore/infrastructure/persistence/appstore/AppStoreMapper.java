package org.edgegallery.appstore.infrastructure.persistence.appstore;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface AppStoreMapper {

    int addAppStore(AppStorePo appStorePo);

    int deleteAppStoreById(String appStoreId);

    int updateAppStoreById(AppStorePo appStorePo);

    AppStorePo queryAppStoreById(String appStoreId);

    List<AppStorePo> queryAppStores(AppStorePo appStorePo);

}
