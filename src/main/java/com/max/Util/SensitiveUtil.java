package com.max.Util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * 敏感词过滤，依靠前缀树实现
 * 1、定义前缀树。 2、初始化前缀树。 3、编写算法实现过滤敏感词
 */
@Component
public class SensitiveUtil {

    public static final Logger logger = LoggerFactory.getLogger(SensitiveUtil.class);

    //替换字符
    private static final String REPLACEMENT = "***";

    //根节点
    private TreeNode root_node = new TreeNode();

    //初始化方法
    @PostConstruct
    public void init() {

        try (
                InputStream is =
                        this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))
        ) {
            String keyword;
            while ((keyword = bufferedReader.readLine()) != null) {
                //前缀树添加敏感词，addkeyword 在下面实现；
                this.addkeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("添加敏感词文件失败" + e.getMessage());
        }
    }

    //定义前缀树添加敏感词的方法 ： addkeyword
    private void addkeyword(String keyword) {
        TreeNode tempnode = root_node;  //根节点
        //判断敏感词 keyword 有多长，遍历加入到 map 中
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TreeNode node = tempnode.getNode(c);  //根据C获取了下一个节点,

            //初始化节点，往空节点输入字符串
            if (node == null) {
                //初始化
                node = new TreeNode();
                //往 tempnode （根节点） 后面加上一个节点
                tempnode.addNode(c, node);
            }
            //两个指针都指向一个节点
            tempnode = node;

            //遍历到最后一个字符，将其设置为结束符
            if (i == keyword.length() - 1) {
                tempnode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词核心算法
     * 传入带过滤的文本
     * 传出过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        //指针1.指向节点 不能new
        TreeNode tempnode = root_node;
        //指针2
        int fast = 0;
        //指针3
        int follow = 0;

        int leng = text.length();

        //重新构建
        StringBuilder sb = new StringBuilder();

        while (fast < leng) {

            if (follow < leng) {
                //"只有fast指针会完整的遍历整个字符串。所以不填follow" 错误的
                //如果填fast,在进行某个敏感词的判断中，c一直是指向fast,不会动，follow的移动没有意义
                //导致每次fast向右移动一位，follow无效判断完以后，sb每次添加一个fast指向的字符
                char c = text.charAt(follow);
                if (isSymbol(c)) {
                    //如果是特殊符号，略过
                    //但是根节点不同，需要往下移动????
                    if (tempnode == root_node) {
                        fast++;
                        sb.append(c);
                    }
                    //fast++;
                    follow++;
                    continue;
                }
                //c不是符号  指针1指向根节点的下一个节点
                tempnode = tempnode.getNode(c);
                //没有下一节点，意味着fast没有对应的前缀树节点，自然不是敏感词。但前缀树不是空的
                if (tempnode == null) {
                    //fast对应的字符是合法的，添加！
                    sb.append(text.charAt(fast));
                    //指针1回到root, 2下移一位 3要及时跟随
                    //fast++;
                    follow = ++fast;
                    tempnode = root_node;

                } else if (tempnode.isKeyWordEnd) {   //是否是敏感词终止结尾

                    //替换，不用管从哪到哪，直接加上一个替换字符即可
                    sb.append(REPLACEMENT);
                    //fast指针移动，指针1回到root,
                    fast = ++follow;
                    tempnode = root_node;
                } else {   //如果是敏感词，2不动，3往下移 但是要加上判断
                    follow++;
                }
            } else {
                //follow 到最后一个仍未发现终止标志
                sb.append(text.charAt(fast));
                follow = ++fast;
                tempnode = root_node;
            }

        }
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    //定义一个内部类
    public class TreeNode {

        //定义一个终止节点，代表是否是敏感词的结尾
        public boolean isKeyWordEnd = false;

        //前缀树的实现是由hashmap来做的
        // 子节点(key是下级字符,value是下级节点)
        private Map<Character, TreeNode> lastnodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        //添加下一个子节点
        public void addNode(Character character, TreeNode treeNode) {
            lastnodes.put(character, treeNode);
        }

        //获取下一个子节点
        public TreeNode getNode(Character character) {
            return lastnodes.get(character);
        }
    }

}
