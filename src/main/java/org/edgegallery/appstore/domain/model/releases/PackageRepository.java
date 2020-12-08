package org.edgegallery.appstore.domain.model.releases;

public interface PackageRepository {

    void updateStatus(String packageId, EnumPackageStatus status);

    Release findReleaseById(String appId, String packageId);
}
