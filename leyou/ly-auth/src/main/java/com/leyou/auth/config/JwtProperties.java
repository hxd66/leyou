package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
@Component
public class JwtProperties {

    //公钥地址
    private String pubKeyPath;
    //私钥地址
    private String priKeyPath;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    @PostConstruct
    public void createKey(){
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 用户token相关属性
     */
    private User user = new User();
    @Data
    public class User{

        //过期时间，单位分钟
        private int expire;
        //cookie名称
        private String cookieName;
        //cookie的域
        private String cookieDomain;
        //刷新时间
        private int refreshTime;
    }
}
