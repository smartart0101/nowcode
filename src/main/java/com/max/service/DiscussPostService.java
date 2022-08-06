package com.max.service;


import com.max.Util.SensitiveUtil;
import com.max.dao.DiscussPostMapper;
import com.max.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * service 有方法，调用dao相关方法
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveUtil sensitiveUtil;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        //System.out.println("11111111");
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


    //添加帖子的方法，不过要做进一步处理：转义字符、敏感词过滤
    public int AddDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //转义特殊字符串，比如html语法中的<>.不能让他破环浏览器页面
        //只有 Title Content 需要这样处理（标题，内容）
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过虑敏感词
        discussPost.setTitle(sensitiveUtil.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveUtil.filter(discussPost.getContent()));

        //service 做一些持久层的操作，得到的结果，作为参数传给dao层
        return discussPostMapper.InsertDiscussPost(discussPost);
    }

    //查询帖子的方法
    public DiscussPost FindDiscussPost(int id){
        return discussPostMapper.selectDiscussPost(id);
    }
}
