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
        if (currentFilter == null) {
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
                System.out.println("InboxScreenTable: Filter changed to ALL");
                updateDynamicComponents();
            }
        });
        filtersTable.add(allButton).width(120).pad(2).row();
        
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
        VisTextButton titleButton = new VisTextButton(message.getTitle() != null ? message.getTitle() : "No Title");
        if (message == selectedMessage) {
            // Highlight selected by changing color
            titleButton.setColor(0.5f, 0.8f, 1.0f, 1.0f); // Light blue for selected
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
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                message.setIsRead(!(message.getIsRead() != null && message.getIsRead()));
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
