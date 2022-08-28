package com.max.service;

import com.max.Util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * redis 对数据库的访问及修改
 */
@Service
public class RedisLikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞功能 特点：第一次点赞成功，第二次取消点赞
    public void Liketosth(int userID, int entityType, int entityId) {
        //设置 key
        String redisEntityKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //redis 命令 isMember 判断 userID 是否存在（在集合中？）
        Boolean member = redisTemplate.opsForSet().isMember(redisEntityKey, userID);

        //存在就是点过赞了，要取消点赞，否则点赞
        if (member) {
            redisTemplate.opsForSet().remove(redisEntityKey, userID);
        } else {
            redisTemplate.opsForSet().add(redisEntityKey, userID);
        }
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

}
