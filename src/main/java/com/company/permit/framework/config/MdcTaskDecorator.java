package com.company.permit.framework.config;

import com.company.permit.framework.log.TraceIdHolder;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        String traceId = TraceIdHolder.get();
        return () -> {
            try {
                if (context != null) {
                    MDC.setContextMap(context);
                } else if (traceId != null) {
                    TraceIdHolder.set(traceId);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
