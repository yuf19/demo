package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WKTests {

    private static final Logger logger = LoggerFactory.getLogger(WKTests.class);

    public static void main(String[] args) {
        String cmd = "C:\\wkhtmltopdf\\bin\\wkhtmltoimage --quality 75 https://www.nowcoder.com C:\\wkhtmltopdf\\wk-images\\3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok!");
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
