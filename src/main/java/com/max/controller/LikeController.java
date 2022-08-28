package com.max.controller;

import com.max.Util.Communityutil;
import com.max.Util.HostHolder;
import com.max.entity.User;
import com.max.service.RedisLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理点赞相关的业务方法
 */
@Controller
public class LikeController {

    @Autowired
    private RedisLikeService redisLikeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String Like(int entityType, int entityId) {
        //当前登陆用户
        User user = hostHolder.getUser();

//        if(user == null ){
//            return "";
//        }

        //点赞
        redisLikeService.Liketosth(user.getId(), entityType, entityId);
        //查赞的数量
        long LikeCount = redisLikeService.findLikenum(entityType, entityId);
        //用户点赞的状态 1:点过了  0 ：没点过
        int LikeStatus = redisLikeService.findLikeStatus(user.getId(), entityType, entityId);

        //添加到 map
        Map<String, Object> likeMap = new HashMap<>();
        likeMap.put("likeCount", LikeCount);
        likeMap.put("likeStatus", LikeStatus);

        return Communityutil.getJSONString(0, null, likeMap);
    }

}
