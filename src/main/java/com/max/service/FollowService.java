package com.max.service;

import com.max.Util.CommunityConstant;
import com.max.Util.RedisKeyUtil;
import com.max.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *
 */
@Service
public class FollowService implements CommunityConstant {

    //想要往 Redis 存入数据，就要注入
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    //关注功能，
    public void follow(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String follloweeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String folllowerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                //做判断，设计事务，要在事务中间执行语句
                redisOperations.multi();  //事务启动

                redisOperations.opsForZSet().add(follloweeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(folllowerKey, userId, System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    //取消关注功能，
    public void unfollow(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String follloweeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String folllowerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                //做判断，设计事务，要在事务中间执行语句
                redisOperations.multi();  //事务启动

                redisOperations.opsForZSet().remove(follloweeKey, entityId);
                redisOperations.opsForZSet().remove(folllowerKey, userId);

                return redisOperations.exec();
            }
        });
    }


    //查询用户关注实体的数量  关注数
    public long findFolloweeCount(int userId, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityId);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //查询关注某实体的数量  粉丝数
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询用户是否关注实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //查询用户的关注列表
    public List<Map<String, Object>> findfollowees(int userId, int offset, int limit) {
        //得到 KEY ，用来取 REDIS 里面的 VALUE
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        //索引倒序排列指定区间元素
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> userList = new ArrayList<>();
        for (Integer targetId : targetIds) {
            HashMap<Object, Object> map = new HashMap<>();
            User userById = userService.findUserById(targetId);
            map.put("user",userById);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
        }
        return userList;
    }

    //查询用户的粉丝列表
    public List<Map<String, Object>> findfollowers(int userId, int offset, int limit) {
        //得到 KEY ，用来取 REDIS 里面的 VALUE
        String followerKey = RedisKeyUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        //索引倒序排列指定区间元素
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> userList = new ArrayList<>();
        for (Integer targetId : targetIds) {
            HashMap<Object, Object> map = new HashMap<>();
            User userById = userService.findUserById(targetId);
            map.put("user",userById);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
        }
        return userList;
    }
}
