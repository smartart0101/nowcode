package com.max;

import com.max.Util.MailClint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 *
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class Mailtest {

    @Autowired
    private MailClint mailClint;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testMailClint(){
        mailClint.MailSender("18438591696@163.com","test","hello");
    }

    @Test
    public void testhtml(){
        //想要给模版传参，利用context
        Context context = new Context();
        //传入的内容
        context.setVariable("username","hello");
        //传入模版
        System.out.println("33333");
        String content = templateEngine.process("/mail/demo",context);
        System.out.println(content);
        mailClint.MailSender("18438591696@163.com","html",content);
    }
}
