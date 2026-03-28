package com.diploma.server.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StatsService {

    private static final ZoneId ALMATY = ZoneId.of("Asia/Almaty");

    public static class LoginEvent {
        public final String username;
        public final String userId;
        public final boolean success;
        public final String reason;
        public final LocalDateTime time;

        public LoginEvent(String username, String userId,
                         boolean success, String reason) {
            this.username = username;
            this.userId = userId;
            this.success = success;
            this.reason = reason;
            this.time = LocalDateTime.now(ALMATY);
        }

        public String getTimeFormatted() {
            return time.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"));
        }
    }

    private final List<LoginEvent> events = new CopyOnWriteArrayList<>();

    public void record(String username, String userId,
                      boolean success, String reason) {
        events.add(new LoginEvent(username, userId, success, reason));
        if (events.size() > 500) events.remove(0);
    }

    public List<LoginEvent> getAll() {
        return events;
    }

    public List<LoginEvent> getLast(int n) {
        int size = events.size();
        if (size <= n) return new ArrayList<>(events);
        return new ArrayList<>(events.subList(size - n, size));
    }

    public long getTotalSuccess() {
        return events.stream().filter(e -> e.success).count();
    }

    public long getTotalFailed() {
        return events.stream().filter(e -> !e.success).count();
    }

    public long getTodayCount() {
        LocalDateTime startOfDay = LocalDateTime.now(ALMATY)
            .withHour(0).withMinute(0).withSecond(0);
        return events.stream()
            .filter(e -> e.time.isAfter(startOfDay))
            .count();
    }

    public Map<String, long[]> getLast7DaysData() {
        Map<String, long[]> data = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM");

        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = LocalDateTime.now(ALMATY).minusDays(i);
            String label = day.format(fmt);
            LocalDateTime start = day.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = day.withHour(23).withMinute(59).withSecond(59);

            long success = events.stream()
                .filter(e -> e.time.isAfter(start) && e.time.isBefore(end) && e.success)
                .count();
            long failed = events.stream()
                .filter(e -> e.time.isAfter(start) && e.time.isBefore(end) && !e.success)
                .count();

            data.put(label, new long[]{success, failed});
        }
        return data;
    }

    public Map<String, long[]> getUserStats() {
        Map<String, long[]> stats = new LinkedHashMap<>();
        for (LoginEvent e : events) {
            stats.putIfAbsent(e.username, new long[]{0, 0});
            if (e.success) stats.get(e.username)[0]++;
            else stats.get(e.username)[1]++;
        }
        return stats;
    }
}
