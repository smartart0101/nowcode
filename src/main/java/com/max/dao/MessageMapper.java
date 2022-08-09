package com.max.dao;

import com.max.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 有关消息、私信的所有方法
 */

@Mapper
public interface MessageMapper {

    //查询当前登陆用户的会话列表（和几个人交流），每个会话值显示最新的一条消息
    List<Message> SelectConversations(int userId, int offset, int limit);

    //查询当前登陆用户的会话列表（和几个人交流）的数量
    int SelectConversationCount(int userId);

    //查询当前登陆用户的某个会话列表(需要用特定的会话列表 id 来定位)，
    List<Message> SelectLetters(String conversationId, int offset, int limit);

    //查询当前登陆用户的某个会话列表内有多少消息
    int SelectLetterCount(String conversationId);

    //查询未读消息的数量
    int SelectLetterUnreadCount(int userId, String conversationId);

    //新增消息的数量
    int InsertMessage(Message message);

    //修改消息状态的数量
    int UpdateStatus(List<Integer> ids, int status);

}
