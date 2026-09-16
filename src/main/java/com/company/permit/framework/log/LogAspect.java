package com.company.permit.framework.log;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.company.permit.framework.util.IpUtils;
import com.company.permit.framework.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final Set<String> SENSITIVE = new HashSet<>();

    static {
        SENSITIVE.add("password");
        SENSITIVE.add("oldPassword");
        SENSITIVE.add("newPassword");
        SENSITIVE.add("confirmPassword");
        SENSITIVE.add("token");
        SENSITIVE.add("captcha");
        SENSITIVE.add("captchaCode");
    }

    private final OperLogWriter operLogWriter;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, Log operLog) throws Throwable {
        long start = System.currentTimeMillis();
        OperLogBO bo = new OperLogBO();
        bo.setTitle(operLog.title());
        bo.setOperType(operLog.operType().getCode());
        bo.setMethod(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
        bo.setOperName(SecurityUtils.getUsername());
        bo.setTraceId(TraceIdHolder.get());
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            bo.setOperIp(IpUtils.getIp(request));
            bo.setOperParam(buildParams(point, request));
        }
        try {
            Object result = point.proceed();
            bo.setStatus("0");
            return result;
        } catch (Throwable ex) {
            bo.setStatus("1");
            bo.setErrorMsg(StrUtil.sub(ex.getMessage(), 0, 1800));
            throw ex;
        } finally {
            bo.setCostTime(System.currentTimeMillis() - start);
            try {
                operLogWriter.writeAsync(bo);
            } catch (Exception e) {
                log.warn("写入操作日志失败", e);
            }
        }
    }

    private String buildParams(ProceedingJoinPoint point, HttpServletRequest request) {
        try {
            Map<String, Object> map = new LinkedHashMap<>();
            MethodSignature signature = (MethodSignature) point.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = point.getArgs();
            if (names != null) {
                for (int i = 0; i < names.length && i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse || arg instanceof MultipartFile) {
                        continue;
                    }
                    map.put(names[i], mask(arg));
                }
            }
            String json = JSONUtil.toJsonStr(map);
            if (json != null && json.length() > 2000) {
                json = json.substring(0, 2000);
            }
            return json;
        } catch (Exception e) {
            return "";
        }
    }

    private Object mask(Object arg) {
        if (arg == null) {
            return null;
        }
        try {
            String json = JSONUtil.toJsonStr(arg);
            cn.hutool.json.JSON parsed = JSONUtil.parse(json);
            if (parsed instanceof cn.hutool.json.JSONObject) {
                cn.hutool.json.JSONObject obj = (cn.hutool.json.JSONObject) parsed;
                for (String key : SENSITIVE) {
                    if (obj.containsKey(key)) {
                        obj.set(key, "******");
                    }
                }
                return obj;
            }
            return parsed;
        } catch (Exception e) {
            return String.valueOf(arg);
        }
    }
}
