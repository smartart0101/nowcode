package com.max.service;

import com.max.Util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * redis 对数据库的访问及修改
 */
@Service
public class RedisLikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞功能 特点：第一次点赞成功，第二次取消点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {

        //由于自己收到的赞，要添加一个查询条件（用户ID），因此方法重构
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //得到实体类和用户的 KEY
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                //当前实体是否为用户点过赞
                boolean member = redisOperations.opsForSet().isMember(entityLikeKey, userId);

                //做判断，设计事务，要在事务中间执行语句
                redisOperations.multi();  //事务启动

                //存在就是点过赞了，要取消点赞，否则点赞
                if (member) {
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else {
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }

                return redisOperations.exec();
            }
        });
    }

    //查询某个实体收到赞的数量, redis 关于 set 的 size 方法
    public long findLikenum(int entityType, int entityId) {
        //设置 key
        String redisLikenum = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //redis 命令 isMember 判断 userID 是否存在（在集合中？）
        return redisTemplate.opsForSet().size(redisLikenum);
    }

    //查询某个人对于某个实体的点赞状况
    public int findLikeStatus(int userID, int entityType, int entityId) {
        //设置 key
        String redisLikeStatus = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //redis 命令 isMember 判断 userID 是否存在（在集合中？）
        return redisTemplate.opsForSet().isMember(redisLikeStatus, userID) ? 1 : 0;
    }

    //查询某个人收到的点赞状况
    public int findUserLikeCount(int userId) {
        //设置 key
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer userLikeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        //redis 命令 isMember 判断 userID 是否存在（在集合中？）
        return userLikeCount == null ? 0 : userLikeCount.intValue();
    }

}
