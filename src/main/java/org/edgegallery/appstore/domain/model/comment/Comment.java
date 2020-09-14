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

package org.edgegallery.appstore.domain.model.comment;

import org.edgegallery.appstore.domain.model.user.User;
import org.edgegallery.appstore.domain.shared.Entity;

public class Comment implements Entity {

    private int commentId;

    private User user;

    private String appId;

    private String body;

    private double score;

    private String commentTime;

    /**
     * Constructor of Comment.
     *
     * @param user user object.
     * @param appId app id.
     * @param body comment content.
     * @param score score of comment.
     * @param commentTime score of comment Time.
     */
    public Comment(User user, String appId, String body, double score, String commentTime) {
        this.user = user;
        this.appId = appId;
        this.body = body;
        this.score = score;
        this.commentTime = commentTime;
    }

    /**
     * Constructor of Comment.
     *
     * @param user user object.
     * @param appId app id.
     * @param body comment content.
     * @param score score of comment.
     */
    public Comment(User user, String appId, String body, double score) {
        this.user = user;
        this.appId = appId;
        this.body = body;
        this.score = score;
    }

    /**
     * Constructor of Comment.
     *
     * @param body comment content.
     * @param score score of comment.
     */
    public Comment(String body, double score) {
        this.body = body;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public User getUser() {
        return user;
    }

    public String getAppId() {
        return appId;
    }

    public String getBody() {
        return body;
    }

    public int getCommentId() {
        return commentId;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }
}
