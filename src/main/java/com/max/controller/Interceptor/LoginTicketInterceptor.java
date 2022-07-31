package com.max.controller.Interceptor;

import com.max.Util.CookieUtil;
import com.max.Util.HostHolder;
import com.max.entity.LoginTicket;
import com.max.entity.User;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 这是一个过滤器实现方法，会拦截一些请求，达到未登录和登陆后的效果不一样
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    /*  实现的接口有三个方法，1、分别作用于controller调用前执行
     * 2、controller调用后执行、3、在TemplateEngine之后执行  */

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //1、作用于controller调用前执行.该方法需要将cookie中的凭证“ticket”取出来,而后根据凭证查询到用户
    //涉及到的具体动作，应该交给专门的类实现
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //得到cookie中的凭证“ticket”
        String ticket = CookieUtil.getValue(request, "ticket");

        //查询，校验
        if (ticket != null) {

            LoginTicket login_ticket = userService.find_login_ticket(ticket);
            //验证ticket是否有效，null? status? 超时时间晚于当前时间
            if (login_ticket != null && login_ticket.getStatus() == 0 && login_ticket.getExpired().after(new Date())) {
                //根据凭证查询用户
                User userById = userService.findUserById(login_ticket.getUserId());
                //将用户添加到本次请求中  多线程，ThreadLocal
                hostHolder.setUser(userById);
            }
        }

        return true;
    }

    //2、controller调用后执行.该方法需要将查到的 user 交给model,进一步显示到浏览器上
    //这里model == null. 不知原因
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //System.out.println("user null !!!!!");
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("LoginUser", user);
        }
    }



    //3、在TemplateEngine之后执行,
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
