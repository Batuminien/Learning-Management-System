package com.lsm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ClientTypeInterceptor implements HandlerInterceptor {
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";
    private static final ThreadLocal<ClientType> currentClientType = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientTypeHeader = request.getHeader(CLIENT_TYPE_HEADER);
        if (clientTypeHeader != null) {
            try {
                currentClientType.set(ClientType.valueOf(clientTypeHeader.toUpperCase()));
            } catch (IllegalArgumentException e) {
                currentClientType.set(ClientType.WEB); // Default to web if invalid
            }
        } else {
            currentClientType.set(ClientType.WEB); // Default to web if not specified
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        currentClientType.remove(); // Clean up
    }

    public static ClientType getCurrentClientType() {
        return currentClientType.get();
    }
}