package com.example.demo.dao.elasticsearch;

import com.example.demo.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//Integer表示主键的类型
@Repository
public interface IDiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {

//    Object save(DiscussPost discussPost);
}
