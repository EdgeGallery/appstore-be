package org.edgegallery.appstore.infrastructure.persistence.apackage;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PackageMapper {

    AppReleasePO findReleaseById(String packageId);

    void updateRelease(AppReleasePO releasePO);

}
