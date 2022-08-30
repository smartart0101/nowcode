package com.max.service;


import com.max.Util.CommunityConstant;
import com.max.Util.Communityutil;
import com.max.Util.MailClint;
import com.max.dao.LoginTicketMapper;
import com.max.dao.UserMapper;
import com.max.entity.LoginTicket;
import com.max.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    //注入域名
    @Value("${community.path.domain}")
    private String domain_name;

    //注入项目服务名
    @Value("${server.servlet.context-path}")
    private String contextpath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //构建一个方法，注册时需要回馈给用户的各种消息，ps:”名字不能为空“...
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
        user.setPassword(Communityutil.MD5(user.getPassword() + user.getSalt())); //密码+salt
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
        //String url = contextpath + domain_name + "/activation" + user.getActivationCode();
        //这里除了bug, 新用户不能注册，url不对

        String url = domain_name + contextpath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);   //到此context拼好了两个关键内容
        String content = templateEngine.process("/mail/activation", context);
        mailClint.MailSender(user.getEmail(), "激活邮件", content);

        return register_msg_Map;
    }

    //判断激活的用户是否成功，并返回激活的三种状态
    // http://localhost:8080/community/activation/101/code  code是激活码
    public int activation(int userId, String code) {
        User activationuser = userMapper.selectById(userId);
        if (activationuser.getStatus() == 1) {    //Status默认为0，1代表已经激活过了
            return ACTIVATION_REPEAT;
        } else if (activationuser.getActivationCode().equals(code)) {  //激活码正确
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }  //写完这些，可以去创建登陆逻辑


    //用户点击立即登陆按钮，这里的方法就会做出判断
    //我的问题，判断信息是否正确，难道是数据库一点一点查的啊。一定关系到dao，
    public Map<String, Object> login_now(String username, String password, long expiredscends) {
        Map<String, Object> login_msg_Map = new HashMap<>();

        //验证输入的用户名和密码是否为空
        if (StringUtils.isBlank(username)) {
            login_msg_Map.put("usernameMsg", "用户名不能为空");
            return login_msg_Map;
        }
        if (StringUtils.isBlank(password)) {
            login_msg_Map.put("passwordMsg", "密码不能为空");
            return login_msg_Map;
        }

        //验证输入的账号，是否为注册过的账号; 首先查到该用户
        User login_user = userMapper.selectByName(username);

        if (login_user == null) {
            login_msg_Map.put("usernameMsg", "该账号不存在");
            return login_msg_Map;
        }

        if (login_user.getStatus() == 0) {
            login_msg_Map.put("statusMsg", "该账号未激活");
            return login_msg_Map;
        }

        //密码是否正确
        //password = Communityutil.MD5(password + login_user.getSalt());
        password = Communityutil.MD5(password + login_user.getSalt());
        if (!login_user.getPassword().equals(password)) {
            login_msg_Map.put("passwordMsg", "密码不正确");
            return login_msg_Map;
        }

        //生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(login_user.getId());
        loginTicket.setTicket(Communityutil.radomuuid());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredscends * 1000));
        loginTicketMapper.InsertLoginTicket(loginTicket);

        login_msg_Map.put("ticket",loginTicket.getTicket());
        return login_msg_Map;
    }

    //退出功能
    public void logout(String ticket){
        loginTicketMapper.UpdateLoginStatus(ticket,1);
    }

    //select ticket from cokkie
    public LoginTicket find_login_ticket(String ticket){
        return loginTicketMapper.SelectLoginTicket(ticket);
    }


    //上传头像的方法
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    //根据名字查用户
    public User FindUserByName(String username){
        return userMapper.selectByName(username);
    }


}
