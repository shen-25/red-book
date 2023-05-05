package com.douyin.service;

import com.douyin.mo.MessageMO;

import java.util.List;
import java.util.Map;

public interface MsgService {
    /**
     * 创建消息
     */
    public void createMsg(String fromUserId, String toUserId, Integer type, Map<String, Object> msgContent);

    /**
     * 获取消息
     */

    public List<MessageMO> queryList(String toId, Integer page, Integer pageSize);


}
