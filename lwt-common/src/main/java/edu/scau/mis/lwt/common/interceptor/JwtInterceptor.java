package edu.scau.mis.lwt.common.interceptor;

import edu.scau.mis.lwt.common.exception.BusinessException;
import edu.scau.mis.lwt.common.exception.ErrorCode;
import edu.scau.mis.lwt.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // ★ OPTIONS 预检请求直接放行，避免拦截跨域预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Long userId = JwtUtils.getUserId(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", JwtUtils.getUsername(token));
            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
}
