package com.solydshop.notifications.service;

import com.solydshop.notifications.dto.NotificationDTO;
import com.solydshop.notifications.entity.Notification;
import com.solydshop.notifications.exception.ResourceNotFoundException;
import com.solydshop.notifications.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl service;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification(1L, "Order placed", "Your order was placed", "ORDER", 42L);
        notification.setId(10L);
    }

    @Test
    void getNotifications_mapsToDTO() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationDTO> result = service.getNotifications(1L);

        assertEquals(1, result.size());
        assertEquals("Order placed", result.get(0).getTitle());
        assertEquals(42L, result.get(0).getResourceId());
    }

    @Test
    void markRead_wrongUser_throws() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        assertThrows(ResourceNotFoundException.class, () -> service.markRead(10L, 999L));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markRead_correctUser_marksAndSaves() {
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

        service.markRead(10L, 1L);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void createForUser_savesNewNotification() {
        service.createForUser(1L, "Title", "Message", "TYPE", 5L);

        verify(notificationRepository).save(argThat(n ->
                n.getUserId().equals(1L)
                        && n.getTitle().equals("Title")
                        && n.getResourceId().equals(5L)));
    }
}
