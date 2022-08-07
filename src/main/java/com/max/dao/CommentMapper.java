package com.max.dao;

import com.max.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评论相关的有关数据库的 CRUD 操作
 * 显然他是一个接口
 */
@Mapper
public interface CommentMapper {

    //定义一个可以查到一个页面上所有的相关帖子的方法。应该是一个集合
    List<Comment> SelectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    //定义一个方法，显示出查询数据的条数
    int SelectCountByEntity(int entityType, int entityId);

    //定义一个方法，用作更改数据
    int InsertComment(Comment comment);


}
