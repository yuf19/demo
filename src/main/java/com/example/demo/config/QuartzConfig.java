package com.example.demo.config;

import com.example.demo.quartz.AlphaJob;
import com.example.demo.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//配置->数据库->调用
@Configuration
public class QuartzConfig {

    //FactoryBean可简化Bean的实例化过程
    //1，通过FactoryBean封装了Bean的实例化过程
    //2，可以将FactoryBean装配到spring容器里
    //3，将FactoryBean注入给其他的Bean
    //4，该Bean得到的是FactoryBean所管理的对象实例

    //配置JobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(AlphaJob.class);
        jobDetailFactoryBean.setName("alphaJob");
        jobDetailFactoryBean.setGroup("alphaJobGroup");
        //声明任务持久保存
        jobDetailFactoryBean.setDurability(true);
        //声明任务可恢复
        jobDetailFactoryBean.setRequestsRecovery(true);

        return jobDetailFactoryBean;
    }

    //配置Trigger（SimpleTriggerFactoryBean，CronTriggerFactoryBean）
    //此处应保证alphaJobDetail名字一致
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(alphaJobDetail);
        simpleTriggerFactoryBean.setName("alphaTrigger");
        simpleTriggerFactoryBean.setGroup("alphaTriggerGroup");
        //每三秒执行一次
        simpleTriggerFactoryBean.setRepeatInterval(3000);
        //指定对象存储Job的状态
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());

        return simpleTriggerFactoryBean;
    }

    //刷新帖子分数任务
    //配置JobDetail
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(PostScoreRefreshJob.class);
        jobDetailFactoryBean.setName("postScoreRefreshJob");
        jobDetailFactoryBean.setGroup("demoJobGroup");
        //声明任务持久保存
        jobDetailFactoryBean.setDurability(true);
        //声明任务可恢复
        jobDetailFactoryBean.setRequestsRecovery(true);

        return jobDetailFactoryBean;
    }

    //配置Trigger（SimpleTriggerFactoryBean，CronTriggerFactoryBean）
    //此处应保证alphaJobDetail名字一致
    @Bean
    public SimpleTriggerFactoryBean postScoreRefresh(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean simpleTriggerFactoryBean = new SimpleTriggerFactoryBean();
        simpleTriggerFactoryBean.setJobDetail(postScoreRefreshJobDetail);
        simpleTriggerFactoryBean.setName("postScoreRefreshTrigger");
        simpleTriggerFactoryBean.setGroup("demoTriggerGroup");
        //每5分钟执行一次
        simpleTriggerFactoryBean.setRepeatInterval(1000 * 60 * 5);
        //指定对象存储Job的状态
        simpleTriggerFactoryBean.setJobDataMap(new JobDataMap());

        return simpleTriggerFactoryBean;
    }
}
