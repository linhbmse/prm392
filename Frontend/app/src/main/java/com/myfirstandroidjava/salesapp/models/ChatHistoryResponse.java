package com.myfirstandroidjava.salesapp.models;

import java.util.List;

public class ChatHistoryResponse {
    private int total;
    private List<ChatMessage> messages;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }
}
