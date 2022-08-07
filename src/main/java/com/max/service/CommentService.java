package com.max.service;

import com.max.Util.CommunityConstant;
import com.max.Util.SensitiveUtil;
import com.max.dao.CommentMapper;
import com.max.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 *
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    //还需要敏感词过滤和讨论
    @Autowired
    private SensitiveUtil sensitiveUtil;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> FindCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.SelectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int FindCountByEntity(int entityType, int entityId) {
        return commentMapper.SelectCountByEntity(entityType, entityId);
    }

    //添加评论相关方法，（在数据库加了几条？）
    //修改数据库内容，涉及事务，需要相关注解
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //添加评论，并且要对内容进行敏感词过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveUtil.filter(comment.getContent()));
        int rows = commentMapper.InsertComment(comment);

        //更新帖子评论数量
        //如果是类型是帖子，
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            //根据comment 类型，id 查询数量
            int comment_num = commentMapper.SelectCountByEntity(comment.getEntityType(), comment.getEntityId());
            //根据 comment 类型id ，传入数量，在页面显示
            discussPostService.UpdateCommentCount(comment.getEntityId(),comment_num);
        }
        return rows;
    }

}
