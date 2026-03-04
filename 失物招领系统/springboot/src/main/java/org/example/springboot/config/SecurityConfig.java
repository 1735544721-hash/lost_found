package org.example.springboot.config;

import jakarta.annotation.Resource;
import org.example.springboot.util.JwtAuthUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * 用于配置系统的安全认证、授权等功能
 * 包括：
 * - 密码加密方式
 * - JWT认证过滤器
 * - 安全过滤器链
 * - 请求授权规则
 * - CSRF防护配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Resource
    private UserDetailsService userDetailsService;

    @Resource
    private JwtAuthUtils jwtAuthUtils;

    /**
     * 密码编码器配置
     * 使用BCrypt加密算法对密码进行加密
     * BCrypt是一种安全的密码哈希函数，自动包含随机盐值
     *
     * @return PasswordEncoder BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // 设置加密强度
    }

    /**
     * 安全过滤器链配置
     * 配置系统的安全规则，包括：
     * 1. 请求授权规则
     *    - 公开接口：登录、注册、物品浏览等
     *    - 认证接口：用户操作、发布、申请等
     *    - 管理员接口：后台管理功能
     * 2. JWT认证过滤器
     * 3. 会话管理策略
     *
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain 配置好的安全过滤器链
     * @throws Exception 配置过程中可能发生的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 无需认证
                        .requestMatchers(
                                "/api/user/login",           // 用户登录
                                "/api/user/register",        // 用户注册
                                "/api/user/forget",          // 忘记密码
                                "/api/user/add",             // 用户注册（另一个接口）
                                "/api/lost-item/list",       // 失物列表（浏览）
                                "/api/lost-item/{id}",       // 失物详情（浏览）
                                "/api/found-item/list",      // 招领列表（浏览）
                                "/api/found-item/{id}",      // 招领详情（浏览）
                                "/api/item-category/list",   // 分类列表
                                "/api/email/**",             // 邮件相关接口
                                "/api/file/**",              // 文件上传接口
                                "/file/**",                  // 文件访问接口（无前缀）
                                "/api/img/**",               // 图片访问接口（带前缀）
                                "/img/**",                   // 图片访问接口
                                "/error",                    // 错误页面
                                "/actuator/health",          // 健康检查
                                // Swagger相关接口
                                "/api/v3/api-docs/**",
                                "/api/swagger-ui.html",
                                "/api/swagger-ui/**",
                                "/api/doc.html",
                                "/api/webjars/**",
                                "/api/favicon.ico"
                        ).permitAll()

                        // 管理员接口 - 需要管理员权限
                        .requestMatchers("/api/admin/**", "/api/back/**").hasRole("ADMIN")

                        // 其他所有接口 - 需要认证
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 无状态会话
                )
                .csrf(csrf -> csrf.disable()) // 禁用CSRF（API接口）
                .addFilterBefore(new JwtAuthenticationFilter(jwtAuthUtils),
                                UsernamePasswordAuthenticationFilter.class); // 添加JWT过滤器

        return http.build();
    }
}