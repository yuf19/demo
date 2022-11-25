package com.example.demo.event;

import com.alibaba.fastjson2.JSONObject;
import com.example.demo.entity.Event;
import com.example.demo.entity.Message;
import com.example.demo.service.MessageService;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.IDenoConstant;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements IDenoConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord consumerRecord) {
        if (consumerRecord == null || consumerRecord.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(consumerRecord.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        Map<String, Object> content = new HashMap<>();

        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord consumerRecord) {
        if (consumerRecord == null || consumerRecord.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(consumerRecord.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;

        try {
            Runtime.getRuntime().exec(cmd);//耗时
            logger.info("生成长图成功：" + cmd);//先执行
        } catch (IOException e) {
            logger.error("生成长图失败：" + e.getMessage());
        }

        //启用定时器监视该图片，一旦生成，就上传至七牛云
        UploadTask uploadTask = new UploadTask(fileName, suffix);
        Future future = threadPoolTaskScheduler.scheduleAtFixedRate(uploadTask, 500);
        uploadTask.setFuture(future);
    }

    private class UploadTask implements Runnable {

        //文件名称
        private String fileName;

        //文件后缀
        private String suffix;

        //启动任务的返回值
        private Future future;

        //开始时间
        private long startTime;

        //上传次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            //生成图片失败
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("执行时间过长，终止任务：" + fileName);
                future.cancel(true);
                return;
            }

            //上传失败
            if (uploadTimes >= 3) {
                logger.error("上传次数过多，终止任务：" + fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                logger.info(String.format("开始第%d次上传[%s]。", ++uploadTimes, fileName));
                //设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", DemoUtil.getJSONString(0));
                //生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                //指定上传的机房
                UploadManager uploadManager = new UploadManager(new Configuration(Zone.zone1()));
                try {
                    //开始上传图片
                    Response response = uploadManager.put(path, fileName, uploadToken, null, "image/" + suffix, false);
                    //处理响应结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if (jsonObject == null || jsonObject.get("code") == null || !jsonObject.get("code").toString().equals("0")) {
                        logger.info(String.format("第%d次上传失败[%s]", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("第%d次上传成功[%s]", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(String.format("第%d次上传失败[%s]", uploadTimes, fileName));
                }
            } else {
                logger.error("等待图片生成[" + fileName + "]");
            }
        }
    }
}
