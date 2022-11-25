package com.example.demo;

import com.example.demo.dao.IAlphaDao;
import com.example.demo.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
class DemoApplicationTests implements ApplicationContextAware {

    //spring����
    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void testContextApplication() {
        System.out.println(applicationContext);

        //ͨ�����ͻ�ȡbean
        IAlphaDao iAlphaDao = applicationContext.getBean(IAlphaDao.class);
        System.out.println(iAlphaDao.select());

        //ͨ�����ֻ�ȡbean
        iAlphaDao = applicationContext.getBean("alphaDaoHibernateImpl", IAlphaDao.class);
        System.out.println(iAlphaDao.select());
    }

    @Test
    public void testBeanManagement() {
        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);

        alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
    }

    @Test
    public void testBeanConfig() {
        SimpleDateFormat simpleDateFormat =
                applicationContext.getBean("simpleDateFormat",SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    //ͨ������ע������
    @Autowired
    @Qualifier("alphaDaoHibernateImpl")
    private IAlphaDao alphaDaoHibernateImpl;

    //ͨ������ע������
    @Autowired
    private IAlphaDao alphaDaoMyBatisImpl;

    @Autowired
    private AlphaService alphaService;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Test
    public void testDI() {
        System.out.println(alphaDaoHibernateImpl);
        System.out.println(alphaDaoMyBatisImpl);
        System.out.println(alphaService);
        System.out.println(simpleDateFormat);
    }
}
