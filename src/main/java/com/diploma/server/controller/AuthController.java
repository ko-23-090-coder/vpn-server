package com.diploma.server.controller;

import com.diploma.server.model.AuthRequests.*;
import com.diploma.server.security.JwtUtil;
import com.diploma.server.service.AuthService;
import com.diploma.server.service.StatsService;
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
    private final StatsService statsService;

    private static final String VPN_IP = "0.0.0.0";
    private static final int VPN_PORT = 1194;

    public AuthController(AuthService a, TotpService t, 
                         JwtUtil j, StatsService s) {
        this.authService = a;
        this.totpService = t;
        this.jwtUtil = j;
        this.statsService = s;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return authService.authenticate(req.getUsername(), req.getPassword())
                .map(user -> {
    authService.generateAndStoreSmsCode(user.getId());
    statsService.record(req.getUsername(), user.getId(),
        true, "Вход: ожидание SMS");
                    LoginResponse r = new LoginResponse();
                    r.setRequiresTwoFactor(true);
                    r.setTwoFactorMethod("SMS");
                    r.setUserId(user.getId());
                    r.setRole(user.getRole());
                    return ResponseEntity.ok(r);
                })
                .orElseGet(() -> {
                    // Записываем неудачную попытку
                    statsService.record(req.getUsername(), "unknown",
                        false, "Неверный логин/пароль");
                    LoginResponse e = new LoginResponse();
                    e.setError("Неверный логин или пароль");
                    return ResponseEntity.status(401).body(e);
                });
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<TwoFactorResponse> verify2fa(
            @RequestBody TwoFactorRequest req) {
        boolean ok = authService.verifySmsCode(req.getUserId(), req.getCode())
                || authService.findById(req.getUserId())
                    .map(u -> totpService.verify(u.getTotpSecret(), req.getCode()))
                    .orElse(false);

        if (!ok) {
            authService.findById(req.getUserId()).ifPresent(u ->
                statsService.record(u.getUsername(), req.getUserId(),
                    false, "Неверный SMS код"));
            TwoFactorResponse e = new TwoFactorResponse();
            e.setError("Неверный код");
            return ResponseEntity.status(401).body(e);
        }

        TwoFactorResponse r = new TwoFactorResponse();
        if (authService.requiresCaptcha(req.getUserId())) {
            r.setRequiresCaptcha(true);
        } else {
            authService.findById(req.getUserId()).ifPresent(u ->
                statsService.record(u.getUsername(), req.getUserId(),
                    true, "Успешный вход"));
            r.setToken(jwtUtil.generateToken(req.getUserId()));
            r.setVpnConfig(new VpnConfig(VPN_IP, VPN_PORT));
        }
        return ResponseEntity.ok(r);
    }

    @PostMapping("/verify-captcha")
    public ResponseEntity<TwoFactorResponse> verifyCaptcha(
            @RequestBody CaptchaRequest req) {
        if (req.getCaptchaToken() == null || req.getCaptchaToken().isEmpty()) {
            authService.findById(req.getUserId()).ifPresent(u ->
                statsService.record(u.getUsername(), req.getUserId(),
                    false, "CAPTCHA не пройдена"));
            TwoFactorResponse e = new TwoFactorResponse();
            e.setError("CAPTCHA не пройдена");
            return ResponseEntity.status(401).body(e);
        }

        authService.findById(req.getUserId()).ifPresent(u ->
            statsService.record(u.getUsername(), req.getUserId(),
                true, "Успешный вход"));

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
