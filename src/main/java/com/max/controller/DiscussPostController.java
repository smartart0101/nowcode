package com.max.controller;

import com.max.Util.CommunityConstant;
import com.max.Util.Communityutil;
import com.max.Util.HostHolder;
import com.max.entity.Comment;
import com.max.entity.DiscussPost;
import com.max.entity.Page;
import com.max.entity.User;
import com.max.service.CommentService;
import com.max.service.DiscussPostService;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 有关客户端所有帖子相关的操作都在这里
 * 22.8.2 AJAX 异步处理
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    //添加帖子. 判断依据是此时登陆用户的各种信息
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody      //return String
    public String Adddiscusspost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return Communityutil.getJSONString(403, "你还没有登陆哦");
        }
        //已登陆用户，将发布的一系列内容,以及一些用户信息，填入post，调用service
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.AddDiscussPost(discussPost);

        //给出一个正确的提示
        return Communityutil.getJSONString(0, "发送成功");
    }

    //查询帖子详情，根据发布帖子用户的 id 查询
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        //根据 id 查询到相关的 post ,再注入到 model 中
        DiscussPost discussPost = discussPostService.FindDiscussPost(discussPostId);
        model.addAttribute("discussPost", discussPost);

        //要查用户的信息，一种是关联查询，一种是在 controller 写方法。
        // 选后者，带来的性能的一点损失，后期 redis 来弥补
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);

        //下面是处理评论相关，都属于页面

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        //分页信息
        page.setLimit(5);  //每页5条评论
        page.setPath("/discuss/detail/" + discussPostId);  //路径
        page.setRows(discussPost.getCommentCount());  // 总评论数

        //评论列表
        List<Comment> commentList = commentService.FindCommentsByEntity(
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());

        //Vo 显示 view object，  map 作为显示的对象
        List<Map<String, Object>> CommentVoList = new ArrayList<>();

        //如果commentList 是空的 没有评论，应该给一个静态页面
//        if(commentList == null ){
//
//        }
        // commentList 不为空
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> commentvo = new HashMap<>();
                //评论
                commentvo.put("comment", comment);
                //作者
                commentvo.put("user", userService.findUserById(comment.getUserId()));

                //不仅帖子有评论，评论也会有评论，叫回复
                //回复列表
                List<Comment> replyList = commentService.FindCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //Vo 显示 view object，  回复vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyvo = new HashMap<>();
                        //评论
                        replyvo.put("reply", reply);
                        //作者
                        replyvo.put("user", userService.findUserById(reply.getUserId()));
                        //回复对象
                        User target_user = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyvo.put("target_user", target_user);

                        //添加进集合
                        replyVoList.add(replyvo);
                    }
                }
                //回复列表
                commentvo.put("replys",replyVoList);

                //回复数量
                int replycount = commentService.FindCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentvo.put("replycount",replycount);

                CommentVoList.add(commentvo);
            }
        }
        model.addAttribute("comments",CommentVoList);
        return "/site/discuss-detail";
    }


}
