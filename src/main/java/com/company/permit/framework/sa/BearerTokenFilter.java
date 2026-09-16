package com.company.permit.framework.sa;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * 需求约定 Authorization: Bearer &lt;token&gt;，Sa-Token 默认会把整段当作 token。
 * 在 Sa-Token 之前剥离 Bearer 前缀。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class BearerTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        if (StrUtil.startWithIgnoreCase(header, PREFIX)) {
            String token = header.substring(PREFIX.length()).trim();
            request = new BearerRequestWrapper(request, token);
        }
        filterChain.doFilter(request, response);
    }

    private static class BearerRequestWrapper extends HttpServletRequestWrapper {
        private final String token;

        BearerRequestWrapper(HttpServletRequest request, String token) {
            super(request);
            this.token = token;
        }

        @Override
        public String getHeader(String name) {
            if (HEADER.equalsIgnoreCase(name)) {
                return token;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (HEADER.equalsIgnoreCase(name)) {
                return Collections.enumeration(Collections.singletonList(token));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            if (names.stream().noneMatch(HEADER::equalsIgnoreCase)) {
                names.add(HEADER);
            }
            return Collections.enumeration(names);
        }
    }
}
