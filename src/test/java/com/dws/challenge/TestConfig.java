package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.NotificationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class TestConfig {
    @Bean
    public NotificationService notificationService() {
        return new NotificationService() {
            @Override
            public void notifyAboutTransfer(Account account, String transferDescription) {
                System.out.println("Notified account:" + account.getAccountId());
            }
        };
    }
}