package org.edgegallery.appstore.infrastructure.persistence.meao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PackageUploadProgressMapper {

    int deleteByPrimaryKey(String id);

    int insert(PackageUploadProgress record);

    int insertSelective(PackageUploadProgress record);

    PackageUploadProgress selectByPrimaryKey(String id);

    List<PackageUploadProgress> selectByPackageAndMeao(String packageId, String meaoId);

    List<PackageUploadProgress> selectByPackageId(String packageId);

    int updateByPrimaryKeySelective(PackageUploadProgress record);

    int updateByPrimaryKey(PackageUploadProgress record);
}