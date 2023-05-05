package com.douyin.interceptor;

import com.douyin.base.BaseInfoProperties;
import com.douyin.exceptions.GraceException;
import com.douyin.grace.result.ResponseStatusEnum;
import com.douyin.utils.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author word
 */

@Slf4j
public class PassportInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIp = IPUtil.getRequestIp(request);
        boolean keyIsExist = redis.keyIsExist(MOBILE_SMSCODE + ":" + userIp);
        if (keyIsExist) {
            log.info("短信发送频率太大");
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            return false;
        }

        return true;

    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
