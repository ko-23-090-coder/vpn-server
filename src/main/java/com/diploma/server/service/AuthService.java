package com.diploma.server.service;

import com.diploma.server.model.User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

@Service
public class AuthService {

    private static final ZoneId ALMATY = ZoneId.of("Asia/Almaty");
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, String> smsCodes = new ConcurrentHashMap<>();

    private static final String BOT_TOKEN = "8231613573:AAFqOGdEosUwqvU1JNXhVFUKRPXNhU4yvqA";
    private static final String ADMIN_CHAT_ID = "6730631376";

    public AuthService() {
        users.put("admin", new User(
            "user_001", "admin", "1234",
            "+79001234567", "admin@gmail.com",
            "JBSWY3DPEHPK3PXP", true, "admin"
        ));
        users.put("user", new User(
            "user_002", "user", "5678",
            "+79007654321", "user@gmail.com",
            "JBSWY3DPEHPK3PXP", true, "user"
        ));
    }

    public Optional<User> authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && !user.isBlocked() && user.getPassword().equals(password))
            return Optional.of(user);
        return Optional.empty();
    }

    // Обновить время последнего входа
    public void updateLastLogin(String userId) {
        users.values().stream()
            .filter(u -> u.getId().equals(userId))
            .findFirst()
            .ifPresent(u -> u.setLastLogin(LocalDateTime.now(ALMATY)));
    }

    // Включить/выключить CAPTCHA
    public void toggleCaptcha(String username) {
        User user = users.get(username);
        if (user != null) {
            user.setCaptchaRequired(!user.isCaptchaRequired());
        }
    }

    public String generateAndStoreSmsCode(String userId) {
        String code = String.format("%06d", new Random().nextInt(999999));
        smsCodes.put(userId, code);

        Optional<User> userOpt = findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("admin".equals(user.getRole())) {
                System.out.println("==================================");
                System.out.println("SMS КОД для " + userId + ": " + code);
                System.out.println("==================================");
            } else {
                sendTelegram(ADMIN_CHAT_ID,
                    "Код подтверждения для @" + user.getUsername() + ":\n\n" +
                    "━━━━━━━━━━━━━━\n" +
                    "  " + code + "\n" +
                    "━━━━━━━━━━━━━━\n" +
                    "Код действителен 5 минут.");
                System.out.println("Telegram код отправлен для: " + userId);
            }
        }
        return code;
    }

    private void sendTelegram(String chatId, String text) {
        try {
            String encoded = java.net.URLEncoder.encode(text, "UTF-8");
            String url = "https://api.telegram.org/bot" + BOT_TOKEN +
                         "/sendMessage?chat_id=" + chatId +
                         "&text=" + encoded;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.out.println("Telegram ошибка: " + e.getMessage());
        }
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
            true, role
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
