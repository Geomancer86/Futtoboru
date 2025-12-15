package com.rndmodgames.futtoboru.tables.inbox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Inbox Screen Table v2
 * 
 * Enhanced inbox system with message list, detail view, and category filtering.
 * 
 * @author Geomancer86
 */
public class InboxScreenTable extends VisTable {

    //
    Game game;
    SaveGame currentGame;
    
    // Selected message
    private Message selectedMessage = null;
    
    // Current filter
    private MessageCategory currentFilter = null; // null = all messages
    private boolean unreadFilter = false; // true = only unread messages
    
    // Layout components
    private VisTable filtersTable = null;
    private VisTable messagesListTable = null;
    private VisScrollPane messagesScrollPane = null;
    private VisTable messageDetailTable = null;
    private VisScrollPane messageDetailScrollPane = null;
    
    // Date formatter
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    
    public InboxScreenTable(Game parent) {
        
        // auto margins
        super(true);
        
        //
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        
        // Initialize layout components
        filtersTable = new VisTable(true);
        messagesListTable = new VisTable(true);
        messageDetailTable = new VisTable(true);
        
        // Wrap in scroll panes
        messagesScrollPane = new VisScrollPane(messagesListTable);
        messagesScrollPane.setFadeScrollBars(false);
        
        messageDetailScrollPane = new VisScrollPane(messageDetailTable);
        messageDetailScrollPane.setFadeScrollBars(false);
        
        // Load screen
        updateDynamicComponents();
    }
    
