package com.campus.literature.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(UserInfo userInfo) {
        CURRENT_USER.set(userInfo);
    }

    public static UserInfo getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static Long getCurrentUserId() {
        UserInfo userInfo = CURRENT_USER.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    public static boolean isAdmin() {
        UserInfo userInfo = CURRENT_USER.get();
        return userInfo != null && "ADMIN".equals(userInfo.getRole());
    }

    public static void clear() {
        CURRENT_USER.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String username;
        private String role;
    }
}
