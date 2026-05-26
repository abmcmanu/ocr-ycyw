package com.ycyw.chat.chat_poc_backend.service;

import com.ycyw.chat.chat_poc_backend.domain.ChatThread;
import com.ycyw.chat.chat_poc_backend.domain.Customer;
import com.ycyw.chat.chat_poc_backend.dto.AdvisorSessionDTO;
import com.ycyw.chat.chat_poc_backend.repository.CustomerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdvisorSessionMapper {

    private final CustomerRepository customerRepository;

    public AdvisorSessionMapper(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public AdvisorSessionDTO toDto(ChatThread thread) {
        Optional<Customer> customerOpt = customerRepository.findById(thread.getCustomerId());

        String firstName = customerOpt.map(Customer::getFirstName).orElse(null);
        String lastName = customerOpt.map(Customer::getLastName).orElse(null);
        String email = customerOpt.map(Customer::getEmail).orElse(null);

        AdvisorSessionDTO dto = new AdvisorSessionDTO();
        dto.setId(thread.getId());
        dto.setCustomerId(thread.getCustomerId());
        dto.setSubject(thread.getSubject());
        dto.setStatus(thread.getStatus());
        dto.setAssignedAgentId(thread.getAssignedAgentId());
        dto.setCreatedAt(thread.getCreatedAt());
        dto.setCustomerFirstName(firstName);
        dto.setCustomerLastName(lastName);
        dto.setCustomerEmail(email);
        dto.setCustomerName(buildDisplayName(firstName, lastName, email, thread.getCustomerId()));
        return dto;
    }

    static String buildDisplayName(String firstName, String lastName, String email, java.util.UUID customerId) {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            name.append(firstName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            if (name.length() > 0) name.append(' ');
            name.append(lastName.trim());
        }
        if (name.length() > 0) {
            return name.toString();
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return "Client " + (customerId != null ? customerId.toString().substring(0, 8) : "inconnu");
    }
}
