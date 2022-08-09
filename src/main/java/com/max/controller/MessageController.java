package com.max.controller;

import com.max.Util.Communityutil;
import com.max.Util.HostHolder;
import com.max.Util.SensitiveUtil;
import com.max.entity.Message;
import com.max.entity.Page;
import com.max.entity.User;
import com.max.service.MessageService;
import com.max.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 表现层方法，
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private SensitiveUtil sensitiveUtil;

    @Autowired
    private UserService userService;

    //用户点开消息界面，看到会话列表，并且是分页显示的
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String GetLitterList(Model model, Page page) {
        //得到已登录的用户
        User user = hostHolder.getUser();

        //分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        //会话列表
        //从数据库中查到的 LIST ，取出其中的数据，装配到特定格式要求的链表中
        List<Message> ConversationList =
                messageService.FindConversations(user.getId(), page.getOffset(), page.getLimit());
        ArrayList<Map<String, Object>> Conversations = new ArrayList<>();
        if (ConversationList != null) {
            for (Message message : ConversationList) {

                //循环得到三条主要信息，每一个会话（message代表）， 会话数，没有阅读的消息数
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));

                //判断用户得到的，到底是 from 还是 to
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                Conversations.add(map);
            }
        }
        model.addAttribute("Conversations", Conversations);

        //查询未读消息数量
        int UnreadLetterCount = messageService.findLetterUnreadCount(
                user.getId(), null);
        model.addAttribute("UnreadLetterCount", UnreadLetterCount);
        return "/site/letter";
    }

    //查询某个会话包含的私信，支持分页效果 根据某个会话的 ID 来查询
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getlitterdetail(@PathVariable("conversationId") String conversationId, Model model, Page page) {

        //设置分页效果
        page.setLimit(5);
        page.setPath("/letter/detail" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        //消息列表
        //从数据库中查到的 LIST ，取出其中的数据，装配到特定格式要求的链表中
        List<Message> LetterList = messageService.findLetters(
                conversationId, page.getOffset(), page.getLimit());
        ArrayList<Map<String, Object>> Letters = new ArrayList<>();
        if (LetterList != null) {
            for (Message message : LetterList) {

                //循环得到信息，设置两个主要的参数， 1、内容  2、谁发给用户的
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));

                Letters.add(map);
            }
        }
        model.addAttribute("Letters", Letters);

        //私信目标-给谁发的。。另外封装一个方法 getlettertarget
        model.addAttribute("target", getlettertarget(conversationId));

        //得到收到的消息队列，选出未读的，设置为已读
        List<Integer> ids = getletters(LetterList);
        if (!ids.isEmpty()) {
            messageService.ReadMessage(ids);
        }


        return "/site/letter-detail";
    }

    //从会话列表中得到未读的消息，应当是一个列表
    private List<Integer> getletters(List<Message> LetterList) {
        List<Integer> ids = new ArrayList<>();

        if (LetterList != null) {
            for (Message message : LetterList) {
                //如果这条信息的id 与接受方id一样，而且未读
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    //得到conversationId，从中解析谁是发送目标，谁是发送者
    private User getlettertarget(String conversationId) {
        //conversationId (111_112) 111 给 112 发送
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);
        //如果 111 是登录用户
        if (hostHolder.getUser().getId() == id0) {
            //那么发送目标应该是 112
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }


    //发送私信功能，改变已读私信状态
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String AddMesasage(String toName, String content) {

        //获取要发私信的目标用户
        User targetuser = userService.FindUserByName(toName);

        if (targetuser == null) {
            return Communityutil.getJSONString(1, "目标用户不存在!");
        }

        Message sendmessage = new Message();

        sendmessage.setFromId(hostHolder.getUser().getId());
        sendmessage.setToId(targetuser.getId());
        sendmessage.setContent(content);
        //conversationId 两个人的 ID 组合而成，判断一下谁在前
        if (sendmessage.getFromId() < sendmessage.getToId()) {
            sendmessage.setConversationId(sendmessage.getFromId() + "_" + sendmessage.getToId());
        } else {
            sendmessage.setConversationId(sendmessage.getToId() + "_" + sendmessage.getFromId());
        }
        sendmessage.setCreateTime(new Date());
        messageService.AddMessage(sendmessage);

        return Communityutil.getJSONString(0);
    }
}
