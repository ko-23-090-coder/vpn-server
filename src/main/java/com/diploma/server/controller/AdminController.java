package com.diploma.server.controller;

import com.diploma.server.model.User;
import com.diploma.server.service.AuthService;
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

    public AdminController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // Главная страница админ-панели
    @GetMapping
    public String adminPanel(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "admin_token", required = false) String cookieToken,
            Model model) {

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (cookieToken != null) {
            token = cookieToken;
        }

        if (token == null || !jwtUtil.isValid(token) || !authService.isAdmin(jwtUtil.extractUserId(token))) {
            return "redirect:/admin/login";
        }

        Collection<User> users = authService.getAllUsers();
        model.addAttribute("users", users);
        return "admin";
    }

    // Страница логина для админа
    @GetMapping("/login")
    public String loginPage() {
        return "admin_login";
    }

    // Обработка логина
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

    // Заблокировать пользователя
    @PostMapping("/block")
    public String blockUser(
            @RequestParam String username,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (token != null && jwtUtil.isValid(token) && 
            authService.isAdmin(jwtUtil.extractUserId(token))) {
            authService.blockUser(username);
        }
        return "redirect:/admin";
    }

    // Разблокировать пользователя
    @PostMapping("/unblock")
    public String unblockUser(
            @RequestParam String username,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (token != null && jwtUtil.isValid(token) && 
            authService.isAdmin(jwtUtil.extractUserId(token))) {
            authService.unblockUser(username);
        }
        return "redirect:/admin";
    }

    // Добавить пользователя
    @PostMapping("/add")
    public String addUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam(defaultValue = "user") String role,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (token != null && jwtUtil.isValid(token) && 
            authService.isAdmin(jwtUtil.extractUserId(token))) {
            authService.addUser(username, password, email, role);
        }
        return "redirect:/admin";
    }

    // Удалить пользователя
    @PostMapping("/delete")
    public String deleteUser(
            @RequestParam String username,
            @CookieValue(value = "admin_token", required = false) String token) {
        if (token != null && jwtUtil.isValid(token) && 
            authService.isAdmin(jwtUtil.extractUserId(token))) {
            if (!"admin".equals(username)) {
                authService.deleteUser(username);
            }
        }
        return "redirect:/admin";
    }

    // Выход
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
