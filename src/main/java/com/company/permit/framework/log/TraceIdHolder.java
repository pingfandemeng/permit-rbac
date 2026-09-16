package com.company.permit.framework.log;

import org.slf4j.MDC;

public final class TraceIdHolder {
    public static final String TRACE_ID = "traceId";
    public static final String HEADER = "X-Trace-Id";

    private TraceIdHolder() {
    }

    public static String get() {
        return MDC.get(TRACE_ID);
    }

    public static void set(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    public static void remove() {
        MDC.remove(TRACE_ID);
    }
}
