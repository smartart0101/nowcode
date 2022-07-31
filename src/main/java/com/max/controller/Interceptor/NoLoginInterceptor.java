package com.max.controller.Interceptor;

import com.max.Util.HostHolder;
import com.max.annotation.NoLogin;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 这是一个拦截器实现方法，为了不让没有登录的，访问到不该访问的资源，
 */
@Component
public class NoLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;


    //调用方法前拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*
         * 逻辑是这样的： 1、判断对象是否是方法. 方法是否存在特定注解
         * 2、判断 hostHolder 能不能 取出来 user ，
         */
        if (handler instanceof HandlerMethod) {
            //Object --HandlerMethod, 为了其中特定的方法
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            //得到方法后，判断该方法是否有 NoLogin 的注解
            NoLogin annotation = method.getAnnotation(NoLogin.class);
            if (hostHolder.getUser() == null && annotation != null) {   //用户没登 ，方法没注解
                //没有登录的话，跳转至登陆页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
