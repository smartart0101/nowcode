package com.max.controller;

import com.max.Util.Communityutil;
import com.max.Util.HostHolder;
import com.max.annotation.NoLogin;
import com.max.entity.User;
import com.max.service.RedisLikeService;
import com.max.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Value("${community.path.upload}")
    private String uploadpath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextpath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisLikeService redisLikeService;

    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @NoLogin
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getsettingpage() {
        return "site/setting";
    }

    //用户点击到相关页面后,完成相关操作,然后跳转页面
    @NoLogin
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String change_header(MultipartFile headimage, Model model) {

        //空值判断
        if (headimage == null) {
            model.addAttribute("error", "头像不能为空");
            return "site/setting";
        }
        //如果不为空，判断一下文件的格式。判断后缀
        String filename = headimage.getOriginalFilename();
        String substring = filename.substring(filename.lastIndexOf("."));  //得到后缀
        if (StringUtils.isBlank(substring)) {
            model.addAttribute("error", "文件格式不正确");
            return "site/setting";
        }

        //生产随机文件名，确定文件储存路径，储存
        filename = Communityutil.radomuuid() + substring;
        File filepath = new File(uploadpath + "/" + filename);

        try {
            //储存到headimage中
            headimage.transferTo(filepath);
        } catch (IOException e) {
            logger.error("上传文件失败", e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        //更新当前用户的头像路径  // http://localhost:8080/community/user/header/xxx.png
        //  取出user
        User user = hostHolder.getUser();
        String user_headerimage = domain + contextpath + "/user/header/" + filename;

        userService.updateHeader(user.getId(), user_headerimage);
        return "redirect:/index";
    }

    //读取首页更新后的头像功能：1、获取头像的储存路径 2、通过输入输出流上传给浏览器 3、该方法输出的是二进制字节流
    //浏览器访问首页，在特定的区域，会查询到对应地址得资源
    //该方法的访问路径，和@RequestMapping("/user") 拼成了 http://localhost:8080/community/user/header/xxx.png
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void get_header_image(@PathVariable("filename") String filename, HttpServletResponse response) {
        //本地服务器存放头像路径
        filename = uploadpath + "/" + filename;
        //声明输出的文件格式：后缀
        String substring = filename.substring(filename.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + substring);  //括号内格式是固定的

        //输出
        try (
                FileInputStream fileInputStream = new FileInputStream(filename);    //输入流
                ServletOutputStream outputStream = response.getOutputStream();  //输出流
        ) {
            //不能一个一个字节的输送，效率低 ，攒够一波一起输出
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1) {    //当b有接受到数据时
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取头像失败" + e.getMessage());
        }
    }


    //用户主页，点击后根据Id 查询该用户收到了多少赞
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        //用户填入model
        model.addAttribute("user", user);
        //点赞数量
        int userLikeCount = redisLikeService.findUserLikeCount(userId);
        model.addAttribute("userLikeCount", userLikeCount);

        return "/site/profile";
    }
}
