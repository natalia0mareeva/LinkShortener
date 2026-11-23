package org.example.core;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkService {
    private final Map<String, Link> links = new ConcurrentHashMap<>();
    private final int defaultTtlMinutes;
    private final int defaultMaxClicks;
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public LinkService(int defaultTtlMinutes, int defaultMaxClicks) {
        this.defaultTtlMinutes = defaultTtlMinutes;
        this.defaultMaxClicks = defaultMaxClicks;
        cleaner.scheduleAtFixedRate(this::cleanupExpiredLinks, 1, 1, TimeUnit.MINUTES);
    }

    public Link createLink(String originalUrl, String ownerId, Integer customTtl, Integer customMaxClicks) {
        String shortCode = generateShortCode();
        int ttl = (customTtl != null) ? customTtl : defaultTtlMinutes;
        int maxClicks = (customMaxClicks != null) ? customMaxClicks : defaultMaxClicks;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(ttl);

        Link link = new Link(shortCode, originalUrl, ownerId, LocalDateTime.now(), expiresAt, maxClicks);
        links.put(shortCode, link);
        return link;
    }

    private String generateShortCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8);
        } while (links.containsKey(code));
        return code;
    }

    public Link getLink(String shortCode) {
        Link link = links.get(shortCode);
        if (link == null) throw new IllegalArgumentException("Ссылка не найдена");
        if (link.isExpired()) {
            links.remove(shortCode);
            throw new IllegalStateException("Срок действия ссылки истек");
        }
        return link;
    }

    public void redirect(String shortCode) {
        Link link = getLink(shortCode);
        if (link.isLimitReached()) {
            throw new IllegalStateException("Лимит переходов исчерпан");
        }
        link.incrementClicks();
        System.out.println("Редирект на: " + link.getOriginalUrl());
    }

    public void deleteLink(String shortCode, String requesterId) {
        Link link = getLink(shortCode);
        if (!link.getOwnerId().equals(requesterId)) {
            throw new SecurityException("Нет прав на удаление этой ссылки");
        }
        links.remove(shortCode);
    }

    public void editLinkLimit(String shortCode, String requesterId, int newMaxClicks) {
        Link link = getLink(shortCode);
        if (!link.getOwnerId().equals(requesterId)) {
            throw new SecurityException("Нет прав на редактирование");
        }
        if (newMaxClicks < link.getCurrentClicks()) {
            throw new IllegalArgumentException("Новый лимит не может быть меньше уже сделанных переходов");
        }
        link.setMaxClicks(newMaxClicks);
    }

    public List<Link> getUserLinks(String ownerId) {
        return links.values().stream()
                .filter(l -> l.getOwnerId().equals(ownerId))
                .toList();
    }

    private void cleanupExpiredLinks() {
        links.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}