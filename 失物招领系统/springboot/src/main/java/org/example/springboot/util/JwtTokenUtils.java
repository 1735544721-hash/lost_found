package org.example.springboot.util;


import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot.entity.User;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class JwtTokenUtils {
    private static UserService staticUserService;
    @Resource
    private  UserService userService;
    public static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtils.class);
    @PostConstruct
    public void setUserService() {
        staticUserService=userService;
    }
    public static String genToken(String userId,String sign){
    return JWT.create().withAudience(userId).withExpiresAt(DateUtil.offsetHour(new Date(),2)).sign(Algorithm.HMAC256(sign));
    }
    /**
     * 获取当前登录用户
     * 优先从Spring Security上下文中获取，如果没有则尝试从请求中解析token
     * @return 当前用户信息，未登录返回null
     */
    public static User getCurrentUser(){
        try {
            // 优先从Spring Security上下文中获取认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken)) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof User) {
                    return (User) principal;
                }
            }

            // 如果Spring Security上下文中没有，则尝试从请求中解析token（兼容旧代码）
            String token = null;
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            token = request.getHeader("token");
            if (StringUtils.isBlank(token)) {
                token = request.getParameter("token");
            }
            if (StringUtils.isBlank(token)) {
                LOGGER.debug("获取当前登录的token失败，token{}", token);
                return null;
            }

            String userId = JWT.decode(token).getAudience().get(0);
            return staticUserService.getUserById(Long.valueOf(userId));
        }catch (Exception e){
            LOGGER.debug("获取当前用户信息失败", e);
            return null;
        }
    }
}
