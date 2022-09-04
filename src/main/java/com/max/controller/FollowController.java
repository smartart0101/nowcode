package com.max.controller;

import com.max.Util.CommunityConstant;
import com.max.Util.Communityutil;
import com.max.Util.HostHolder;
import com.max.entity.Page;
import com.max.entity.User;
import com.max.service.FollowService;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;


    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        return Communityutil.getJSONString(0, "已关注!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return Communityutil.getJSONString(0, "已取消关注!");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        //1,看关注的用户是否存在，
        User userById = userService.findUserById(userId);
        if (userById == null) {
            throw new RuntimeException("关注的用户不存在");
        }

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));


        List<Map<String, Object>> followee_userList = followService.findfollowees(userId, page.getOffset(), page.getLimit());
        if (followee_userList != null) {
            for (Map<String, Object> map : followee_userList) {
                User user = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users", followee_userList);

        return "/site/followee";
    }


    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        //1,看关注的用户是否存在，
        User userById = userService.findUserById(userId);
        if (userById == null) {
            throw new RuntimeException("关注的用户不存在");
        }

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(userId, ENTITY_TYPE_USER));


        List<Map<String, Object>> follower_userList = followService.findfollowers(userId, page.getOffset(), page.getLimit());
        if (follower_userList != null) {
            for (Map<String, Object> map : follower_userList) {
                User user = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(user.getId()));
            }
        }
        model.addAttribute("users", follower_userList);

        return "/site/follower";
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

}
