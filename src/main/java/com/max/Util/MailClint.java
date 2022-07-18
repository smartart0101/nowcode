package com.max.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 这里是发送邮件功能的封装类
 */
@Component
public class MailClint {

    //定义一个日志，方便打印日志排除bug
    //public static final Logger logger = new LoggerFactory.getLogger(MailClint.class)
    public static final Logger logger = LoggerFactory.getLogger(MailClint.class);

    //发送邮件的核心接口
    @Autowired
    private JavaMailSender mailSender;

    //指定发送人
    @Value("${spring.mail.username}")
    public String from;

    //写发送方法，参数有：接收人，主题，内容
    public void MailSender(String to, String subject, String content) {
        try {
            //建立发送壳子
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            //装入发送,
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            //设置参数
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            //发送
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("邮件发送失败" + e.getMessage());
        }
    }


}
