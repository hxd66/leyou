package com.leyou.gateway.filters;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 编写一个过滤器，用来验证用户身份权限信息
 */
@Slf4j
@Component
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProp;
    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取路径
        String requestURI = request.getRequestURI();
        return !isAllowPath(requestURI);
    }

    //判断用户的请求是否可以放行
    private boolean isAllowPath(String requestURI) {
        //定义一个标记
        boolean flag = false;
        //遍历允许访问的路径
        List<String> allowPaths = filterProp.getAllowPaths();
        for (String allowPath : allowPaths) {
            //判断是否符合
            if (requestURI.startsWith(allowPath)){
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取token
        String token = CookieUtils.getCookieValue(request,jwtProp.getUser().getCookieName());
//        校验
        //解析token
        try {
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey(), UserInfo.class);
            //获取用户
            UserInfo userInfo = payload.getUserInfo();
            //查询用户权限
            String role = userInfo.getRole();
            //获取当前资源路径
            String path = request.getRequestURI();
            String method = request.getMethod();
            //将获取到用户的信息放到请求头部转发
            ctx.addZuulRequestHeader("USER_ID",userInfo.getId().toString());
            //TODO 判断权限，目前没有，以后补充
            log.info("【网关】用户{}，角色{}。访问服务{} : {}，",userInfo.getUsername(),role,method,path);
        } catch (Exception e) {
            //检验出现异常，返回403，服务器拒绝访问
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
            log.error("非法访问，未登录，地址：{}", request.getRemoteHost(), e );
        }
        return null;
    }
}
