package com.github.tiagolofi.models;

public enum AuthenticationMethod {
    TOTP("totp"),
    PASSWORD("password");

    private String method;

    private AuthenticationMethod(String method) {
        this.method = method;
    }

    public static AuthenticationMethod fromMethod(String method) throws IllegalArgumentException {
        for (AuthenticationMethod m : AuthenticationMethod.values()) {
            if (m.method.equalsIgnoreCase(method)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid authentication method: " + method);
    }
}
