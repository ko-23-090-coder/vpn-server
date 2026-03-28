package com.diploma.server.controller;

import com.diploma.server.model.User;
import com.diploma.server.service.AuthService;
import com.diploma.server.service.StatsService;
import com.diploma.server.security.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final StatsService statsService;

    public AdminController(AuthService authService, 
                          JwtUtil jwtUtil, 
                          StatsService statsService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.statsService = statsService;
    }

    private boolean isAuthorized(String token) {
        return token != null && jwtUtil.isValid(token)
            && authService.isAdmin(jwtUtil.extractUserId(token));
    }

    @GetMapping
    public String adminPanel(
            @CookieValue(value = "admin_token", required = false) String token,
            Model model) {
        if (!isAuthorized(token)) return "redirect:/admin/login";

        model.addAttribute("users", authService.getAllUsers());
        model.addAttribute("totalSuccess", statsService.getTotalSuccess());
        model.addAttribute("totalFailed", statsService.getTotalFailed());
        model.addAttribute("todayCount", statsService.getTodayCount());
        model.addAttribute("recentEvents", statsService.getLast(10));
        model.addAttribute("chartData", statsService.getLast7DaysData());
        model.addAttribute("userStats", statsService.getUserStats());
        return "admin";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin_login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String username,
            @RequestParam String password,
            jakarta.servlet.http.HttpServletResponse response,
            Model model) {
        var userOpt = authService.authenticate(username, password);
        if (userOpt.isPresent() && "admin".equals(userOpt.get().getRole())) {
            String token = jwtUtil.generateToken(userOpt.get().getId());
            jakarta.servlet.http.Cookie cookie =
                new jakarta.servlet.http.Cookie("admin_token", token);
            cookie.setPath("/");
            cookie.setMaxAge(3600);
            response.addCookie(cookie);
            return "redirect:/admin";
        }
        model.addAttribute("error", "Неверный логин или пароль");
        return "admin_login";
    }

    @PostMapping("/block")
    public String blockUser(@RequestParam String username,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (isAuthorized(token)) authService.blockUser(username);
        return "redirect:/admin";
    }

    @PostMapping("/unblock")
    public String unblockUser(@RequestParam String username,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (isAuthorized(token)) authService.unblockUser(username);
        return "redirect:/admin";
    }

    @PostMapping("/add")
    public String addUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam(defaultValue = "user") String role,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (isAuthorized(token)) authService.addUser(username, password, email, role);
        return "redirect:/admin";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam String username,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (isAuthorized(token) && !"admin".equals(username))
            authService.deleteUser(username);
        return "redirect:/admin";
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie =
            new jakarta.servlet.http.Cookie("admin_token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/admin/login";
    }
}
