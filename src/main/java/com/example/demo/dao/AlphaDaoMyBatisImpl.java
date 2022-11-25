package com.example.demo.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaDaoMyBatisImpl implements IAlphaDao {
    @Override
    public String select() {
        return "MyBatis";
    }
}
