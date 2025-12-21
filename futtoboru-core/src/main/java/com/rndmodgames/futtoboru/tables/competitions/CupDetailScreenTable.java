package com.rndmodgames.futtoboru.tables.competitions;

import java.time.format.DateTimeFormatter;
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
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Cup Detail Screen Table v1.0
 * 
 * Displays detailed information about a cup competition:
 * - Cup name, country, number of participants
 * - Current round information
 * - Fixtures and Results
 * - Playoff Bracket (Graphical)
 * 
 * @author Geomancer86
 */
public class CupDetailScreenTable extends VisTable {

    private Futtoboru game;
    private SaveGame currentGame;
    private MainMenuManager menuManager;
    private Competition selectedCup;
    private CompetitionEdition currentEdition;
    
    private VisScrollPane contentScrollPane;
    private VisTable contentTable;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    
    public CupDetailScreenTable(Game parent) {
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
    
    public void setSelectedCup(Competition cup) {
        this.selectedCup = cup;
        findCurrentEdition();
        updateDynamicComponents();
    }
    
    private void findCurrentEdition() {
        if (selectedCup == null || selectedCup.getEditions() == null || selectedCup.getEditions().isEmpty()) {
            currentEdition = null;
            return;
        }
        
        java.time.LocalDateTime currentDate = currentGame.getGameDate();
        for (CompetitionEdition edition : selectedCup.getEditions()) {
            if (edition.getStartDate() != null && edition.getEndDate() != null) {
                if (!currentDate.isBefore(edition.getStartDate()) && !currentDate.isAfter(edition.getEndDate())) {
                    currentEdition = edition;
                    return;
                }
            }
        }
        
        // Fallback: use last edition
        currentEdition = selectedCup.getEditions().get(selectedCup.getEditions().size() - 1);
    }
    
    public void updateDynamicComponents() {
        if (game != null) {
            this.currentGame = game.getCurrentGame();
        }
        
        contentTable.clear();
        
        if (selectedCup == null) {
            contentTable.add(new VisLabel("No cup selected")).pad(20).row();
            return;
        }
        
        // Title
        VisLabel titleLabel = new VisLabel(selectedCup.getName() != null ? selectedCup.getName() : "Unnamed Cup");
        titleLabel.setFontScale(1.3f);
        contentTable.add(titleLabel).pad(10).row();
        
        contentTable.addSeparator().pad(5).row();
        
        // Cup Info Section
        contentTable.add(new VisLabel("Competition Information")).pad(5).row();
        
        if (currentEdition != null) {
            contentTable.add(new VisLabel("Season: " + currentEdition.getName())).left().pad(2).row();
            Integer participants = currentEdition.getParticipantClubs();
            String participantsText = participants != null ? String.valueOf(participants) : "0";
            contentTable.add(new VisLabel("Participants: " + participantsText)).left().pad(2).row();
        }
        
        contentTable.add().height(10).row();
        
        // View Bracket Button
        VisTextButton bracketButton = new VisTextButton("View Playoff Bracket");
        bracketButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.CUP_PLAYOFFS_SCREEN);
                }
            }
        });
        contentTable.add(bracketButton).pad(10).row();
        
        contentTable.addSeparator().pad(5).row();
        
        // Fixtures and Results - Show ALL rounds (complete schedule)
        List<Match> matches = getEditionMatches();
        if (!matches.isEmpty()) {
            contentTable.add(new VisLabel("Complete Tournament Schedule")).pad(5).row();
            contentTable.addSeparator().pad(2).row();
            
            // Group matches by round
            java.util.Map<Integer, List<Match>> matchesByRound = new java.util.HashMap<>();
            int maxRound = 0;
            for (Match m : matches) {
                if (m.getRound() == null) continue;
                int r = m.getRound();
                matchesByRound.computeIfAbsent(r, k -> new java.util.ArrayList<>()).add(m);
                if (r > maxRound) maxRound = r;
            }
            
            // Sort matches within each round by bracket position
            for (List<Match> roundMatches : matchesByRound.values()) {
                roundMatches.sort((m1, m2) -> {
                    Integer pos1 = m1.getBracketPosition();
                    Integer pos2 = m2.getBracketPosition();
                    if (pos1 == null && pos2 == null) return 0;
                    if (pos1 == null) return 1;
                    if (pos2 == null) return -1;
                    return pos1.compareTo(pos2);
                });
            }
            
            // Display rounds in order
            for (int round = 1; round <= maxRound; round++) {
                List<Match> roundMatches = matchesByRound.get(round);
                if (roundMatches == null || roundMatches.isEmpty()) continue;
                
                // Round header
                String roundName = getRoundName(round, roundMatches.size() * 2);
                VisLabel roundLabel = new VisLabel(roundName + " (Round " + round + ")");
                roundLabel.setColor(0.7f, 0.7f, 1f, 1f);
                contentTable.add(roundLabel).padTop(10).padBottom(5).row();
                
                // Matches in this round
                for (Match match : roundMatches) {
                    VisTable row = new VisTable(true);
                    
                    // Date
                    String dateStr = match.getMatchDateTime() != null ? match.getMatchDateTime().format(dateFormatter) : "TBD";
                    row.add(new VisLabel(dateStr)).width(100);
                    
                    // Home team or "Winner of X"
                    String homeName = getTeamNameOrWinner(match, true);
                    row.add(new VisLabel(homeName)).width(150).right();
                    
                    // Score or vs
                    String score = " vs ";
                    if (match.getIsPlayed() != null && match.getIsPlayed() && 
                        match.getHomeGoals() != null && match.getAwayGoals() != null) {
                        score = " " + match.getHomeGoals() + " - " + match.getAwayGoals() + " ";
                    }
                    row.add(new VisLabel(score)).width(60).center();
                    
                    // Away team or "Winner of X"
                    String awayName = getTeamNameOrWinner(match, false);
                    row.add(new VisLabel(awayName)).width(150).left();
                    
                    contentTable.add(row).pad(2).row();
                }
            }
        } else {
            contentTable.add(new VisLabel("No matches scheduled yet for this edition.")).pad(20).row();
        }
        
        contentTable.add().height(20).row();
        
        // Back button
        VisTextButton backButton = new VisTextButton("Back to Competitions");
        backButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.COMPETITIONS_SCREEN);
                }
            }
        });
        contentTable.add(backButton).pad(10).row();
    }
    
    private List<Match> getEditionMatches() {
        List<Match> matches = new java.util.ArrayList<>();
        if (currentEdition == null) return matches;
        
        // Use Set to track added match IDs (more reliable than contains())
        java.util.Set<Long> matchIdsAdded = new java.util.HashSet<>();
        
        // CRITICAL: Search BOTH scheduled AND played matches
        // Matches are moved from scheduledMatches to playedMatches after simulation
        for (Club club : currentGame.getAllClubs()) {
            if (club == null) continue;
            
            // Search scheduled matches
            if (club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    if (match != null && 
                        match.getCompetitionEditionId() != null && 
                        match.getCompetitionEditionId().equals(currentEdition.getId()) &&
                        match.getId() != null && !matchIdsAdded.contains(match.getId())) {
                        matches.add(match);
                        matchIdsAdded.add(match.getId());
                    }
                }
            }
            
            // Search played matches (where completed matches are stored)
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    if (match != null && 
                        match.getCompetitionEditionId() != null && 
                        match.getCompetitionEditionId().equals(currentEdition.getId()) &&
                        match.getId() != null && !matchIdsAdded.contains(match.getId())) {
                        matches.add(match);
                        matchIdsAdded.add(match.getId());
                    }
                }
            }
        }
        return matches;
    }
    
    /**
     * Get team name or "Winner of X" label for a match
     */
    private String getTeamNameOrWinner(Match match, boolean isHome) {
        Long teamId = isHome ? match.getHomeClubId() : match.getAwayClubId();
        Long parentMatchId = isHome ? match.getParentMatch1Id() : match.getParentMatch2Id();
        
        if (teamId != null) {
            Club club = currentGame.getClubById(teamId);
            if (club != null) {
                return club.getName();
            }
        }
        
        // Team not determined - show "Winner of X"
        if (parentMatchId != null) {
            Match parentMatch = findMatchById(parentMatchId);
            if (parentMatch != null && parentMatch.getBracketPath() != null) {
                return "Winner of " + parentMatch.getBracketPath();
            }
            return "Winner of Match " + parentMatchId;
        }
        
        return "TBD";
    }
    
    /**
     * Find a match by ID
     */
    private Match findMatchById(Long matchId) {
        if (matchId == null) return null;
        
        List<Match> allMatches = getEditionMatches();
        for (Match match : allMatches) {
            if (match != null && match.getId() != null && match.getId().equals(matchId)) {
                return match;
            }
        }
        
        return null;
    }
    
    /**
     * Get round name (First Round, Semi-Final, Final, etc.)
     */
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
}

