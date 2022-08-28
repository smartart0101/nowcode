package com.max.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  统一处理日志，AOP 思想
 */
@Component
@Aspect
public class SeverLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(SeverLogAspect.class);

    // 在所有 service 层下所有类、方法 设置切点/连接点
    @Pointcut("execution(* com.max.service.*.*(..))")
    public void pointcut() {
    }

    //在切点开始的地方记日志 Before
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户[1.2.3.4],在[xxx],访问了[com.nowcoder.community.service.xxx()].
        //通过工具类得到 HttpServletRequest 对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        //得到类型名和方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        //拼凑完整日志信息
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }

}
