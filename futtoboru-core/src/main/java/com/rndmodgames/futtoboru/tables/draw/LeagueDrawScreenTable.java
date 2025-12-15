package com.rndmodgames.futtoboru.tables.draw;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    
    public LeagueDrawScreenTable(Game parent) {
        super(true);
        
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
        
        // Create scrollable content table
        contentTable = new VisTable(true);
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
        currentRevealedIndex = 0;
        allRevealed = false;
        collectFixtures();
        updateDynamicComponents();
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
            contentTable.add(new VisLabel("No league selected")).pad(20).row();
            return;
        }
        
        // Title
        VisLabel titleLabel = new VisLabel(selectedLeague.getName() != null ? selectedLeague.getName() + " - Fixture Draw" : "Fixture Draw");
        titleLabel.setFontScale(1.3f);
        contentTable.add(titleLabel).pad(10).row();
        
        contentTable.addSeparator().pad(5).row();
        
        // Instructions
        contentTable.add(new VisLabel("The fixture draw for " + selectedLeague.getName() + " is being revealed.")).pad(5).row();
        contentTable.add(new VisLabel("Click 'Next' to reveal fixtures one by one, or 'Draw All' to see all fixtures at once.")).pad(5).row();
        
        contentTable.add().height(10).row();
        
        // Fixtures table
        fixturesTable = new VisTable(true);
        fixturesTable.add(new VisLabel("Date")).width(120);
        fixturesTable.add(new VisLabel("Home")).width(200);
        fixturesTable.add(new VisLabel("Away")).width(200).row();
        fixturesTable.addSeparator().colspan(3).row();
        
        // Show revealed fixtures
        int revealedCount = allRevealed ? allFixtures.size() : currentRevealedIndex;
        for (int i = 0; i < revealedCount && i < allFixtures.size(); i++) {
            Match match = allFixtures.get(i);
            Club homeClub = currentGame.getClubById(match.getHomeClubId());
            Club awayClub = currentGame.getClubById(match.getAwayClubId());
            
            if (homeClub != null && awayClub != null) {
                String dateStr = match.getMatchDateTime() != null ? 
                    dateFormatter.format(match.getMatchDateTime()) : "TBA";
                fixturesTable.add(new VisLabel(dateStr)).width(120);
                fixturesTable.add(new VisLabel(homeClub.getName())).width(200);
                fixturesTable.add(new VisLabel(awayClub.getName())).width(200).row();
            }
        }
        
        // Show "..." for unrevealed fixtures
        if (!allRevealed && currentRevealedIndex < allFixtures.size()) {
            fixturesTable.add(new VisLabel("...")).colspan(3).pad(5).row();
            fixturesTable.add(new VisLabel((allFixtures.size() - currentRevealedIndex) + " more fixtures to be revealed")).colspan(3).pad(5).row();
        }
        
        contentTable.add(fixturesTable).growX().pad(10).row();
        
        contentTable.add().height(20).row();
        
        // Buttons
        VisTable buttonTable = new VisTable(true);
        
        // Next button (only show if not all revealed)
        if (!allRevealed && currentRevealedIndex < allFixtures.size()) {
            nextButton = new VisTextButton("Next");
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
            buttonTable.add(nextButton).pad(5);
        }
        
        // Draw All button (only show if not all revealed)
        if (!allRevealed && currentRevealedIndex < allFixtures.size()) {
            drawAllButton = new VisTextButton("Draw All");
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
            buttonTable.add(drawAllButton).pad(5);
        }
        
        // Continue button (only show when all fixtures are revealed)
        if (allRevealed) {
            continueButton = new VisTextButton("Continue");
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
            buttonTable.add(continueButton).pad(5);
        }
        
        contentTable.add(buttonTable).pad(10).row();
    }
    
    /**
     * Mark the draw message as read and non-mandatory
     * This allows time advancement to continue
     */
    private void markDrawMessageAsComplete() {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            return;
        }
        
        // Find the draw message and mark it as read and non-mandatory
        for (com.rndmodgames.futtoboru.data.Message message : currentGame.getAllMessages()) {
            if (message != null && 
                message.getMessageType() != null && 
                (message.getMessageType().equals("LEAGUE_DRAW") || 
                 message.getMessageType().equals("FIXTURE_DRAW")) &&
                message.getIsMandatory() != null && message.getIsMandatory()) {
                
                message.setIsRead(true);
                message.setIsMandatory(false);  // No longer blocks time advancement
                System.out.println("LeagueDrawScreenTable: Marked draw message as complete");
                break;
            }
        }
    }
}

