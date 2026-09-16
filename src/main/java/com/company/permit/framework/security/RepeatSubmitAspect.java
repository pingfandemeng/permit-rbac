package com.company.permit.framework.security;

import cn.dev33.satoken.stp.StpUtil;
import com.company.permit.framework.cache.CacheKeys;
import com.company.permit.framework.web.ErrorCode;
import com.company.permit.framework.web.ServiceException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RepeatSubmitAspect {

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(repeatSubmit)")
    public Object around(ProceedingJoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return point.proceed();
        }
        HttpServletRequest request = attrs.getRequest();
        String userId = StpUtil.isLogin() ? String.valueOf(StpUtil.getLoginId()) : request.getRemoteAddr();
        String key = CacheKeys.repeat(userId, request.getRequestURI());
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", repeatSubmit.intervalSeconds(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(ok)) {
            throw new ServiceException(ErrorCode.C01004);
        }
        return point.proceed();
    }
}
