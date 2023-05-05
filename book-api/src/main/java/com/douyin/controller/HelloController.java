package com.douyin.controller;

import com.douyin.base.RabbitMQConfig;
import com.douyin.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "Hello 测试接口")
@RestController
public class HelloController {

    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ApiOperation(value = "这是hello测试路由")
    @GetMapping("hello")
    public Object hello(){

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.send", "我发了一个消息");
        return null;
    }

    @ApiOperation(value = "这是hello2测试路由")
    @GetMapping("hello2")
    public Object hello2(){

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_MSG,
                "sys.msg.delete", "我删除了一个消息");
        return null;
    }

}
