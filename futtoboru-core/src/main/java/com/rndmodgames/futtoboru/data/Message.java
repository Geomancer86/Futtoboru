package com.rndmodgames.futtoboru.data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message v1
 * 
 *  - Are messages always for the player? if that is the way we don't need the recipient
 * 
 * @author Geomancer86
 */
public class Message implements Serializable {

    //
    private static final long serialVersionUID = 4266690590638322700L;

    private Long id;
    private Person remitent;
    private LocalDateTime messageTime;
    
    // Basic Text Message
    private String title;
    private String plainTextMessage;

    // 
    private Boolean isRead;
    private Boolean isDeleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getRemitent() {
        return remitent;
    }

    public void setRemitent(Person remitent) {
        this.remitent = remitent;
    }

    public LocalDateTime getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(LocalDateTime messageTime) {
        this.messageTime = messageTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPlainTextMessage() {
        return plainTextMessage;
    }

    public void setPlainTextMessage(String plainTextMessage) {
        this.plainTextMessage = plainTextMessage;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}