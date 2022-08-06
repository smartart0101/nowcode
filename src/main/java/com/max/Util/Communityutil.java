package com.max.Util;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
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
        if (StringUtils.isBlank(key)) {
            return null;
        }
        //不为空，就将key+salt组成密码
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //Map<String ,Object> map = new HashMap<>();
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if (map != null) {  //遍历map  填入json..
            for(String key : map.keySet()){
                jsonObject.put(key,map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    //下面的两个方法是上面方法的重载，
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg,null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code,null,null);
    }


}
