package com.solydshop.notifications.controller;

import com.solydshop.notifications.dto.CreateNotificationRequest;
import com.solydshop.notifications.dto.NotificationDTO;
import com.solydshop.notifications.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateNotificationRequest request) {
        notificationService.createForUser(
                request.getUserId(), request.getTitle(), request.getMessage(),
                request.getType(), request.getResourceId());
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestParam Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOne(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.deleteOne(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestParam Long userId) {
        notificationService.deleteAll(userId);
        return ResponseEntity.ok().build();
    }
}
