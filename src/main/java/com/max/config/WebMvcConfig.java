package com.max.config;

import com.max.controller.Interceptor.LoginTicketInterceptor;
import com.max.controller.Interceptor.NoLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private NoLoginInterceptor noLoginInterceptor;

    //为项目所以资源加过滤器，除了静态页面资源。达到登陆和不登陆状态不同
    //这里应该是重写相关方法，自定义方法根本没有效果
//    public void addintreceptor(InterceptorRegistry registry){
//        System.out.println("111111111111111111");
//        registry.addInterceptor(loginTicketInterceptor)
//                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
//        System.out.println("222222");
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

        registry.addInterceptor(noLoginInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");

    }
}
