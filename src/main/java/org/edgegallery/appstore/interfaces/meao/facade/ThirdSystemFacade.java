package org.edgegallery.appstore.interfaces.meao.facade;

import java.util.UUID;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystem;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("ThirdSystemFacade")
public class ThirdSystemFacade {
    @Autowired
    ThirdSystemMapper thirdSystemMapper;

    public ResponseEntity<String> createThirdSystem(ThirdSystem thirdSystem) {
        thirdSystem.setId(UUID.randomUUID().toString());
        int ret = thirdSystemMapper.insertSelective(thirdSystem);
        if (ret > 0) {
            return ResponseEntity.ok("create third system success.");
        } else {
            throw new AppException("create third system fail.");
        }
    }

    public ResponseEntity<ThirdSystem> getThirdSystemById(String id) {
        ThirdSystem ret = thirdSystemMapper.selectByPrimaryKey(id);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException("get third system fail.");
        }
    }

    public ResponseEntity<String> updateThirdSystem(ThirdSystem thirdSystem) {
        ThirdSystem record = thirdSystemMapper.selectByPrimaryKey(thirdSystem.getId());
        if (record == null) {
            throw new AppException("third system not exist.");
        }

        int ret = thirdSystemMapper.updateByPrimaryKey(thirdSystem);
        if (ret > 0) {
            return ResponseEntity.ok("update third system success.");
        } else {
            throw new AppException("update third system fail.");
        }
    }

    public ResponseEntity<String> deleteThirdSystem(String id) {
        int ret = thirdSystemMapper.deleteByPrimaryKey(id);

        if (ret < 0) {
            throw new AppException("delete third system fail.");
        } else {
            return ResponseEntity.ok("delete third system success.");
        }
    }

}
