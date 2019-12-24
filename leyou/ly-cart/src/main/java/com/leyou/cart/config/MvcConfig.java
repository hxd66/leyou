package com.leyou.cart.config;

import com.leyou.cart.interceptors.UserInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //把拦截器配置到SpringMvc中，路径为全路径
        registry.addInterceptor(new UserInterceptor()).addPathPatterns("/**");
    }
}
