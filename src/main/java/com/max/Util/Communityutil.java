package com.max.Util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 *
 */
public class Communityutil {

    //生产随机字符串
    public static String radomuuid() {
        //使用spring自带api生产                replace方法作用：将-替换成空格（为了美观）  //这里出了错误，tomcat高版本cookie不能有空格
        return UUID.randomUUID().toString().replace("-", "");
    }

    //MD5加密
    public static String MD5(String key) {
        //该方法判断是否为空？
        if(StringUtils.isBlank(key)){
            return null;
        }
        //不为空，就将key+salt组成密码
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
