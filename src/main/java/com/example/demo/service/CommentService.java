package com.example.demo.service;

import com.example.demo.dao.ICommentMapper;
import com.example.demo.entity.Comment;
import com.example.demo.util.IDenoConstant;
import com.example.demo.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements IDenoConstant {

    @Autowired
    private ICommentMapper iCommentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return iCommentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return iCommentMapper.selectCountByEntity(entityType, entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = iCommentMapper.insertComment(comment);

        //更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = iCommentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public Comment findCommentById(int id) {
        return iCommentMapper.selectCommentById(id);
    }

    public List<Comment> findCommentByUserId(int entityType, int userId, int offset, int limit) {

        return iCommentMapper.selectCommentByUserId(entityType, userId, offset, limit);
    }

    public int findCommentCountByUserId(int entityType, int userId) {
        return iCommentMapper.selectCommentCountByUserId(entityType, userId);
    }
}
