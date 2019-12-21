package com.leyou.common;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class AuthTest {
    private String privateFilePath = "E:\\ssh\\id_rsa";
    private String publicFilePath = "E:\\ssh\\id_rsa.pub";

    @Test
    public void testRsa() throws Exception {
        //生成密钥对
        RsaUtils.generateKey(publicFilePath,privateFilePath,"hello",2048);

        //获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        System.out.println("pri="  + privateKey);

        //获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        System.out.println("pub=" + publicKey);
    }

    @Test
    public void testJWT() throws Exception {
        //获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        //生成token
        String token = JwtUtils.generateTokenExpireInMinutes(new UserInfo(1l, "Java", "juest"), privateKey, 5);
        System.out.println("token = " + token);

        //获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        //解析token
        Payload<UserInfo> info = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);

        System.out.println("exp= " + info.getExpiration());
        System.out.println("info= " + info.getUserInfo());
        System.out.println("id= " + info.getId());
    }
}
