package com.myfirstandroidjava.salesapp.models;

public class SendMessageRequest {
    private String message;
    private Integer receiverUserId;

    public SendMessageRequest(String message, Integer receiverUserId) {
        this.message = message;
        this.receiverUserId = receiverUserId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(Integer receiverUserId) { this.receiverUserId = receiverUserId; }
}
