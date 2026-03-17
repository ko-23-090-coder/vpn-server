package com.diploma.server.service;

import com.diploma.server.model.User;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> smsCodes = new ConcurrentHashMap<>();

    public AuthService() {
        // Администратор
        users.put("admin", new User(
            "user_001", "admin", "1234",
            "+79001234567", "admin@gmail.com",
            "JBSWY3DPEHPK3PXP", true, "admin"
        ));

        // Обычный пользователь
        users.put("user", new User(
            "user_002", "user", "5678",
            "+79007654321", "user@gmail.com",
            "JBSWY3DPEHPK3PXP", false, "user"
        ));
    }

    public Optional<User> authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && !user.isBlocked() && user.getPassword().equals(password))
            return Optional.of(user);
        return Optional.empty();
    }

    public String generateAndStoreSmsCode(String userId) {
        String code = String.format("%06d", new Random().nextInt(999999));
        smsCodes.put(userId, code);
        System.out.println("==================================");
        System.out.println("SMS КОД для " + userId + ": " + code);
        System.out.println("==================================");
        return code;
    }

    public boolean verifySmsCode(String userId, String code) {
        String stored = smsCodes.get(userId);
        if (stored != null && stored.equals(code)) {
            smsCodes.remove(userId);
            return true;
        }
        return false;
    }

    public boolean requiresCaptcha(String userId) {
        return users.values().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .map(User::isCaptchaRequired)
                .orElse(false);
    }

    public Optional<User> findById(String userId) {
        return users.values().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst();
    }

    // ── Методы для админ-панели ──

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public void blockUser(String username) {
        User user = users.get(username);
        if (user != null) user.setBlocked(true);
    }

    public void unblockUser(String username) {
        User user = users.get(username);
        if (user != null) user.setBlocked(false);
    }

    public void addUser(String username, String password, 
                        String email, String role) {
        String id = "user_" + String.format("%03d", users.size() + 1);
        users.put(username, new User(
            id, username, password,
            "", email, "JBSWY3DPEHPK3PXP",
            false, role
        ));
    }

    public void deleteUser(String username) {
        users.remove(username);
    }

    public boolean isAdmin(String userId) {
        return users.values().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .map(u -> "admin".equals(u.getRole()))
                .orElse(false);
    }
}
