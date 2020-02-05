package com.leyou.order.interceptors;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadlocals.UserHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取用户的id
        String user_id = request.getHeader("USER_ID");
        if (StringUtils.isBlank(user_id)){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        //保存用户信息
        UserHolder.setUser(user_id);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //返回后清除threadLocal中的用户信息
        UserHolder.removeUserId();
    }
}
