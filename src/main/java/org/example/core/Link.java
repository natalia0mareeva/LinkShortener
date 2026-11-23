package org.example.core;

import java.time.LocalDateTime;
import java.util.UUID;

public class Link {
    private final String shortCode;
    private final String originalUrl;
    private final String ownerId;
    private final LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int maxClicks;
    private int currentClicks;

    public Link(String shortCode, String originalUrl, String ownerId, LocalDateTime createdAt, LocalDateTime expiresAt, int maxClicks) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.maxClicks = maxClicks;
        this.currentClicks = 0;
    }

    public String getShortCode() { return shortCode; }
    public String getOriginalUrl() { return originalUrl; }
    public String getOwnerId() { return ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public int getMaxClicks() { return maxClicks; }
    public void setMaxClicks(int maxClicks) { this.maxClicks = maxClicks; }
    public int getCurrentClicks() { return currentClicks; }
    public void incrementClicks() { this.currentClicks++; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isLimitReached() {
        return currentClicks >= maxClicks;
    }
}