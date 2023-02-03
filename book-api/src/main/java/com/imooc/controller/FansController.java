package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.model.Student;
import com.imooc.service.FanService;
import com.imooc.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "粉丝业务接口")
@RestController
public class FansController extends BaseInfoProperties {

    @Autowired
    private FanService fanService;

    @ApiOperation(value = "关注博主")
    @GetMapping("")
    public GraceJSONResult hello(){
        Student student = new Student();
        student.setAge(11);
        student.setName("曾深");
//        log.info(student.toString());
//        log.error(student.toString());
//        log.debug(student.getName());
        return GraceJSONResult.ok(student);
    }

}
