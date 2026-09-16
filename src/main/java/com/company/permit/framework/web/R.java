package com.company.permit.framework.web;

import com.company.permit.framework.log.TraceIdHolder;
import lombok.Data;

@Data
public class R<T> {
    private Object code;
    private String msg;
    private T data;
    private String traceId;

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        r.setTraceId(TraceIdHolder.get());
        return r;
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return fail(errorCode, errorCode.getMsg());
    }

    public static <T> R<T> fail(ErrorCode errorCode, String msg) {
        R<T> r = new R<>();
        r.setCode(errorCode.getCode());
        r.setMsg(msg);
        r.setTraceId(TraceIdHolder.get());
        return r;
    }
}
