/*
 *    Copyright 2020 Huawei Technologies Co., Ltd.
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
import java.util.List;
import javax.validation.constraints.Pattern;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.interfaces.comment.facade.CommentServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RestSchema(schemaId = "comment")
@RequestMapping("/mec/appstore/v1/")
@Api(tags = {"Comment Controller"})
@Validated
public class CommentController {

    private static final String REG_USER_ID = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static final String REG_USER_NAME = "^[a-zA-Z][a-zA-Z0-9_]{5,29}$";

    private static final String REG_APP_ID = "[0-9a-f]{32}";

    @Autowired
    CommentServiceFacade appCommentService;

    @PostMapping(value = "/apps/{appId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "add comment to a app.", response = String.class)
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<String> addComments(@RequestParam("userId") @Pattern(regexp = REG_USER_ID) String userId,
                @RequestParam("userName") @Pattern(regexp = REG_USER_NAME) String userName,
                @ApiParam(value = "appId", required = true) @Pattern(regexp = REG_APP_ID) @PathVariable("appId")
                            String appId, @Validated @RequestBody CommentRequest entity) {
        appCommentService.comment(new User(userId, userName), appId, entity.body, entity.score);
        return ResponseEntity.ok("comments success.");
    }

    @GetMapping(value = "/apps/{appId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "get comments by csar id.", response = Comment.class, responseContainer = "List")
    @ApiResponses(value = {
                @ApiResponse(code = 404, message = "microservice not found", response = String.class),
                @ApiResponse(code = 415, message = "Unprocessable MicroServiceInfo Entity ", response = String.class),
                @ApiResponse(code = 500, message = "resource grant " + "error", response = String.class)
                })
    @PreAuthorize("hasRole('APPSTORE_TENANT')")
    public ResponseEntity<List<Comment>> getComments(@ApiParam(value = "app Id", required = true) @PathVariable("appId")
            @Pattern(regexp = REG_APP_ID) String appId) {
        return appCommentService.getComments(appId, 100, 0);
    }
}
