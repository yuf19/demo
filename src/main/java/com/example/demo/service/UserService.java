package com.example.demo.service;

import com.example.demo.dao.ILoginTicketMapper;
import com.example.demo.dao.IUserMapper;
import com.example.demo.entity.LoginTicket;
import com.example.demo.entity.User;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.IDenoConstant;
import com.example.demo.util.MailClient;
import com.example.demo.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.Cookie;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements IDenoConstant {

    @Autowired
    private IUserMapper iUserMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${demo.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

//    @Autowired
//    ILoginTicketMapper iLoginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
//        return iUserMapper.selectById(id);

        User user = getCache(id);
        if (user == null) {
            user = initalCache(id);
        }

        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //验证账号
        User u = iUserMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = iUserMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(DemoUtil.generateUUID().substring(0, 5));
        user.setPassword(DemoUtil.MD5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(DemoUtil.generateUUID());
        //图片路径加上协议写完整，不然会在本地主机本项目下获取，就找不到图片了
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        iUserMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/demo/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = iUserMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            iUserMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        //空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = iUserMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        //验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        //验证密码
        password = DemoUtil.MD5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(DemoUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        iLoginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket) {
//        iLoginTicketMapper.updateStatus(ticket, 1);

        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public Map<String, Object> getEmailCode(String email) {
        Map<String, Object> map = new HashMap<>();

        User user = iUserMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱还没有注册！");
            return map;
        }

        //生成六位验证码
        String code = DemoUtil.generateUUID().substring(0, 6);
        String name = DemoUtil.generateUUID();
        map.put("code", name);

        //邮箱验证码存进session
//        session.setAttribute("code", code);
//
//        //设置session过期时间为5分钟
//        session.setMaxInactiveInterval(5 * 60);

        String redisKey = RedisKeyUtil.getCodeKey(name);
        redisTemplate.opsForValue().set(redisKey, code, 5 * 50, TimeUnit.SECONDS);//5分钟失效

        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("code", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "重置密码", content);
        return map;
    }

    public void updatePassword(String email, String password) {
        User user = iUserMapper.selectByEmail(email);
        password = DemoUtil.MD5((password + user.getSalt()));
        iUserMapper.updatePassword(user.getId(), password);
        clearCache(user.getId());
    }

    public LoginTicket findLoginTicket(String ticket) {
//        return iLoginTicketMapper.selectByTicket(ticket);

        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl) {
//        return iUserMapper.updateHeaderUrl(userId, headerUrl);

        int rows = iUserMapper.updateHeaderUrl(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public User findUserByUsername(String username) {
        return iUserMapper.selectByName(username);
    }

    //优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //取不到时初始化缓存数据
    private User initalCache(int userId) {
        User user = iUserMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    //数据变更时删除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }
}
