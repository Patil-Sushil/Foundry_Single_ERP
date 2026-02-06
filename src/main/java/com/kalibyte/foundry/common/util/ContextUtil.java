package com.kalibyte.foundry.common.util;

public class ContextUtil {
    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    private ContextUtil() {}

    public static void setTenant(String tenant) {
        TENANT.set(tenant);
    }

    public static String getTenant() {
        return TENANT.get();
    }

    public static void clear() {
        TENANT.remove();
    }
}
