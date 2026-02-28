package com.diploma.server.model;

public class AuthRequests {

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }

    public static class TwoFactorRequest {
        private String userId;
        private String code;
        public String getUserId() { return userId; }
        public String getCode() { return code; }
    }

    public static class CaptchaRequest {
        private String userId;
        private String captchaToken;
        public String getUserId() { return userId; }
        public String getCaptchaToken() { return captchaToken; }
    }

    public static class LoginResponse {
        private boolean requiresTwoFactor;
        private String twoFactorMethod;
        private String userId;
        private String token;
        private VpnConfig vpnConfig;
        private String error;
        public boolean isRequiresTwoFactor() { return requiresTwoFactor; }
        public void setRequiresTwoFactor(boolean v) { this.requiresTwoFactor = v; }
        public String getTwoFactorMethod() { return twoFactorMethod; }
        public void setTwoFactorMethod(String v) { this.twoFactorMethod = v; }
        public String getUserId() { return userId; }
        public void setUserId(String v) { this.userId = v; }
        public String getToken() { return token; }
        public void setToken(String v) { this.token = v; }
        public VpnConfig getVpnConfig() { return vpnConfig; }
        public void setVpnConfig(VpnConfig v) { this.vpnConfig = v; }
        public String getError() { return error; }
        public void setError(String v) { this.error = v; }
    }

    public static class TwoFactorResponse {
        private boolean requiresCaptcha;
        private String token;
        private VpnConfig vpnConfig;
        private String error;
        public boolean isRequiresCaptcha() { return requiresCaptcha; }
        public void setRequiresCaptcha(boolean v) { this.requiresCaptcha = v; }
        public String getToken() { return token; }
        public void setToken(String v) { this.token = v; }
        public VpnConfig getVpnConfig() { return vpnConfig; }
        public void setVpnConfig(VpnConfig v) { this.vpnConfig = v; }
        public String getError() { return error; }
        public void setError(String v) { this.error = v; }
    }

    public static class VpnConfig {
        private String serverIp;
        private int serverPort;
        public VpnConfig(String serverIp, int serverPort) {
            this.serverIp = serverIp; this.serverPort = serverPort;
        }
        public String getServerIp() { return serverIp; }
        public int getServerPort() { return serverPort; }
    }
}
