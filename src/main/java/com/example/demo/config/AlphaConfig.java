package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AlphaConfig {

    @Bean
    //该注解必须要在标有 @Configuration的配置类中使用才会有效。
    //告知Spring此方法将会返回一个对象，将返回的对象注入到容器中。
    //方法名就是bean的名字
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
