package com.company.permit.framework.dataperm;

public final class DataScopeContext {
    private static final ThreadLocal<DataScopeInfo> HOLDER = new ThreadLocal<>();

    private DataScopeContext() {
    }

    public static void set(DataScopeInfo info) {
        HOLDER.set(info);
    }

    public static DataScopeInfo get() {
        return HOLDER.get();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
