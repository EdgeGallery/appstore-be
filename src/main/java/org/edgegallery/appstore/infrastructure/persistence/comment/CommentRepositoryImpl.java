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

import java.util.List;
import java.util.stream.Collectors;
import org.edgegallery.appstore.domain.model.comment.Comment;
import org.edgegallery.appstore.domain.model.comment.CommentHistory;
import org.edgegallery.appstore.domain.model.comment.CommentRepository;
import org.edgegallery.appstore.domain.shared.Page;
import org.edgegallery.appstore.domain.shared.PageCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public int store(Comment comment) {
        return commentMapper.insert(CommentPo.of(comment));
    }

    @Override
    public CommentHistory lookupCommentHistoryOfApp(String appId) {
        return new CommentHistory(commentMapper.findAll(appId));
    }

    @Override
    public void removeByAppId(String appId) {
        commentMapper.removeAll(appId);
    }

    @Override
    public Page<Comment> findAllWithPagination(PageCriteria pageCriteria) {
        long total = Long.parseLong(String.valueOf(commentMapper.countTotal(pageCriteria)));
        List<Comment> commentList = commentMapper.findAllWithPagination(pageCriteria).stream()
            .map(CommentPo::toDomainModel).collect(Collectors.toList());
        return new Page<>(commentList, pageCriteria.getLimit(), pageCriteria.getOffset(), total);
    }

    @Override
    public int getNumofComments(String appId) {
        return commentMapper.getNumOfComments(appId);
    }
}
