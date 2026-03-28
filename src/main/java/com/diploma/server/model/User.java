package com.diploma.server.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class User {
    private String id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String totpSecret;
    private boolean captchaRequired;
    private String role;
    private boolean blocked;
    private LocalDateTime lastLogin;

    public User() {}

    public User(String id, String username, String password,
                String phone, String email, String totpSecret,
                boolean captchaRequired, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.totpSecret = totpSecret;
        this.captchaRequired = captchaRequired;
        this.role = role;
        this.blocked = false;
        this.lastLogin = null;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getTotpSecret() { return totpSecret; }
    public boolean isCaptchaRequired() { return captchaRequired; }
    public String getRole() { return role; }
    public boolean isBlocked() { return blocked; }
    public LocalDateTime getLastLogin() { return lastLogin; }

    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
    public void setCaptchaRequired(boolean captchaRequired) {
        this.captchaRequired = captchaRequired;
    }
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLastLoginFormatted() {
        if (lastLogin == null) return "Ещё не входил";
        return lastLogin.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
