package com.rndmodgames.futtoboru.tables.competitions;

import java.time.format.DateTimeFormatter;
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
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * League Detail Screen Table v1.0
 * 
 * Displays detailed information about a league:
 * - League name, country, number of clubs
 * - League standings (if matches have been played)
 * - Upcoming fixtures
 * - Recent results
 * 
 * @author Geomancer86
 */
public class LeagueDetailScreenTable extends VisTable {

    private Futtoboru game;
    private SaveGame currentGame;
    private MainMenuManager menuManager;
    private League selectedLeague;
    
    private VisScrollPane contentScrollPane;
    private VisTable contentTable;
    
    private LeagueStandingsManager standingsManager;
    
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    
    public LeagueDetailScreenTable(Game parent) {
        super(true);
        
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getCurrentGame();
        
        // Initialize standings manager
        this.standingsManager = new LeagueStandingsManager();
        
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
        updateDynamicComponents();
    }
    
    /**
     * Update the selected league reference without triggering a full update
     * This is useful when we just need to sync the reference but will call updateDynamicComponents separately
     */
    public void syncSelectedLeague(League league) {
        this.selectedLeague = league;
    }
    
    public void updateDynamicComponents() {
        // Refresh currentGame reference to avoid stale references
        if (game != null) {
            this.currentGame = game.getCurrentGame();
        }
        
        contentTable.clear();
        
        if (selectedLeague == null) {
            contentTable.add(new VisLabel("No league selected")).pad(20).row();
            return;
        }
        
        // Title
        VisLabel titleLabel = new VisLabel(selectedLeague.getName() != null ? selectedLeague.getName() : "Unnamed League");
        titleLabel.setFontScale(1.3f);
        contentTable.add(titleLabel).pad(10).row();
        
        contentTable.addSeparator().pad(5).row();
        
        // League Info Section
        contentTable.add(new VisLabel("League Information")).pad(5).row();
        
        // Country
        if (selectedLeague.getCountry() != null) {
            contentTable.add(new VisLabel("Country: " + selectedLeague.getCountry().getCommonName())).left().pad(2).row();
        }
        
        // Number of clubs
        int clubCount = (selectedLeague.getLeagueClubs() != null) ? selectedLeague.getLeagueClubs().size() : 0;
        contentTable.add(new VisLabel("Clubs: " + clubCount)).left().pad(2).row();
        
        contentTable.add().height(10).row();
        
        // Clubs List Section
        if (selectedLeague.getLeagueClubs() != null && !selectedLeague.getLeagueClubs().isEmpty()) {
            contentTable.add(new VisLabel("Participating Clubs")).pad(5).row();
            contentTable.addSeparator().pad(2).row();
            
            for (Club club : selectedLeague.getLeagueClubs()) {
                if (club != null) {
                    contentTable.add(new VisLabel(club.getName() != null ? club.getName() : "Unnamed Club")).left().pad(2).row();
                }
            }
            
            contentTable.add().height(10).row();
        }
        
        // Competition Rules Section
        com.rndmodgames.futtoboru.data.CompetitionRules rules = selectedLeague.getRulesOrDefault();
        contentTable.add(new VisLabel("Competition Rules")).pad(5).row();
        contentTable.addSeparator().pad(2).row();
        
        // Points system
        String pointsSystem = rules.getPointsForWin() + " points for win, " + 
                              rules.getPointsForDraw() + " point" + (rules.getPointsForDraw() != 1 ? "s" : "") + 
                              " for draw, " + rules.getPointsForLoss() + " for loss";
        contentTable.add(new VisLabel("Points System: " + pointsSystem)).left().pad(2).row();
        
        // Tie-breaking criteria
        StringBuilder tieBreakers = new StringBuilder("Tie-breakers: ");
        java.util.List<com.rndmodgames.futtoboru.data.CompetitionRules.TieBreaker> tieBreakingOrder = rules.getTieBreakingOrder();
        for (int i = 0; i < tieBreakingOrder.size(); i++) {
            if (i > 0) {
                tieBreakers.append(" → ");
            }
            tieBreakers.append(formatTieBreaker(tieBreakingOrder.get(i)));
        }
        contentTable.add(new VisLabel(tieBreakers.toString())).left().pad(2).row();
        
        // Match format
        String matchFormat = rules.getMatchFormat() == com.rndmodgames.futtoboru.data.CompetitionRules.MatchFormat.DOUBLE_ROUND_ROBIN 
            ? "Double round-robin (home and away)" 
            : "Single round-robin";
        contentTable.add(new VisLabel("Format: " + matchFormat)).left().pad(2).row();
        
        contentTable.add().height(10).row();
        
        // Past Champions Section
        if (selectedLeague.getEditions() != null && !selectedLeague.getEditions().isEmpty()) {
            contentTable.add(new VisLabel("Past Champions")).pad(5).row();
            contentTable.addSeparator().pad(2).row();
            
            // Show completed editions (those with champions)
            int completedCount = 0;
            for (com.rndmodgames.futtoboru.data.CompetitionEdition edition : selectedLeague.getEditions()) {
                if (edition.getChampionsId() != null) {
                    completedCount++;
                    Club champion = currentGame.getClubById(edition.getChampionsId());
                    Club runnerUp = edition.getRunnersUpId() != null ? currentGame.getClubById(edition.getRunnersUpId()) : null;
                    
                    String seasonInfo = edition.getName() != null ? edition.getName() : "Season " + completedCount;
                    String championName = champion != null ? champion.getName() : "Unknown";
                    String runnerUpName = runnerUp != null ? runnerUp.getName() : "N/A";
                    
                    VisTable historyRow = new VisTable(true);
                    historyRow.add(new VisLabel(seasonInfo)).width(150).left();
                    historyRow.add(new VisLabel("Champion: " + championName)).expandX().left();
                    if (runnerUp != null) {
                        historyRow.add(new VisLabel("Runner-up: " + runnerUpName)).expandX().left();
                    }
                    contentTable.add(historyRow).pad(2).row();
                }
            }
            
            if (completedCount == 0) {
                contentTable.add(new VisLabel("No completed seasons yet.")).left().pad(2).row();
            }
            
            contentTable.add().height(10).row();
        }
        
        // Standings Section (always show, even if no matches played yet)
        contentTable.add(new VisLabel("League Standings")).pad(5).row();
        contentTable.addSeparator().pad(2).row();
        
        // Header
        VisTable standingsHeader = new VisTable(true);
        standingsHeader.add(new VisLabel("Pos")).width(50);
        standingsHeader.add(new VisLabel("Club")).width(200);
        standingsHeader.add(new VisLabel("P")).width(40);
        standingsHeader.add(new VisLabel("W")).width(40);
        standingsHeader.add(new VisLabel("D")).width(40);
        standingsHeader.add(new VisLabel("L")).width(40);
        standingsHeader.add(new VisLabel("GF")).width(40);
        standingsHeader.add(new VisLabel("GA")).width(40);
        standingsHeader.add(new VisLabel("GD")).width(50);
        standingsHeader.add(new VisLabel("Pts")).width(50);
        contentTable.add(standingsHeader).pad(2).row();
        contentTable.addSeparator().pad(2).row();
        
        // Use LeagueStandingsManager to calculate standings based on competition rules
        // This will sort by rules even when all stats are 0 (alphabetical fallback)
        List<Club> sortedClubs = standingsManager.calculateStandings(selectedLeague);
        
        int position = 1;
        for (Club club : sortedClubs) {
            if (club == null) continue;
            
            VisTable row = new VisTable(true);
            row.add(new VisLabel(String.valueOf(position++))).width(50);
            row.add(new VisLabel(club.getName() != null ? club.getName() : "Unnamed")).width(200);
            row.add(new VisLabel(String.valueOf(club.getMatchesPlayed() != null ? club.getMatchesPlayed() : 0))).width(40);
            row.add(new VisLabel(String.valueOf(club.getMatchesWon() != null ? club.getMatchesWon() : 0))).width(40);
            row.add(new VisLabel(String.valueOf(club.getMatchesDrawn() != null ? club.getMatchesDrawn() : 0))).width(40);
            row.add(new VisLabel(String.valueOf(club.getMatchesLost() != null ? club.getMatchesLost() : 0))).width(40);
            row.add(new VisLabel(String.valueOf(club.getGoalsScored() != null ? club.getGoalsScored() : 0))).width(40);
            row.add(new VisLabel(String.valueOf(club.getGoalsConceded() != null ? club.getGoalsConceded() : 0))).width(40);
            row.add(new VisLabel(String.valueOf(club.getGoalDifference() != null ? club.getGoalDifference() : 0))).width(50);
            row.add(new VisLabel(String.valueOf(club.getPoints() != null ? club.getPoints() : 0))).width(50);
            
            contentTable.add(row).pad(2).row();
        }
        
        contentTable.add().height(10).row();
        
        // Upcoming Fixtures Section
        List<Match> upcomingMatches = getUpcomingMatches(selectedLeague);
        if (!upcomingMatches.isEmpty()) {
            contentTable.add(new VisLabel("Upcoming Fixtures (Next 5)")).pad(5).row();
            contentTable.addSeparator().pad(2).row();
            
            int count = 0;
            for (Match match : upcomingMatches) {
                if (count >= 5) break;
                
                Club homeClub = currentGame.getClubById(match.getHomeClubId());
                Club awayClub = currentGame.getClubById(match.getAwayClubId());
                
                if (homeClub != null && awayClub != null) {
                    String matchText = "";
                    if (match.getMatchDateTime() != null) {
                        matchText = match.getMatchDateTime().format(dateFormatter) + " - ";
                    }
                    matchText += homeClub.getName() + " vs " + awayClub.getName();
                    
                    contentTable.add(new VisLabel(matchText)).left().pad(2).row();
                    count++;
                }
            }
            
            contentTable.add().height(10).row();
        }
        
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
    
    /**
     * Get upcoming matches for the league
     */
    private List<Match> getUpcomingMatches(League league) {
        List<Match> upcoming = new java.util.ArrayList<>();
        
        if (league == null || league.getLeagueClubs() == null) {
            return upcoming;
        }
        
        java.time.LocalDateTime currentDate = currentGame.getGameDate();
        
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getScheduledMatches() == null) {
                continue;
            }
            
            for (Match match : club.getScheduledMatches()) {
                if (match == null || match.getIsPlayed() != null && match.getIsPlayed()) {
                    continue;
                }
                
                // Only include league matches
                if (match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH) {
                    // Check if not already in list
                    boolean alreadyAdded = false;
                    for (Match existing : upcoming) {
                        if (existing.getId() != null && existing.getId().equals(match.getId())) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    
                    if (!alreadyAdded && match.getMatchDateTime() != null && !match.getMatchDateTime().isBefore(currentDate)) {
                        upcoming.add(match);
                    }
                }
            }
        }
        
        // Sort by date
        upcoming.sort(Comparator.comparing(Match::getMatchDateTime, Comparator.nullsLast(Comparator.naturalOrder())));
        
        return upcoming;
    }
    
    /**
     * Format tie-breaker enum to readable string
     */
    private String formatTieBreaker(com.rndmodgames.futtoboru.data.CompetitionRules.TieBreaker breaker) {
        switch (breaker) {
            case POINTS:
                return "Points";
            case GOAL_DIFFERENCE:
                return "Goal Difference";
            case GOAL_AVERAGE:
                return "Goal Average";
            case GOALS_SCORED:
                return "Goals Scored";
            case GOALS_CONCEDED:
                return "Goals Conceded";
            case HEAD_TO_HEAD:
                return "Head-to-Head";
            case ALPHABETICAL:
                return "Alphabetical";
            default:
                return breaker.toString();
        }
    }
}

