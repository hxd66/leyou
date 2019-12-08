package com.leyou.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "ly.cors")   //写上前缀
public class CORSProperties {
    //跟yml文件里名字一一对应
    private List<String> allowedOrigins;
    private Boolean allowedCredentials;
    private List<String> allowedHeaders;
    private List<String> allowedMethods;
    private Long maxAge;
    private String filterPath;
}
