package com.rndmodgames.futtoboru.tables.competitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
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
 * Cup Playoffs Table v1.0
 * 
 * Graphical representation of the tournament bracket:
 * - Shows rounds as columns
 * - Shows matches as connected boxes
 * - Displays results and progression
 * 
 * @author Geomancer86
 */
public class CupPlayoffsTable extends VisTable {

    private Futtoboru game;
    private SaveGame currentGame;
    private MainMenuManager menuManager;
    private Competition currentCup;
    private CompetitionEdition currentEdition;
    
    private VisScrollPane scrollPane;
    private VisTable bracketContainer;
    
    public CupPlayoffsTable(Game parent) {
        super(true);
        this.game = (Futtoboru) parent;
        this.currentGame = game.getCurrentGame();
        
        bracketContainer = new VisTable(true);
        scrollPane = new VisScrollPane(bracketContainer);
        scrollPane.setFadeScrollBars(false);
        
        this.add(scrollPane).grow().fill();
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    public void setCompetitionEdition(CompetitionEdition edition) {
        this.currentEdition = edition;
        findCupForEdition();
        updateDynamicComponents();
    }
    
    private void findCupForEdition() {
        if (currentEdition == null) return;
        for (Competition cup : currentGame.getAllCups()) {
            if (cup != null && cup.getEditions() != null && cup.getEditions().contains(currentEdition)) {
                currentCup = cup;
                return;
            }
        }
    }
    
    public void updateDynamicComponents() {
        // Refresh currentGame reference
        if (game != null) {
            this.currentGame = game.getCurrentGame();
        }
        
        bracketContainer.clear();
        
        if (currentEdition == null || currentCup == null) {
            bracketContainer.add(new VisLabel("No edition selected")).pad(20);
            return;
        }
        
        // Header
        VisLabel titleLabel = new VisLabel(currentCup.getName() + " - " + currentEdition.getName() + " Bracket");
        titleLabel.setFontScale(1.2f);
        bracketContainer.add(titleLabel).colspan(10).pad(10).row();
        
        // Collect matches by round
        Map<Integer, List<Match>> matchesByRound = new HashMap<>();
        int maxRound = 0;
        
        List<Match> allMatches = getEditionMatches();
        System.out.println("CupPlayoffsTable: Found " + allMatches.size() + " matches for edition ID: " + currentEdition.getId());
        
        for (Match m : allMatches) {
            if (m.getRound() == null) continue;
            int r = m.getRound();
            matchesByRound.computeIfAbsent(r, k -> new ArrayList<>()).add(m);
            if (r > maxRound) maxRound = r;
        }
        
        if (maxRound == 0) {
            bracketContainer.add(new VisLabel("The draw has not been conducted yet.")).pad(20).row();
            System.out.println("CupPlayoffsTable: No matches found - edition ID: " + currentEdition.getId() + ", currentGame: " + (currentGame != null ? "not null" : "null"));
            return;
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
        
        // Draw rounds as columns
        VisTable columnsTable = new VisTable(true);
        columnsTable.top().left();
        
        for (int r = 1; r <= maxRound; r++) {
            List<Match> roundMatches = matchesByRound.get(r);
            if (roundMatches == null || roundMatches.isEmpty()) continue;
            
            VisTable roundColumn = new VisTable(true);
            roundColumn.top();
            
            // Round Title
            VisLabel roundTitle = new VisLabel("Round " + r);
            roundTitle.setColor(Color.GOLD);
            roundColumn.add(roundTitle).pad(10).row();
            
            // Matches in this round
            for (Match match : roundMatches) {
                roundColumn.add(createMatchBox(match)).pad(5).row();
            }
            
            columnsTable.add(roundColumn).top().pad(10);
        }
        
        bracketContainer.add(columnsTable).grow().row();
        
        // Back Button
        VisTextButton backButton = new VisTextButton("Back to Cup Details");
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (menuManager != null) {
                    menuManager.setActiveMainScreen(MainMenuManager.CUP_DETAIL_SCREEN);
                }
            }
        });
        bracketContainer.add(backButton).pad(20);
    }
    
    private VisTable createMatchBox(Match match) {
        VisTable box = new VisTable(true);
        box.setBackground("button-over"); // Use a subtle background
        
        Club home = match.getHomeClubId() != null ? currentGame.getClubById(match.getHomeClubId()) : null;
        Club away = match.getAwayClubId() != null ? currentGame.getClubById(match.getAwayClubId()) : null;
        
        String homeName;
        String awayName;
        
        // Determine team names or "Winner of X" labels
        if (home != null) {
            homeName = home.getName();
        } else if (match.getParentMatch1Id() != null) {
            // Show "Winner of Match X"
            homeName = getWinnerLabel(match, match.getParentMatch1Id());
        } else {
            homeName = "TBD";
        }
        
        if (away != null) {
            awayName = away.getName();
        } else if (match.getParentMatch2Id() != null) {
            // Show "Winner of Match X"
            awayName = getWinnerLabel(match, match.getParentMatch2Id());
        } else {
            awayName = "TBD";
        }
        
        VisLabel homeLabel = new VisLabel(homeName);
        VisLabel awayLabel = new VisLabel(awayName);
        
        // Style based on match state
        if (match.getIsPlayed() != null && match.getIsPlayed()) {
            // Match completed - show score
            homeLabel.setText(homeName + " (" + match.getHomeGoals() + ")");
            awayLabel.setText(awayName + " (" + match.getAwayGoals() + ")");
            
            // Highlight winner
            if (match.getHomeGoals() != null && match.getAwayGoals() != null) {
                if (match.getHomeGoals() > match.getAwayGoals()) {
                    homeLabel.setColor(Color.GREEN);
                } else if (match.getAwayGoals() > match.getHomeGoals()) {
                    awayLabel.setColor(Color.GREEN);
                }
            }
        } else if (home == null || away == null) {
            // Future match - teams not yet determined
            homeLabel.setColor(Color.GRAY);
            awayLabel.setColor(Color.GRAY);
        }
        
        box.add(homeLabel).width(150).left().pad(2).row();
        box.add(new VisLabel("vs")).center().pad(2).row();
        box.add(awayLabel).width(150).left().pad(2);
        
        return box;
    }
    
    /**
     * Get label for winner of a parent match (e.g., "Winner of R1M1")
     */
    private String getWinnerLabel(Match currentMatch, Long parentMatchId) {
        if (parentMatchId == null) {
            return "TBD";
        }
        
        // Find the parent match to get its bracket path
        Match parentMatch = findMatchById(parentMatchId);
        if (parentMatch != null && parentMatch.getBracketPath() != null) {
            return "Winner of " + parentMatch.getBracketPath();
        }
        
        return "Winner of Match " + parentMatchId;
    }
    
    /**
     * Find a match by ID from all edition matches
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
    
    private List<Match> getEditionMatches() {
        List<Match> matches = new ArrayList<>();
        if (currentEdition == null) {
            System.out.println("CupPlayoffsTable.getEditionMatches: currentEdition is null");
            return matches;
        }
        
        if (currentGame == null) {
            System.out.println("CupPlayoffsTable.getEditionMatches: currentGame is null");
            return matches;
        }
        
        if (currentGame.getAllClubs() == null) {
            System.out.println("CupPlayoffsTable.getEditionMatches: getAllClubs() is null");
            return matches;
        }
        
        System.out.println("CupPlayoffsTable.getEditionMatches: Looking for matches with edition ID: " + currentEdition.getId());
        System.out.println("CupPlayoffsTable.getEditionMatches: Checking " + currentGame.getAllClubs().size() + " clubs");
        
        // Use Set to track added match IDs (more reliable than contains())
        java.util.Set<Long> matchIdsAdded = new java.util.HashSet<>();
        
        int totalMatchesChecked = 0;
        int matchesWithEditionId = 0;
        int matchesMatchingEdition = 0;
        int fromScheduled = 0;
        int fromPlayed = 0;
        
        // CRITICAL: Search BOTH scheduled AND played matches
        // Matches are moved from scheduledMatches to playedMatches after simulation
        for (Club club : currentGame.getAllClubs()) {
            if (club == null) continue;
            
            // Search scheduled matches
            if (club.getScheduledMatches() != null) {
                for (Match match : club.getScheduledMatches()) {
                    totalMatchesChecked++;
                    
                    if (match != null && match.getCompetitionEditionId() != null) {
                        matchesWithEditionId++;
                        
                        if (match.getCompetitionEditionId().equals(currentEdition.getId()) &&
                            match.getId() != null && !matchIdsAdded.contains(match.getId())) {
                            matchesMatchingEdition++;
                            matches.add(match);
                            matchIdsAdded.add(match.getId());
                            fromScheduled++;
                            System.out.println("CupPlayoffsTable: Found match in scheduled - ID: " + match.getId() + 
                                " - Round: " + match.getRound() + 
                                ", Home: " + match.getHomeClubId() + ", Away: " + match.getAwayClubId() +
                                ", Played: " + (match.getIsPlayed() != null && match.getIsPlayed()));
                        }
                    }
                }
            }
            
            // Search played matches (where completed matches are stored)
            if (club.getPlayedMatches() != null) {
                for (Match match : club.getPlayedMatches()) {
                    totalMatchesChecked++;
                    
                    if (match != null && match.getCompetitionEditionId() != null) {
                        matchesWithEditionId++;
                        
                        if (match.getCompetitionEditionId().equals(currentEdition.getId()) &&
                            match.getId() != null && !matchIdsAdded.contains(match.getId())) {
                            matchesMatchingEdition++;
                            matches.add(match);
                            matchIdsAdded.add(match.getId());
                            fromPlayed++;
                            System.out.println("CupPlayoffsTable: Found match in played - ID: " + match.getId() + 
                                " - Round: " + match.getRound() + 
                                ", Home: " + match.getHomeClubId() + ", Away: " + match.getAwayClubId() +
                                ", Played: " + (match.getIsPlayed() != null && match.getIsPlayed()));
                        }
                    }
                }
            }
        }
        
        System.out.println("CupPlayoffsTable.getEditionMatches: Total matches checked: " + totalMatchesChecked + 
            ", with edition ID: " + matchesWithEditionId + ", matching this edition: " + matchesMatchingEdition +
            " (from scheduled: " + fromScheduled + ", from played: " + fromPlayed + ")");
        
        return matches;
    }
}

