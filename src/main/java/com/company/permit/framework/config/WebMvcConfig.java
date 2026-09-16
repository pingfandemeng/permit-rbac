package com.company.permit.framework.config;

import com.company.permit.framework.dataperm.DataScopeInterceptor;
import com.company.permit.framework.sa.PwdResetInterceptor;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final DataScopeInterceptor dataScopeInterceptor;
    private final PwdResetInterceptor pwdResetInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter
                        .match("/api/**")
                        .notMatch("/api/auth/login", "/api/auth/captcha")
                        .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/captcha", "/actuator/**");
        registry.addInterceptor(pwdResetInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/captcha", "/api/auth/logout",
                        "/api/system/profile/password", "/actuator/**");
        registry.addInterceptor(dataScopeInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Trace-Id", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
