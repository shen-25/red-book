package com.imooc.service.impl;

import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.MessageEnum;
import com.imooc.mo.MessageMO;
import com.imooc.pojo.Users;
import com.imooc.repository.MessageRepository;
import com.imooc.service.MsgService;
import com.imooc.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    /**
     * 创建消息
     */
    @Override
    public void createMsg(String fromUserId, String toUserId, Integer type, Map<String, Object> msgContent) {
        MessageMO messageMO = new MessageMO();

        messageMO.setFromUserId(fromUserId);
        Users formUser = userService.getUser(fromUserId);
        messageMO.setFromNickname(formUser.getNickname());
        messageMO.setFromFace(formUser.getFace());

        messageMO.setToUserId(toUserId);
        if (msgContent != null && !msgContent.isEmpty()) {
            messageMO.setMsgContent(msgContent);
        }
        messageMO.setMsgType(type);
        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }

    /**
     * 获取消息
     */
    @Override
    public List<MessageMO> queryList(String toId, Integer page, Integer pageSize) {
        Pageable pageAble = PageRequest.of(page, pageSize, Sort.Direction.DESC, "createTime");
        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toId, pageAble);

        for (MessageMO msg : list) {
            // 如果类型是关注消息，则需要查询我之前有没有关注过他，用于在前端标记“互粉”“互关”
            if (msg.getMsgType() != null && msg.getMsgType().equals(MessageEnum.FOLLOW_YOU.type)) {
                Map map = msg.getMsgContent();
                if (map == null) {
                    map = new HashMap();
                }
                String relationship = redis.get(REDIS_FANS_AND_VLOGGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if (StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")) {
                    map.put("isFriend", true);
                } else {
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);
            }
        }
        return list;
    }
}
