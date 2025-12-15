package com.rndmodgames.futtoboru.engine.messages;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Message Manager v1
 * 
 * Centralized system for creating, scheduling, and delivering inbox messages.
 * 
 * Responsibilities:
 * - Create messages for different game events (league, cup, authority, match, job)
 * - Schedule messages for future delivery
 * - Deliver scheduled messages when their date arrives
 * - Provide message queries (unread, by category, etc.)
 * 
 * @author Geomancer86
 */
public class MessageManager {
    
    private Futtoboru gameInstance;
    private SaveGame currentGame;
    private long nextMessageId = 1L;
    
    public MessageManager(Futtoboru game) {
        this.gameInstance = game;
        this.currentGame = game.getCurrentGame();
    }
    
    /**
     * Deliver scheduled messages that are due
     * 
     * Called daily in game engine to check for messages to deliver
     */
    public void deliverScheduledMessages(LocalDateTime currentDate) {
        if (currentGame == null || currentGame.getScheduledMessages() == null) {
            return;
        }
        
        List<Message> toDeliver = new ArrayList<>();
        
        // Find messages that should be delivered
        for (Message message : currentGame.getScheduledMessages()) {
            if (message.getScheduledDate() != null && 
                !message.getScheduledDate().isAfter(currentDate)) {
                toDeliver.add(message);
            }
        }
        
        // Move to delivered messages
        for (Message message : toDeliver) {
            currentGame.getScheduledMessages().remove(message);
            message.setMessageTime(currentDate); // Set delivery time
            currentGame.getAllMessages().add(message);
            
            Gdx.app.log("MessageManager", "Delivered scheduled message: " + message.getTitle());
        }
        
        if (!toDeliver.isEmpty()) {
            Gdx.app.log("MessageManager", "Delivered " + toDeliver.size() + " scheduled messages");
        }
    }
    
    /**
     * Schedule a message for future delivery
     */
    public void scheduleMessage(Message message) {
        if (message == null) {
            return;
        }
        
        if (message.getScheduledDate() == null) {
            // If no scheduled date, deliver immediately
            deliverMessage(message);
            return;
        }
        
        // Add to scheduled messages
        if (currentGame.getScheduledMessages() == null) {
            currentGame.setScheduledMessages(new ArrayList<>());
        }
        
        message.setId(nextMessageId++);
        currentGame.getScheduledMessages().add(message);
        
        Gdx.app.log("MessageManager", "Scheduled message: " + message.getTitle() + " for " + message.getScheduledDate());
    }
    
    /**
     * Deliver a message immediately (add to inbox)
     */
    public void deliverMessage(Message message) {
        if (message == null) {
            return;
        }
        
        if (currentGame.getAllMessages() == null) {
            currentGame.setAllMessages(new ArrayList<>());
        }
        
        message.setId(nextMessageId++);
        if (message.getMessageTime() == null) {
            message.setMessageTime(currentGame.getGameDate());
        }
        currentGame.getAllMessages().add(message);
        
        Gdx.app.log("MessageManager", "Delivered message: " + message.getTitle());
    }
    
    // ========================================
    // LEAGUE MESSAGE CREATION METHODS
    // ========================================
    
    /**
     * Create a league creation announcement message
     */
    public Message createLeagueCreationMessage(League league) {
        if (league == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_CREATION");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("English Football League Created");
        
        // Build message content with club list
        StringBuilder content = new StringBuilder();
        content.append("The English Football League has been formed with ");
        content.append(league.getLeagueClubs() != null ? league.getLeagueClubs().size() : 0);
        content.append(" founding clubs:\n\n");
        
        if (league.getLeagueClubs() != null) {
            for (int i = 0; i < league.getLeagueClubs().size(); i++) {
                content.append("- ");
                content.append(league.getLeagueClubs().get(i).getName());
                if (i < league.getLeagueClubs().size() - 1) {
                    content.append("\n");
                }
            }
        }
        
        content.append("\n\nThe first season will begin on 8 September 1888.");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null); // System message
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        // Action: Link to league standings (when implemented)
        // message.setActionScreen(MainMenuManager.LEAGUE_STANDINGS_SCREEN);
        // message.setActionData(league.getId());
        
        return message;
    }
    
