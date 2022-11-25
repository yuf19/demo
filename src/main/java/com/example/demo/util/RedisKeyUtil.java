package com.example.demo.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    public static final String PREFIX_USER_LIKE = "like:user";

    //关注
    private static final String PREFIX_FOLLOWEE = "followee";

    //粉丝
    private static final String PREFIX_FOLLOWER = "follower";

    //验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";

    //登陆凭证
    private static final String PREFIX_TICKET = "ticket";

    //用户信息
    private static final String PREFIX_USER = "user";

    //邮箱验证码
    private static final String PREFIX_CODE = "code";

    private static final String PREFIX_UV = "uv";

    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";

    //某个实体的赞
    //like:entity:entityType:entityId->set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType->zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的粉丝
    //follower:entityType:entityId->zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登陆的验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登陆的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    public static String getCodeKey(String code) {
        return PREFIX_CODE + SPLIT + code;
    }

    //单日uv
    public static String getUVKey(String data) {
        return PREFIX_UV + SPLIT + data;
    }

    //区间uv
    public static String getUVkey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
