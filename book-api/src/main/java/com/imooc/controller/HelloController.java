package com.imooc.controller;

import com.imooc.RabbitMQConfig;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.model.Student;
import com.imooc.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

}
