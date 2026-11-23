package org.example;

import org.example.core.Link;
import org.example.core.LinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    private LinkService service;

    @BeforeEach
    void setUp() {
        service = new LinkService(60, 10);
    }

    //Тесты создания ссылок

    @Test
    void testCreateLink() {
        Link link = service.createLink("https://google.com", "user1", null, null);
        assertNotNull(link);
        assertEquals("https://google.com", link.getOriginalUrl());
        assertEquals("user1", link.getOwnerId());
        assertEquals(0, link.getCurrentClicks());
    }

    @Test
    void testUniqueShortCodes() {
        Link l1 = service.createLink("https://a.com", "user1", null, null);
        Link l2 = service.createLink("https://b.com", "user1", null, null);
        Link l3 = service.createLink("https://c.com", "user1", null, null);
        assertNotEquals(l1.getShortCode(), l2.getShortCode());
        assertNotEquals(l2.getShortCode(), l3.getShortCode());
        assertNotEquals(l1.getShortCode(), l3.getShortCode());
    }

    @Test
    void testCustomTtlAndMaxClicks() {
        Link link = service.createLink("https://a.com", "user1", 120, 5);
        assertEquals(5, link.getMaxClicks());
        assertTrue(link.getExpiresAt().isAfter(java.time.LocalDateTime.now()));
    }

    //Тесты редиректа

    @Test
    void testRedirectIncrementsClicks() {
        Link link = service.createLink("https://a.com", "user1", null, null);
        service.redirect(link.getShortCode());
        assertEquals(1, link.getCurrentClicks());
    }

    @Test
    void testRedirectNonExistentLink() {
        assertThrows(IllegalArgumentException.class, () -> service.redirect("no-such-code"));
    }

    @Test
    void testLimitReached() {
        Link link = service.createLink("https://a.com", "user1", null, 2);
        service.redirect(link.getShortCode());
        service.redirect(link.getShortCode());
        assertThrows(IllegalStateException.class, () -> service.redirect(link.getShortCode()));
    }

    //Тесты прав доступа

    @Test
    void testDeleteByOwner() {
        Link link = service.createLink("https://a.com", "user1", null, null);
        service.deleteLink(link.getShortCode(), "user1");
        assertThrows(IllegalArgumentException.class, () -> service.getLink(link.getShortCode()));
    }

    @Test
    void testDeleteByWrongUser() {
        Link link = service.createLink("https://a.com", "user1", null, null);
        assertThrows(SecurityException.class, () -> service.deleteLink(link.getShortCode(), "user2"));
    }

    @Test
    void testEditLimitByOwner() {
        Link link = service.createLink("https://a.com", "user1", null, 5);
        service.editLinkLimit(link.getShortCode(), "user1", 20);
        assertEquals(20, link.getMaxClicks());
    }

    @Test
    void testEditLimitByWrongUser() {
        Link link = service.createLink("https://a.com", "user1", null, 5);
        assertThrows(SecurityException.class,
                () -> service.editLinkLimit(link.getShortCode(), "user2", 20));
    }

    @Test
    void testEditLimitBelowCurrentClicks() {
        Link link = service.createLink("https://a.com", "user1", null, 5);
        service.redirect(link.getShortCode());
        service.redirect(link.getShortCode());
        assertThrows(IllegalArgumentException.class,
                () -> service.editLinkLimit(link.getShortCode(), "user1", 1));
    }

    //Тесты мультипользовательского режима

    @Test
    void testUserSeesOnlyOwnLinks() {
        service.createLink("https://a.com", "user1", null, null);
        service.createLink("https://b.com", "user1", null, null);
        service.createLink("https://c.com", "user2", null, null);

        List<Link> user1Links = service.getUserLinks("user1");
        List<Link> user2Links = service.getUserLinks("user2");

        assertEquals(2, user1Links.size());
        assertEquals(1, user2Links.size());
    }

    @Test
    void testMultipleLinksPerUser() {
        service.createLink("https://a.com", "user1", null, null);
        service.createLink("https://b.com", "user1", null, null);
        service.createLink("https://c.com", "user1", null, null);
        assertEquals(3, service.getUserLinks("user1").size());
    }


    @Test
    void testExpiredLinkNotAccessible() throws InterruptedException {
        Link link = service.createLink("https://a.com", "user1", 0, 10);
        Thread.sleep(50);
        assertTrue(link.isExpired());
        assertThrows(IllegalStateException.class, () -> service.getLink(link.getShortCode()));
    }

    @Test
    void testNotExpiredLinkAccessible() {
        Link link = service.createLink("https://a.com", "user1", 60, 10);
        assertFalse(link.isExpired());
        assertNotNull(service.getLink(link.getShortCode()));
    }
}