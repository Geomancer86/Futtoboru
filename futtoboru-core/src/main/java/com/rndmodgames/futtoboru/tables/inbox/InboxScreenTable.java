package com.rndmodgames.futtoboru.tables.inbox;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

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
    private com.rndmodgames.futtoboru.menu.MainMenuManager menuManager = null;
    
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
    private DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.MEDIUM); // U-12: the game language
    
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
        messagesScrollPane.setScrollingDisabled(true, false); // U-12: rows fit the column (long titles end in "..."), dates stay visible
        
        messageDetailScrollPane = new VisScrollPane(messageDetailTable);
        messageDetailScrollPane.setFadeScrollBars(false);
        
        // Load screen
        updateDynamicComponents();
    }
    
    public void setMenuManager(com.rndmodgames.futtoboru.menu.MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    /**
     * Select a message by message type (e.g., "LEAGUE_DRAW")
     * This is used when navigating from the main button to show a specific message
     * 
     * @param messageType The message type to select (e.g., "LEAGUE_DRAW")
     */
    public void selectMessageByType(String messageType) {
        if (messageType == null || currentGame == null || currentGame.getAllMessages() == null) {
            Log.info(Log.UI_INBOX, "InboxScreenTable: Cannot select message - messageType or currentGame is null");
            return;
        }
        
        Log.info(Log.UI_INBOX, "InboxScreenTable: Selecting message by type: " + messageType);
        
        // Find the first unread message of this type
        for (Message message : currentGame.getAllMessages()) {
            if (message != null && 
                message.getMessageType() != null && 
                message.getMessageType().equals(messageType) &&
                (message.getIsRead() == null || !message.getIsRead()) &&
                (message.getIsDeleted() == null || !message.getIsDeleted()) &&
                isMessageRelevantToPlayer(message)) {
                
                selectedMessage = message;
                Log.info(Log.UI_INBOX, "InboxScreenTable: Selected message: " + message.getTitle() + " (ID: " + message.getId() + ")");
                
                // Refresh display to show selected message
                updateDynamicComponents();
                return;
            }
        }
        
        Log.info(Log.UI_INBOX, "InboxScreenTable: No unread message found with type: " + messageType);
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
        filtersTable.add(new VisLabel(LanguageModLoader.getValue("inbox_filters"))).row();
        filtersTable.add().height(UiTheme.space("sm")).row(); // Spacing
        
        // All messages button
        VisTextButton allButton = new VisTextButton(LanguageModLoader.getValue("inbox_all"));
        if (currentFilter == null && !unreadFilter) {
            // Highlight selected
            allButton.setColor(UiTheme.color("selection")); // Light blue for selected
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
                Log.info(Log.UI_INBOX, "InboxScreenTable: Filter changed to ALL");
                updateDynamicComponents();
            }
        });
        filtersTable.add(allButton).width(UiTheme.size("col.long")).pad(UiTheme.space("xxs")).row();
        
        // Unread filter button
        VisTextButton unreadButton = new VisTextButton(LanguageModLoader.getValue("inbox_unread"));
        if (unreadFilter) {
            // Highlight selected
            unreadButton.setColor(UiTheme.color("selection")); // Light blue for selected
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
                Log.info(Log.UI_INBOX, "InboxScreenTable: Filter changed to UNREAD");
                updateDynamicComponents();
            }
        });
        filtersTable.add(unreadButton).width(UiTheme.size("col.long")).pad(UiTheme.space("xxs")).row();
        
        filtersTable.add().height(UiTheme.space("sm")).row(); // Spacing
        
        // Category filter buttons
        for (MessageCategory category : MessageCategory.values()) {
            VisTextButton categoryButton = new VisTextButton(categoryName(category));
            if (currentFilter == category) {
                // Highlight selected by changing color
                categoryButton.setColor(UiTheme.color("selection")); // Light blue for selected
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
                    Log.info(Log.UI_INBOX, "InboxScreenTable: Filter changed to " + category.name());
                    updateDynamicComponents();
                }
            });
            filtersTable.add(categoryButton).width(UiTheme.size("col.long")).pad(UiTheme.space("xxs")).row();
        }
        
        // Unread count
        int unreadCount = getUnreadCount();
        if (unreadCount > 0) {
            filtersTable.add().height(UiTheme.space("sm")).row();
            filtersTable.add(new VisLabel(LanguageModLoader.format("inbox_unread_count", unreadCount))).row();
        }
        
        // ========================================
        // MIDDLE PANEL: Message List
        // ========================================
        messagesListTable.add(new VisLabel(LanguageModLoader.format("inbox_messages_count", messagesToDisplay.size()))).row();
        messagesListTable.add().height(UiTheme.space("xs")).row();
        
        if (messagesToDisplay.isEmpty()) {
            messagesListTable.add(new VisLabel(LanguageModLoader.getValue("inbox_no_messages"))).row();
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
            messageDetailTable.add(new VisLabel(LanguageModLoader.getValue("inbox_select_message"))).row();
        }
        
        // ========================================
        // LAYOUT: Three-column layout
        // ========================================
        this.row();
        filtersTable.top(); // U-12: the filters start at the top like the list beside them
        this.add(filtersTable).width(UiTheme.size("col.label")).fillY().top();
        this.add(messagesScrollPane).width(UiTheme.size("panel.list")).fillY();
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
            indicatorLabel.setColor(UiTheme.color("indicator.unread")); // Blue for unread
        } else {
            indicatorLabel.setColor(UiTheme.color("text.muted")); // Gray for read
        }
        messageRow.add(indicatorLabel).width(UiTheme.size("indicator"));
        
        // Title (clickable)
        String titleText = message.getTitle() != null ? message.getTitle() : LanguageModLoader.getValue("inbox_no_title");
        VisTextButton titleButton = new VisTextButton(titleText);
        if (message == selectedMessage) {
            // Highlight selected by changing color
            titleButton.setColor(UiTheme.color("selection")); // Light blue for selected
        } else if (message.getIsRead() == null || !message.getIsRead()) {
            // Unread messages: make text bold/darker
            titleButton.setColor(UiTheme.color("text.unread")); // Lighter color for unread
        } else {
            // Read messages: normal color
            titleButton.setColor(UiTheme.color("text.read")); // Gray for read
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
        titleButton.getLabel().setEllipsis(true);
        titleButton.getLabelCell().minWidth(UiTheme.space("none")); // a Label's min width is its whole text
        messageRow.add(titleButton).growX().minWidth(UiTheme.space("none")); // may shrink: the label ends in "..."
        
        // Date
        String dateStr = LanguageModLoader.getValue("inbox_no_date");
        if (message.getMessageTime() != null) {
            dateStr = message.getMessageTime().format(dateFormatter);
        }
        messageRow.add(new VisLabel(dateStr)).width(UiTheme.size("col.number"));
        
        // Category badge
        if (message.getCategory() != null) {
            VisLabel categoryLabel = new VisLabel(categoryName(message.getCategory()));
            categoryLabel.setColor(getCategoryColor(message.getCategory()));
            messageRow.add(categoryLabel).width(UiTheme.size("col.number"));
        }
        
        messagesListTable.add(messageRow).growX().pad(UiTheme.space("xxs")).row();
    }
    
    /**
     * Helper method to set selected league from message actionData if navigating to draw screen
     * Tries multiple methods: actionData (Long ID), title extraction, content extraction
     */
    private void setSelectedLeagueFromMessageIfNeeded(Message message) {
        if (message == null || menuManager == null) {
            return;
        }
        
        // Check if navigating to draw screen
        if (message.getActionScreen() != null && 
            message.getActionScreen() == GameScreenId.LEAGUE_DRAW) {
            
            com.rndmodgames.futtoboru.data.League league = null;
            
            // Method 1: Try to get league from actionData (preferred method)
            if (message.getActionData() instanceof Long) {
                Long leagueId = (Long) message.getActionData();
                Log.info(Log.UI_INBOX, "InboxScreenTable: Attempting to find league by ID from actionData: " + leagueId);
                
                if (currentGame != null && currentGame.getMainAuthority() != null && 
                    currentGame.getMainAuthority().getLeagues() != null) {
                    for (com.rndmodgames.futtoboru.data.League l : currentGame.getMainAuthority().getLeagues()) {
                        if (l != null && l.getId() != null && l.getId().equals(leagueId)) {
                            league = l;
                            Log.info(Log.UI_INBOX, "InboxScreenTable: Found league by ID: " + league.getName());
                            break;
                        }
                    }
                }
                
                if (league == null) {
                    Log.info(Log.UI_INBOX, "InboxScreenTable: WARNING - Could not find league with ID: " + leagueId);
                }
            } else {
                Log.info(Log.UI_INBOX, "InboxScreenTable: actionData is not a Long, trying to extract league name from title/content");
            }
            
            // Method 2: Extract league name from message title (fallback)
            // Title format: "League Fixture Draw - {League Name}"
            if (league == null && message.getTitle() != null) {
                String title = message.getTitle();
                if (title.contains(" - ")) {
                    String leagueName = title.substring(title.lastIndexOf(" - ") + 3).trim();
                    Log.info(Log.UI_INBOX, "InboxScreenTable: Attempting to find league by name from title: " + leagueName);
                    
                    if (currentGame != null && currentGame.getMainAuthority() != null && 
                        currentGame.getMainAuthority().getLeagues() != null) {
                        for (com.rndmodgames.futtoboru.data.League l : currentGame.getMainAuthority().getLeagues()) {
                            if (l != null && l.getName() != null && l.getName().equals(leagueName)) {
                                league = l;
                                Log.info(Log.UI_INBOX, "InboxScreenTable: Found league by name from title: " + league.getName() + " (ID: " + league.getId() + ")");
                                break;
                            }
                        }
                        
                        // If exact match failed, try partial match
                        if (league == null) {
                            Log.info(Log.UI_INBOX, "InboxScreenTable: Exact name match failed, trying partial match");
                            for (com.rndmodgames.futtoboru.data.League l : currentGame.getMainAuthority().getLeagues()) {
                                if (l != null && l.getName() != null && l.getName().contains(leagueName)) {
                                    league = l;
                                    Log.info(Log.UI_INBOX, "InboxScreenTable: Found league by partial name match: " + league.getName() + " (ID: " + league.getId() + ")");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            // Method 3: Try to extract from message content as last resort
            if (league == null && message.getPlainTextMessage() != null) {
                String content = message.getPlainTextMessage();
                // Content format: "The fixture draw for the {League Name} is ready..."
                if (content.contains("fixture draw for the ")) {
                    int startIdx = content.indexOf("fixture draw for the ") + "fixture draw for the ".length();
                    int endIdx = content.indexOf(" is", startIdx);
                    if (endIdx > startIdx) {
                        String leagueName = content.substring(startIdx, endIdx).trim();
                        Log.info(Log.UI_INBOX, "InboxScreenTable: Attempting to find league by name from content: " + leagueName);
                        
                        if (currentGame != null && currentGame.getMainAuthority() != null && 
                            currentGame.getMainAuthority().getLeagues() != null) {
                            for (com.rndmodgames.futtoboru.data.League l : currentGame.getMainAuthority().getLeagues()) {
                                if (l != null && l.getName() != null && l.getName().equals(leagueName)) {
                                    league = l;
                                    Log.info(Log.UI_INBOX, "InboxScreenTable: Found league by name from content: " + league.getName() + " (ID: " + league.getId() + ")");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            // Set the league if found
            if (league != null) {
                menuManager.setSelectedLeague(league);
            } else {
                Log.info(Log.UI_INBOX, "InboxScreenTable: ERROR - Could not recover league from message (title: " + 
                                 (message.getTitle() != null ? message.getTitle() : "null") + ")");
            }
        }
    }
    
    /**
     * Display message detail in right panel
     */
    /** The category's label (inbox filter button and badge). */
    private static String categoryName(MessageCategory category) {
        switch (category) {
        case LEAGUE:
            return LanguageModLoader.getValue("inbox_category_league");
        case CUP:
            return LanguageModLoader.getValue("inbox_category_cup");
        case AUTHORITY:
            return LanguageModLoader.getValue("inbox_category_authority");
        case MATCH:
            return LanguageModLoader.getValue("inbox_category_match");
        case JOB:
            return LanguageModLoader.getValue("inbox_category_job");
        default:
            return LanguageModLoader.getValue("inbox_category_system");
        }
    }

    private static String priorityName(com.rndmodgames.futtoboru.data.MessagePriority priority) {
        switch (priority) {
        case URGENT:
            return LanguageModLoader.getValue("inbox_priority_urgent");
        case LOW:
            return LanguageModLoader.getValue("inbox_priority_low");
        default:
            return LanguageModLoader.getValue("inbox_priority_normal");
        }
    }

    private void displayMessageDetail(Message message) {
        messageDetailTable.clear();
        
        // Title
        messageDetailTable.add(new VisLabel(LanguageModLoader.getValue("inbox_title"))).row();
        VisLabel titleLabel = new VisLabel(message.getTitle() != null ? message.getTitle() : LanguageModLoader.getValue("inbox_no_title"));
        titleLabel.setWrap(true);
        messageDetailTable.add(titleLabel).growX().pad(UiTheme.space("xs")).row();
        
        messageDetailTable.add().height(UiTheme.space("sm")).row();
        
        // Date
        if (message.getMessageTime() != null) {
            messageDetailTable.add(new VisLabel(LanguageModLoader.format("inbox_date", message.getMessageTime().format(dateFormatter)))).row();
        }
        
        // Category
        if (message.getCategory() != null) {
            VisLabel categoryLabel = new VisLabel(LanguageModLoader.format("inbox_category", categoryName(message.getCategory())));
            categoryLabel.setColor(getCategoryColor(message.getCategory()));
            messageDetailTable.add(categoryLabel).row();
        }
        
        // Priority
        if (message.getPriority() != null) {
            messageDetailTable.add(new VisLabel(LanguageModLoader.format("inbox_priority", priorityName(message.getPriority())))).row();
        }
        
        messageDetailTable.add().height(UiTheme.space("sm")).row();
        
        // Content
        messageDetailTable.add(new VisLabel(LanguageModLoader.getValue("inbox_message"))).row();
        VisLabel contentLabel = new VisLabel(message.getPlainTextMessage() != null ? message.getPlainTextMessage() : LanguageModLoader.getValue("inbox_no_content"));
        contentLabel.setWrap(true);
        messageDetailTable.add(contentLabel).growX().pad(UiTheme.space("xs")).row();
        
        // Action button (if applicable)
        if (message.getActionScreen() != null) {
            messageDetailTable.add().height(UiTheme.space("sm")).row();
            
            String buttonText = LanguageModLoader.getValue("inbox_view_draw");
            if (message.getMessageType() != null && message.getMessageType().equals("LEAGUE_DRAW")) {
                buttonText = LanguageModLoader.getValue("inbox_view_league_draw");
            } else if ("MATCH_RESULT".equals(message.getMessageType())) {
                buttonText = LanguageModLoader.getValue("inbox_read_report"); // G-03
            } else {
                buttonText = LanguageModLoader.getValue("inbox_view_related");
            }
            
            VisTextButton actionButton = new VisTextButton(buttonText);
            actionButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    Log.info(Log.UI_INBOX, "InboxScreenTable: Action button clicked for message: " + message.getTitle());
                    Log.info(Log.UI_INBOX, "Action Screen: " + message.getActionScreen());
                    Log.info(Log.UI_INBOX, "Action Data: " + message.getActionData());
                    
                    // Navigate to action screen
                    if (message.getActionScreen() != null) {
                        // Handle LEAGUE_DRAW messages specifically
                        if (message.getMessageType() != null && message.getMessageType().equals("LEAGUE_DRAW")) {
                            Log.info(Log.UI_INBOX, "InboxScreenTable: Handling LEAGUE_DRAW message action");
                            
                            // Use the helper method to set selected league (handles actionData, title, and content extraction)
                            setSelectedLeagueFromMessageIfNeeded(message);
                            
                            // Navigate to draw screen
                            if (menuManager != null) {
                                menuManager.setActiveMainScreen(message.getActionScreen());
                            } else {
                                Log.info(Log.UI_INBOX, "InboxScreenTable: ERROR - menuManager is null");
                            }
                        } else if ("MATCH_RESULT".equals(message.getMessageType())) {
                            // G-03: the report of this message's match (actionData = match id)
                            if (menuManager != null && message.getActionData() instanceof Number) {
                                menuManager.showMatchResult(menuManager.findPlayedMatch(
                                        ((Number) message.getActionData()).longValue()));
                            }
                        } else {
                            // Generic action for other message types
                            Log.info(Log.UI_INBOX, "InboxScreenTable: Generic action screen navigation");
                            if (menuManager != null) {
                                setSelectedLeagueFromMessageIfNeeded(message);
                                menuManager.setActiveMainScreen(message.getActionScreen());
                            }
                        }
                    } else {
                        Log.info(Log.UI_INBOX, "InboxScreenTable: ERROR - message.getActionScreen() is null");
                    }
                }
            });
            messageDetailTable.add(actionButton).pad(UiTheme.space("xs")).row();
        }
        
        // Mark as read/unread button
        messageDetailTable.add().height(UiTheme.space("sm")).row();
        VisTextButton readButton = new VisTextButton(
            (message.getIsRead() != null && message.getIsRead()) ? LanguageModLoader.getValue("inbox_mark_unread") : LanguageModLoader.getValue("inbox_mark_read")
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
                Log.info(Log.UI_INBOX, "InboxScreenTable: Message '" + message.getTitle() + "' marked as " + (!currentReadStatus ? "read" : "unread"));
                updateDynamicComponents();
            }
        });
        messageDetailTable.add(readButton).pad(UiTheme.space("xs")).row();
    }
    
    /**
     * Get filtered messages based on current filter
     */
    private List<Message> getFilteredMessages() {
        if (currentGame == null) {
            Log.info(Log.UI_INBOX, "InboxScreenTable: currentGame is null");
            return new ArrayList<>();
        }
        
        if (currentGame.getAllMessages() == null) {
            Log.info(Log.UI_INBOX, "InboxScreenTable: getAllMessages() is null");
            return new ArrayList<>();
        }
        
        List<Message> allMessages = currentGame.getAllMessages();
        Log.info(Log.UI_INBOX, "InboxScreenTable: Total messages in SaveGame: " + allMessages.size());
        
        List<Message> filtered = new ArrayList<>();
        
        for (Message message : allMessages) {
            if (message == null) {
                Log.info(Log.UI_INBOX, "InboxScreenTable: Found null message in list");
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
                    Log.info(Log.UI_INBOX, "InboxScreenTable: Message '" + message.getTitle() + "' has no category, skipping (filter: " + currentFilter.name() + ")");
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
        
        Log.info(Log.UI_INBOX, "InboxScreenTable: Filtered messages count: " + filtered.size());
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
        
        // League-wide messages (creation, fixture release, draw) are relevant to all
        if (message.getMessageType() != null) {
            if (message.getMessageType().equals("LEAGUE_CREATION") || 
                message.getMessageType().equals("FIXTURE_RELEASE") ||
                message.getMessageType().equals("LEAGUE_DRAW")) {
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
            // LEAGUE_WELCOME messages are club-specific, so they should have been handled above
            // If we get here and it's a LEAGUE_WELCOME, it means it's not for this player's club
            if (message.getMessageType() != null && message.getMessageType().equals("LEAGUE_WELCOME")) {
                return false; // LEAGUE_WELCOME is club-specific, not league-wide
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
            return UiTheme.color("text.primary");
        }
        
        switch (category) {
            case LEAGUE:
                return UiTheme.color("category.league");
            case CUP:
                return UiTheme.color("category.cup");
            case AUTHORITY:
                return UiTheme.color("category.authority");
            case MATCH:
                return UiTheme.color("category.match");
            case JOB:
                return UiTheme.color("category.job");
            case SYSTEM:
            default:
                return UiTheme.color("category.system");
        }
    }
}
