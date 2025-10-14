package com.awsgbsa.sigma_BE.security.filter;

import com.awsgbsa.sigma_BE.exception.ApiResponder;
import com.awsgbsa.sigma_BE.exception.CustomException;
import com.awsgbsa.sigma_BE.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionFilter extends OncePerRequestFilter {

    private final ApiResponder apiResponder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        try {
            filterChain.doFilter(request, response);
//        } catch (AuthenticationException ex) {
//            // Spring Security 내부 Authentication 실패
//            log.warn("ExceptionFilter - AuthenticationException : {}", ex.getMessage());
//            apiResponder.sendError(response, ErrorCode.INVALID_AUTENTICATION, ex);
//        } catch (AccessDeniedException ex) {
//            // Spring Security 내부 인가 실패
//            log.warn("ExceptionFilter - AccessDeniedException : {}", ex.getMessage());
//            apiResponder.sendError(response, ErrorCode.ACCESS_DENIED, ex);
        } catch (CustomException ex) {
            // 커스텀 예외
            log.warn("[ExceptionFilter] CustomException at URI={} → {}", uri, ex.getMessage());
            apiResponder.sendError(response, ex.getErrorCode(), ex);
        } catch (Exception ex) {
            // 기타 모든 예외
            log.warn("[ExceptionFilter] Unexpected Exception at URI={} → {}", uri, ex.getMessage());
            apiResponder.sendError(response, ErrorCode.INTERNAL_SERVER_ERROR, ex);
        }
    }
}
