package com.diploma.server.controller;

import com.diploma.server.model.AuthRequests.*;
import com.diploma.server.security.JwtUtil;
import com.diploma.server.service.AuthService;
import com.diploma.server.service.TotpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final AuthService authService;
    private final TotpService totpService;
    private final JwtUtil jwtUtil;
    private static final String VPN_IP = "0.0.0.0";
    private static final int VPN_PORT = 1194;

    public AuthController(AuthService a, TotpService t, JwtUtil j) {
        this.authService = a; this.totpService = t; this.jwtUtil = j;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return authService.authenticate(req.getUsername(), req.getPassword())
                .map(user -> {
                    authService.generateAndStoreSmsCode(user.getId());
                    LoginResponse r = new LoginResponse();
                    r.setRequiresTwoFactor(true);
                    r.setTwoFactorMethod("SMS");
                    r.setUserId(user.getId());
                    return ResponseEntity.ok(r);
                })
                .orElseGet(() -> {
                    LoginResponse e = new LoginResponse();
                    e.setError("Неверный логин или пароль");
                    return ResponseEntity.status(401).body(e);
                });
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<TwoFactorResponse> verify2fa(@RequestBody TwoFactorRequest req) {
        boolean ok = authService.verifySmsCode(req.getUserId(), req.getCode())
                || authService.findById(req.getUserId())
                    .map(u -> totpService.verify(u.getTotpSecret(), req.getCode()))
                    .orElse(false);
        if (!ok) {
            TwoFactorResponse e = new TwoFactorResponse();
            e.setError("Неверный код");
            return ResponseEntity.status(401).body(e);
        }
        TwoFactorResponse r = new TwoFactorResponse();
        if (authService.requiresCaptcha(req.getUserId())) {
            r.setRequiresCaptcha(true);
        } else {
            r.setToken(jwtUtil.generateToken(req.getUserId()));
            r.setVpnConfig(new VpnConfig(VPN_IP, VPN_PORT));
        }
        return ResponseEntity.ok(r);
    }

    @PostMapping("/verify-captcha")
    public ResponseEntity<TwoFactorResponse> verifyCaptcha(@RequestBody CaptchaRequest req) {
        if (req.getCaptchaToken() == null || req.getCaptchaToken().isEmpty()) {
            TwoFactorResponse e = new TwoFactorResponse();
            e.setError("CAPTCHA не пройдена");
            return ResponseEntity.status(401).body(e);
        }
        TwoFactorResponse r = new TwoFactorResponse();
        r.setToken(jwtUtil.generateToken(req.getUserId()));
        r.setVpnConfig(new VpnConfig(VPN_IP, VPN_PORT));
        return ResponseEntity.ok(r);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("VPN Auth Server is running!");
    }
}
