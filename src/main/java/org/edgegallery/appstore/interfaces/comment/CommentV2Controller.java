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

package org.edgegallery.appstore.interfaces.comment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.constants.Consts;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.interfaces.comment.facade.CommentServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "commentV2")
@RequestMapping("/mec/appstore/v2/")
@Api(tags = {"Comment V2Controller"})
@Validated
public class CommentV2Controller {

    @Autowired
    CommentServiceFacade appCommentService;

    /**
     * query comments list.
     *
     * @param appId appId。
     * @param limit limit。
     * @param offset offset
     * @return page object.
     */
    @GetMapping(value = "/apps/{appId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "get comments by csar id.", response = Comment.class, responseContainer = "List")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "microservice not found", response = String.class),
        @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
    })
    @PreAuthorize("hasRole('APPSTORE_TENANT') || hasRole('APPSTORE_GUEST') || hasRole('APPSTORE_ADMIN')")
    public ResponseEntity<Page<Comment>> getCommentsV2(
        @ApiParam(value = "app Id", required = true) @PathVariable("appId") @Pattern(
            regexp = Consts.REG_APP_ID) String appId,
        @ApiParam(value = "the max count of one page", required = true) @Min(1) @RequestParam("limit") int limit,
        @ApiParam(value = "start index of the page", required = true) @Min(0) @RequestParam("offset") int offset) {
        return appCommentService.getCommentsV2(appId, limit, offset);
    }
}
