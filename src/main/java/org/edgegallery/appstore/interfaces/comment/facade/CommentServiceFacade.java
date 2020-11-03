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

package org.edgegallery.appstore.interfaces.comment.facade;

import java.util.List;
import org.edgegallery.appstore.application.AppCommentService;
import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.model.comment.CommentRepository;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service("CommentServiceFacade")
public class CommentServiceFacade {

    public static final Logger LOGGER = LoggerFactory.getLogger(CommentServiceFacade.class);

    @Autowired
    AppCommentService appCommentService;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    private AppRepository appRepository;
    /**
     * comment method with parameters.
     */
    public void comment(User user, String appId, String comments, double score) {
        LOGGER.info("User {} comments length{} to app {}, score{}", user.getUserName(), comments.length(), appId,
                score);
        appCommentService.comment(user, appId, comments, score);
    }

    /**
     * get comments by app id and page parameters.
     */
    public ResponseEntity<List<Comment>> getComments(String appId, int limit, long offset) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        return ResponseEntity
            .ok(commentRepository.findAllWithPagination(new PageCriteria(limit, offset, app.getAppId()))
                .getResults());
    }
}
