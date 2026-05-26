package com.ycyw.chat.chat_poc_backend.dto;

public class ChatSessionRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String subject;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}
