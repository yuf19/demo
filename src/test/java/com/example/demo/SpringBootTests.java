package com.example.demo;

import org.junit.jupiter.api.*;
import com.example.demo.entity.DiscussPost;
import com.example.demo.service.DiscussPostService;
import org.junit.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;

    @BeforeAll
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    @AfterAll
    public static void afterClass() {
        System.out.println("afterClass");
    }

    @BeforeEach
    public void before() {
        System.out.println("before");

        //初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Test Title");
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    @AfterEach
    public void after() {
        System.out.println("after");

        //删除测试数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());

        Assert.assertNotNull(post);
        Assert.assertEquals(data.getTitle(), post.getTitle());
        Assert.assertEquals(data.getContent(),post.getContent());
    }

    @Test
    public void testUpdateScore(){
        int rows=discussPostService.updateScore(data.getId(), 2000);
        Assert.assertEquals(1,rows);

        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assert.assertEquals(2000.00,post.getScore(),2);
    }
}
