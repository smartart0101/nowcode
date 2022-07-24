package com.max.controller;


import com.google.code.kaptcha.Producer;
import com.max.Util.CommunityConstant;
import com.max.Util.Communityutil;
import com.max.entity.User;
import com.max.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.server.PathParam;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


/**
 * 点击注册，调用此类方法，展示静态页面
 */
@Controller
public class LoginController implements CommunityConstant {

    public static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaproducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @RequestMapping(path = "/register", method = RequestMethod.GET)   //访问注册页面
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)   //访问login页面
    public String getloginPage() {
        return "/site/login";
    }


    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register_msg(Model model, User user) {
        Map<String, Object> register_msg_map = userService.RegisterMsg(user);
        if (register_msg_map == null || register_msg_map.isEmpty()) {    //如果没有错误消息，发送邮件提醒
            //
            model.addAttribute("msg", "注册成功，请查看邮箱并激活账号");
            model.addAttribute("traget", "/index");   //注册成功后会跳转到首页，激活账号
            return "/site/operate-result";   //最后会访问一个页面，显示激活成功
        } else {
            model.addAttribute("usernameMsg", register_msg_map.get("usernameMsg"));
            model.addAttribute("passswordMsg", register_msg_map.get("passswordMsg"));
            model.addAttribute("emailMsg", register_msg_map.get("emailMsg"));
            return "/site/register";   //如果不成功，返回注册页
        }
    }

    // http://localhost:8080/community/activation/101/code

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activationMsg(Model model, @PathParam("userId") int userId, @PathParam("code") String code) {
        int activation_num = userService.activation(userId, code);
        if (activation_num == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (activation_num == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    //该方法将创建的验证码图片发送
    @RequestMapping(path = "/kaptchaimage", method = RequestMethod.GET)
    public void getkapycha(HttpServletResponse response, HttpSession session) {
        //生产验证码、这里的作用是传入文本，kaptchaproducer会将其转为图片
        String text = kaptchaproducer.createText();
        BufferedImage image = kaptchaproducer.createImage(text);
        //验证码存入session、
        session.setAttribute("kaptcha", text);
        //图片发送给浏览器
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response) {
        //验证码填写是否正确,没有填写，code没有，两者不等，都不行
        //这里把session存的kaptcha,转为字符串，debug发现kaptcha为空值，
        // 结果就很显然了，将存入session的语句，改写为正确的名称即可
        String kaptcha = (String) session.getAttribute("kaptcha");

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码出错");
            return "/site/login";
        }


        //设置 ”记住我“ 的时间
        int expiredscends = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

        //判断账号信息，直接调用Userservice 的方法
        Map<String, Object> map = userService.login_now(username, password, expiredscends);
        if (map.containsKey("ticket")) {   //如果map里面包含了ticket,账号注册成功了 这里除了bug
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredscends);
            response.addCookie(cookie);    //这一步出异常
            return "redirect:/index";  //登录成功，跳转到首页
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        //改变cookie,调用service的方法
        userService.logout(ticket);
        return "redirect:/login";
    }


}

