package com.rndmodgames.futtoboru.engine.messages;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
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
    private long nextMessageId = 1L;
    
    public MessageManager(Futtoboru game) {
        this.gameInstance = game;
    }
    
    /**
     * Get current SaveGame (always fresh from game instance)
     * This ensures we always have the latest SaveGame reference
     */
    private SaveGame getCurrentGame() {
        if (gameInstance == null) {
            return null;
        }
        return gameInstance.getCurrentGame();
    }
    
    /**
     * Deliver scheduled messages that are due
     * 
     * Called daily in game engine to check for messages to deliver
     */
    public void deliverScheduledMessages(LocalDateTime currentDate) {
        SaveGame currentGame = getCurrentGame();
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
            
            // Ensure message has ID
            if (message.getId() == null) {
                message.setId(nextMessageId++);
            }
            
            // Set delivery time
            message.setMessageTime(currentDate);
            
            // Ensure allMessages list exists
            if (currentGame.getAllMessages() == null) {
                currentGame.setAllMessages(new ArrayList<>());
            }
            
            // Check if already delivered (by ID)
            boolean alreadyDelivered = false;
            for (Message existing : currentGame.getAllMessages()) {
                if (existing != null && existing.getId() != null && existing.getId().equals(message.getId())) {
                    alreadyDelivered = true;
                    break;
                }
            }
            
            if (!alreadyDelivered) {
                currentGame.getAllMessages().add(message);
                Gdx.app.log("MessageManager", "Delivered scheduled message ID " + message.getId() + ": " + message.getTitle());
                System.out.println("MessageManager: Delivered scheduled message ID " + message.getId() + ": " + message.getTitle());
            } else {
                Gdx.app.log("MessageManager", "Scheduled message ID " + message.getId() + " already delivered: " + message.getTitle());
            }
        }
        
        if (!toDeliver.isEmpty()) {
            int totalMessages = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
            Gdx.app.log("MessageManager", "Delivered " + toDeliver.size() + " scheduled messages. Total messages: " + totalMessages);
            System.out.println("MessageManager: Delivered " + toDeliver.size() + " scheduled messages. Total messages: " + totalMessages);
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
        
        SaveGame currentGame = getCurrentGame();
        if (currentGame == null) {
            Gdx.app.error("MessageManager", "Cannot schedule message: SaveGame is null");
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
     * 
     * NOTE: If message is already in scheduledMessages, remove it first
     */
    public void deliverMessage(Message message) {
        if (message == null) {
            Gdx.app.error("MessageManager", "Cannot deliver null message");
            return;
        }
        
        SaveGame currentGame = getCurrentGame();
        if (currentGame == null) {
            Gdx.app.error("MessageManager", "Cannot deliver message: SaveGame is null");
            System.out.println("MessageManager ERROR: SaveGame is null, cannot deliver message: " + message.getTitle());
            return;
        }
        
        // Ensure allMessages list exists
        if (currentGame.getAllMessages() == null) {
            currentGame.setAllMessages(new ArrayList<>());
            Gdx.app.log("MessageManager", "Initialized allMessages list");
            System.out.println("MessageManager: Initialized allMessages list");
        }
        
        // Check if message with same ID already exists (more reliable than contains())
        if (message.getId() != null) {
            for (Message existing : currentGame.getAllMessages()) {
                if (existing != null && existing.getId() != null && existing.getId().equals(message.getId())) {
                    Gdx.app.log("MessageManager", "Message with ID " + message.getId() + " already delivered: " + message.getTitle());
                    System.out.println("MessageManager: Message with ID " + message.getId() + " already delivered: " + message.getTitle());
                    return;
                }
            }
        }
        
        // Also check for duplicate messages by type and title (for welcome messages, etc.)
        // This prevents creating multiple welcome messages if updateDynamicComponents is called multiple times
        if (message.getMessageType() != null && message.getTitle() != null) {
            for (Message existing : currentGame.getAllMessages()) {
                if (existing != null && 
                    existing.getMessageType() != null && existing.getMessageType().equals(message.getMessageType()) &&
                    existing.getTitle() != null && existing.getTitle().equals(message.getTitle())) {
                    Gdx.app.log("MessageManager", "Duplicate message detected by type/title: " + message.getMessageType() + " / " + message.getTitle());
                    System.out.println("MessageManager: Duplicate message detected by type/title: " + message.getMessageType() + " / " + message.getTitle());
                    return;
                }
            }
        }
        
        // Remove from scheduled if it exists there (by ID if available, otherwise by reference)
        if (currentGame.getScheduledMessages() != null) {
            if (message.getId() != null) {
                currentGame.getScheduledMessages().removeIf(m -> m != null && m.getId() != null && m.getId().equals(message.getId()));
            } else {
                currentGame.getScheduledMessages().remove(message);
            }
        }
        
        // Set message ID if not set
        if (message.getId() == null) {
            message.setId(nextMessageId++);
        }
        
        // Set message time if not set
        if (message.getMessageTime() == null) {
            message.setMessageTime(currentGame.getGameDate());
        }
        
        // Add to allMessages
        currentGame.getAllMessages().add(message);
        
        int totalMessages = currentGame.getAllMessages().size();
        Gdx.app.log("MessageManager", "Delivered message ID " + message.getId() + ": " + message.getTitle() + " (Total messages: " + totalMessages + ")");
        System.out.println("MessageManager: Delivered message ID " + message.getId() + ": " + message.getTitle() + " (Total messages: " + totalMessages + ")");
    }
    
    // ========================================
    // WELCOME MESSAGE CREATION METHODS
    // ========================================
    
    /**
     * Create a welcome message for new game start
     * 
     * This message is personalized based on:
     * - Player's profession
     * - Starting club (if any)
     * - Starting country
     * - Game start date
     */
    public Message createWelcomeMessage(com.rndmodgames.futtoboru.data.Person owner, 
                                        com.rndmodgames.futtoboru.data.Profession profession,
                                        com.rndmodgames.futtoboru.data.Country country,
                                        com.rndmodgames.futtoboru.data.Club startingClub,
                                        java.time.LocalDateTime gameStartDate) {
        if (owner == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.SYSTEM);
        message.setMessageType("WELCOME");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Welcome to Futtoboru!");
        
        StringBuilder content = new StringBuilder();
        content.append("Welcome, ");
        
        // Add player name
        if (owner.getName() != null && owner.getLastname() != null) {
            content.append(owner.getName()).append(" ").append(owner.getLastname());
        } else if (owner.getName() != null) {
            content.append(owner.getName());
        } else {
            content.append("Manager");
        }
        
        content.append("!\n\n");
        
        // Add profession
        if (profession != null) {
            content.append("You are starting your career as a ");
            content.append(profession.getName() != null ? profession.getName() : "Professional");
            content.append(".\n\n");
        }
        
        // Add starting club or unemployed status
        if (startingClub != null) {
            content.append("You are currently managing ");
            content.append(startingClub.getName() != null ? startingClub.getName() : "a club");
            if (country != null) {
                content.append(" in ").append(country.getCommonName() != null ? country.getCommonName() : "your country");
            }
            content.append(".\n\n");
        } else {
            content.append("You are currently unemployed and looking for your first opportunity.\n\n");
            if (country != null) {
                content.append("You are based in ");
                content.append(country.getCommonName() != null ? country.getCommonName() : "your country");
                content.append(".\n\n");
            }
        }
        
        // Add game start date
        if (gameStartDate != null) {
            content.append("The year is ");
            content.append(gameStartDate.getYear());
            content.append(".\n\n");
        }
        
        content.append("Good luck in your career!\n\n");
        content.append("Check your inbox regularly for important messages and opportunities.");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null); // System message
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
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
    
    /**
     * Create a league welcome message for a specific club
     * Sent to each club when they join a league
     */
    public Message createLeagueWelcomeMessage(League league, Club club, LocalDateTime seasonStart, LocalDateTime seasonEnd) {
        if (league == null || club == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_WELCOME");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Welcome to the " + league.getName());
        
        StringBuilder content = new StringBuilder();
        content.append("Congratulations! Your club, ");
        content.append(club.getName());
        content.append(", has been accepted into the ");
        content.append(league.getName());
        content.append(" for the upcoming season.\n\n");
        
        if (league.getLeagueClubs() != null) {
            content.append("The league consists of ");
            content.append(league.getLeagueClubs().size());
            content.append(" clubs, and you will play each team twice (home and away) throughout the season.\n\n");
        }
        
        content.append("The season begins on ");
        content.append(formatDate(seasonStart));
        content.append(" and concludes on ");
        content.append(formatDate(seasonEnd));
        content.append(".\n\n");
        
        content.append("Good luck!");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    // ========================================
    // CUP MESSAGE CREATION METHODS
    // ========================================
    
    /**
     * Create a cup welcome message
     */
    public Message createCupWelcomeMessage(com.rndmodgames.futtoboru.data.Competition cup, Club club) {
        if (cup == null || club == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.CUP);
        message.setMessageType("CUP_WELCOME");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Welcome to the " + cup.getName());
        
        StringBuilder content = new StringBuilder();
        content.append("Your club, ");
        content.append(club.getName());
        content.append(", has been invited to participate in the ");
        content.append(cup.getName());
        content.append(".\n\n");
        content.append("The competition will begin shortly. Good luck!");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create a cup draw date announcement message
     */
    public Message createCupDrawAnnouncementMessage(com.rndmodgames.futtoboru.data.Competition cup, LocalDateTime drawDate) {
        if (cup == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.CUP);
        message.setMessageType("CUP_DRAW_ANNOUNCEMENT");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(cup.getName() + " Draw Date Announced");
        
        StringBuilder content = new StringBuilder();
        content.append("The Football Association has announced that the draw for the First Round of the ");
        content.append(cup.getName());
        content.append(" will take place on ");
        content.append(formatDate(drawDate));
        content.append(".\n\n");
        content.append("All participating clubs will be notified of their opponents following the draw.");
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create a cup draw result message
     */
    public Message createCupDrawResultMessage(com.rndmodgames.futtoboru.data.Competition cup, Club club, Club opponent, LocalDateTime matchDate, String roundName) {
        if (cup == null || club == null || opponent == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.CUP);
        message.setMessageType("CUP_DRAW_RESULT");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(cup.getName() + " " + (roundName != null ? roundName : "Draw"));
        
        StringBuilder content = new StringBuilder();
        content.append("The draw for the ");
        if (roundName != null) {
            content.append(roundName);
            content.append(" of the ");
        }
        content.append(cup.getName());
        content.append(" has been completed.\n\n");
        content.append("Your club, ");
        content.append(club.getName());
        content.append(", has been drawn against ");
        content.append(opponent.getName());
        content.append(".\n\n");
        
        if (matchDate != null) {
            content.append("The match will be played on ");
            content.append(formatDate(matchDate));
            content.append(".");
        } else {
            content.append("The match date will be announced shortly.");
        }
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        // Store club ID in actionData so we can filter messages by club
        if (club != null && club.getId() != null) {
            message.setActionData(club.getId());
        }
        
        return message;
    }
    
    // ========================================
    // MATCH MESSAGE CREATION METHODS
    // ========================================
    
    /**
     * Create a match result message
     */
    public Message createMatchResultMessage(com.rndmodgames.futtoboru.data.Match match, Club homeClub, Club awayClub) {
        if (match == null || homeClub == null || awayClub == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.MATCH);
        message.setMessageType("MATCH_RESULT");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Match Result: " + homeClub.getName() + " " + 
                         match.getHomeGoals() + " - " + match.getAwayGoals() + " " + awayClub.getName());
        
        StringBuilder content = new StringBuilder();
        content.append(homeClub.getName());
        content.append(" ");
        content.append(match.getHomeGoals());
        content.append(" - ");
        content.append(match.getAwayGoals());
        content.append(" ");
        content.append(awayClub.getName());
        content.append("\n\n");
        
        if (match.getMatchDateTime() != null) {
            content.append("Date: ");
            content.append(formatDate(match.getMatchDateTime()));
            content.append("\n");
        }
        
        if (match.getAttendance() != null) {
            content.append("Attendance: ");
            content.append(match.getAttendance());
            content.append("\n");
        }
        
        // Match type
        String matchType = "Friendly";
        if (match.getMatchType() != null) {
            if (match.getMatchType() == com.rndmodgames.futtoboru.data.Match.LEAGUE_MATCH) {
                matchType = "League Match";
            } else if (match.getMatchType() == com.rndmodgames.futtoboru.data.Match.CUP_MATCH) {
                matchType = "Cup Match";
            }
        }
        content.append("Type: ");
        content.append(matchType);
        content.append("\n\n");
        
        // Result summary
        if (match.getHomeGoals() > match.getAwayGoals()) {
            content.append(homeClub.getName());
            content.append(" won the match.");
        } else if (match.getAwayGoals() > match.getHomeGoals()) {
            content.append(awayClub.getName());
            content.append(" won the match.");
        } else {
            content.append("The match ended in a draw.");
        }
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        return message;
    }
    
    /**
     * Create a match preview message
     */
    public Message createMatchPreviewMessage(com.rndmodgames.futtoboru.data.Match match, Club homeClub, Club awayClub) {
        if (match == null || homeClub == null || awayClub == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.MATCH);
        message.setMessageType("MATCH_PREVIEW");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle("Upcoming Match: " + homeClub.getName() + " vs " + awayClub.getName());
        
        StringBuilder content = new StringBuilder();
        content.append("Your next match is scheduled for ");
        if (match.getMatchDateTime() != null) {
            content.append(formatDate(match.getMatchDateTime()));
        } else {
            content.append("TBD");
        }
        content.append(".\n\n");
        
        content.append("Match Details:\n");
        content.append("Home: ");
        content.append(homeClub.getName());
        content.append("\n");
        content.append("Away: ");
        content.append(awayClub.getName());
        content.append("\n");
        
        if (homeClub.getStadium() != null) {
            content.append("Venue: ");
            content.append(homeClub.getStadium().getName());
            content.append("\n");
        }
        
        // Match type
        String matchType = "Friendly";
        if (match.getMatchType() != null) {
            if (match.getMatchType() == com.rndmodgames.futtoboru.data.Match.LEAGUE_MATCH) {
                matchType = "League Match";
            } else if (match.getMatchType() == com.rndmodgames.futtoboru.data.Match.CUP_MATCH) {
                matchType = "Cup Match";
            }
        }
        content.append("Type: ");
        content.append(matchType);
        
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
        SaveGame currentGame = getCurrentGame();
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
        SaveGame currentGame = getCurrentGame();
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
        SaveGame currentGame = getCurrentGame();
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

