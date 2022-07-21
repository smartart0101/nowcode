package com.max.service;


import com.max.Util.CommunityConstant;
import com.max.Util.Communityutil;
import com.max.Util.MailClint;
import com.max.dao.UserMapper;
import com.max.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 1、解决 DiscussPostService 中提到的问题
 * 功能和 user 相关，就是根据 id 来查询 user
 * <p>
 * 2、由于注册功能也会涉及 user 表。所以相关方法也放在这里
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClint mailClint;

    @Autowired
    private TemplateEngine templateEngine;

    //注入域名
    @Value("${community.path.domain}")
    private String domain_name;

    //注入项目服务名
    @Value("${server.servlet.context-path}")
    private String contextpath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //构建一个方法，它返回注册时需要回馈给用户的各种消息，ps:”名字不能为空“...
    //这里按照教程做，但是是否应该把判断的方法抽离到一个类里？
    public Map<String, Object> RegisterMsg(User user) {
        Map<String, Object> register_msg_Map = new HashMap<>();

        //判断用户是否为空，可以直接抛出错误消息
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        //判断用户注册时输入的各种信息是否为空,如果为空，就把消息封装告诉客户端提示用户
        if (StringUtils.isBlank(user.getUsername())) {
            register_msg_Map.put("username_msg", "用户名不能为空");
            return register_msg_Map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            register_msg_Map.put("email_msg", "注册邮箱不能为空");
            return register_msg_Map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            register_msg_Map.put("password_msg", "账号密码不能为空");
            return register_msg_Map;
        }

        //如果用户输入正确，判断各种信息是否已经被注册过
        //一般的注册会在用户输入时实时提醒，更加人性化，后期看能不能改动
        //这里除了大问题，当邮箱或账号重复时，并没有报错，因为没有renturn!!!!!

        User new_user = userMapper.selectByName(user.getUsername());
        if (new_user != null) {
            register_msg_Map.put("usernameMsg", "该昵称已被注册过");
            //一定要return来结束该方法
            return register_msg_Map;
        }

        new_user = userMapper.selectByEmail(user.getEmail());
        if (new_user != null) {
            register_msg_Map.put("emailMsg", "该邮箱已被注册过");
            return register_msg_Map;
        }


        //各种信息验证后，可以注册新用户了
        user.setSalt(Communityutil.radomuuid().substring(0, 5));  //0-5位的salt,用于密码加密
        user.setPassword(Communityutil.MD5(user.getPassword()) + user.getSalt()); //密码+salt
        user.setType(0);
        user.setStatus(0);  //设置账号默认状态：是否是大V
        user.setActivationCode(Communityutil.radomuuid());  //随机激活码
        //随机头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());  //创建日期
        userMapper.insertUser(user);  //数据库中添加新用户

        //发送邮件
        Context context = new Context();
        context.setVariable("user_email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = contextpath + domain_name + "/activation" + user.getActivationCode();
        context.setVariable("url", url);   //到此context拼好了两个关键内容
        String content = templateEngine.process("/mail/activation", context);
        mailClint.MailSender(user.getEmail(), "激活邮件", content);


        return register_msg_Map;
    }

    //判断激活的用户是否成功，并返回激活的三种状态
    // http://localhost:8080/community/activation/101/code  code是激活码
    public int activation(int userId, String code) {
        User activationuser = userMapper.selectById(userId);
        if(activationuser.getStatus() == 1){    //Status默认为0，1代表已经激活过了
            return ACTIVATION_REPEAT;
        }else if(activationuser.getActivationCode().equals(code)){  //激活码正确
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else{
            return ACTIVATION_FAILURE;
        }
    }  //写完这些，可以去创建登陆逻辑

}
