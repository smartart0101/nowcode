package com.max.controller;

import com.max.Util.Communityutil;
import com.max.Util.HostHolder;
import com.max.entity.DiscussPost;
import com.max.entity.User;
import com.max.service.DiscussPostService;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 *有关客户端所有帖子相关的操作都在这里
 * 22.8.2 AJAX 异步处理
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    //添加帖子. 判断依据是此时登陆用户的各种信息
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody      //return String
    public String Adddiscusspost(String title, String content){
        User user = hostHolder.getUser();
        if(user == null){
            return Communityutil.getJSONString(403,"你还没有登陆哦");
        }
        //已登陆用户，将发布的一系列内容,以及一些用户信息，填入post，调用service
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPostService.AddDiscussPost(discussPost);

        //给出一个正确的提示
        return Communityutil.getJSONString(0,"发送成功");
    }

    //查询帖子详情，根据发布帖子用户的 id 查询
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model){

        //根据 id 查询到相关的 post ,再注入到 model 中
        DiscussPost discussPost = discussPostService.FindDiscussPost(discussPostId);
        Model postmodel = model.addAttribute("discussPost",discussPost);

        //要查用户的信息，一种是关联查询，一种是在 controller 写方法。
        // 选后者，带来的性能的一点损失，后期 redis 来弥补
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        return "/site/discuss-detail";
    }


}
