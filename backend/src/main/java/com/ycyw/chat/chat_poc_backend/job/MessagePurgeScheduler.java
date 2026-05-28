package com.ycyw.chat.chat_poc_backend.job;

import com.ycyw.chat.chat_poc_backend.service.MessagePurgeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MessagePurgeScheduler {

    private final MessagePurgeService messagePurgeService;

    public MessagePurgeScheduler(MessagePurgeService messagePurgeService) {
        this.messagePurgeService = messagePurgeService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledPurge() {
        messagePurgeService.purge(false, "scheduled", "system");
    }
}
