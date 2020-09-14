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

package org.edgegallery.appstore.application;

import org.edgegallery.appstore.domain.model.app.App;
import org.edgegallery.appstore.domain.model.app.AppRepository;
import org.edgegallery.appstore.domain.model.comment.CannotCreateCommentExecption;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.model.comment.CommentRepository;
import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.exceptions.EntityNotFoundException;
import org.edgegallery.appstore.domain.shared.exceptions.RedundantCommentsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("AppCommentService")
public class AppCommentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppCommentService.class);

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Constructor of comments.
     *
     * @param user user info.
     * @param appId id of app.
     * @param comments content of comments.
     * @param score score of app.
     * @throws EntityNotFoundException throw EntityNotFoundException.
     */
    @Transactional(rollbackFor = CannotCreateCommentExecption.class)
    public void comment(User user, String appId, String comments, double score) {
        App app = appRepository.find(appId).orElseThrow(() -> new EntityNotFoundException(App.class, appId));
        Comment comment = new Comment(user, app.getAppId(), comments, score);
        if (commentRepository.store(comment) == 1) {
            app.comment(comment);
            appRepository.store(app);
        } else {
            LOGGER.info("User {0} has already comments to app {1} yet.", user.getUserId(), appId);
            throw new RedundantCommentsException(user.getUserId(), appId);
        }
    }
}