    /**
     * Update inbox display with current messages
     */
    public void updateDynamicComponents() {
        
        // Refresh currentGame reference to avoid stale references
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        
        // Clear all tables
        this.clear();
        filtersTable.clear();
        messagesListTable.clear();
        messageDetailTable.clear();
        
        // Get messages to display
        List<Message> messagesToDisplay = getFilteredMessages();
        
        // ========================================
        // LEFT PANEL: Filters
        // ========================================
        filtersTable.add(new VisLabel("Filters")).row();
        filtersTable.add().height(10).row(); // Spacing
        
        // All messages button
        VisTextButton allButton = new VisTextButton("All");
        if (currentFilter == null && !unreadFilter) {
            // Highlight selected
            allButton.setColor(0.5f, 0.8f, 1.0f, 1.0f); // Light blue for selected
        }
        allButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                currentFilter = null;
                unreadFilter = false;
                System.out.println("InboxScreenTable: Filter changed to ALL");
                updateDynamicComponents();
            }
        });
        filtersTable.add(allButton).width(120).pad(2).row();
        
        // Unread filter button
        VisTextButton unreadButton = new VisTextButton("Unread");
        if (unreadFilter) {
            // Highlight selected
            unreadButton.setColor(0.5f, 0.8f, 1.0f, 1.0f); // Light blue for selected
        }
        unreadButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                unreadFilter = true;
                currentFilter = null; // Clear category filter when using unread filter
                System.out.println("InboxScreenTable: Filter changed to UNREAD");
                updateDynamicComponents();
            }
        });
        filtersTable.add(unreadButton).width(120).pad(2).row();
        
        filtersTable.add().height(10).row(); // Spacing
        
        // Category filter buttons
        for (MessageCategory category : MessageCategory.values()) {
            VisTextButton categoryButton = new VisTextButton(category.name());
            if (currentFilter == category) {
                // Highlight selected by changing color
                categoryButton.setColor(0.5f, 0.8f, 1.0f, 1.0f); // Light blue for selected
            }
            categoryButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    currentFilter = category;
                    unreadFilter = false; // Clear unread filter when using category filter
                    System.out.println("InboxScreenTable: Filter changed to " + category.name());
                    updateDynamicComponents();
                }
            });
            filtersTable.add(categoryButton).width(120).pad(2).row();
        }
        
        // Unread count
        int unreadCount = getUnreadCount();
        if (unreadCount > 0) {
            filtersTable.add().height(10).row();
            filtersTable.add(new VisLabel("Unread: " + unreadCount)).row();
        }
        
        // ========================================
        // MIDDLE PANEL: Message List
        // ========================================
        messagesListTable.add(new VisLabel("Messages (" + messagesToDisplay.size() + ")")).row();
        messagesListTable.add().height(5).row();
        
        if (messagesToDisplay.isEmpty()) {
            messagesListTable.add(new VisLabel("No messages")).row();
        } else {
            // Sort messages by date (newest first)
            List<Message> sortedMessages = new ArrayList<>(messagesToDisplay);
            Collections.sort(sortedMessages, new Comparator<Message>() {
                @Override
                public int compare(Message m1, Message m2) {
                    if (m1.getMessageTime() == null && m2.getMessageTime() == null) return 0;
                    if (m1.getMessageTime() == null) return 1;
                    if (m2.getMessageTime() == null) return -1;
                    return m2.getMessageTime().compareTo(m1.getMessageTime()); // Descending (newest first)
                }
            });
            
            // Display each message
            for (Message message : sortedMessages) {
                addMessageToList(message);
            }
        }
        
        // ========================================
        // RIGHT PANEL: Message Detail
        // ========================================
        if (selectedMessage != null) {
            displayMessageDetail(selectedMessage);
        } else {
            messageDetailTable.add(new VisLabel("Select a message to view details")).row();
        }
        
        // ========================================
        // LAYOUT: Three-column layout
        // ========================================
        this.row();
        this.add(filtersTable).width(150).fillY();
        this.add(messagesScrollPane).width(400).fillY();
        this.add(messageDetailScrollPane).grow().fill();
    }
    
    /**
     * Add a message to the message list
     */
    private void addMessageToList(Message message) {
        VisTable messageRow = new VisTable(true);
        
        // Unread indicator
        String indicator = (message.getIsRead() == null || !message.getIsRead()) ? "●" : "○";
        VisLabel indicatorLabel = new VisLabel(indicator);
        if (message.getIsRead() == null || !message.getIsRead()) {
            indicatorLabel.setColor(0.2f, 0.6f, 1.0f, 1.0f); // Blue for unread
        } else {
            indicatorLabel.setColor(0.5f, 0.5f, 0.5f, 1.0f); // Gray for read
        }
        messageRow.add(indicatorLabel).width(20);
        
        // Title (clickable)
        String titleText = message.getTitle() != null ? message.getTitle() : "No Title";
        VisTextButton titleButton = new VisTextButton(titleText);
        if (message == selectedMessage) {
            // Highlight selected by changing color
            titleButton.setColor(0.5f, 0.8f, 1.0f, 1.0f); // Light blue for selected
        } else if (message.getIsRead() == null || !message.getIsRead()) {
            // Unread messages: make text bold/darker
            titleButton.setColor(0.9f, 0.9f, 0.9f, 1.0f); // Lighter color for unread
        } else {
            // Read messages: normal color
            titleButton.setColor(0.6f, 0.6f, 0.6f, 1.0f); // Gray for read
        }
        titleButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                selectedMessage = message;
                // Mark as read when clicked
                if (message.getIsRead() == null || !message.getIsRead()) {
                    message.setIsRead(true);
                }
                updateDynamicComponents();
            }
        });
        messageRow.add(titleButton).growX();
        
        // Date
        String dateStr = "No date";
        if (message.getMessageTime() != null) {
            dateStr = message.getMessageTime().format(dateFormatter);
        }
        messageRow.add(new VisLabel(dateStr)).width(100);
        
        // Category badge
        if (message.getCategory() != null) {
            VisLabel categoryLabel = new VisLabel(message.getCategory().name());
            categoryLabel.setColor(getCategoryColor(message.getCategory()));
            messageRow.add(categoryLabel).width(100);
        }
        
        messagesListTable.add(messageRow).growX().pad(2).row();
    }
    
    /**
     * Display message detail in right panel
     */
    private void displayMessageDetail(Message message) {
        messageDetailTable.clear();
        
        // Title
        messageDetailTable.add(new VisLabel("Title:")).row();
        VisLabel titleLabel = new VisLabel(message.getTitle() != null ? message.getTitle() : "No Title");
        titleLabel.setWrap(true);
        messageDetailTable.add(titleLabel).growX().pad(5).row();
        
        messageDetailTable.add().height(10).row();
        
        // Date
        if (message.getMessageTime() != null) {
            messageDetailTable.add(new VisLabel("Date: " + message.getMessageTime().format(dateFormatter))).row();
        }
        
        // Category
        if (message.getCategory() != null) {
            VisLabel categoryLabel = new VisLabel("Category: " + message.getCategory().name());
            categoryLabel.setColor(getCategoryColor(message.getCategory()));
            messageDetailTable.add(categoryLabel).row();
        }
        
        // Priority
        if (message.getPriority() != null) {
            messageDetailTable.add(new VisLabel("Priority: " + message.getPriority().name())).row();
        }
        
        messageDetailTable.add().height(10).row();
        
        // Content
        messageDetailTable.add(new VisLabel("Message:")).row();
        VisLabel contentLabel = new VisLabel(message.getPlainTextMessage() != null ? message.getPlainTextMessage() : "No content");
        contentLabel.setWrap(true);
        messageDetailTable.add(contentLabel).growX().pad(5).row();
        
        // Action button (if applicable)
        if (message.getActionScreen() != null) {
            messageDetailTable.add().height(10).row();
            VisTextButton actionButton = new VisTextButton("View Related Screen");
            // TODO: Implement action button navigation
            messageDetailTable.add(actionButton).pad(5).row();
        }
        
        // Mark as read/unread button
        messageDetailTable.add().height(10).row();
        VisTextButton readButton = new VisTextButton(
            (message.getIsRead() != null && message.getIsRead()) ? "Mark as Unread" : "Mark as Read"
        );
        readButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // Toggle read status
                boolean currentReadStatus = (message.getIsRead() != null && message.getIsRead());
                message.setIsRead(!currentReadStatus);
                System.out.println("InboxScreenTable: Message '" + message.getTitle() + "' marked as " + (!currentReadStatus ? "read" : "unread"));
                updateDynamicComponents();
            }
        });
        messageDetailTable.add(readButton).pad(5).row();
    }
    
    /**
     * Get filtered messages based on current filter
     */
    private List<Message> getFilteredMessages() {
        if (currentGame == null) {
            System.out.println("InboxScreenTable: currentGame is null");
            return new ArrayList<>();
        }
        
        if (currentGame.getAllMessages() == null) {
            System.out.println("InboxScreenTable: getAllMessages() is null");
            return new ArrayList<>();
        }
        
        List<Message> allMessages = currentGame.getAllMessages();
        System.out.println("InboxScreenTable: Total messages in SaveGame: " + allMessages.size());
        
        List<Message> filtered = new ArrayList<>();
        
        for (Message message : allMessages) {
            if (message == null) {
                System.out.println("InboxScreenTable: Found null message in list");
                continue;
            }
            if (message.getIsDeleted() != null && message.getIsDeleted()) {
                continue;
            }
            
            // Filter by player's club (only show messages relevant to player)
            if (!isMessageRelevantToPlayer(message)) {
                continue; // Skip messages not relevant to player
            }
            
            // Apply category filter
            if (currentFilter != null) {
                if (message.getCategory() == null) {
                    // Message has no category, skip if filtering
                    System.out.println("InboxScreenTable: Message '" + message.getTitle() + "' has no category, skipping (filter: " + currentFilter.name() + ")");
                    continue;
                }
                if (message.getCategory() != currentFilter) {
                    // Category doesn't match filter
                    continue;
                }
            }
            
            // Apply unread filter
            if (unreadFilter) {
                if (message.getIsRead() != null && message.getIsRead()) {
                    continue; // Skip read messages when unread filter is active
                }
            }
            
            filtered.add(message);
        }
        
        System.out.println("InboxScreenTable: Filtered messages count: " + filtered.size());
        return filtered;
    }
    
    /**
     * Get unread message count
     */
    private int getUnreadCount() {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            return 0;
        }
        
        int count = 0;
        for (Message message : currentGame.getAllMessages()) {
            if (message != null && 
                (message.getIsRead() == null || !message.getIsRead()) &&
                (message.getIsDeleted() == null || !message.getIsDeleted())) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Check if message is relevant to the player
     * 
     * Messages are relevant if:
     * - They are system messages (category SYSTEM)
     * - They are league-wide announcements (LEAGUE_CREATION, FIXTURE_RELEASE)
     * - They are for the player's current club (if player has a club)
     * - They are for the player's owner (job offers, etc.)
     */
    private boolean isMessageRelevantToPlayer(Message message) {
        if (currentGame == null || message == null) {
            return false;
        }
        
        // System messages are always relevant
        if (message.getCategory() == MessageCategory.SYSTEM) {
            return true;
        }
        
        // League-wide messages (creation, fixture release) are relevant to all
        if (message.getMessageType() != null) {
            if (message.getMessageType().equals("LEAGUE_CREATION") || 
                message.getMessageType().equals("FIXTURE_RELEASE")) {
                return true;
            }
        }
        
        // Get player's current club
        com.rndmodgames.futtoboru.data.Club playerClub = currentGame.getCurrentClub();
        
        // If player has no club, only show system messages and league-wide messages
        if (playerClub == null) {
            // For unemployed players, show system messages and job-related messages
            return message.getCategory() == MessageCategory.SYSTEM || 
                   message.getCategory() == MessageCategory.JOB;
        }
        
        // Check if message is for player's club
        // Messages with actionData containing club ID, or messages with specific club context
        if (message.getActionData() != null) {
            // If actionData is a Long (club ID), check if it matches player's club
            if (message.getActionData() instanceof Long) {
                Long clubId = (Long) message.getActionData();
                if (playerClub.getId() != null && playerClub.getId().equals(clubId)) {
                    return true;
                }
            }
        }
        
        // League welcome messages: check if message content mentions player's club
        // This is a simple check - in the future, we should store club ID in message
        if (message.getMessageType() != null && message.getMessageType().equals("LEAGUE_WELCOME")) {
            // Check if message content contains player's club name
            if (message.getPlainTextMessage() != null && playerClub.getName() != null) {
                if (message.getPlainTextMessage().contains(playerClub.getName())) {
                    return true;
                }
            }
        }
        
        // Authority and league-wide messages are relevant
        if (message.getCategory() == MessageCategory.AUTHORITY || 
            message.getCategory() == MessageCategory.LEAGUE) {
            // Check if it's a league-wide message (not club-specific)
            if (message.getMessageType() == null || 
                message.getMessageType().equals("LEAGUE_CREATION") ||
                message.getMessageType().equals("FIXTURE_RELEASE")) {
                return true;
            }
        }
        
        // Match messages: check if they involve player's club
        if (message.getCategory() == MessageCategory.MATCH) {
            // Match messages should be relevant if they involve player's club
            // For now, show all match messages (we'll refine this later)
            return true;
        }
        
        // Job messages are always relevant
        if (message.getCategory() == MessageCategory.JOB) {
            return true;
        }
        
        // Default: show message if we can't determine relevance
        // This is safer than hiding potentially important messages
        return true;
    }
    
    /**
     * Get color for message category
     */
    private com.badlogic.gdx.graphics.Color getCategoryColor(MessageCategory category) {
        if (category == null) {
            return com.badlogic.gdx.graphics.Color.WHITE;
        }
        
        switch (category) {
            case LEAGUE:
                return com.badlogic.gdx.graphics.Color.CYAN;
            case CUP:
                return com.badlogic.gdx.graphics.Color.GOLD;
            case AUTHORITY:
                return com.badlogic.gdx.graphics.Color.ORANGE;
            case MATCH:
                return com.badlogic.gdx.graphics.Color.GREEN;
            case JOB:
                return com.badlogic.gdx.graphics.Color.MAGENTA;
            case SYSTEM:
            default:
                return com.badlogic.gdx.graphics.Color.WHITE;
        }
    }
}
