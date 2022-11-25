package com.example.demo.contrller;

import com.example.demo.annotion.LoginRequired;
import com.example.demo.entity.Comment;
import com.example.demo.entity.DiscussPost;
import com.example.demo.entity.Page;
import com.example.demo.entity.User;
import com.example.demo.service.*;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.HostHolder;
import com.example.demo.util.IDenoConstant;
import com.example.demo.util.RedisKeyUtil;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = "/user")
public class UserController implements IDenoConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${demo.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${demo.path.uplode}")
    private String uploadPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        //上传文件名称
        String fileName = DemoUtil.generateUUID();
        //设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", DemoUtil.getJSONString(0));
        //生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    //更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return DemoUtil.getJSONString(1, "文件名不能为空！");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return DemoUtil.getJSONString(0);
    }

    //废弃
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImag, Model model) {
        if (headerImag == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImag.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }

        //生成随机的文件名
        fileName = DemoUtil.generateUUID() + suffix;
        //确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImag.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }
        //更新当前用户的头像的头像的路径（web访问路径）
        //http://localhost:8080/demo/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //废弃
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //相应图片
        response.setContentType("imag/" + suffix);
        try (FileInputStream fis = new FileInputStream(fileName);) {
            OutputStream os = response.getOutputStream();//spring mvc会自动关闭输出流，因为response由它管理
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = new HashMap<>();
        //检查原密码
        if (StringUtils.isBlank(oldPassword)) {
            model.addAttribute("oldPasswordMsg", "请输入原密码！");
            return "/site/setting";
        }
        if (!user.getPassword().equals(DemoUtil.MD5(oldPassword + user.getSalt()))) {
            model.addAttribute("oldPasswordMsg", "原密码不正确！");
            return "/site/setting";
        }

        //检查新密码
        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("oldPasswordMsg", "请输入新密码！");
            return "/site/setting";
        }

        userService.updatePassword(user.getEmail(), newPassword);
        return "redirect:/login";
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    @RequestMapping(path = "/myPost/{userId}", method = RequestMethod.GET)
    public String getMyPostPage(@PathVariable("userId") int userId, Model model, Page page) {

        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        int rows = discussPostService.findDiscussPostRows(userId);
        model.addAttribute("rows", rows);

        List<DiscussPost> posts = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);
        List<Map<String, Object>> postsVO = new ArrayList<>();
        if (posts != null) {

            for (DiscussPost post : posts) {
                Map<String, Object> map = new HashMap<>();

                map.put("post", post);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);
                postsVO.add(map);
            }
            model.addAttribute("posts", postsVO);
        }

        page.setRows(rows);
        page.setPath("/user/myPost/" + userId);
        model.addAttribute("page", page);

        return "/site/my-post";
    }

    @RequestMapping(path = "/myReply/{userId}", method = RequestMethod.GET)
    public String getMyReplyPage(@PathVariable("userId") int userId, Model model, Page page) {

        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user", user);

        int rows = commentService.findCommentCountByUserId(ENTITY_TYPE_POST,userId);
        model.addAttribute("rows", rows);

        List<Comment> comments = commentService.findCommentByUserId(ENTITY_TYPE_POST,userId,page.getOffset(),page.getLimit());
        List<Map<String, Object>> commentsVO = new ArrayList<>();
        if (comments != null) {

            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();

                map.put("comment", comment);
                DiscussPost post=discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("post",post);
                commentsVO.add(map);
            }
            model.addAttribute("comments", commentsVO);
        }

        page.setRows(rows);
        page.setPath("/user/myReply/" + userId);
        model.addAttribute("page", page);

        return "/site/my-reply";
    }

}
