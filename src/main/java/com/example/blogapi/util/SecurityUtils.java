package com.example.blogapi.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class cho Security operations
 */
@Component
public class SecurityUtils {

    /**
     * Lấy username của user hiện tại từ Security Context
     * 
     * @return username hoặc "system" nếu không có authentication
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return "system";
        }

        return authentication.getName();
    }

    /**
     * Kiểm tra user hiện tại đã authenticated chưa
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser");
    }
}
