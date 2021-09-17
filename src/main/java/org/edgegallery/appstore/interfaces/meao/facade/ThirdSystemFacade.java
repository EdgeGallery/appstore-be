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

import java.util.List;
import java.util.UUID;
import org.edgegallery.appstore.domain.constants.ResponseConst;
import org.edgegallery.appstore.domain.shared.exceptions.AppException;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystem;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("ThirdSystemFacade")
public class ThirdSystemFacade {
    private static final String CREATE_THIRD_SYSTEM_ERR_MESSAGES = "create third system fail.";

    private static final String QUERY_THIRD_SYSTEM_ERR_MESSAGES = "get third system fail.";

    private static final String UPDATE_THIRD_SYSTEM_ERR_MESSAGES = "update third system fail.";

    private static final String DELETE_THIRD_SYSTEM_ERR_MESSAGES = "delete third system fail.";

    private static final String THIRD_SYSTEM_ERR_NOT_FOUND_MESSAGES = "third system not exist.";

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
            throw new AppException(CREATE_THIRD_SYSTEM_ERR_MESSAGES, ResponseConst.RET_CREATE_THIRD_SYSTEM_FAILED);
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
            throw new AppException(QUERY_THIRD_SYSTEM_ERR_MESSAGES, ResponseConst.RET_QUERY_THIRD_SYSTEM_FAILED);
        }
    }

    /**
     * query thirdSystem by type.
     *
     * @param type type
     * @return ThirdSystem
     */
    public ResponseEntity<List<ThirdSystem>> getThirdSystemByType(String type) {
        List<ThirdSystem> ret = thirdSystemMapper.selectBySystemType(type);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException(QUERY_THIRD_SYSTEM_ERR_MESSAGES, ResponseConst.RET_QUERY_THIRD_SYSTEM_FAILED);
        }
    }

    /**
     * query thirdSystem by like name.
     *
     * @param name name
     * @return ThirdSystem
     */
    public ResponseEntity<List<ThirdSystem>> selectByNameLike(String name) {
        List<ThirdSystem> ret = thirdSystemMapper.selectByNameLike(name);
        if (ret != null) {
            return ResponseEntity.ok(ret);
        } else {
            throw new AppException(QUERY_THIRD_SYSTEM_ERR_MESSAGES, ResponseConst.RET_QUERY_THIRD_SYSTEM_FAILED);
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
            throw new AppException(THIRD_SYSTEM_ERR_NOT_FOUND_MESSAGES, ResponseConst.RET_THIRD_SYSTEM_NOT_FOUND);
        }

        int ret = thirdSystemMapper.updateByPrimaryKeySelective(thirdSystem);
        if (ret > 0) {
            return ResponseEntity.ok("update third system success.");
        } else {
            throw new AppException(UPDATE_THIRD_SYSTEM_ERR_MESSAGES, ResponseConst.RET_UPDATE_THIRD_SYSTEM_FAILED);
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
            throw new AppException(DELETE_THIRD_SYSTEM_ERR_MESSAGES, ResponseConst.RET_DELETE_THIRD_SYSTEM_FAILED);
        } else {
            return ResponseEntity.ok("delete third system success.");
        }
    }

}
