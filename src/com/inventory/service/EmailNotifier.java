package com.inventory.service;

public class EmailNotifier implements Observer {
    @Override
    public void update(String message) {
        // In a real system, this would send an email.
        System.out.println("Email notification: " + message);
    }
}
