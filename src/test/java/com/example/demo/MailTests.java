package com.example.demo;


import com.example.demo.util.MailClient;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class MailTests {

    @Autowired
    MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("yuf19@lzu.edu.cn", "TEST", "hello");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "yuf");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("yuf19@lzu.edu.cn", "html", content);
    }
}
