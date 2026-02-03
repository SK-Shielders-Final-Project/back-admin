package org.rookies.zdme.security;

public class SecurityConstants {

    public static final String[] PUBLIC_URLS = {
            "/api/user/auth/login",
            "/api/user/auth/signup",
            "/api/admin/auth/login",
            "/api/2fa/**", // Allow 2FA related endpoints for the demonstration
            "/api/.well-known/jwks.json",
            "/api/auth/password-reset/**",
            "/api/auth/refresh",
            "/api/app/verify-integrity",
            "/api/map/bikes-nearby", // 자전거 위치 정보 API 허용
            "/api/scrap",
            "/error"
    };
}
