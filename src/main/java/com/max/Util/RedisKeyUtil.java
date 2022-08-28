package com.max.Util;

/**
 * 设置 redis key
 */
public class RedisKeyUtil {

    //设置全局变量 分隔符  /  前缀key
    public static final String SPLIT = ":";
    public static final String PREFIX_ENTITY_LIKE = "like:entity";

    //某个实体的赞 的 key
    //赞的 key 不应该是 int 类型，因为要能够 根据赞 找到点赞的人
    //key 应该是这种格式： like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + entityId;
    }

}
