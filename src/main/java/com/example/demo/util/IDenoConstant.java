package com.example.demo.util;

public interface IDenoConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS=0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT=1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE=2;

    /**
     * 默认状态的登陆凭证的超时时间(12小时)
     */
    int DEFAULT_EXPIRED_SECONDS=3600*12;

    /**
     * 记住状态的登陆凭证的超时时间(100小时)
     */
    int REMEMBER_EXPIRED_SECONDS=3600*100;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST=1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT=2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER=3;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT="comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE="like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW="follow";

    /**
     * 主题：分享
     */
    String TOPIC_SHARE="share";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID=1;

    /**
     * 权限：普通用户
     */
    String AUTHORITY_USER="user";

    /**
     * 权限：管理员
     */
    String AUTHORITY_ADMIN="admin";

    /**
     * 权限：版主
     */
    String AUTHORITY_MODERATOR="moderator";

    /**
     * 帖子类型：普通
     */
    int POST_TYPE_ORDINARY=0;

    /**
     * 帖子类型：置顶
     */
    int POST_TYPE_TOP=1;

    /**
     * 帖子状态：正常
     */
    int POST_STATUS_NORMAL=0;

    /**
     * 帖子状态：加精
     */
    int POST_STATUS_WONDERFUL=1;

    /**
     * 帖子状态：拉黑（删除）
     */
    int POST_STATUS_DELETE=2;
}
