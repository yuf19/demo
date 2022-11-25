package com.example.demo.contrller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.util.CookieUtil;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.IDenoConstant;
import com.example.demo.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements IDenoConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    //http://localhost:8080/demo/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效的操作，该账号已经激活过了！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session
//        session.setAttribute("kaptcha", text);

        //验证码的归属
        String kaptchaOwner = DemoUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);//60秒
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);//60秒失效

        //将图片输出给浏览器
        response.setContentType("img/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/* ,HttpSession session, *//*, @CookieValue("kaptchaOwner") String kaptchaOwner*/, HttpServletRequest request, HttpServletResponse response) {
        //检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        String kaptchaOwner = CookieUtil.getValue(request, "kaptchaOwner");

        if (kaptchaOwner.equals("kaptchaOwner")) {
            model.addAttribute("codeMsg", "验证码已过期！");
            return "/site/login";
        }

        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        //检查账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";//重定向
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";//重定向，默认为get请求
    }

    @RequestMapping(path = "forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }

    @RequestMapping(path = "/forget/getEmailCode", method = RequestMethod.POST)
    @ResponseBody
    public String getEmailCode(String email/*, HttpSession session*/, HttpServletResponse response) {

        if (StringUtils.isBlank(email)) {
            return DemoUtil.getJSONString(1, "邮箱不能为空！");
        }

        //尝试发送邮件
        Map<String, Object> map = userService.getEmailCode(email);

        if (map.containsKey("code")) {
            String name = (String) map.get("code");
            Cookie cookie = new Cookie("code", name);
            cookie.setMaxAge(5 * 60);//5分钟失效
            cookie.setPath(contextPath);
            response.addCookie(cookie);

            return DemoUtil.getJSONString(0, "验证码发送成功！");
        }

        return DemoUtil.getJSONString(2, (String) map.get("emailMsg"));
    }

    @RequestMapping(path = "/forget/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String email, String code, String password, HttpSession session, Model model, HttpServletRequest request) {

        if (StringUtils.isBlank(email)) {
            model.addAttribute("emailMsg", "邮箱不能为空！");
            return "/site/forget";
        }

        if (StringUtils.isBlank(code)) {
            model.addAttribute("codeMsg", "验证码不能为空！");
            return "/site/forget";
        }

        //如果没有这个cookie，则值就是cookie的名字
        //如果没有这个cookie，在方法参数里使用@CookieValue就会抛出异常
        String name = CookieUtil.getValue(request, "code");

        if (name.equals("code")) {
            model.addAttribute("codeMsg", "验证码已过期！");
            return "/site/forget";
        }

        /*Object trueCode = session.getAttribute("code");
        if (trueCode == null) {
            model.addAttribute("codeMsg", "验证码已过期！");
            return "/site/forget";
        }*/

        String redisKey = RedisKeyUtil.getCodeKey(name);
        if (!code.equals(redisTemplate.opsForValue().get(redisKey))) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/forget";
        }

        if (StringUtils.isBlank(password)) {
            model.addAttribute("passwordMsg", "密码不能为空！");
            return "/site/forget";
        }

        userService.updatePassword(email, password);
        return "redirect:/login";
    }
}
