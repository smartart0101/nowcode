package com.max.service;

import com.max.Util.SensitiveUtil;
import com.max.dao.MessageMapper;
import com.max.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * 实现 dao 方法
 */
@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveUtil sensitiveUtil;

    public List<Message> FindConversations(int userId, int offset, int limit) {
        return messageMapper.SelectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return messageMapper.SelectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.SelectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return messageMapper.SelectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.SelectLetterUnreadCount(userId, conversationId);
    }

    //新增消息,及其数量, 对内容进行敏感词过滤
    public int AddMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveUtil.filter(message.getContent()));
        return messageMapper.InsertMessage(message);
    }

    //改变会话中已读消息的状态
    public int ReadMessage(List<Integer> ids){
        return messageMapper.UpdateStatus(ids,1);
    }


}
