package org.example.ui;

import org.example.core.Link;
import org.example.core.LinkService;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleUI {
    private final LinkService service;
    private final Scanner scanner = new Scanner(System.in);
    private String currentUserId;

    public ConsoleUI(LinkService service) {
        this.service = service;
    }

    public void start() {
        System.out.print("Введите ваше имя (или нажмите Enter для генерации UUID): ");
        String name = scanner.nextLine().trim();
        currentUserId = name.isEmpty() ? UUID.randomUUID().toString() : name;
        System.out.println("Ваш ID пользователя: " + currentUserId);

        while (true) {
            printHelp();
            String command = scanner.nextLine().trim();
            try {
                switch (command) {
                    case "1" -> createLink();
                    case "2" -> redirect();
                    case "3" -> listLinks();
                    case "4" -> deleteLink();
                    case "5" -> editLimit();
                    case "0" -> { return; }
                    default -> System.out.println("Неизвестная команда");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("\n--- МЕНЮ ---");
        System.out.println("1. Создать ссылку");
        System.out.println("2. Перейти по ссылке");
        System.out.println("3. Мои ссылки");
        System.out.println("4. Удалить ссылку");
        System.out.println("5. Изменить лимит переходов");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private void createLink() {
        System.out.print("Введите оригинальный URL: ");
        String url = scanner.nextLine().trim();
        System.out.print("Введите TTL в минутах (Enter для значения по умолчанию): ");
        String ttlStr = scanner.nextLine().trim();
        Integer ttl = ttlStr.isEmpty() ? null : Integer.parseInt(ttlStr);

        System.out.print("Введите лимит переходов (Enter для значения по умолчанию): ");
        String limitStr = scanner.nextLine().trim();
        Integer limit = limitStr.isEmpty() ? null : Integer.parseInt(limitStr);

        Link link = service.createLink(url, currentUserId, ttl, limit);
        System.out.println("Ссылка создана: " + link.getShortCode());
        System.out.println("Действует до: " + link.getExpiresAt());
        System.out.println("Лимит переходов: " + link.getMaxClicks());
    }

    private void redirect() {
        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine().trim();
        service.redirect(code);
    }

    private void listLinks() {
        List<Link> links = service.getUserLinks(currentUserId);
        if (links.isEmpty()) {
            System.out.println("У вас пока нет ссылок.");
            return;
        }
        for (Link l : links) {
            System.out.printf("%s -> %s (Переходов: %d/%d, Истекает: %s)\n",
                    l.getShortCode(), l.getOriginalUrl(), l.getCurrentClicks(), l.getMaxClicks(), l.getExpiresAt());
        }
    }

    private void deleteLink() {
        System.out.print("Введите короткий код для удаления: ");
        String code = scanner.nextLine().trim();
        service.deleteLink(code, currentUserId);
        System.out.println("Ссылка удалена.");
    }

    private void editLimit() {
        System.out.print("Введите короткий код: ");
        String code = scanner.nextLine().trim();
        System.out.print("Введите новый лимит переходов: ");
        int newLimit = Integer.parseInt(scanner.nextLine().trim());
        service.editLinkLimit(code, currentUserId, newLimit);
        System.out.println("Лимит обновлен.");
    }
}