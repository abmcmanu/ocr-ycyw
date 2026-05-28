package com.ycyw.chat.chat_poc_backend.metrics;

import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ChatMetrics {

    private final Counter messagesCounter;
    private final AtomicLong wsConnections = new AtomicLong(0);

    public ChatMetrics(MeterRegistry registry, ChatThreadRepository chatThreadRepository) {
        this.messagesCounter = Counter.builder("chat.messages.total")
                .description("Nombre total de messages persistés")
                .register(registry);

        Gauge.builder("chat.queue.depth", chatThreadRepository,
                repo -> repo.findByStatusOrderByCreatedAtAsc("waiting").size())
                .description("Clients en file d'attente")
                .register(registry);

        Gauge.builder("chat.ws.connections", wsConnections, AtomicLong::get)
                .description("Connexions WebSocket actives (approximation)")
                .register(registry);
    }

    public void incrementMessages() {
        messagesCounter.increment();
    }

    public void wsConnected() {
        wsConnections.incrementAndGet();
    }

    public void wsDisconnected() {
        wsConnections.updateAndGet(v -> Math.max(0, v - 1));
    }
}
