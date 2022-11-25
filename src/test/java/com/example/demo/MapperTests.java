package com.example.demo;

import com.example.demo.dao.IDiscussPostMapper;
import com.example.demo.dao.ILoginTicketMapper;
import com.example.demo.dao.IMessageMapper;
import com.example.demo.dao.IUserMapper;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.LoginTicket;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class MapperTests {

    @Autowired
    IUserMapper iUserMapper;

    @Autowired
    IDiscussPostMapper iDiscussPostMapper;

    @Autowired
    ILoginTicketMapper iLoginTicketMapper;

    @Autowired
    IMessageMapper iMessageMapper;

    @Test
    public void testSelectUser() {
        User user = iUserMapper.selectById(101);
        System.out.println(user);

        user = iUserMapper.selectByName("liubei");
        System.out.println(user);

        user = iUserMapper.selectByEmail("nowcoder103@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("yuf");
        user.setPassword("Yf210110");
        user.setSalt("abccba");
        user.setEmail("yuf19@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");//ţ������0~1000��ͼƬ
        user.setCreateTime(new Date());

        int rows = iUserMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser() {
        int rows = iUserMapper.updateStatus(151, 1);
        System.out.println(rows);

        rows = iUserMapper.updateHeaderUrl(151, "http://www.nowcoder.com/999.png");
        System.out.println(rows);

        rows = iUserMapper.updatePassword(151, "Xz20160300");
        System.out.println(rows);
    }

    @Test
    public void testSelectDiscussPosts() {
        List<DiscussPost> list = iDiscussPostMapper.selectDiscussPosts(149, 0, 10,0);
        for (DiscussPost discussPost : list) {
            System.out.println(discussPost);
        }

        int rows = iDiscussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        iLoginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = iLoginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        iLoginTicketMapper.updateStatus("abc", 1);
        loginTicket = iLoginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectLetters() {
        List<Message> list = iMessageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count=iMessageMapper.selectConversationCount(111);
        System.out.println(count);

        list=iMessageMapper.selectLetters("111_112",0,10);
        for (Message message : list) {
            System.out.println(message);
        }

        count=iMessageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count=iMessageMapper.selectLetterUnreadCount(131,"111_131");
        System.out.println(count);
    }
}
