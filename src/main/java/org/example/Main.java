package org.example;

import org.example.core.LinkService;
import org.example.infra.ConfigLoader;
import org.example.ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        ConfigLoader config = new ConfigLoader();
        LinkService service = new LinkService(config.getDefaultTtlMinutes(), config.getDefaultMaxClicks());
        ConsoleUI ui = new ConsoleUI(service);
        ui.start();
    }
}