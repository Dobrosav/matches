package com.dobrosav.matches.api.model.request;

public class ChatMessageRequest {
    private Integer senderId;
    private String content;

    public ChatMessageRequest() {
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
