package org.example.springboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JWT 认证工具类
 * 独立的JWT验证逻辑，避免循环依赖
 */
@Component
public class JwtAuthUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthUtils.class);
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 验证JWT Token并返回用户信息
     * @param token JWT token
     * @return 用户信息，验证失败返回null
     */
    public User validateTokenAndGetUser(String token) {
        if (StringUtils.isBlank(token)) {
            log.debug("Token为空");
            return null;
        }
        
        try {
            // 解码token获取用户ID
            String userId = JWT.decode(token).getAudience().get(0);
            User user = userMapper.selectById(Long.valueOf(userId));
            
            if (user == null) {
                log.debug("用户不存在: userId={}", userId);
                return null;
            }
            
            // 验证token签名
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
            jwtVerifier.verify(token);
            
            log.debug("Token验证成功: userId={}", userId);
            return user;
            
        } catch (JWTVerificationException e) {
            log.debug("Token验证失败: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Token解析异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 从请求中提取Token
     * @param request HTTP请求
     * @return Token字符串，未找到返回null
     */
    public String extractToken(jakarta.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        return StringUtils.isBlank(token) ? null : token;
    }
    
    /**
     * 验证用户状态是否可以登录
     * @param user 用户信息
     * @return 是否可以登录
     */
    public boolean canUserLogin(User user) {
        if (user == null) {
            return false;
        }
        
        // 检查用户状态
        Integer status = user.getStatus();
        // 只有状态为1（正常）的用户才能登录
        return Integer.valueOf(1).equals(status);
    }
    
    /**
     * 验证用户是否可以执行操作
     * @param user 用户信息
     * @return 是否可以操作
     */
    public boolean canUserOperate(User user) {
        return canUserLogin(user); // 目前逻辑相同，后续可以扩展
    }
}
