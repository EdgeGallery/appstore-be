/* Copyright 2020 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edgegallery.appstore.interfaces.apackage.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.interfaces.apackage.facade.PackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.PushablePackageServiceFacade;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushTargetAppStoreDto;
import org.edgegallery.appstore.interfaces.apackage.facade.dto.PushablePackageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RestSchema(schemaId = "pushable-package")
@RequestMapping("/mec/appstore/poke/pushable")
@Api(tags = {"Pushable Package Controller"})
@Validated
public class PushablePackageController {

    @Autowired
    private PushablePackageServiceFacade pushablePackageServiceFacade;

    @Autowired
    private PackageServiceFacade packageServiceFacade;

    @GetMapping(value = "/packages", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get all the pushable packages", response = PushablePackageDto.class,
        responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<List<PushablePackageDto>> queryAllPushablePackages() {
        return pushablePackageServiceFacade.queryAllPushablePackages();
    }

    @GetMapping(value = "/packages/{packageId}", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get one the pushable packages by id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<PushablePackageDto> getPushablePackage(String packageId) {
        return pushablePackageServiceFacade.getPushablePackage(packageId);
    }

    @PostMapping(value = "/packages/{packageId}/action/push", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "get one the pushable packages by id.", response = PushablePackageDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "bad request", response = String.class)
    })
    public ResponseEntity<String> pushPackage(String packageId, PushTargetAppStoreDto dto) {
        pushablePackageServiceFacade.pushPackage(packageId, dto);
        return ResponseEntity.ok("");
    }

}
