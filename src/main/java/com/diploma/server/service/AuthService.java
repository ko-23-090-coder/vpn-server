package com.diploma.server.service;

import com.diploma.server.model.User;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, String> smsCodes = new ConcurrentHashMap<>();

    public AuthService() {
        users.put("admin", new User(
            "user_001", "admin", "1234",
            "+79001234567", "JBSWY3DPEHPK3PXP", true
        ));
    }

    public Optional<User> authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password))
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
}
