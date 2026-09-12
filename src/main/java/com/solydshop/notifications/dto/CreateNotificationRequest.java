package com.solydshop.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateNotificationRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotBlank
    private String type;

    private Long resourceId;

    public Long getUserId()                { return userId; }
    public void setUserId(Long userId)     { this.userId = userId; }

    public String getTitle()               { return title; }
    public void setTitle(String title)     { this.title = title; }

    public String getMessage()             { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType()                { return type; }
    public void setType(String type)       { this.type = type; }

    public Long getResourceId()                { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
}
