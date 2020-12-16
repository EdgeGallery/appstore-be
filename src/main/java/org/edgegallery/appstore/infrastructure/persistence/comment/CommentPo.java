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

package org.edgegallery.appstore.infrastructure.persistence.comment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.model.user.User;

@Getter
@Setter
@Entity
@Table(name = "csar_pakage_score")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentPo {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Id
    @Column(name = "COMMENTID")
    private int commentId;

    @Column(name = "USERID")
    private String userId;

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "APPID")
    private String appId;

    @Column(name = "COMMENTS")
    private String comments;

    @Column(name = "SCORE")
    private double score;

    @Column(name = "COMMENTTIME")
    private Date commentTime;

    public CommentPo() {
    }

    /**
     * Constructor of CommentPO.
     */
    public CommentPo(int commentId, String userId, String userName, String appId, String comments, double score,
        Date commentTime) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.appId = appId;
        this.comments = comments;
        this.score = score;
        if (commentTime != null) {
            this.commentTime = (Date) commentTime.clone();
        } else {
            this.commentTime = null;
        }
    }

    /**
     * Constructor of CommentPO.
     */
    public CommentPo(int commentId, String userId, String userName, String appId, String comments, double score) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.appId = appId;
        this.comments = comments;
        this.score = score;
    }

    /**
     * Transfer Comment to CommentPO.
     */
    public static CommentPo of(Comment comment) {
        return new CommentPo(comment.getCommentId(), comment.getUser().getUserId(), comment.getUser().getUserName(),
            comment.getAppId(), comment.getBody(), comment.getScore());
    }

    public Date getCommentTime() {
        return (Date) commentTime.clone();
    }

    public void setCommentTime(Date commentTime) {
        this.commentTime = (Date) commentTime.clone();
    }

    public Comment toDomainModel() {
        return new Comment(new User(userId, userName), appId, comments, score,
            new SimpleDateFormat(DATE_FORMAT).format(commentTime));
    }
}
