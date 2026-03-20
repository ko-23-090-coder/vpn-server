package com.diploma.server.controller;

import com.diploma.server.security.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
public class WebController {

    private final JwtUtil jwtUtil;

    public WebController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Главная страница — доступна всем
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Защищённая страница — только с токеном
    @GetMapping("/secure")
    public String secure(
            @RequestHeader(value = "Authorization",
                          required = false) String authHeader,
            Model model) {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isValid(token)) {
                String userId = jwtUtil.extractUserId(token);
                model.addAttribute("userId", userId);
                model.addAttribute("message",
                    "Добро пожаловать! VPN канал активен и защищён.");
                return "secure";
            }
        }
        return "denied";
    }

    // Страница без токена через браузер
    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }

    @GetMapping("/protected")
    public String protectedPage() {
        return "protected";
    }

    // Gateway — веб-версия входа с 2FA
    @GetMapping("/gateway")
    public String gateway() {
        return "gateway";
    }
}
