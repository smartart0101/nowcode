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
    //增加功能，实现关注，取消关注的功能
    private static final String PREFIX_FOLLOWEE = "followee";   //被关注的目标
    private static final String PREFIX_FOLLOWER = "follower";   //关注的粉丝

    private static final String PREFIX_KAPTCHA = "kaptcha";     //验证码
    private static final String PREFIX_TICKET = "ticket";       //登录凭证
    private static final String PREFIX_USER = "user";           //缓存用户信息


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

    //被某个用户关注的实体的 key
    // followee:userId:entityType -> 有序数组  zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登录的验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

}
