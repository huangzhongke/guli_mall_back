package com.hzk.gulimall.order.config;

import com.hzk.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 15:24
 */
@Configuration
public class OrderWebConfig implements WebMvcConfigurer {

    @Autowired
    LoginUserInterceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(interceptor).addPathPatterns("/**");
    }
}
