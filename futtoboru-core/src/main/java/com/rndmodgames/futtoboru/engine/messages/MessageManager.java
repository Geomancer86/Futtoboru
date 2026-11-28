package com.rndmodgames.futtoboru.engine.messages;

import java.time.LocalDateTime;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.localization.LanguageModLoader;

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
     * Message ids come from the game's allocator (P2-4); null when there is no game yet
     */
    private Long nextId() {
        SaveGame game = getCurrentGame();
        return game != null ? game.getIdAllocator().nextId() : null;
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
                message.setId(nextId());
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
                Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Delivered scheduled message ID " + message.getId() + ": " + message.getTitle());
                Log.info(Log.ENGINE_MESSAGES, "MessageManager: Delivered scheduled message ID " + message.getId() + ": " + message.getTitle() + 
                                 " (Type: " + message.getMessageType() + ", Mandatory: " + message.getIsMandatory() + ")");
                
                // If it's a mandatory message, log it prominently
                if (message.getIsMandatory() != null && message.getIsMandatory()) {
                    Log.info(Log.ENGINE_MESSAGES, "========================================");
                    Log.info(Log.ENGINE_MESSAGES, "MANDATORY MESSAGE DELIVERED!");
                    Log.info(Log.ENGINE_MESSAGES, "Message ID: " + message.getId());
                    Log.info(Log.ENGINE_MESSAGES, "Title: " + message.getTitle());
                    Log.info(Log.ENGINE_MESSAGES, "Type: " + message.getMessageType());
                    Log.info(Log.ENGINE_MESSAGES, "This message blocks time advancement until viewed!");
                    Log.info(Log.ENGINE_MESSAGES, "========================================");
                }
            } else {
                Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Scheduled message ID " + message.getId() + " already delivered: " + message.getTitle());
            }
        }
        
        if (!toDeliver.isEmpty()) {
            int totalMessages = (currentGame.getAllMessages() != null) ? currentGame.getAllMessages().size() : 0;
            Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Delivered " + toDeliver.size() + " scheduled messages. Total messages: " + totalMessages);
            Log.info(Log.ENGINE_MESSAGES, "MessageManager: Delivered " + toDeliver.size() + " scheduled messages. Total messages: " + totalMessages);
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
            Log.error(Log.ENGINE_MESSAGES, "MessageManager", "Cannot schedule message: SaveGame is null");
            return;
        }
        
        // Add to scheduled messages
        if (currentGame.getScheduledMessages() == null) {
            currentGame.setScheduledMessages(new ArrayList<>());
        }
        
        if (message.getId() == null) {
            message.setId(nextId());
        }
        currentGame.getScheduledMessages().add(message);
        
        Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Scheduled message: " + message.getTitle() + " for " + message.getScheduledDate());
    }
    
    /**
     * Deliver a message immediately (add to inbox)
     * 
     * NOTE: If message is already in scheduledMessages, remove it first
     */
    public void deliverMessage(Message message) {
        if (message == null) {
            Log.error(Log.ENGINE_MESSAGES, "MessageManager", "Cannot deliver null message");
            return;
        }
        
        SaveGame currentGame = getCurrentGame();
        if (currentGame == null) {
            Log.error(Log.ENGINE_MESSAGES, "MessageManager", "Cannot deliver message: SaveGame is null");
            Log.info(Log.ENGINE_MESSAGES, "MessageManager ERROR: SaveGame is null, cannot deliver message: " + message.getTitle());
            return;
        }
        
        // Ensure allMessages list exists
        if (currentGame.getAllMessages() == null) {
            currentGame.setAllMessages(new ArrayList<>());
            Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Initialized allMessages list");
            Log.info(Log.ENGINE_MESSAGES, "MessageManager: Initialized allMessages list");
        }
        
        // Check if message with same ID already exists (more reliable than contains())
        if (message.getId() != null) {
            for (Message existing : currentGame.getAllMessages()) {
                if (existing != null && existing.getId() != null && existing.getId().equals(message.getId())) {
                    Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Message with ID " + message.getId() + " already delivered: " + message.getTitle());
                    Log.info(Log.ENGINE_MESSAGES, "MessageManager: Message with ID " + message.getId() + " already delivered: " + message.getTitle());
                    return;
                }
            }
        }
        
        // Also check for duplicate messages by type and title (for welcome messages, etc.)
        // This prevents creating multiple welcome messages if updateDynamicComponents is called multiple times.
        // Never for a mandatory message: dropping one leaves its scheduled copy blocking the game forever (P2-3).
        // Never for a match result either (G-03): the same clubs and score next season (or a replay) is a new
        // result, not a duplicate; a result is one per match (MatchResultRecorder records a match once).
        if (message.getMessageType() != null && message.getTitle() != null
                && !Boolean.TRUE.equals(message.getIsMandatory())
                && !"MATCH_RESULT".equals(message.getMessageType())) {
            for (Message existing : currentGame.getAllMessages()) {
                if (existing != null && 
                    existing.getMessageType() != null && existing.getMessageType().equals(message.getMessageType()) &&
                    existing.getTitle() != null && existing.getTitle().equals(message.getTitle())) {
                    Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Duplicate message detected by type/title: " + message.getMessageType() + " / " + message.getTitle());
                    Log.info(Log.ENGINE_MESSAGES, "MessageManager: Duplicate message detected by type/title: " + message.getMessageType() + " / " + message.getTitle());
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
            message.setId(nextId());
        }
        
        // Set message time if not set
        if (message.getMessageTime() == null) {
            message.setMessageTime(currentGame.getGameDate());
        }
        
        // Add to allMessages
        currentGame.getAllMessages().add(message);
        
        int totalMessages = currentGame.getAllMessages().size();
        Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Delivered message ID " + message.getId() + ": " + message.getTitle() + " (Total messages: " + totalMessages + ")");
        Log.info(Log.ENGINE_MESSAGES, "MessageManager: Delivered message ID " + message.getId() + ": " + message.getTitle() + " (Total messages: " + totalMessages + ")");
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
        message.setTitle(LanguageModLoader.getValue("msg_welcome_title"));

        StringBuilder content = new StringBuilder();

        // Add player name
        String ownerName;
        if (owner.getName() != null && owner.getLastname() != null) {
            ownerName = owner.getName() + " " + owner.getLastname();
        } else if (owner.getName() != null) {
            ownerName = owner.getName();
        } else {
            ownerName = LanguageModLoader.getValue("msg_welcome_default_name");
        }

        content.append(LanguageModLoader.format("msg_welcome_greeting", ownerName)).append("\n\n");

        // Add profession
        if (profession != null) {
            content.append(LanguageModLoader.format("msg_welcome_profession", profession.getName() != null
                    ? profession.getName() : LanguageModLoader.getValue("msg_welcome_default_profession")));
            content.append("\n\n");
        }

        String countryName = country != null && country.getCommonName() != null ? country.getCommonName()
                : LanguageModLoader.getValue("msg_welcome_default_country");

        // Add starting club or unemployed status
        if (startingClub != null) {
            String clubName = startingClub.getName() != null ? startingClub.getName()
                    : LanguageModLoader.getValue("msg_welcome_default_club");
            if (country != null) {
                content.append(LanguageModLoader.format("msg_welcome_managing_in", clubName, countryName));
            } else {
                content.append(LanguageModLoader.format("msg_welcome_managing", clubName));
            }
            content.append("\n\n");
        } else {
            content.append(LanguageModLoader.getValue("msg_welcome_unemployed")).append("\n\n");
            if (country != null) {
                content.append(LanguageModLoader.format("msg_welcome_based_in", countryName));
                content.append("\n\n");
            }
        }

        // Add game start date
        if (gameStartDate != null) {
            content.append(LanguageModLoader.format("msg_welcome_year", gameStartDate.getYear()));
            content.append("\n\n");
        }

        content.append(LanguageModLoader.getValue("msg_welcome_closing"));
        
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
     *
     * @param firstLeagueDay the day of the first league matches (P2-13: the text named 8 September 1888 whatever
     *        the calendar); null shows "to be decided"
     */
    public Message createLeagueCreationMessage(League league, LocalDateTime firstLeagueDay) {
        if (league == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_CREATION");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(LanguageModLoader.getValue("msg_league_created_title"));

        // Build message content with club list
        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_league_created_text",
                league.getLeagueClubs() != null ? league.getLeagueClubs().size() : 0));
        content.append("\n\n");
        
        if (league.getLeagueClubs() != null) {
            for (int i = 0; i < league.getLeagueClubs().size(); i++) {
                content.append("- ");
                content.append(league.getLeagueClubs().get(i).getName());
                if (i < league.getLeagueClubs().size() - 1) {
                    content.append("\n");
                }
            }
        }
        
        content.append("\n\n").append(LanguageModLoader.format("msg_league_created_first_season", formatDate(firstLeagueDay)));
        
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
    /**
     * Create a mandatory league draw message
     * This message blocks time advancement until the draw is viewed
     * 
     * @param league The league for which the draw is being made
     * @return Message with isMandatory=true and actionScreen=LEAGUE_DRAW_SCREEN
     */
    public Message createLeagueDrawMessage(League league) {
        if (league == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("LEAGUE_DRAW");
        message.setPriority(MessagePriority.URGENT);
        // Keep the "... - {0}" ending in every language: the inbox recovers the league name after the last " - "
        message.setTitle(LanguageModLoader.format("msg_league_draw_title", league.getName()));
        message.setIsMandatory(true);  // CRITICAL: Blocks time advancement

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_league_draw_text", league.getName()));
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        message.setActionScreen(com.rndmodgames.futtoboru.menu.MainMenuManager.LEAGUE_DRAW_SCREEN);
        message.setActionData(league.getId());  // Store league ID for navigation
        
        // Set message ID if not set
        if (message.getId() == null) {
            message.setId(nextId());
        }
        
        Log.info(Log.ENGINE_MESSAGES, "MessageManager", "Created mandatory league draw message for league: " + league.getName() + 
                    " (ID: " + message.getId() + ", Mandatory: " + message.getIsMandatory() + ")");
        Log.info(Log.ENGINE_MESSAGES, "MessageManager: Created mandatory league draw message for league: " + league.getName() + 
                         " (ID: " + message.getId() + ", Mandatory: " + message.getIsMandatory() + ")");
        
        return message;
    }
    
    /**
     * @param firstLeagueDay the day of the first league matches (P2-13: the text said "your club's first match is
     *        scheduled for 8 September 1888", also to a manager whose club is not in the league); null: "to be decided"
     */
    public Message createFixtureReleaseMessage(League league, LocalDateTime firstLeagueDay) {
        if (league == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.LEAGUE);
        message.setMessageType("FIXTURE_RELEASE");
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(LanguageModLoader.getValue("msg_fixtures_released_title"));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_fixtures_released_text", league.getName(), formatDate(firstLeagueDay)));
        
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
        message.setTitle(LanguageModLoader.getValue("msg_season_starts_soon_title"));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_season_starts_soon_text", formatDate(seasonStartDate)));
        
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
        message.setTitle(LanguageModLoader.format("msg_welcome_to_competition_title", league.getName()));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_league_welcome_accepted", club.getName(), league.getName()));
        content.append("\n\n");

        if (league.getLeagueClubs() != null) {
            content.append(LanguageModLoader.format("msg_league_welcome_clubs", league.getLeagueClubs().size()));
            content.append("\n\n");
        }

        content.append(LanguageModLoader.format("msg_league_welcome_dates", formatDate(seasonStart), formatDate(seasonEnd)));
        content.append("\n\n");

        content.append(LanguageModLoader.getValue("msg_good_luck"));
        
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
        message.setTitle(LanguageModLoader.format("msg_welcome_to_competition_title", cup.getName()));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_cup_welcome_text", club.getName(), cup.getName()));
        
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
        message.setTitle(LanguageModLoader.format("msg_cup_draw_date_title", cup.getName()));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_cup_draw_date_text", cup.getName(), formatDate(drawDate)));
        
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
        message.setTitle(LanguageModLoader.format("msg_cup_draw_result_title", cup.getName(),
                roundName != null ? roundName : LanguageModLoader.getValue("msg_draw")));

        StringBuilder content = new StringBuilder();
        if (roundName != null) {
            content.append(LanguageModLoader.format("msg_cup_draw_result_round", roundName, cup.getName()));
        } else {
            content.append(LanguageModLoader.format("msg_cup_draw_result_done", cup.getName()));
        }
        content.append("\n\n");
        content.append(LanguageModLoader.format("msg_cup_draw_result_opponent", club.getName(), opponent.getName()));
        content.append("\n\n");

        if (matchDate != null) {
            content.append(LanguageModLoader.format("msg_cup_draw_result_date", formatDate(matchDate)));
        } else {
            content.append(LanguageModLoader.getValue("msg_cup_draw_result_no_date"));
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
    
    /**
     * Create a mandatory cup draw message
     * This message blocks time advancement until the draw is viewed
     */
    public Message createCupDrawMessage(com.rndmodgames.futtoboru.data.Competition cup, 
                                       com.rndmodgames.futtoboru.data.CompetitionEdition edition, 
                                       int round, String roundName) {
        if (cup == null || edition == null) {
            return null;
        }
        
        Message message = new Message();
        message.setCategory(MessageCategory.CUP);
        message.setMessageType("CUP_DRAW");
        message.setPriority(MessagePriority.URGENT);
        // The edition's name (e.g. "1888-89 FA Cup") keeps every season's draw title distinct (P2-3)
        String competitionName = edition.getName() != null ? edition.getName() : cup.getName();
        String roundLabel = roundName != null ? roundName : LanguageModLoader.format("msg_round_number", round);
        message.setTitle(LanguageModLoader.format("msg_cup_draw_title", competitionName, roundLabel));
        message.setIsMandatory(true);  // Blocks time advancement

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_cup_draw_text", roundLabel, cup.getName()));
        
        message.setPlainTextMessage(content.toString());
        message.setRemitent(null);
        message.setIsRead(false);
        message.setIsDeleted(false);
        
        // Link to cup draw screen (shared or specific)
        message.setActionScreen(com.rndmodgames.futtoboru.menu.MainMenuManager.CUP_DRAW_SCREEN);
        message.setActionData(edition.getId()); // Pass edition ID to show relevant matches
        
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
        message.setTitle(LanguageModLoader.format("msg_match_result_title", homeClub.getName(),
                         match.getHomeGoals(), match.getAwayGoals(), awayClub.getName()));
        
        StringBuilder content = new StringBuilder();
        // G-03: the report's headline over the score; the full report is one click away (actionData = match id)
        SaveGame reportGame = getCurrentGame();
        if (reportGame != null) {
            content.append(com.rndmodgames.futtoboru.engine.report.MatchReport.of(reportGame, match).getHeadline());
            content.append("\n");
        }
        content.append(homeClub.getName());
        content.append(" ");
        content.append(match.getHomeGoals());
        content.append(" - ");
        content.append(match.getAwayGoals());
        content.append(" ");
        content.append(awayClub.getName());
        content.append("\n\n");

        if (match.getMatchDateTime() != null) {
            content.append(LanguageModLoader.format("msg_match_date", formatDate(match.getMatchDateTime())));
            content.append("\n");
        }

        if (match.getAttendance() != null) {
            content.append(LanguageModLoader.format("msg_match_attendance", match.getAttendance()));
            content.append("\n");
        }

        // Match revenue (v1.0)
        if (match.getMatchRevenue() != null && match.getMatchRevenue().compareTo(java.math.BigDecimal.ZERO) > 0) {
            content.append(LanguageModLoader.format("msg_match_revenue", Money.format(match.getMatchRevenue())));
            content.append("\n");
        }

        // Match type
        content.append(LanguageModLoader.format("msg_match_type", matchTypeName(match)));
        content.append("\n\n");

        // Result summary
        if (match.getHomeGoals() > match.getAwayGoals()) {
            content.append(LanguageModLoader.format("msg_match_won", homeClub.getName()));
        } else if (match.getAwayGoals() > match.getHomeGoals()) {
            content.append(LanguageModLoader.format("msg_match_won", awayClub.getName()));
        } else {
            content.append(LanguageModLoader.getValue("msg_match_drawn"));
        }

        message.setActionScreen(com.rndmodgames.futtoboru.menu.MainMenuManager.MATCH_RESULT_SCREEN);
        message.setActionData(match.getId());
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
        message.setTitle(LanguageModLoader.format("msg_match_preview_title", homeClub.getName(), awayClub.getName()));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("msg_match_preview_text", formatDate(match.getMatchDateTime())));
        content.append("\n\n");

        content.append(LanguageModLoader.getValue("msg_match_details")).append("\n");
        content.append(LanguageModLoader.format("msg_match_home", homeClub.getName()));
        content.append("\n");
        content.append(LanguageModLoader.format("msg_match_away", awayClub.getName()));
        content.append("\n");

        // P2-14: the ground played at (a neutral venue, else the home club's)
        com.rndmodgames.futtoboru.data.Stadium ground = com.rndmodgames.futtoboru.engine.cup.NeutralVenues.stadiumOf(
                match, getCurrentGame());
        if (ground == null) {
            ground = homeClub.getStadium();
        }
        if (ground != null) {
            content.append(LanguageModLoader.format("msg_match_venue", ground.getName()));
            content.append("\n");
        }

        // Match type
        content.append(LanguageModLoader.format("msg_match_type", matchTypeName(match)));
        
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
        message.setTitle(title != null ? title : LanguageModLoader.getValue("msg_fa_announcement_title"));
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
        message.setTitle(LanguageModLoader.getValue("msg_important_dates_title"));

        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.getValue("msg_important_dates_text")).append("\n\n");
        content.append(LanguageModLoader.format("msg_important_dates_start", formatDate(seasonStart)));
        content.append("\n");
        content.append(LanguageModLoader.format("msg_important_dates_end", formatDate(seasonEnd)));
        content.append("\n\n");
        content.append(LanguageModLoader.getValue("msg_important_dates_closing"));
        
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
            return LanguageModLoader.getValue("msg_tbd");
        }

        // Format: "8 September 1888" (msg_date orders day, month and year per language)
        return formatDay(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    /** "8 September 1888" in the current language (month names from the bundles). */
    public static String formatDay(int day, int month, int year) {
        return LanguageModLoader.format("msg_date", day, monthName(month), year);
    }

    /** The month's name, 1 = January. */
    public static String monthName(int month) {
        switch (month) {
        case 1: return LanguageModLoader.getValue("msg_month_january");
        case 2: return LanguageModLoader.getValue("msg_month_february");
        case 3: return LanguageModLoader.getValue("msg_month_march");
        case 4: return LanguageModLoader.getValue("msg_month_april");
        case 5: return LanguageModLoader.getValue("msg_month_may");
        case 6: return LanguageModLoader.getValue("msg_month_june");
        case 7: return LanguageModLoader.getValue("msg_month_july");
        case 8: return LanguageModLoader.getValue("msg_month_august");
        case 9: return LanguageModLoader.getValue("msg_month_september");
        case 10: return LanguageModLoader.getValue("msg_month_october");
        case 11: return LanguageModLoader.getValue("msg_month_november");
        default: return LanguageModLoader.getValue("msg_month_december");
        }
    }

    /** "League Match", "Cup Match" or "Friendly" in the current language. */
    private static String matchTypeName(com.rndmodgames.futtoboru.data.Match match) {
        if (match.getMatchType() != null) {
            if (match.getMatchType() == com.rndmodgames.futtoboru.data.Match.LEAGUE_MATCH) {
                return LanguageModLoader.getValue("msg_match_type_league");
            } else if (match.getMatchType() == com.rndmodgames.futtoboru.data.Match.CUP_MATCH) {
                return LanguageModLoader.getValue("msg_match_type_cup");
            }
        }
        return LanguageModLoader.getValue("msg_match_type_friendly");
    }
}

