package com.douyin;

import com.douyin.base.RabbitMQConfig;
import com.douyin.enums.MessageEnum;
import com.douyin.exceptions.GraceException;
import com.douyin.grace.result.ResponseStatusEnum;
import com.douyin.mo.MessageMO;
import com.douyin.service.MsgService;
import com.douyin.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitMQConsumer {

    @Autowired
    private MsgService msgService;

    @RabbitListener(queues = {RabbitMQConfig.QUEUE_SYS_MSG})
    public void watchQueue(String payload, Message message) {
        log.info(payload);
        log.info(message.toString());

        MessageMO messageMO = JsonUtils.jsonToPojo(payload, MessageMO.class);

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info(routingKey);

        // TODO: 下面这段代码可以优化，一个地方是参数优化，另外是枚举的判断优化

        if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.FOLLOW_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.FOLLOW_YOU.type,
                    null);
        }
        else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.FOLLOW_YOU.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.COMMENT_VLOG.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.COMMENT_VLOG.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.REPLY_YOU.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.REPLY_YOU.type,
                    messageMO.getMsgContent());
        } else if (routingKey.equalsIgnoreCase("sys.msg." + MessageEnum.LIKE_COMMENT.enValue)) {
            msgService.createMsg(messageMO.getFromUserId(),
                    messageMO.getToUserId(),
                    MessageEnum.LIKE_COMMENT.type,
                    messageMO.getMsgContent());
        } else {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

    }


}
