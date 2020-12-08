package org.edgegallery.appstore.infrastructure.persistence.apackage;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.springframework.stereotype.Component;

@Component
@Mapper
public interface PackageMapper {

    AppReleasePo findReleaseById(String packageId);

    void updateRelease(AppReleasePo releasePo);

    void insertRelease(AppReleasePo releasePo);

    void removeByPackageId(String packageId);

    void removeReleasesByAppId(String appId);

    List<AppReleasePo> findAllByAppId(String appId);

    List<AppReleasePo> findAllWithPagination(PageCriteria pageCriteria);

    Integer countTotalForReleases(PageCriteria pageCriteria);
}
