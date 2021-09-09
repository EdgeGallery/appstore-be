package org.edgegallery.appstore.infrastructure.persistence.meao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface ThirdSystemMapper {

    int deleteByPrimaryKey(String id);

    int insert(ThirdSystem record);

    int insertSelective(ThirdSystem record);

    ThirdSystem selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ThirdSystem record);

    int updateByPrimaryKey(ThirdSystem record);
}