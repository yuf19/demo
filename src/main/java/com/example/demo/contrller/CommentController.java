package com.example.demo.contrller;

import com.example.demo.annotion.LoginRequired;
import com.example.demo.entity.Comment;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.Event;
import com.example.demo.event.EventProducer;
import com.example.demo.service.CommentService;
import com.example.demo.service.DiscussPostService;
import com.example.demo.util.HostHolder;
import com.example.demo.util.IDenoConstant;
import com.example.demo.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements IDenoConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @LoginRequired
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, int current, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", String.valueOf(discussPostId));

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
            //当不是自己给自己评论时才发通知
            if (target.getUserId() != hostHolder.getUser().getId()) {
                event.setEntityUserId(target.getUserId());
                eventProducer.fireEvent(event);

                //计算帖子分数
                String redisKey = RedisKeyUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(redisKey, discussPostId);
            }


        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.findCommentById(comment.getEntityId());
            //自己给自己评论不要通知
            if (target.getUserId() != hostHolder.getUser().getId()) {
                event.setEntityUserId(target.getUserId());
                eventProducer.fireEvent(event);
            }
        }


        return "redirect:/discuss/detail/" + discussPostId + "?current=" + current;
    }
}
