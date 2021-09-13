/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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

    /**
     * create a thirdSystem.
     *
     * @param thirdSystem thirdSystem
     * @return String
     */
    public ResponseEntity<String> createThirdSystem(ThirdSystem thirdSystem) {
        thirdSystem.setId(UUID.randomUUID().toString());
        int ret = thirdSystemMapper.insertSelective(thirdSystem);
        if (ret > 0) {
            return ResponseEntity.ok("create third system success.");
        } else {
            throw new AppException("create third system fail.");
        }
    }

    /**
     * query a thirdSystem by id.
     *
     * @param id thirdSystem id
     * @return ThirdSystem
     */
    public ResponseEntity<ThirdSystem> getThirdSystemById(String id) {
        ThirdSystem ret = thirdSystemMapper.selectByPrimaryKey(id);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException("get third system fail.");
        }
    }

    /**
     * update a thirdSystem.
     *
     * @param thirdSystem thirdSystem
     * @return String
     */
    public ResponseEntity<String> updateThirdSystem(ThirdSystem thirdSystem) {
        ThirdSystem record = thirdSystemMapper.selectByPrimaryKey(thirdSystem.getId());
        if (record == null) {
            throw new AppException("third system not exist.");
        }

        int ret = thirdSystemMapper.updateByPrimaryKeySelective(thirdSystem);
        if (ret > 0) {
            return ResponseEntity.ok("update third system success.");
        } else {
            throw new AppException("update third system fail.");
        }
    }

    /**
     * delete a thirdSystem.
     *
     * @param id thirdSystem id
     * @return String
     */
    public ResponseEntity<String> deleteThirdSystem(String id) {
        int ret = thirdSystemMapper.deleteByPrimaryKey(id);

        if (ret < 0) {
            throw new AppException("delete third system fail.");
        } else {
            return ResponseEntity.ok("delete third system success.");
        }
    }

}
