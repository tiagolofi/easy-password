package com.github.tiagolofi.repository;

public record Metadata(
    String timestamp,
    String serverId,
    String method,
    String uri,
    String path,
    String clientIP,
    String userAgent,
    String deviceInfo,
    String location,
    String acceptLanguage,
    String referrer,
    String acceptMediaTypes,
    String queryParameters,
    String headers,
    String contentType,
    Long contentLength
) {}
