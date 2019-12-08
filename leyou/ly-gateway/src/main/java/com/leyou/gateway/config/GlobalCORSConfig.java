package com.leyou.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class GlobalCORSConfig {
    @Bean
    public CorsFilter corsFilter(CORSProperties properties){
        //添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //允许的域，不要写*，否则cookie就无法使用了
        properties.getAllowedOrigins().forEach(config::addAllowedOrigin);
//        config.addAllowedOrigin("http://manage.leyou.com");
//        config.addAllowedOrigin("http://www.leyou.com");
        //是否要发送cookie信息
        config.setAllowCredentials(properties.getAllowedCredentials());
//        config.setAllowCredentials(true);
        //允许的请求方式
        properties.getAllowedMethods().forEach(config::addAllowedMethod);
//        config.addAllowedMethod("OPTIONS");
//        config.addAllowedMethod("HEAD");
//        config.addAllowedMethod("GET");
//        config.addAllowedMethod("PUT");
//        config.addAllowedMethod("POST");
//        config.addAllowedMethod("DELETE");
        //允许的头信息
        properties.getAllowedHeaders().forEach(config::addAllowedHeader);
//        config.addAllowedHeader("*");
        //有效期
        config.setMaxAge(properties.getMaxAge());
//        config.setMaxAge(360000L);

        //添加映射路径,拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(properties.getFilterPath(),config);
        //返回新的CorsFilter
        return new CorsFilter(source);
    }
}
