package com.max.dao;


import com.max.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 页面相关的处理方法
 */

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    //添加帖子的方法.这写完要到 xml 文件写相应的 sql 语句
    int InsertDiscussPost(DiscussPost discussPost);

    //查询帖子详情的方法，一样要配置 sql 语句
    DiscussPost selectDiscussPost(int id);

    //添加评论后，配置sql语句，跟据id，修改评论
    int UpdateCommentCount(int id, int commentCount);

}
