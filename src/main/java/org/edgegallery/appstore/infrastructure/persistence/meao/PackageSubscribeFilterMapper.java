package org.edgegallery.appstore.infrastructure.persistence.meao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PackageSubscribeFilterMapper {

    int deleteByPrimaryKey(String id);

    int insert(PackageSubscribeFilter record);

    int insertSelective(PackageSubscribeFilter record);

    PackageSubscribeFilter selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PackageSubscribeFilter record);

    int updateByPrimaryKey(PackageSubscribeFilter record);
}