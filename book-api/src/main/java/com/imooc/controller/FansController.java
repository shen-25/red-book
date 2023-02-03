package com.imooc.controller;

import com.imooc.base.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.model.Student;
import com.imooc.pojo.Users;
import com.imooc.service.FanService;
import com.imooc.service.UserService;
import com.imooc.utils.SMSUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "粉丝业务接口")
@RestController
@RequestMapping("fans")
public class FansController extends BaseInfoProperties {

    @Autowired
    private FanService fanService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "关注博主")
    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId){

        if (StringUtils.isBlank(myId) && StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断自己不能关注自己，是公司而定
        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断用户是否存在

        Users user = userService.getUser(myId);

        if (user == null ) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断博主是否存在
        Users vloger = userService.getUser(vlogerId);
        if (vloger == null ) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        fanService.doFollow(myId, vlogerId);
        //redis计数
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        redis.set(REDIS_FANS_AND_VLOGGER_RELATIONSHIP
                + ":" + myId + ":" + vlogerId, "1");
        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "取关博主")
    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId,
                                  @RequestParam String vlogerId){

        if (StringUtils.isBlank(myId) && StringUtils.isBlank(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断自己不能取关自己，是公司而定
        if (myId.equalsIgnoreCase(vlogerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断用户是否存在
        Users user = userService.getUser(myId);

        if (user == null ) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        //判断博主是否存在
        Users vloger = userService.getUser(vlogerId);
        if (vloger == null ) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }

        fanService.doCancel(myId, vlogerId);
        //redis计数
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);
        redis.del(REDIS_FANS_AND_VLOGGER_RELATIONSHIP
                + ":" + myId + ":" + vlogerId);
        return GraceJSONResult.ok();
    }


    @ApiOperation(value = "判断用户是否关注博主")
    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,
                                  @RequestParam String vlogerId){
        return GraceJSONResult.ok( fanService.queryDoIFollowVloger(myId, vlogerId));
    }

    @ApiOperation(value = "查询用户关注列表")
    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        return GraceJSONResult.ok(fanService.queryMyFollows(
                myId,
                page,
                pageSize));
    }

    @ApiOperation(value = "查询用户粉丝列表")
    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize){
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }
        return GraceJSONResult.ok(fanService.queryMyFans(
                myId,
                page,
                pageSize));
    }
}
