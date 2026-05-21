package com.campus.literature.security;

import com.campus.literature.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录拦截器
 * <p>
 * 公开接口：无 token 也放行，有 token 则解析上下文。
 * 非公开接口：必须携带有效 token，否则返回 401。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        boolean isPublic = isPublicPath(uri);

        String authHeader = request.getHeader("Authorization");

        // 尝试解析 token（如果有的话），放入上下文
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseToken(token);
            if (claims != null) {
                Long userId = Long.valueOf(claims.getSubject());
                String username = claims.get("username", String.class);
                String role = claims.get("role", String.class);
                UserContext.setCurrentUser(new UserContext.UserInfo(userId, username, role));
            } else if (!isPublic) {
                // token 无效且非公开接口
                writeError(response, 401, "未登录或登录失效");
                return false;
            }
        } else if (!isPublic) {
            // 没有 token 且非公开接口
            writeError(response, 401, "未登录或登录失效");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除上下文，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 判断是否为公开接口（无需 token）
     */
    private boolean isPublicPath(String uri) {
        // 认证接口
        if ("/api/auth/register".equals(uri) || "/api/auth/login".equals(uri)) {
            return true;
        }
        // 文献搜索与详情
        if ("/api/literatures/search".equals(uri)) {
            return true;
        }
        if (uri.matches("/api/literatures/\\d+")) {
            return true;
        }
        // 分类列表
        if ("/api/categories".equals(uri)) {
            return true;
        }
        return false;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code);
        Result<Void> result = Result.error(code, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
