package com.myfirstandroidjava.salesapp.models;

public class ChatMessage {
    private int chatMessageId;
    private int userId;
    private String username;
    private String userRole;
    private Integer receiverUserId;
    private String message;
    private String sentAt;

    public ChatMessage() {}

    public ChatMessage(int chatMessageId, int userId, String username, String userRole, Integer receiverUserId, String message, String sentAt) {
        this.chatMessageId = chatMessageId;
        this.userId = userId;
        this.username = username;
        this.userRole = userRole;
        this.receiverUserId = receiverUserId;
        this.message = message;
        this.sentAt = sentAt;
    }

    public int getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(int chatMessageId) { this.chatMessageId = chatMessageId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Integer getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(Integer receiverUserId) { this.receiverUserId = receiverUserId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}
