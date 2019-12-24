package com.leyou.auth.service.impl;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private UserClient userClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USER_ROLE = "user_role";

    /**
     * 用户登录
     * @param username  用户名
     * @param password 密码
     * @param response 操作cookie
     */
    @Override
    public void login(String username, String password, HttpServletResponse response) {
        try {
            //查询用户
            UserDTO userDTO = userClient.queryUserByUsernameAndPassword(username, password);
            //生成userInfo
            UserInfo userInfo = new UserInfo(userDTO.getId(), userDTO.getUsername(), USER_ROLE);
            //生成token
            String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());

            //写入cookie
            CookieUtils.newCookieBuilder()
                    .response(response)    //response，用于写cookie
                    .domain(prop.getUser().getCookieDomain())   //设置domain
                    .httpOnly(true)   //设置为true，防止XSS攻击，不允许js操作cookie
                    .name(prop.getUser().getCookieName())
                    .value(token)      //设置cookie名称和值
                    .build();  //写cookie
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

    }

    /**
     * 通过cookie获取token，然后校验通过返回用户信息
     * @param request
     * @param response
     * @return
     */
    @Override
    public UserInfo verifyUser(HttpServletRequest request, HttpServletResponse response) {
        try {
            //读取cookie
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            //获取token信息
            Payload<UserInfo> payLoad = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            //获取token的id。，校验黑名单
            String id = payLoad.getId();
            //判断redis中是否存在该tokenId
            Boolean boo = redisTemplate.hasKey(id);
            if (boo != null && boo){
                //如果存在，直接抛出异常
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }

            //获取过期时间
            Date expiration = payLoad.getExpiration();
            //获取刷新时间
            DateTime refreshTime = new DateTime(expiration.getTime()).minusMinutes(prop.getUser().getRefreshTime());
            //判断是否已经过了刷新时间
            if (refreshTime.isBefore(System.currentTimeMillis())){
                //如果过了刷新时间，则生成新的token
                token = JwtUtils.generateTokenExpireInMinutes(payLoad.getUserInfo(),prop.getPrivateKey(),
                        prop.getUser().getExpire());
                //写入cookie
                CookieUtils.newCookieBuilder()
                        .response(response)
                        .httpOnly(true)
                        .domain(prop.getUser().getCookieDomain())
                        .name(prop.getUser().getCookieName())
                        .value(token)
                        .build();
            }
            return payLoad.getUserInfo();
        } catch (Exception e) {
            log.error("用户信息认证失败",e);
            // 抛出异常，证明token无效，直接返回401
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

    /**
     * 用户退出
     * @param request
     * @param response
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        //获取token
        String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
        //解析token
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
        //获取id和有效时长
        String id = payload.getId();
        long time = payload.getExpiration().getTime() - System.currentTimeMillis();
        //写入reids,剩余时间超过5秒才写入
        if (time > 5000){
            redisTemplate.boundValueOps(id).set(id,time);
        }
        //删除cookie
        CookieUtils.deleteCookie(prop.getUser().getCookieName(),prop.getUser().getCookieDomain(),response);
    }
}
