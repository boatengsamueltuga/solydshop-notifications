package com.solydshop.notifications.messaging;

import com.solydshop.notifications.dto.CreateNotificationRequest;
import com.solydshop.notifications.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCreatedListenerTest {

    @Mock private NotificationService notificationService;

    @InjectMocks
    private NotificationCreatedListener listener;

    private CreateNotificationRequest request() {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(1L);
        request.setTitle("Title");
        request.setMessage("Message");
        request.setType("TYPE");
        request.setResourceId(5L);
        return request;
    }

    @Test
    void handle_callsCreateForUserWithMessageFields() {
        listener.handle(request());

        verify(notificationService).createForUser(1L, "Title", "Message", "TYPE", 5L);
    }

    @Test
    void handle_onProcessingFailure_doesNotThrow() {
        doThrow(new RuntimeException("db unavailable"))
                .when(notificationService).createForUser(1L, "Title", "Message", "TYPE", 5L);

        assertDoesNotThrow(() -> listener.handle(request()));
    }
}
