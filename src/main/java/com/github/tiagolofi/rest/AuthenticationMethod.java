package com.github.tiagolofi.rest;

public enum AuthenticationMethod {
    QRCODE("qrcode"),
    TOTP("totp"),
    PASSWORD("password");

    private String method;

    private AuthenticationMethod(String method) {
        this.method = method;
    }

    public static AuthenticationMethod fromMethod(String method) {
        for (AuthenticationMethod m : AuthenticationMethod.values()) {
            if (m.method.equalsIgnoreCase(method)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid authentication method: " + method);
    }
}
