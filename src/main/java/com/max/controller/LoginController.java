package com.max.controller;


import com.max.Util.CommunityConstant;
import com.max.entity.User;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.jws.WebParam;
import javax.websocket.server.PathParam;
import java.util.Map;


/**
 * 点击注册，调用此类方法，展示静态页面
 */
@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;


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
}

