package com.leyou.gateway.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
@ConfigurationProperties(prefix = "ly.jwt")
@Slf4j
@Data
public class JwtProperties implements InitializingBean {
    //公钥地址
    private String pubKeyPath;
    private PublicKey publicKey;

    //用户token相关属性
    private UserToken user = new UserToken();

    @Data
    public class UserToken{
        //cookie的名称
        private String cookieName;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            //获取公钥
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}
