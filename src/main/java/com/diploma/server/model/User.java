package com.diploma.server.model;

public class User {
    private String id;
    private String username;
    private String password;
    private String phone;
    private String totpSecret;
    private boolean captchaRequired;

    public User() {}

    public User(String id, String username, String password,
                String phone, String totpSecret, boolean captchaRequired) {
        this.id = id; this.username = username; this.password = password;
        this.phone = phone; this.totpSecret = totpSecret;
        this.captchaRequired = captchaRequired;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getPhone() { return phone; }
    public String getTotpSecret() { return totpSecret; }
    public boolean isCaptchaRequired() { return captchaRequired; }
}
