package com.example.demo.service;

import com.example.demo.dao.IAlphaDao;
import com.example.demo.dao.IDiscussPostMapper;
import com.example.demo.dao.IUserMapper;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.User;
import com.example.demo.util.DemoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//设置多实例，默认singleton
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private IAlphaDao iAlphaDao;

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    public AlphaService() {
        System.out.println(System.getProperty("file.encoding"));
        System.out.println("construct方法");
        logger.info("construct方法");
    }

    @PostConstruct
    public void init() {
        System.out.println("init");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("destroy方法");
    }

    @Autowired
    private IUserMapper iUserMapper;

    @Autowired
    private IDiscussPostMapper iDiscussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public String find() {
        return iAlphaDao.select();
    }

    //声明式事务
    //isolation：不加参数就是默认的隔离级别
    //propagation：事务的传播机制
    //当前事务（外部事务），a调用b，a的事务就是b的外部事务
    //REQUIRED：支持当前事务（外部事务），如果不存在就创建新事务
    //REQUIRES_NEW：创建一个新事务，暂停当前事务（外部事务）
    //NESTED：如果存在当前事务（外部事务），则嵌套在外部事务中执行（有独立的提交和回滚），否则就和REAUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        //新增用户
        User user = new User();
        user.setUsername("老王");
        user.setSalt(DemoUtil.generateUUID().substring(0, 5));
        user.setPassword(DemoUtil.MD5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("https://images.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        iUserMapper.insertUser(user);

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("新人报道！");
        post.setCreateTime(new Date());
        iDiscussPostMapper.insertDiscussPost(post);

//        Integer.valueOf("abc");?

        return "ok";
    }

    //编程式事务
    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //新增用户
                User user = new User();
                user.setUsername("老王");
                user.setSalt(DemoUtil.generateUUID().substring(0, 5));
                user.setPassword(DemoUtil.MD5("123" + user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setHeaderUrl("https://images.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                iUserMapper.insertUser(user);

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("hello");
                post.setContent("新人报道！");
                post.setCreateTime(new Date());
                iDiscussPostMapper.insertDiscussPost(post);

//                Integer.valueOf("abc");

                return "ok";
            }
        });
    }

    //让该方法可以在多线程的情况下能够被异步调用
    @Async
    public void execute1() {
        logger.debug("execute1");
    }

    //等10s后每隔1s执行一次
//    @Scheduled(initialDelay = 10000, fixedRate = 1000)
    public void execute2() {
        logger.debug("execute2");
    }
}
