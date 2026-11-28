package com.rndmodgames.futtoboru.data;

import com.rndmodgames.futtoboru.menu.GameScreenId;

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
    
    // Enhanced fields (v2.0)
    private MessageCategory category;
    private String messageType;  // Specific type within category (e.g., "LEAGUE_CREATION", "FIXTURE_RELEASE")
    private MessagePriority priority;
    private LocalDateTime scheduledDate;  // For scheduled messages (when to deliver)
    private Integer actionScreen;  // GameScreenId number to link to (saves store the number)
    private Serializable actionData;  // Data to pass to action screen (e.g., league ID)
    private LocalDateTime expirationDate;  // When message expires (optional)
    private String sender;  // Authority id when the message is an official circular or press item (e.g. "ifab"); null otherwise
    private Boolean isMandatory;  // If true, blocks time advancement until viewed/actioned (e.g., draw screen)

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

    // Enhanced getters and setters (v2.0)
    
    public MessageCategory getCategory() {
        return category;
    }

    public void setCategory(MessageCategory category) {
        this.category = category;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    /** @return the screen the message links to, or null (RF-01: saves keep its number, {@link GameScreenId#id()}) */
    public GameScreenId getActionScreen() {
        return actionScreen == null ? null : GameScreenId.fromId(actionScreen);
    }

    public void setActionScreen(GameScreenId actionScreen) {
        this.actionScreen = actionScreen == null ? null : actionScreen.id();
    }

    public Serializable getActionData() {
        return actionData;
    }

    public void setActionData(Serializable actionData) {
        this.actionData = actionData;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
    
    /**
     * Check if message is scheduled (not yet delivered)
     */
    public boolean isScheduled() {
        return scheduledDate != null && (messageTime == null || scheduledDate.isAfter(messageTime));
    }
    
    /**
     * Check if message has expired
     */
    public boolean isExpired(LocalDateTime currentDate) {
        return expirationDate != null && currentDate.isAfter(expirationDate);
    }
    
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }
    
    public void setIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
    }
}