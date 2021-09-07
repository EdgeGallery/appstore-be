package org.edgegallery.appstore.infrastructure.persistence.apackage;

public interface PackageSubscribeMapper {

    int deleteByPrimaryKey(String id);

    int insert(PackageSubscribe record);

    int insertSelective(PackageSubscribe record);

    PackageSubscribe selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(PackageSubscribe record);

    int updateByPrimaryKey(PackageSubscribe record);
}