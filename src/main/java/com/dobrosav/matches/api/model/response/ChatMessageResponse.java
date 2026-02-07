package com.dobrosav.matches.api.model.response;

import java.io.Serializable;
import java.util.Date;

public class ChatMessageResponse implements Serializable {

    private Integer id;
    private Integer matchId;
    private Integer senderId;
    private String senderUsername;
    private String content;
    private Date timestamp;

    public ChatMessageResponse(Integer id, Integer matchId, Integer senderId, String senderUsername, String content, Date timestamp) {
        this.id = id;
        this.matchId = matchId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMatchId() {
        return matchId;
    }

    public void setMatchId(Integer matchId) {
        this.matchId = matchId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
