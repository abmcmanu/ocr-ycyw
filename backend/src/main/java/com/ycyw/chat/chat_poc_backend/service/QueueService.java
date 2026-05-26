package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionRequest;
import com.ycyw.chat.chat_poc_backend.dto.ChatSessionResponse;
import com.ycyw.chat.chat_poc_backend.repository.ChatThreadRepository;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueService.class);
    
    private final ChatThreadRepository chatThreadRepository;
    private final CustomerRepository customerRepository;

    public QueueService(ChatThreadRepository chatThreadRepository, CustomerRepository customerRepository) {
        this.chatThreadRepository = chatThreadRepository;
        this.customerRepository = customerRepository;
    }

    public ChatSessionResponse createSession(ChatSessionRequest request) {
        // 1. Trouver ou créer le client
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .map(existingCustomer -> {
                    // Mettre à jour le nom si fourni
                    if (request.getFirstName() != null) existingCustomer.setFirstName(request.getFirstName());
                    if (request.getLastName() != null) existingCustomer.setLastName(request.getLastName());
                    return customerRepository.save(existingCustomer);
                })
                .orElseGet(() -> {
                    Customer newCustomer = new Customer(request.getEmail(), request.getFirstName(), request.getLastName());
                    return customerRepository.save(newCustomer);
                });

        // 2. Créer le thread en statut "waiting"
        ChatThread thread = new ChatThread();
        thread.setCustomerId(customer.getId());
        thread.setSubject(request.getSubject());
        thread.setStatus("waiting");
        thread.setCreatedAt(OffsetDateTime.now());
        thread.setUpdatedAt(OffsetDateTime.now());
        
        thread = chatThreadRepository.save(thread);
        logger.info("Nouveau thread créé avec ID : {}", thread.getId());

        // 3. Calculer la position dans la file d'attente
        List<ChatThread> waitingThreads = chatThreadRepository.findByStatusOrderByCreatedAtAsc("waiting");
        
        int position = 1;
        for (ChatThread t : waitingThreads) {
            if (t.getId().equals(thread.getId())) {
                break;
            }
            position++;
        }

        // 4. Estimation du temps d'attente (POC: 2 minutes par personne devant)
        int estimatedWaitTime = (position - 1) * 2;
        if (estimatedWaitTime < 1) {
            estimatedWaitTime = 1;
        }

        return new ChatSessionResponse(thread.getId(), thread.getStatus(), position, estimatedWaitTime);
    }
}
