package com.douyin.controller;

import com.douyin.base.BaseInfoProperties;
import com.douyin.bo.LoginBo;
import com.douyin.bo.RegisterBO;
import com.douyin.bo.SmsCodeBo;
import com.douyin.grace.result.GraceJSONResult;
import com.douyin.grace.result.ResponseStatusEnum;
import com.douyin.pojo.Users;
import com.douyin.service.UserService;
import com.douyin.utils.IPUtil;
import com.douyin.utils.SMSUtils;
import com.douyin.vo.UsersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;


/**
 * @author word
 */
@Slf4j
@Api(tags = "PassportController通信接口模块")
@RequestMapping("passport")
@RestController
//允许跨域访问
//@CrossOrigin(origins = {"http://localhost:8080"})
//预检请求时间处理时间maxAge
//@CrossOrigin(origins = "*", maxAge = 3600)
public class PassportController extends BaseInfoProperties {

    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "获取短信接口")
    @PostMapping("/getSMSCode")
    public Object getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception {
        log.info(mobile);
        String userIp = IPUtil.getRequestIp(request);
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        log.info(userIp);
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        log.info("短信的验证码为：" + code);
        // 暂时用不了
        //  smsUtils.sendSMS("", code);
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);

        return GraceJSONResult.ok();
    }

    /**
     * 添加valid开启校验
     *
     */

    @ApiOperation(value = "登录接口")
    @PostMapping("/login")
    public GraceJSONResult login(@Valid @RequestBody SmsCodeBo registerBo) {

        String mobile = registerBo.getMobile();
        String code = registerBo.getSmsCode();

        //1.从redis中获取验证码进行验证是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        //2.查询数据库，判断用户是否存在
        Users user = userService.queryMobileIsExit(mobile);
        if (user == null) {
            //2.1 没有注册，需要注册入库
            user = userService.createUser(mobile, null);
        }

        //3.保存用户会话信息
        String uToken = UUID.randomUUID().toString();
        log.info("用户的token: " +   uToken);
        log.info("用户id: " +  user.getId());
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);

        //4.用户登录和注册成功后，删除redis的短信验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        //5.放回用户信息，包含token
        UsersVO UsersVO = new UsersVO();
        BeanUtils.copyProperties(user, UsersVO);
        UsersVO.setUserToken(uToken);

        return GraceJSONResult.ok(UsersVO);
    }

    /**
     * 通过密码登录

     */
    @ApiOperation(value = "登录接口")
    @PostMapping("/login")
    public GraceJSONResult loginByPassword(@Valid @RequestBody LoginBo loginBo) throws NoSuchAlgorithmException {

        Users user = userService.getUserByPassword(loginBo);
        //3.保存用户会话信息
        String uToken = UUID.randomUUID().toString();
        log.info("用户的token: " +   uToken);
        log.info("用户id: " +  user.getId());
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);


        //5.放回用户信息，包含token
        UsersVO UsersVO = new UsersVO();
        BeanUtils.copyProperties(user, UsersVO);
        UsersVO.setUserToken(uToken);

        return GraceJSONResult.ok(UsersVO);
    }


    @ApiOperation(value = "退出接口")
    @PostMapping("/logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request) {
        redis.del(REDIS_USER_TOKEN + ":" + userId);
        return GraceJSONResult.ok();
    }


    @PostMapping("/register")
    @ResponseBody
    public GraceJSONResult register(@Valid @RequestBody RegisterBO registerBo) throws Exception {

        userService.register(registerBo);
        return GraceJSONResult.ok();
    }

}
