package com.example.demo.actuator;

import com.example.demo.util.DemoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Endpoint(id = "database")
public class DataBaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DataBaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    //@ReadOperation（get请求）
    //@WriteOperation（post，put请求）
    @ReadOperation
    public String checkConnection() {
        try (Connection connection = dataSource.getConnection();) {
            return DemoUtil.getJSONString(0, "获取连接成功！");
        } catch (SQLException e) {
            logger.error("获取连接失败：" + e);
            return DemoUtil.getJSONString(1, "获取连接失败！");
        }
    }

}
