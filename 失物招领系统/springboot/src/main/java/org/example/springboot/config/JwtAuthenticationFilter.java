package org.example.springboot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.springboot.entity.User;
import org.example.springboot.util.JwtAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 *
 * 该过滤器负责：
 * 1. 从请求中提取JWT token
 * 2. 验证token的有效性
 * 3. 设置Spring Security的认证上下文
 * 4. 处理认证失败的情况
 *
 * @author 系统开发团队
 * @version 1.0
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtAuthUtils jwtAuthUtils;

    public JwtAuthenticationFilter(JwtAuthUtils jwtAuthUtils) {
        this.jwtAuthUtils = jwtAuthUtils;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        // 获取请求路径
        String requestPath = request.getRequestURI();

        // 对于公开接口，直接放行
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 提取token
            String token = jwtAuthUtils.extractToken(request);

            if (token != null) {
                // 验证token并获取用户信息
                User currentUser = jwtAuthUtils.validateTokenAndGetUser(token);

                if (currentUser != null && jwtAuthUtils.canUserOperate(currentUser)) {
                    // 创建认证对象
                    String role = "ROLE_" + currentUser.getRoleCode(); // Spring Security需要ROLE_前缀
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            currentUser,
                            null,
                            Collections.singletonList(authority)
                        );

                    // 设置认证上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    LOGGER.debug("用户 {} 认证成功，角色: {}", currentUser.getUsername(), role);

                    // 继续过滤器链
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            // 认证失败
            LOGGER.debug("JWT认证失败: path={}", requestPath);
            SecurityContextHolder.clearContext();

            // 设置401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"认证失败，请重新登录\"}");

        } catch (Exception e) {
            LOGGER.error("JWT认证过程中发生错误: {}", e.getMessage(), e);
            // 认证失败，清除认证上下文
            SecurityContextHolder.clearContext();

            // 设置401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"401\",\"msg\":\"认证失败，请重新登录\"}");
        }
    }
    
    /**
     * 判断是否为公开路径（无需认证）
     */
    private boolean isPublicPath(String path) {
        String[] publicPaths = {
            "/api/user/login",
            "/api/user/register",
            "/api/user/forget",
            "/api/user/add",
            "/api/lost-item/list",
            "/api/found-item/list",
            "/api/item-category/list",
            "/api/email/",
            "/api/file/",
            "/file/",
            "/api/img/",
            "/img/",
            "/error",
            "/actuator/health",
            // Swagger相关
            "/api/v3/api-docs/",
            "/api/swagger-ui",
            "/api/doc.html",
            "/api/webjars/",
            "/api/favicon.ico"
        };

        for (String publicPath : publicPaths) {
            if (path.equals(publicPath) || path.startsWith(publicPath)) {
                return true;
            }
        }

        // 检查是否为详情页面（GET请求的ID路径）
        if (path.matches("/api/lost-item/\\d+") || path.matches("/api/found-item/\\d+")) {
            return true;
        }
        
        // 检查是否为图片路径
        if (path.matches("/api/img/\\d+\\.\\w+") || path.matches("/img/\\d+\\.\\w+")) {
            return true;
        }

        return false;
    }
}
