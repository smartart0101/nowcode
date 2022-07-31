package com.max.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)   //只有方法上该注解 NoLogin 才可以生效
@Retention(RetentionPolicy.RUNTIME)  //只有程序运行时该注解 NoLogin 才可以生效
public @interface NoLogin {
}
