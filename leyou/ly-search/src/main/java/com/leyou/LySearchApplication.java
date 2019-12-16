package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients  //需要通过feign进行远程item微服务模块的调用
public class LySearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySearchApplication.class,args);
    }
}
