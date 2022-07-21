package com.max.service;


import com.max.dao.DiscussPostMapper;
import com.max.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * service 有方法，调用dao相关方法
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        System.out.println("11111111");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * 这里存在一个问题，findDiscussPosts 方法返回 DiscussPost 类型的数据，包含了userId这样的数据，
     * 但是我们习惯将用户昵称展示出来，而不是一串串数字--有两个方法
     * 1、在写 sql 语句时，将名称和 ID 关联在一起，然后处理
     * 2、在得到每一个 DiscussPost 数据后，单独调用方法去查询相关 user信息
     * 选方法 2 ，耦合度低，可以改进的空间大
     */

    public int findDiscussRows(int userId) {
        //return findDiscussRows(userId);  //这个语法出现重大错误。导致了栈溢出
        return discussPostMapper.selectDiscussPostRows(userId);
    }
}
