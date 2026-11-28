package com.rndmodgames.futtoboru.tables.competitions;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.ui.UiTooltips;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

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
    
    private DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.MEDIUM); // U-12: the game language
    
    public LeagueDetailScreenTable(Game parent) {
        super(true);
        
        this.game = ((Futtoboru) parent);
        this.currentGame = game.getSession().getGame();
        
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
            this.currentGame = game.getSession().getGame();
        }
        
        contentTable.clear();
        
        if (selectedLeague == null) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("league_none_selected"))).pad(UiTheme.space("lg")).row();
            return;
        }
        
        // Title
        VisLabel titleLabel = new VisLabel(selectedLeague.getName() != null ? selectedLeague.getName() : LanguageModLoader.getValue("competitions_unnamed_league"));
        titleLabel.setFontScale(UiTheme.fontScale("title"));
        contentTable.add(titleLabel).pad(UiTheme.space("sm")).row();
        
        contentTable.addSeparator().pad(UiTheme.space("xs")).row();
        
        // League Info Section
        contentTable.add(new VisLabel(LanguageModLoader.getValue("league_information"))).pad(UiTheme.space("xs")).row();
        
        // Country
        if (selectedLeague.getCountry() != null) {
            contentTable.add(new VisLabel(LanguageModLoader.format("league_country", selectedLeague.getCountry().getCommonName()))).left().pad(UiTheme.space("xxs")).row();
        }
        
        // Number of clubs
        int clubCount = (selectedLeague.getLeagueClubs() != null) ? selectedLeague.getLeagueClubs().size() : 0;
        contentTable.add(new VisLabel(LanguageModLoader.format("league_clubs", clubCount))).left().pad(UiTheme.space("xxs")).row();
        
        contentTable.add().height(UiTheme.space("sm")).row();
        
        // Clubs List Section
        if (selectedLeague.getLeagueClubs() != null && !selectedLeague.getLeagueClubs().isEmpty()) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("league_participating"))).pad(UiTheme.space("xs")).row();
            contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
            
            for (Club club : selectedLeague.getLeagueClubs()) {
                if (club != null) {
                    contentTable.add(new VisLabel(club.getName() != null ? club.getName() : LanguageModLoader.getValue("competitions_unnamed_club"))).left().pad(UiTheme.space("xxs")).row();
                }
            }
            
            contentTable.add().height(UiTheme.space("sm")).row();
        }
        
        // Competition Rules Section
        com.rndmodgames.futtoboru.data.CompetitionRules rules = selectedLeague.getRulesOrDefault();
        contentTable.add(new VisLabel(LanguageModLoader.getValue("league_rules"))).pad(UiTheme.space("xs")).row();
        contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
        
        // Points system
        String pointsSystem = LanguageModLoader.format("league_points_system", rules.getPointsForWin(),
                rules.getPointsForDraw(), rules.getPointsForLoss());
        contentTable.add(new VisLabel(pointsSystem)).left().pad(UiTheme.space("xxs")).row();
        
        // Tie-breaking criteria
        StringBuilder tieBreakers = new StringBuilder();
        java.util.List<com.rndmodgames.futtoboru.data.CompetitionRules.TieBreaker> tieBreakingOrder = rules.getTieBreakingOrder();
        for (int i = 0; i < tieBreakingOrder.size(); i++) {
            if (i > 0) {
                tieBreakers.append(" → ");
            }
            tieBreakers.append(formatTieBreaker(tieBreakingOrder.get(i)));
        }
        contentTable.add(new VisLabel(LanguageModLoader.format("league_tie_breakers", tieBreakers))).left().pad(UiTheme.space("xxs")).row();
        
        // Match format
        String matchFormat = rules.getMatchFormat() == com.rndmodgames.futtoboru.data.CompetitionRules.MatchFormat.DOUBLE_ROUND_ROBIN 
            ? LanguageModLoader.getValue("league_format_double")
            : LanguageModLoader.getValue("league_format_single");
        contentTable.add(new VisLabel(LanguageModLoader.format("league_format", matchFormat))).left().pad(UiTheme.space("xxs")).row();
        
        contentTable.add().height(UiTheme.space("sm")).row();
        
        // Past Champions Section
        if (selectedLeague.getEditions() != null && !selectedLeague.getEditions().isEmpty()) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("competitions_past_champions"))).pad(UiTheme.space("xs")).row();
            contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
            
            // Show completed editions (those with champions)
            int completedCount = 0;
            for (com.rndmodgames.futtoboru.data.CompetitionEdition edition : selectedLeague.getEditions()) {
                if (edition.getChampionsId() != null) {
                    completedCount++;
                    Club champion = currentGame.getClubById(edition.getChampionsId());
                    Club runnerUp = edition.getRunnersUpId() != null ? currentGame.getClubById(edition.getRunnersUpId()) : null;
                    
                    String seasonInfo = edition.getName() != null ? edition.getName() : LanguageModLoader.format("competitions_season_n", completedCount);
                    String championName = champion != null ? champion.getName() : LanguageModLoader.getValue("unknown");
                    String runnerUpName = runnerUp != null ? runnerUp.getName() : LanguageModLoader.getValue("not_available");
                    
                    VisTable historyRow = new VisTable(true);
                    historyRow.add(new VisLabel(seasonInfo)).width(UiTheme.size("col.label")).left();
                    historyRow.add(new VisLabel(LanguageModLoader.format("competitions_champion", championName))).expandX().left();
                    if (runnerUp != null) {
                        historyRow.add(new VisLabel(LanguageModLoader.format("competitions_runner_up", runnerUpName))).expandX().left();
                    }
                    contentTable.add(historyRow).pad(UiTheme.space("xxs")).row();
                }
            }
            
            if (completedCount == 0) {
                contentTable.add(new VisLabel(LanguageModLoader.getValue("league_no_completed"))).left().pad(UiTheme.space("xxs")).row();
            }
            
            contentTable.add().height(UiTheme.space("sm")).row();
        }
        
        // Standings Section (always show, even if no matches played yet)
        contentTable.add(new VisLabel(LanguageModLoader.getValue("league_standings"))).pad(UiTheme.space("xs")).row();
        contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
        
        // Header
        VisTable standingsHeader = new VisTable(true);
        // labels and tooltips from the language files (U-09)
        String[] columnWidths = { "table.league.pos", "table.league.club", "table.league.stat", "table.league.stat",
                "table.league.stat", "table.league.stat", "table.league.stat", "table.league.stat", "table.league.gd",
                "table.league.pts" };
        for (int c = 0; c < UiTooltips.LEAGUE_COLUMNS.size(); c++) {
            String[] column = UiTooltips.LEAGUE_COLUMNS.get(c);
            standingsHeader.add(UiTooltips.header(column[0], column[1])).width(UiTheme.size(columnWidths[c]));
        }
        contentTable.add(standingsHeader).pad(UiTheme.space("xxs")).row();
        contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
        
        // Use LeagueStandingsManager to calculate standings based on competition rules
        // This will sort by rules even when all stats are 0 (alphabetical fallback)
        List<Club> sortedClubs = standingsManager.calculateStandings(selectedLeague);
        
        int position = 1;
        for (Club club : sortedClubs) {
            if (club == null) continue;
            
            VisTable row = new VisTable(true);
            row.add(new VisLabel(String.valueOf(position++))).width(UiTheme.size("table.league.pos"));
            row.add(new VisLabel(club.getName() != null ? club.getName() : LanguageModLoader.getValue("competitions_unnamed_club"))).width(UiTheme.size("table.league.club"));
            row.add(new VisLabel(String.valueOf(club.getMatchesPlayed() != null ? club.getMatchesPlayed() : 0))).width(UiTheme.size("table.league.stat"));
            row.add(new VisLabel(String.valueOf(club.getMatchesWon() != null ? club.getMatchesWon() : 0))).width(UiTheme.size("table.league.stat"));
            row.add(new VisLabel(String.valueOf(club.getMatchesDrawn() != null ? club.getMatchesDrawn() : 0))).width(UiTheme.size("table.league.stat"));
            row.add(new VisLabel(String.valueOf(club.getMatchesLost() != null ? club.getMatchesLost() : 0))).width(UiTheme.size("table.league.stat"));
            row.add(new VisLabel(String.valueOf(club.getGoalsScored() != null ? club.getGoalsScored() : 0))).width(UiTheme.size("table.league.stat"));
            row.add(new VisLabel(String.valueOf(club.getGoalsConceded() != null ? club.getGoalsConceded() : 0))).width(UiTheme.size("table.league.stat"));
            row.add(new VisLabel(String.valueOf(club.getGoalDifference() != null ? club.getGoalDifference() : 0))).width(UiTheme.size("table.league.gd"));
            row.add(new VisLabel(String.valueOf(club.getPoints() != null ? club.getPoints() : 0))).width(UiTheme.size("table.league.pts"));
            
            contentTable.add(row).pad(UiTheme.space("xxs")).row();
        }
        
        contentTable.add().height(UiTheme.space("sm")).row();
        
        // Latest results (G-03): every club's matches, each score opens its result and report
        List<Match> latestResults = getLatestResults(selectedLeague);
        if (!latestResults.isEmpty()) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("league_latest_results"))).pad(UiTheme.space("xs")).row();
            contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
            for (Match match : latestResults) {
                Club homeClub = currentGame.getClubById(match.getHomeClubId());
                Club awayClub = currentGame.getClubById(match.getAwayClubId());
                if (homeClub == null || awayClub == null) {
                    continue;
                }
                VisTable row = new VisTable(true);
                row.add(new VisLabel(match.getMatchDateTime() != null ? match.getMatchDateTime().format(dateFormatter) : ""))
                    .width(UiTheme.size("col.number"));
                row.add(new VisLabel(homeClub.getName())).width(UiTheme.size("table.league.club")).right();
                com.kotcrab.vis.ui.widget.LinkLabel scoreLink = new com.kotcrab.vis.ui.widget.LinkLabel(
                    match.getHomeGoals() + " - " + match.getAwayGoals());
                scoreLink.setListener(url -> {
                    if (menuManager != null) {
                        menuManager.showMatchResult(match);
                    }
                });
                row.add(scoreLink).width(UiTheme.size("col.score")).center();
                row.add(new VisLabel(awayClub.getName())).width(UiTheme.size("table.league.club")).left();
                contentTable.add(row).left().pad(UiTheme.space("xxs")).row();
            }
            contentTable.add().height(UiTheme.space("sm")).row();
        }

        // Upcoming Fixtures Section
        List<Match> upcomingMatches = getUpcomingMatches(selectedLeague);
        if (!upcomingMatches.isEmpty()) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("league_upcoming"))).pad(UiTheme.space("xs")).row();
            contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
            
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
                    matchText += homeClub.getName() + " " + LanguageModLoader.getValue("match_vs") + " " + awayClub.getName();
                    
                    contentTable.add(new VisLabel(matchText)).left().pad(UiTheme.space("xxs")).row();
                    count++;
                }
            }
            
            contentTable.add().height(UiTheme.space("sm")).row();
        }
        
    }
    
    /** How many played league matches "Latest Results" lists (a 12-club matchday is 6 matches). */
    private static final int LATEST_RESULTS = 6;

    /** The league's most recently played matches, newest first (G-03). */
    private List<Match> getLatestResults(League league) {
        List<Match> played = new java.util.ArrayList<>();
        if (league == null || league.getLeagueClubs() == null) {
            return played;
        }
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getPlayedMatches() == null) {
                continue;
            }
            for (Match match : club.getPlayedMatches()) {
                if (match != null && match.getId() != null && match.getIsPlayed() != null && match.getIsPlayed()
                        && match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH
                        && match.getHomeGoals() != null && match.getAwayGoals() != null && seen.add(match.getId())) {
                    played.add(match);
                }
            }
        }
        played.sort(Comparator.comparing(Match::getMatchDateTime, Comparator.nullsFirst(Comparator.naturalOrder()))
            .reversed());
        return played.size() > LATEST_RESULTS ? new java.util.ArrayList<>(played.subList(0, LATEST_RESULTS)) : played;
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
                return LanguageModLoader.getValue("league_tb_points");
            case GOAL_DIFFERENCE:
                return LanguageModLoader.getValue("league_tb_goal_difference");
            case GOAL_AVERAGE:
                return LanguageModLoader.getValue("league_tb_goal_average");
            case GOALS_SCORED:
                return LanguageModLoader.getValue("league_tb_goals_scored");
            case GOALS_CONCEDED:
                return LanguageModLoader.getValue("league_tb_goals_conceded");
            case HEAD_TO_HEAD:
                return LanguageModLoader.getValue("league_tb_head_to_head");
            case ALPHABETICAL:
                return LanguageModLoader.getValue("league_tb_alphabetical");
            default:
                return breaker.toString();
        }
    }
}

