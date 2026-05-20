package com.swiftlinkai.security;

public final class TenantContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenantId) { TENANT.set(tenantId); }
    public static String getTenantId() { return TENANT.get(); }

    public static void setUserId(String userId) { USER_ID.set(userId); }
    public static String getUserId() { return USER_ID.get(); }

    public static void clear() {
        TENANT.remove();
        USER_ID.remove();
    }
}
