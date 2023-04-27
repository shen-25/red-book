package com.imooc.interceptor;

import com.imooc.base.BaseInfoProperties;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author word
 */
public class UserTokenInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*
            重点！！！！
            预检请求不会携带header
            发现cors跨域复杂请求会先发送一个方法为OPTIONS的预检请求，这个请求是用来验证本次请求是否安全的
            第二个过滤器判断token时会把预请求当做真正的请求去判断，
            所以在第二个过滤器判断token之前先判断是不是预请求OPTIONS，不是则验证token，是则放行。
         */
        System.out.println(request.getMethod());
        //从header获取用户的id和token

        String userId = request.getHeader("headerUserId");
        String userToken = request.getHeader("headerUserToken");
        System.out.println("pre" +  request.getHeader("headerUserId"));
        //用户的id和token都不能为空
        if (StringUtils.isNoneBlank(userId) && StringUtils.isNoneBlank(userToken)) {
            String redisToken = redis.get(REDIS_USER_TOKEN + ":" + userId);
            if (StringUtils.isBlank(redisToken)) {
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            } else {
                if (!redisToken.equalsIgnoreCase(userToken)) {
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
                return true;
            }
        }else{
            return false;
        }
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
