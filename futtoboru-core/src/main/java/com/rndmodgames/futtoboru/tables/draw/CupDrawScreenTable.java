package com.rndmodgames.futtoboru.tables.draw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Cup Draw Screen Table v1.0
 * 
 * Mandatory draw screen for cup competitions:
 * - Reveals matchups for a specific round step-by-step
 * - Similar flow to League Draw Screen
 * 
 * @author Geomancer86
 */
public class CupDrawScreenTable extends VisTable {

    private Futtoboru game;
    private SaveGame currentGame;
    private MainMenuManager menuManager;
    private CompetitionEdition currentEdition;
    private Competition currentCup;
    
    // Draw state
    private List<Match> roundFixtures;
    private List<Long> drawOrder; // Teams in draw order (for one-by-one reveal)
    private int currentRevealedIndex = 0;
    private boolean allRevealed = false;
    private String roundName = "First Round";
    
    // UI components
    private VisScrollPane contentScrollPane;
    private VisTable contentTable;
    private VisTable fixturesTable;
    private VisTextButton nextButton;
    private VisTextButton drawAllButton;
    private VisTextButton continueButton;
    
    public CupDrawScreenTable(Game parent) {
        super(true);
        
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
        
        // Create scrollable content table
        contentTable = new VisTable(true);
        contentScrollPane = new VisScrollPane(contentTable);
        contentScrollPane.setFadeScrollBars(false);
        
        this.add(contentScrollPane).grow().fill();
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    /**
     * Set the edition to show draw for
     */
    public void setCompetitionEdition(CompetitionEdition edition) {
        this.currentEdition = edition;
        
        findCupForEdition();
        collectRoundFixtures();
        
        // Check if draw has already been completed (message no longer mandatory)
        // If so, show all fixtures immediately to prevent redrawing
        if (isDrawAlreadyCompleted()) {
            allRevealed = true;
            currentRevealedIndex = roundFixtures != null ? roundFixtures.size() : 0;
        } else {
            currentRevealedIndex = 0;
            allRevealed = false;
        }
        
        updateDynamicComponents();
    }
    
    /**
     * Check if the cup draw message has already been completed
     * (i.e., marked as read and no longer mandatory)
     */
    private boolean isDrawAlreadyCompleted() {
        if (currentGame == null || currentGame.getAllMessages() == null) {
            return false;
        }
        
        for (Message message : currentGame.getAllMessages()) {
            if (message != null) {
                String messageType = message.getMessageType();
                boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                boolean isRead = message.getIsRead() != null && message.getIsRead();
                
                if (messageType != null && 
                    "CUP_DRAW".equals(messageType) &&
                    !isMandatory && isRead) {
                    // Draw has been completed
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private void findCupForEdition() {
        if (currentEdition == null) return;
        
        List<Competition> cups = currentGame.getAllCups();
        for (Competition cup : cups) {
            if (cup != null && cup.getEditions() != null) {
                if (cup.getEditions().contains(currentEdition)) {
                    currentCup = cup;
                    return;
                }
            }
        }
    }
    
    private void collectRoundFixtures() {
        roundFixtures = new ArrayList<>();
        drawOrder = new ArrayList<>();
        if (currentEdition == null) {
            System.out.println("CupDrawScreenTable: currentEdition is null!");
            return;
        }
        
        System.out.println("CupDrawScreenTable: Collecting fixtures for edition: " + currentEdition.getName() + " (ID: " + currentEdition.getId() + ")");
        
        // Find all matches for this edition that haven't been played
        List<Match> editionMatches = new ArrayList<>();
        int totalClubsChecked = 0;
        int matchesFoundForEdition = 0;
        
        for (Club club : currentGame.getAllClubs()) {
            totalClubsChecked++;
            if (club != null && club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match != null && 
                        match.getCompetitionEditionId() != null && 
                        match.getCompetitionEditionId().equals(currentEdition.getId())) {
                        
                        matchesFoundForEdition++;
                        if (!match.getIsPlayed() && !editionMatches.contains(match)) {
                            editionMatches.add(match);
                        }
                    }
                }
            }
        }
        
        System.out.println("CupDrawScreenTable: Checked " + totalClubsChecked + " clubs, found " + matchesFoundForEdition + " total matches for edition, " + editionMatches.size() + " are unplayed.");
        
        // If no matches exist, generate the draw now
        if (editionMatches.isEmpty()) {
            System.out.println("CupDrawScreenTable: No matches found for this edition, generating draw now...");
            generateCupDraw();
            // Re-collect matches after generation - refresh currentGame reference first
            currentGame = game.getCurrentGame();
            editionMatches = new ArrayList<>();
            for (Club club : currentGame.getAllClubs()) {
                if (club != null && club.getScheduledMatches() != null) {
                    for (Match match : club.getScheduledMatches()) {
                        if (match != null && 
                            match.getCompetitionEditionId() != null && 
                            match.getCompetitionEditionId().equals(currentEdition.getId()) &&
                            !match.getIsPlayed() && !editionMatches.contains(match)) {
                            editionMatches.add(match);
                        }
                    }
                }
            }
            System.out.println("CupDrawScreenTable: After generation, found " + editionMatches.size() + " matches");
        }
        
        if (editionMatches.isEmpty()) {
            System.out.println("CupDrawScreenTable: Still no matches after generation attempt!");
            return;
        }
        
        // Find the latest round among unplayed matches (but only Round 1 for the draw screen)
        int maxRound = 0;
        for (Match m : editionMatches) {
            if (m.getRound() != null && m.getRound() > maxRound && m.getRound() == 1) {
                maxRound = m.getRound();
            }
        }
        
        // If no round found, default to round 1 (first round)
        if (maxRound == 0) {
            maxRound = 1;
            System.out.println("CupDrawScreenTable: No round found in matches, defaulting to round 1");
        }
        
        System.out.println("CupDrawScreenTable: Max round found: " + maxRound);
        
        // Collect matches for Round 1 only (for the draw screen)
        for (Match m : editionMatches) {
            if (m.getRound() != null && m.getRound() == 1) {
                roundFixtures.add(m);
            }
        }
        
        System.out.println("CupDrawScreenTable: Collected " + roundFixtures.size() + " matches for round " + maxRound);
        
        // Build draw order: teams in the order they were drawn (team1, team2, team3, team4...)
        // This allows revealing teams one by one like Football Manager
        // Extract teams from matches in order: home1, away1, home2, away2, etc.
        // This creates pairs: team1 vs team2, team3 vs team4, etc.
        drawOrder = new ArrayList<>();
        for (Match match : roundFixtures) {
            if (match.getHomeClubId() != null) {
                drawOrder.add(match.getHomeClubId());
            }
            if (match.getAwayClubId() != null) {
                drawOrder.add(match.getAwayClubId());
            }
        }
        
        System.out.println("CupDrawScreenTable: Built drawOrder with " + drawOrder.size() + " teams");
        
        // Sort matches for deterministic display (e.g. by home club name)
        Collections.sort(roundFixtures, (m1, m2) -> {
            Club c1 = currentGame.getClubById(m1.getHomeClubId());
            Club c2 = currentGame.getClubById(m2.getHomeClubId());
            if (c1 == null || c2 == null) return 0;
            return c1.getName().compareTo(c2.getName());
        });
        
        // Determine round name
        roundName = getRoundName(maxRound, roundFixtures.size() * 2);
    }
    
    private String getRoundName(int roundNumber, int participants) {
        if (participants == 2) return "Final";
        if (participants == 4) return "Semi-Final";
        if (participants == 8) return "Quarter-Final";
        
        String[] ordinals = {"", "First", "Second", "Third", "Fourth", "Fifth", "Sixth"};
        if (roundNumber > 0 && roundNumber < ordinals.length) {
            return ordinals[roundNumber] + " Round";
        }
        return "Round " + roundNumber;
    }
    
    /**
     * Generate the cup draw if matches don't exist yet
     */
    private void generateCupDraw() {
        if (currentCup == null || currentEdition == null) {
            System.out.println("CupDrawScreenTable: Cannot generate draw - cup or edition is null");
            return;
        }
        
        if (currentEdition.getParticipantClubsIds() == null || currentEdition.getParticipantClubsIds().isEmpty()) {
            System.out.println("CupDrawScreenTable: Cannot generate draw - no participants");
            return;
        }
        
        System.out.println("CupDrawScreenTable: *** GENERATING COMPLETE CUP BRACKET *** for " + currentCup.getName());
        
        // CRITICAL FIX: Use CupBracketGenerator to generate COMPLETE bracket (all rounds)
        // NOT CompetitionScheduler which only generates Round 1
        java.time.LocalDateTime currentDate = currentGame.getGameDate();
        java.time.LocalDateTime firstRoundDate = currentDate.plusWeeks(2);
        int weeksBetweenRounds = 2;
        
        com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator bracketGenerator = 
            new com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator();
        
        List<Match> allMatches = bracketGenerator.generateCompleteBracket(
            currentEdition.getParticipantClubsIds(),
            currentCup.getId(),
            currentEdition.getId(),
            firstRoundDate,
            weeksBetweenRounds
        );
        
        if (allMatches == null || allMatches.isEmpty()) {
            System.out.println("CupDrawScreenTable: ERROR - Bracket generation returned no matches!");
            return;
        }
        
        System.out.println("CupDrawScreenTable: Generated " + allMatches.size() + " total matches (all rounds)");
        
        // Process all matches and add to clubs (same logic as AuthorityManager)
        int round1Count = 0;
        int round2PlusCount = 0;
        
        for (Match match : allMatches) {
            if (match == null) continue;
            
            if (match.getRound() != null && match.getRound() == 1) {
                round1Count++;
                // Round 1: Add to specific clubs
                if (match.getHomeClubId() != null && match.getAwayClubId() != null) {
                    Club homeClub = currentGame.getClubById(match.getHomeClubId());
                    Club awayClub = currentGame.getClubById(match.getAwayClubId());
                    
                    if (homeClub != null) {
                        if (homeClub.getScheduledMatches() == null) {
                            homeClub.setScheduledMatches(new ArrayList<>());
                        }
                        // Use ID-based check
                        boolean alreadyAdded = false;
                        for (Match m : homeClub.getScheduledMatches()) {
                            if (m != null && m.getId() != null && m.getId().equals(match.getId())) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            homeClub.getScheduledMatches().add(match);
                        }
                    }
                    if (awayClub != null) {
                        if (awayClub.getScheduledMatches() == null) {
                            awayClub.setScheduledMatches(new ArrayList<>());
                        }
                        // Use ID-based check
                        boolean alreadyAdded = false;
                        for (Match m : awayClub.getScheduledMatches()) {
                            if (m != null && m.getId() != null && m.getId().equals(match.getId())) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            awayClub.getScheduledMatches().add(match);
                        }
                    }
                }
            } else if (match.getRound() != null && match.getRound() > 1) {
                // Future rounds: Add to ALL participating clubs
                round2PlusCount++;
                int addedCount = 0;
                for (Long clubId : currentEdition.getParticipantClubsIds()) {
                    Club club = currentGame.getClubById(clubId);
                    if (club != null) {
                        if (club.getScheduledMatches() == null) {
                            club.setScheduledMatches(new ArrayList<>());
                        }
                        // Use ID-based check
                        boolean alreadyAdded = false;
                        for (Match m : club.getScheduledMatches()) {
                            if (m != null && m.getId() != null && m.getId().equals(match.getId())) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            club.getScheduledMatches().add(match);
                            addedCount++;
                        }
                    }
                }
                System.out.println("CupDrawScreenTable: Added Round " + match.getRound() + " match " + 
                    match.getBracketPath() + " to " + addedCount + " clubs");
            }
        }
        
        System.out.println("CupDrawScreenTable: *** COMPLETE BRACKET ADDED *** Round 1: " + round1Count + 
            " matches, Round 2+: " + round2PlusCount + " matches");
        
        // Update participant count
        int totalParticipants = currentEdition.getParticipantClubsIds() != null ? 
            currentEdition.getParticipantClubsIds().size() : allMatches.size() * 2;
        currentEdition.setParticipantClubs(totalParticipants);
    }
    
    public void updateDynamicComponents() {
        contentTable.clear();
        
        if (currentCup == null || currentEdition == null) {
            contentTable.add(new VisLabel("No cup draw selected")).pad(20).row();
            return;
        }
        
        // Title
        VisLabel titleLabel = new VisLabel(currentCup.getName() + " - " + roundName + " Draw");
        titleLabel.setFontScale(1.3f);
        contentTable.add(titleLabel).pad(10).row();
        
        contentTable.addSeparator().pad(5).row();
        
        // Instructions
        contentTable.add(new VisLabel("The draw for the " + roundName + " is being revealed.")).pad(5).row();
        contentTable.add(new VisLabel("Click 'Next' to reveal teams one by one (like Football Manager).")).pad(5).row();
        
        contentTable.add().height(10).row();
        
        // Draw table - shows teams being drawn one by one
        fixturesTable = new VisTable(true);
        fixturesTable.add(new VisLabel("Team")).width(200).row();
        fixturesTable.addSeparator().row();
        
        // Show revealed teams
        int revealedCount = allRevealed ? (drawOrder != null ? drawOrder.size() : 0) : currentRevealedIndex;
        if (drawOrder != null) {
            for (int i = 0; i < revealedCount && i < drawOrder.size(); i++) {
                Long teamId = drawOrder.get(i);
                Club club = currentGame.getClubById(teamId);
                
                if (club != null) {
                    VisLabel teamLabel = new VisLabel((i + 1) + ". " + club.getName());
                    // If this is the second team in a pair, show it as opponent
                    if (i > 0 && i % 2 == 1) {
                        teamLabel.setColor(0.7f, 0.9f, 0.7f, 1f); // Light green for opponent
                        fixturesTable.add(teamLabel).width(200).pad(2).row();
                        // Add separator line for completed matchup
                        fixturesTable.addSeparator().row();
                    } else {
                        fixturesTable.add(teamLabel).width(200).pad(2).row();
                    }
                }
            }
            
            // Show "???" for unrevealed teams
            if (!allRevealed && currentRevealedIndex < drawOrder.size()) {
                fixturesTable.add(new VisLabel("???")).width(200).pad(2).row();
                fixturesTable.add(new VisLabel((drawOrder.size() - currentRevealedIndex) + " more teams to be drawn")).pad(5).row();
            }
        }
        
        contentTable.add(fixturesTable).growX().pad(10).row();
        
        contentTable.add().height(20).row();
        
        // Buttons
        VisTable buttonTable = new VisTable(true);
        
        // Debug: Log button state
        System.out.println("CupDrawScreenTable: Button state - allRevealed: " + allRevealed + 
            ", drawOrder: " + (drawOrder != null ? drawOrder.size() + " teams" : "null") + 
            ", currentRevealedIndex: " + currentRevealedIndex);
        
        // Show draw buttons if draw is not complete and we have teams to reveal
        if (!allRevealed && drawOrder != null && drawOrder.size() > 0 && currentRevealedIndex < drawOrder.size()) {
            nextButton = new VisTextButton("Next");
            nextButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    currentRevealedIndex++;
                    if (currentRevealedIndex >= drawOrder.size()) {
                        allRevealed = true;
                    }
                    updateDynamicComponents();
                }
            });
            buttonTable.add(nextButton).pad(5);
            
            drawAllButton = new VisTextButton("Draw All");
            drawAllButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    allRevealed = true;
                    currentRevealedIndex = drawOrder != null ? drawOrder.size() : 0;
                    updateDynamicComponents();
                }
            });
            buttonTable.add(drawAllButton).pad(5);
        } else if (drawOrder == null || drawOrder.isEmpty()) {
            // If no draw order, show a message and allow manual generation
            contentTable.add(new VisLabel("No draw data available. Generating draw...")).pad(10).row();
            VisTextButton generateButton = new VisTextButton("Generate Draw");
            generateButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    generateCupDraw();
                    collectRoundFixtures(); // Re-collect after generation
                    updateDynamicComponents();
                }
            });
            buttonTable.add(generateButton).pad(5);
        }
        
        if (allRevealed || (drawOrder != null && drawOrder.size() > 0 && currentRevealedIndex >= drawOrder.size())) {
            continueButton = new VisTextButton("Continue");
            continueButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    markDrawMessageAsComplete();
                    if (menuManager != null) {
                        menuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                    }
                    if (game.getGameEngine() != null) {
                        game.getGameEngine().getNextGameAction();
                    }
                }
            });
            buttonTable.add(continueButton).pad(5);
        }
        
        contentTable.add(buttonTable).pad(10).row();
    }
    
    private void markDrawMessageAsComplete() {
        if (currentGame == null || currentGame.getAllMessages() == null) return;
        
        for (Message message : currentGame.getAllMessages()) {
            if (message != null && 
                message.getIsMandatory() != null && message.getIsMandatory() &&
                "CUP_DRAW".equals(message.getMessageType())) {
                
                message.setIsRead(true);
                message.setIsMandatory(false);
                
                // Refresh top menu button
                if (menuManager != null && menuManager.getTopMenu() != null) {
                    menuManager.getTopMenu().setMainContainerButton();
                }
                break;
            }
        }
    }
}


