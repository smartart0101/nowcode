package com.max.Util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 在这里实现得到 cookie 中 ticket 的方法,
 */
public class CookieUtil {

    public static String getValue(HttpServletRequest request, String name) {
        //空值处理
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空");
        }
        //Cookie[] cookies = new Cookie();   不能new,要从 request 取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
