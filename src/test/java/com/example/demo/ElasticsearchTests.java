package com.example.demo;

import com.example.demo.dao.IDiscussPostMapper;
import com.example.demo.dao.elasticsearch.IDiscussPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class ElasticsearchTests {

    @Autowired
    private IDiscussPostMapper iDiscussPostMapper;

    private IDiscussPostRepository iDiscussPostRepository;

//    @Autowired
//    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert() {
        iDiscussPostRepository.save(iDiscussPostMapper.selectDiscussPostById(234));
        iDiscussPostRepository.save(iDiscussPostMapper.selectDiscussPostById(239));
        iDiscussPostRepository.save(iDiscussPostMapper.selectDiscussPostById(242));
    }
}
