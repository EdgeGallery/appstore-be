package org.edgegallery.appstore.domain.model.releases;

public interface PackageRepository {

    void updateRelease(Release release);

    Release findReleaseById(String appId, String packageId);

    void storeRelease(Release release);

    void removeRelease(Release release);
}