    /**
     * Create a fixture release message
     */
    public Message createFixtureReleaseMessage(League league) {
        if (league == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("FIXTURE_RELEASE");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("League Fixtures Released");
        
        StringBuilder content = new StringBuilder();
        content.append("The complete fixture list for the ");
        content.append(league.getName());
        content.append(" season has been released.\n\n");
        content.append("Your club's first match is scheduled for 8 September 1888.\n\n");
        content.append("View the complete fixture list in the Schedule screen.");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        // Action: Link to schedule screen
        // message.setActionScreen(MainMenuManager.SCHEDULE_SCREEN);
        
        return message;
    }
    
    /**
     * Create a season start reminder message
     */
    public Message createSeasonStartReminderMessage(LocalDateTime seasonStartDate) {
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("SEASON_START_REMINDER");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Season Starts Soon");
        
        StringBuilder content = new StringBuilder();
        content.append("The new season begins on ");
        content.append(formatDate(seasonStartDate));
        content.append(".\n\n");
        content.append("Make sure your squad is ready for the upcoming matches.");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    // ========================================
    // AUTHORITY MESSAGE CREATION METHODS
    // ========================================
    
    /**
     * Create an authority announcement message
     */
    public Message createAuthorityAnnouncement(String title, String content) {
        Message message = new Message();
        message.setCategory(MessageCategory.AUTHORITY);
        message.setMessageType("AUTHORITY_ANNOUNCEMENT");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(title != null ? title : "Football Association Announcement");
        message.setPlainTextMessage(content != null ? content : "");
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create an important dates announcement
     */
    public Message createImportantDatesMessage(LocalDateTime seasonStart, LocalDateTime seasonEnd) {
        Message message = new Message();
        message.setCategory(MessageCategory.AUTHORITY);
        message.setMessageType("IMPORTANT_DATES");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Important Dates Announced");
        
        StringBuilder content = new StringBuilder();
        content.append("The Football Association has announced the following important dates:\n\n");
        content.append("Season Start: ");
        content.append(formatDate(seasonStart));
        content.append("\n");
        content.append("Season End: ");
        content.append(formatDate(seasonEnd));
        content.append("\n\n");
        content.append("Please mark these dates in your calendar.");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    // ========================================
    // MESSAGE QUERY METHODS
    // ========================================
    
    /**
     * Get all unread messages
     */
    public List<Message> getUnreadMessages() {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            return new ArrayList<>();
        }
        
        return currentGame.getAllMessages().stream()
            .filter(msg -> msg != null && 
                          (msg.getIsRead() == null || !msg.getIsRead()) &&
                          (msg.getIsDeleted() == null || !msg.getIsDeleted()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get messages by category
     */
    public List<Message> getMessagesByCategory(MessageCategory category) {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            return new ArrayList<>();
        }
        
        return currentGame.getAllMessages().stream()
            .filter(msg -> msg != null && 
                          msg.getCategory() == category &&
                          (msg.getIsDeleted() == null || !msg.getIsDeleted()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread message count
     */
    public int getUnreadCount() {
        return getUnreadMessages().size();
    }
    
    /**
     * Get all messages (excluding deleted)
     */
    public List<Message> getAllActiveMessages() {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            return new ArrayList<>();
        }
        
        return currentGame.getAllMessages().stream()
            .filter(msg -> msg != null && 
                          (msg.getIsDeleted() == null || !msg.getIsDeleted()))
            .collect(Collectors.toList());
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Format date for display in messages
     */
    private String formatDate(LocalDateTime date) {
        if (date == null) {
            return "TBD";
        }
        
        // Format: "8 September 1888"
        return date.getDayOfMonth() + " " + 
               date.getMonth().toString().substring(0, 1) + 
               date.getMonth().toString().substring(1).toLowerCase() + " " + 
               date.getYear();
    }
}

