package com.example.demo.dao;

import com.example.demo.entity.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface IMessageMapper {

    //查询当前用户的会话列表，针对每个会话，只返回一条最新的私信
    @Select({
            "select id,from_id,to_id,conversation_id,content,status,create_time ",
            "from message ",
            "where id in ",
            "(select max(id) from message where status!=2 and from_id!=1 and (from_id=#{userId} or to_id=#{userId}) group by conversation_id)",
            "order by id desc ",
            "limit #{offset},#{limit}"
    })
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    @Select({
            "select count(m.max_id) ",
            "from (select max(id) as max_id from message where status!=2 and from_id!=1 and (from_id=#{userId} or to_id=#{userId}) group by conversation_id) as m"
    })
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    @Select({
            "select id,from_id,to_id,conversation_id,content,status,create_time ",
            "from message ",
            "where status!=2 and from_id!=1 and conversation_id=#{conversationId} ",
            "order by id desc ",
            "limit #{offset},#{limit}"
    })
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    @Select({
            "select count(id) ",
            "from message ",
            "where status!=2 and from_id!=1 and conversation_id=#{conversationId}"
    })
    int selectLetterCount(String conversationId);

    //查询未读私信数量
    @Select({
            "<script>",
            "select count(id) ",
            "from message ",
            "where status=0 and from_id!=1 and to_id=#{userId} ",
            "<if test=\"conversationId!=null\">",
            "and conversation_id=#{conversationId}",
            "</if>",
            "</script>"
    })
    int selectLetterUnreadCount(int userId, String conversationId);

    //新增一个消息
    @Insert({
            "insert into message(from_id,to_id,conversation_id,content,status,create_time) ",
            "values(#{fromId},#{toId},#{conversationId},#{content},#{status},#{createTime})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(Message message);

    //修改消息状态
    @Update({
            "<script>",
            "update message set status=#{status} ",
            "where id in ",
            "<foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int updateStatus(List<Integer> ids, int status);

    //查询某个主题下最新的通知
    @Select({
            "select id,from_id,to_id,conversation_id,content,status,create_time ",
            "from message ",
            "where id in ( ",
            "select max(id) from message where status!=2 and from_id=1 and to_id=#{userId} and conversation_id=#{topic} ",
            ")"
    })
    Message selectLatestNotice(int userId, String topic);

    //查询某个主题所包含的通知的数量
    @Select({
            "select count(id) ",
            "from message ",
            "where status!=2 and from_id=1 and to_id=#{userId} and conversation_id=#{topic} "
    })
    int selectNoticeCount(int userId, String topic);

    //查询未读的通知的数量
    @Select({
            "<script>",
            "select count(id) ",
            "from message ",
            "where status=0 and from_id=1 and to_id=#{userId} ",
            "<if test=\"topic!=null\">",
            "and conversation_id=#{topic} ",
            "</if>",
            "</script>"
    })
    int selectNoticeUnreadCount(int userId, String topic);

    //查询某个主题所包含的通知列表
    @Select({
            "select id,from_id,to_id,conversation_id,content,status,create_time ",
            "from message ",
            "where status!=2 and from_id=1 and to_id=#{userId} and conversation_id=#{topic} ",
            "order by create_time desc ",
            "limit #{offset},#{limit}"
    })
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
