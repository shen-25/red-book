package com.imooc;

import com.imooc.interceptor.PassportInterceptor;
import com.imooc.interceptor.UserTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author word
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 所有接口
        registry.addMapping("/**")
                // 是否发送 Cookie
                .allowCredentials(true)
                // 支持域
                .allowedOriginPatterns("*")
                // 支持方法
                .allowedMethods(new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"})
                .allowedHeaders("*")
                .exposedHeaders("*")
                .maxAge(1);
    }


    @Bean
    public PassportInterceptor passportInterceptor() {
        return new PassportInterceptor();
    }

    @Bean
    public UserTokenInterceptor userTokenInterceptor() {
        return new UserTokenInterceptor();
    }


    /**
     * Add Spring MVC lifecycle interceptors for pre- and post-processing of
     * controller method invocations and resource handler requests.
     * Interceptors can be registered to apply to all requests or be limited
     * to a subset of URL patterns.
     *
     * @param registry
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportInterceptor()).
                addPathPatterns("/passport/getSMSCode");
        registry.addInterceptor(userTokenInterceptor())
                .addPathPatterns("/userInfo/modifyUserInfo")
                .addPathPatterns("/userInfo/modifyImage");
    }


}
