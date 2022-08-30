package com.max.Util;

/**
 * 设置 redis key
 */
public class RedisKeyUtil {

    //设置全局变量 分隔符  /  前缀key
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 增加功能，某个用户自己收到的赞
    private static final String PREFIX_USER_LIKE = "like:user";

    //某个实体(可能是帖子、评论、回复)收到的赞 的 key
    //赞的 key 不应该是 int 类型，因为要能够 根据赞 找到点赞的人
    //key 应该是这种格式： like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + entityId;
    }

    //某个用户(可能是帖子、评论、回复)收到的赞 的 key  like:user:userId
    //这里直接采用int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

}
