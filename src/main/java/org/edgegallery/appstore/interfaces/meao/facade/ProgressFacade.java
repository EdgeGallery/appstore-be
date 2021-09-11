package org.edgegallery.appstore.interfaces.meao.facade;

import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgress;
import org.edgegallery.appstore.infrastructure.persistence.meao.PackageUploadProgressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("ProgressFacade")
public class ProgressFacade {
    @Autowired
    PackageUploadProgressMapper packageUploadProgressMapper;

    public ResponseEntity<String> createProgress(PackageUploadProgress process) {
        process.setId(UUID.randomUUID().toString());
        int ret = packageUploadProgressMapper.insertSelective(process);
        if (ret > 0) {
            return ResponseEntity.ok("create process success.");
        } else {
            throw new AppException("create process fail.");
        }
    }

    public ResponseEntity<PackageUploadProgress> getProgress(String id) {
        PackageUploadProgress ret = packageUploadProgressMapper.selectByPrimaryKey(id);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException("get process fail.");
        }
    }

    public ResponseEntity<List<PackageUploadProgress>> getProgressByPackageAndMeao(String packageId, String meaoId) {
        List<PackageUploadProgress> ret = packageUploadProgressMapper.selectByPackageAndMeao(packageId, meaoId);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException("get process fail.");
        }
    }

    public ResponseEntity<String> updateProgress(PackageUploadProgress process) {
        PackageUploadProgress record = packageUploadProgressMapper.selectByPrimaryKey(process.getId());
        if (record == null) {
            throw new AppException("process not exist.");
        }

        int ret = packageUploadProgressMapper.updateByPrimaryKeySelective(process);
        if (ret > 0) {
            return ResponseEntity.ok("update process success.");
        } else {
            throw new AppException("update process fail.");
        }
    }

    public ResponseEntity<String> deleteProgress(String id) {
        int ret = packageUploadProgressMapper.deleteByPrimaryKey(id);

        if (ret < 0) {
            throw new AppException("delete process fail.");
        } else {
            return ResponseEntity.ok("delete process success.");
        }
    }
}
