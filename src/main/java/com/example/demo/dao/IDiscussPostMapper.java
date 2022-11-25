package com.example.demo.dao;

import com.example.demo.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IDiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    //@Param可以给参数取别名，此处仅一个参数，sql要用到动态的条件，需要这个参数，故必须给
    //如果只有一个参数，并且在if里使用，就必须使用别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updataScore(int id, double score);

}
