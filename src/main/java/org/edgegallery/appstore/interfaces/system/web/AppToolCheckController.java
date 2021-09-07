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

package org.edgegallery.appstore.interfaces.system.web;

import io.swagger.annotations.ApiOperation;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "appToolCheck")
@RequestMapping("/mec/appstore/v1/tool")
@Controller
public class AppToolCheckController {

    @Value("${appstore-be.app-pkg-trans-tool.enabled}")
    private String appToolEnabled;

    /**
     * check if deploy app package trans tool.
     *
     * @return true or false
     */
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "app tool check", response = Boolean.class)
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_ADMIN') || hasRole('APPSTORE_GUEST')")
    public ResponseEntity<Boolean> appPkgTransToolCheck() {
        Boolean checkResult = "true".equals(appToolEnabled);
        return ResponseEntity.ok(checkResult);
    }
}
