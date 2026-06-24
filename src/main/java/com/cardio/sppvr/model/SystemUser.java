package com.cardio.sppvr.model;

import java.time.LocalDateTime;

/**
 * Сущность «Пользователь системы», соответствует таблице system_user.
 * Содержит данные учётной записи: логин, хеш пароля, роль, статус блокировки,
 * счётчик неудачных попыток входа и признак необходимости смены пароля.
 */
public class SystemUser {

    private int userId;
    private String login;
    private String passwordHash;
    private String role;
    private boolean isBlocked;
    private int failedAttempts;
    private LocalDateTime lastLogin;
    private boolean passwordChanged;
    private LocalDateTime createdAt;

    public SystemUser() {}

    public SystemUser(int userId, String login, String passwordHash, String role,
                      boolean isBlocked, int failedAttempts,
                      LocalDateTime lastLogin, boolean passwordChanged,
                      LocalDateTime createdAt) {
        this.userId = userId;
        this.login = login;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isBlocked = isBlocked;
        this.failedAttempts = failedAttempts;
        this.lastLogin = lastLogin;
        this.passwordChanged = passwordChanged;
        this.createdAt = createdAt;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { this.isBlocked = blocked; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public boolean isPasswordChanged() { return passwordChanged; }
    public void setPasswordChanged(boolean passwordChanged) { this.passwordChanged = passwordChanged; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
