package com.rndmodgames.futtoboru.tables.draw;

import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

/**
 * League Draw Screen Table v1.0
 * 
 * Mandatory draw screen (Football Manager style):
 * - Blocks time advancement until draw is fully viewed
 * - Shows teams being drawn one by one (step-by-step reveal)
 * - "Next" button to reveal next fixture
 * - "Draw All" button to reveal all fixtures at once
 * - "Continue" button appears only after all fixtures are revealed
 * 
 * @author Geomancer86
 */
public class LeagueDrawScreenTable extends VisTable {

    private Futtoboru game;
    private SaveGame currentGame;
    private MainMenuManager menuManager;
    private League selectedLeague;
    
    // Draw state
    private List<Match> allFixtures;  // All fixtures for the league
    private int currentRevealedIndex = 0;  // How many fixtures have been revealed
    private boolean allRevealed = false;  // True when all fixtures are shown
    
    // UI components
    private VisScrollPane contentScrollPane;
    private VisTable contentTable;
    private VisTable fixturesTable;
    private VisTextButton nextButton;
    private VisTextButton drawAllButton;
    private VisTextButton continueButton;
    
    private DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.MEDIUM); // U-12: the game language
    
    public LeagueDrawScreenTable(Game parent) {
        super(true);
        
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
        
        // Create scrollable content table
        contentTable = new VisTable(true);
        contentTable.top(); // V-01: the draw starts under the top bar, not mid-screen
        contentScrollPane = new VisScrollPane(contentTable);
        contentScrollPane.setFadeScrollBars(false);
        
        // Add scroll pane to main table
        this.add(contentScrollPane).grow().fill();
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    public void setSelectedLeague(League league) {
        this.selectedLeague = league;
        collectFixtures();
        
        // Check if draw has already been completed (message no longer mandatory)
        // If so, show all fixtures immediately to prevent redrawing
        if (isDrawAlreadyCompleted()) {
            allRevealed = true;
            currentRevealedIndex = allFixtures != null ? allFixtures.size() : 0;
        } else {
            currentRevealedIndex = 0;
            allRevealed = false;
        }
        
        updateDynamicComponents();
    }
    
    /**
     * Check if the league draw message has already been completed
     * (i.e., marked as read and no longer mandatory)
     */
    private boolean isDrawAlreadyCompleted() {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            return false;
        }
        
        for (com.rndmodgames.futtoboru.data.Message message : currentGame.getAllMessages()) {
            if (message != null) {
                String messageType = message.getMessageType();
                boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                boolean isRead = message.getIsRead() != null && message.getIsRead();
                
                if (messageType != null && 
                    (messageType.equals("LEAGUE_DRAW") || messageType.equals("FIXTURE_DRAW")) &&
                    !isMandatory && isRead) {
                    // Draw has been completed
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Collect all fixtures for the league
     */
    private void collectFixtures() {
        allFixtures = new ArrayList<>();
        
        if (selectedLeague == null || selectedLeague.getLeagueClubs() == null) {
            return;
        }
        
        // Collect all league matches
        for (Club club : selectedLeague.getLeagueClubs()) {
            if (club != null && club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match != null && 
                        match.getMatchType() != null && 
                        match.getMatchType() == Match.LEAGUE_MATCH &&
                        !match.getIsPlayed()) {
                        
                        // Check if not already in list
                        boolean alreadyAdded = false;
                        for (Match existing : allFixtures) {
                            if (existing.getId() != null && existing.getId().equals(match.getId())) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            allFixtures.add(match);
                        }
                    }
                }
            }
        }
        
        // Sort by date
        Collections.sort(allFixtures, (m1, m2) -> {
            if (m1.getMatchDateTime() == null && m2.getMatchDateTime() == null) return 0;
            if (m1.getMatchDateTime() == null) return 1;
            if (m2.getMatchDateTime() == null) return -1;
            return m1.getMatchDateTime().compareTo(m2.getMatchDateTime());
        });
    }
    
    public void updateDynamicComponents() {
        contentTable.clear();
        
        if (selectedLeague == null) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("draw_no_league_selected"))).pad(UiTheme.space("lg")).row();
            return;
        }
        
        // Title
        VisLabel titleLabel = new VisLabel(selectedLeague.getName() != null ? LanguageModLoader.format("draw_league_title", selectedLeague.getName()) : LanguageModLoader.getValue("draw_fixture_draw"));
        titleLabel.setFontScale(UiTheme.fontScale("title"));
        contentTable.add(titleLabel).pad(UiTheme.space("sm")).row();
        
        contentTable.addSeparator().pad(UiTheme.space("xs")).row();
        
        // Instructions
        contentTable.add(new VisLabel(LanguageModLoader.format("draw_league_revealing", selectedLeague.getName()))).pad(UiTheme.space("xs")).row();
        contentTable.add(new VisLabel(LanguageModLoader.getValue("draw_league_hint"))).pad(UiTheme.space("xs")).row();
        
        contentTable.add().height(UiTheme.space("sm")).row();
        
        // Fixtures table
        fixturesTable = new VisTable(true);
        fixturesTable.add(new VisLabel(LanguageModLoader.getValue("history_date"))).width(UiTheme.size("col.long"));
        fixturesTable.add(new VisLabel(LanguageModLoader.getValue("history_home"))).width(UiTheme.size("col.wide"));
        fixturesTable.add(new VisLabel(LanguageModLoader.getValue("history_away"))).width(UiTheme.size("col.wide")).row();
        fixturesTable.addSeparator().colspan(3).row();
        
        // Show revealed fixtures
        int revealedCount = allRevealed ? allFixtures.size() : currentRevealedIndex;
        for (int i = 0; i < revealedCount && i < allFixtures.size(); i++) {
            Match match = allFixtures.get(i);
            Club homeClub = currentGame.getClubById(match.getHomeClubId());
            Club awayClub = currentGame.getClubById(match.getAwayClubId());
            
            if (homeClub != null && awayClub != null) {
                String dateStr = match.getMatchDateTime() != null ? 
                    dateFormatter.format(match.getMatchDateTime()) : LanguageModLoader.getValue("draw_tba");
                fixturesTable.add(new VisLabel(dateStr)).width(UiTheme.size("col.long"));
                fixturesTable.add(new VisLabel(homeClub.getName())).width(UiTheme.size("col.wide"));
                fixturesTable.add(new VisLabel(awayClub.getName())).width(UiTheme.size("col.wide")).row();
            }
        }
        
        // Show "..." for unrevealed fixtures
        if (!allRevealed && currentRevealedIndex < allFixtures.size()) {
            fixturesTable.add(new VisLabel("...")).colspan(3).pad(UiTheme.space("xs")).row();
            fixturesTable.add(new VisLabel(LanguageModLoader.format("draw_more_fixtures", allFixtures.size() - currentRevealedIndex))).colspan(3).pad(UiTheme.space("xs")).row();
        }
        
        contentTable.add(fixturesTable).growX().pad(UiTheme.space("sm")).row();
        
        contentTable.add().height(UiTheme.space("lg")).row();
        
        // Buttons
        VisTable buttonTable = new VisTable(true);
        
        // Next button (only show if not all revealed)
        if (!allRevealed && currentRevealedIndex < allFixtures.size()) {
            nextButton = new VisTextButton(LanguageModLoader.getValue("draw_next"));
            nextButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    // Reveal next fixture (or next 5 for faster progression)
                    currentRevealedIndex = Math.min(currentRevealedIndex + 5, allFixtures.size());
                    if (currentRevealedIndex >= allFixtures.size()) {
                        allRevealed = true;
                    }
                    updateDynamicComponents();
                }
            });
            buttonTable.add(nextButton).pad(UiTheme.space("xs"));
        }
        
        // Draw All button (only show if not all revealed)
        if (!allRevealed && currentRevealedIndex < allFixtures.size()) {
            drawAllButton = new VisTextButton(LanguageModLoader.getValue("draw_all"));
            drawAllButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    // Reveal all fixtures
                    allRevealed = true;
                    currentRevealedIndex = allFixtures.size();
                    updateDynamicComponents();
                }
            });
            buttonTable.add(drawAllButton).pad(UiTheme.space("xs"));
        }
        
        // Continue button (only show when all fixtures are revealed)
        if (allRevealed) {
            continueButton = new VisTextButton(LanguageModLoader.getValue("continue_game"));
            continueButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        // Mark draw message as read and non-mandatory
                    markDrawMessageAsComplete();
                    
                    // Return to inbox or home screen
                    if (menuManager != null) {
                        menuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                    }
                    
                    // Update button state via game engine
                    if (game != null && game.getGameEngine() != null) {
                        // Trigger button update by calling getNextGameAction
                        game.getGameEngine().getNextGameAction();
                    }
                }
            });
            buttonTable.add(continueButton).pad(UiTheme.space("xs"));
        }
        
        contentTable.add(buttonTable).pad(UiTheme.space("sm")).row();
    }
    
    /**
     * Mark the draw message as read and non-mandatory
     * This allows time advancement to continue
     */
    private void markDrawMessageAsComplete() {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: ERROR - currentGame or getAllMessages is null");
            return;
        }
        
        Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: Searching for draw message to mark as complete...");
        Log.info(Log.UI_DRAW, "Total messages: " + currentGame.getAllMessages().size());
        
        // Find the draw message and mark it as read and non-mandatory
        boolean found = false;
        for (com.rndmodgames.futtoboru.data.Message message : currentGame.getAllMessages()) {
            if (message != null) {
                String messageType = message.getMessageType();
                boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                
                Log.info(Log.UI_DRAW, "Checking message: Type=" + messageType + ", Mandatory=" + isMandatory + 
                                 ", Read=" + message.getIsRead() + ", Title=" + message.getTitle());
                
                if (messageType != null && 
                    (messageType.equals("LEAGUE_DRAW") || 
                     messageType.equals("FIXTURE_DRAW")) &&
                    isMandatory) {
                    
                    message.setIsRead(true);
                    message.setIsMandatory(false);  // No longer blocks time advancement
                    found = true;
                    
                    Log.info(Log.UI_DRAW, "========================================");
                    Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: Marked draw message as complete!");
                    Log.info(Log.UI_DRAW, "Message ID: " + message.getId());
                    Log.info(Log.UI_DRAW, "Message Title: " + message.getTitle());
                    Log.info(Log.UI_DRAW, "IsRead: " + message.getIsRead());
                    Log.info(Log.UI_DRAW, "IsMandatory: " + message.getIsMandatory());
                    Log.info(Log.UI_DRAW, "========================================");
                    
                    // Create post-draw message with league info
                    createPostDrawMessage(selectedLeague);
                    
                    // Update UI button state by triggering getNextGameAction check
                    if (game != null && game.getGameEngine() != null) {
                        // Force refresh of button state
                        int nextAction = game.getGameEngine().getNextGameAction();
                        Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: Next game action after marking complete: " + nextAction);
                    }
                    
                    // Update top menu button immediately
                    if (menuManager != null) {
                        // Get the top menu and refresh button
                        com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable topMenu = 
                            menuManager.getTopMenu();
                        if (topMenu != null) {
                            Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: Refreshing top menu button state");
                            topMenu.setMainContainerButton();
                        } else {
                            Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: WARNING - topMenu is null, button state may not refresh");
                        }
                    } else {
                        Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: WARNING - menuManager is null, button state may not refresh");
                    }
                    
                    break;
                }
            }
        }
        
        if (!found) {
            Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: WARNING - No mandatory draw message found to mark as complete!");
        }
    }
    
    /**
     * Create a post-draw message with league information and match count
     */
    private void createPostDrawMessage(League league) {
        if (league == null || game == null || game.getGameEngine() == null) {
            return;
        }
        
        com.rndmodgames.futtoboru.engine.messages.MessageManager messageManager = 
            game.getGameEngine().getMessageManager();
        
        if (messageManager == null) {
            return;
        }
        
        // Count total fixtures for this league
        int totalFixtures = 0;
        if (league.getLeagueClubs() != null) {
            for (com.rndmodgames.futtoboru.data.Club club : league.getLeagueClubs()) {
                if (club != null && club.getScheduledMatches() != null) {
                    for (com.rndmodgames.futtoboru.data.Match match : club.getScheduledMatches()) {
                        if (match != null && match.getMatchType() == com.rndmodgames.futtoboru.data.Match.LEAGUE_MATCH) {
                            totalFixtures++;
                        }
                    }
                }
            }
            // Each match appears twice (once for home, once for away club), so divide by 2
            totalFixtures = totalFixtures / 2;
        }
        
        com.rndmodgames.futtoboru.data.Message postDrawMessage = new com.rndmodgames.futtoboru.data.Message();
        postDrawMessage.setCategory(com.rndmodgames.futtoboru.data.MessageCategory.LEAGUE);
        postDrawMessage.setMessageType("LEAGUE_FIXTURES_RELEASED");
        postDrawMessage.setPriority(com.rndmodgames.futtoboru.data.MessagePriority.NORMAL);
        postDrawMessage.setTitle(LanguageModLoader.format("draw_msg_fixtures_released_title", league.getName()));
        postDrawMessage.setIsMandatory(false);
        
        StringBuilder content = new StringBuilder();
        content.append(LanguageModLoader.format("draw_msg_fixtures_released_text", league.getName(), totalFixtures,
                league.getLeagueClubs() != null ? String.valueOf(league.getLeagueClubs().size())
                        : LanguageModLoader.getValue("draw_msg_multiple")));
        
        postDrawMessage.setPlainTextMessage(content.toString());
        postDrawMessage.setRemitent(null);
        postDrawMessage.setIsRead(false);
        postDrawMessage.setIsDeleted(false);
        
        // Set message ID (MessageManager will assign one if null)
        // We'll let deliverMessage handle ID assignment
        
        // Deliver immediately
        messageManager.deliverMessage(postDrawMessage);
        
        Log.info(Log.UI_DRAW, "LeagueDrawScreenTable: Created post-draw message with " + totalFixtures + " fixtures");
    }
}

