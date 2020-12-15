package org.edgegallery.appstore.infrastructure.persistence.appstore;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface AppStoreMapper {

    ExAppStorePo findExAppStoreById(String id);

}
