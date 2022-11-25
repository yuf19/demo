package com.example.demo;

import com.example.demo.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class SensitiveTests {

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Test
    public void TestSensitiveFilter(){
        System.out.println(("0息1息2信息3".substring(8))=="");
        System.out.println(System.getProperty("file.encoding"));

        String text="你可以赌博，你还可以嫖娼，你更加可以吸毒！";
        text=sensitiveFilter.filter(text);
        System.out.println(text);

        text="你可以Ā赌Ā博Ā，你还可以Ā嫖Ā娼Ā，你更加可以Ā吸Ā毒Ā！";
        text=sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
