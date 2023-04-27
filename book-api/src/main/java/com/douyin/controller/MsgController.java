package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.mo.MessageMO;
import com.imooc.model.Student;
import com.imooc.service.MsgService;
import com.imooc.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "消息业务 接口")
@RestController
@RequestMapping("msg")
public class MsgController extends BaseInfoProperties {

    @Autowired
    private MsgService msgService;


    @ApiOperation(value = "获取用户的消息，给用户发消息")
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String userId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE - 1;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        List<MessageMO> messageMOS = msgService.queryList(userId, page, pageSize);
        return GraceJSONResult.ok(messageMOS);
    }
}
