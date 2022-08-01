package com.max;


import com.max.Util.SensitiveUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;



@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveUtil sensitiveUtil;

    @Test
    public void testSensitiveFilter() {
        String text = "j杀人赌博,可以嫖娼,可以吸毒,可以开票,哈哈哈!";
        text = sensitiveUtil.filter(text);
        System.out.println(text);

        text = "☆杀@人@赌☆博☆,可以☆嫖☆娼☆,可以☆吸☆毒☆,可以☆开☆票☆,哈哈哈!";
        text = sensitiveUtil.filter(text);
        System.out.println(text);
    }

}
