package com.example.demo.dao;

import com.example.demo.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ICommentMapper {

    @Select({
            "select id,user_id,entity_type,entity_id,target_id,content,status,create_time ",
            "from comment ",
            "where status=0 and entity_type=#{entityType} and entity_id=#{entityId} ",
            "order by create_time asc ",
            "limit #{offset},#{limit}",
    })
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    @Select({
            "select count(id) ",
            "from comment ",
            "where status=0 and entity_type=#{entityType} and entity_id=#{entityId}"
    })
    int selectCountByEntity(int entityType, int entityId);

    @Insert({
            "insert into comment(user_id,entity_type,entity_id,target_id,content,status,create_time) ",
            "values(#{userId},#{entityType},#{entityId},#{targetId},#{content},#{status},#{createTime})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertComment(Comment comment);


    @Select({
            "select id,user_id,entity_type,entity_id,target_id,content,status,create_time ",
            "from comment ",
            "where id=#{id}"
    })
    Comment selectCommentById(int id);

    @Select({
            "select id,user_id,entity_type,entity_id,target_id,content,status,create_time ",
            "from comment ",
            "where status=0 and entity_type=#{entityType} and user_id=#{userId} ",
            "order by create_time asc ",
            "limit #{offset},#{limit}",
    })
    List<Comment> selectCommentByUserId(int entityType,int userId,int offset,int limit);

    @Select({
            "select count(id)",
            "from comment ",
            "where status=0 and entity_type=#{entityType} and user_id=#{userId}"
    })
    int selectCommentCountByUserId(int entityType,int userId);
}
