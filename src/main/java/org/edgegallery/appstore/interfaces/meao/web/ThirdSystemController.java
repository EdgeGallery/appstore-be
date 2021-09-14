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

package org.edgegallery.appstore.interfaces.meao.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.infrastructure.persistence.meao.ThirdSystem;
import org.edgegallery.appstore.interfaces.meao.facade.ThirdSystemFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "thirdSystem")
@RequestMapping("/mec/appstore/v1/thirdsystem")
@Api(tags = {"ThirdSystem Controller"})
@Validated
public class ThirdSystemController {
    @Autowired
    ThirdSystemFacade thirdSystemFacade;

    /**
     * create a thirdSystem.
     */
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "create a thirdSystem", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> createThirdSystem(@RequestBody ThirdSystem thirdSystem) {
        return thirdSystemFacade.createThirdSystem(thirdSystem);
    }

    /**
     * query a thirdSystem.
     */
    @GetMapping(value = "/{thirdSystemId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get a thirdSystem", response = ThirdSystem.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<ThirdSystem> getThirdSystem(
        @ApiParam(value = "thirdSystemId") @PathVariable("thirdSystemId") String thirdSystemId) {
        return thirdSystemFacade.getThirdSystemById(thirdSystemId);
    }

    /**
     * query thirdSystem by type.
     */
    @GetMapping(value = "/systemType/{type}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "query thirdSystem by type", response = ThirdSystem.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<List<ThirdSystem>> getThirdSystemByType(
        @ApiParam(value = "type") @PathVariable("type") String type) {
        return thirdSystemFacade.getThirdSystemByType(type);
    }

    /**
     * update a thirdSystem.
     */
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update a thirdSystem", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> updateThirdSystem(@RequestBody ThirdSystem thirdSystem) {
        return thirdSystemFacade.updateThirdSystem(thirdSystem);
    }

    /**
     * delete a thirdSystem.
     */
    @DeleteMapping(value = "/{thirdSystemId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "delete a thirdSystem", response = String.class)
    @ApiResponses(value = {
        @ApiResponse(code = 500, message = "resource grant error", response = String.class)
    })
    public ResponseEntity<String> deleteThirdSystem(
        @ApiParam(value = "thirdSystemId") @PathVariable("thirdSystemId") String thirdSystemId) {
        return thirdSystemFacade.deleteThirdSystem(thirdSystemId);
    }
}
