package com.max.controller;


import com.max.Util.CommunityConstant;
import com.max.entity.DiscussPost;
import com.max.entity.Page;
import com.max.entity.User;
import com.max.service.DiscussPostService;
import com.max.service.RedisLikeService;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用service,得到 model ,和浏览器交互
 */
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisLikeService redisLikeService;

    /**
     * 定义一个处理请求的方法、该方法把视图处理后，为了处理方便，直接返回一个访问路径，也就是字符串
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {

        /**
         *得到页面显示的总行数，默认是首页
         * 在调用方法前，mvc会将model和page实例化，并将page注入model
         * 因此可以在thymeleaf中直接访问page的数据
         * 但是这里出了问题，猜测并没有将page注入
         */

        page.setRows(discussPostService.findDiscussRows(0));
        page.setPath("/index");


        //自定义一个查询条件，得到 DiscussPost 数据集合
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        //想要将得到的 DiscussPost，遍历处理后，存入新的集合
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        //处理 list集合中的DiscussPost数据，把 userId 和 user 关联
        if (list != null) {
            for (DiscussPost post : list) {
                //将数据取出来后，放进Map集合，先实例化Map
                Map<String, Object> map = new HashMap<>();
                //把post放入
                map.put("post", post);
                //调用方法，查到id对应的user
                User user = userService.findUserById(post.getUserId());
                //把得到的user放入map
                map.put("user", user);
                //map放入discussPosts

                // 页面要显示用户的言论收到了多少赞，就要得到数据并添加进 map
                long likeCount = redisLikeService.findLikenum(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        //得到的集合放入模版
        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }

    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }
}
