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
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

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
            contentTable.add(new VisLabel(LanguageModLoader.getValue("cup_none_selected"))).pad(UiTheme.space("lg")).row();
            return;
        }
        
        // Title
        VisLabel titleLabel = new VisLabel(selectedCup.getName() != null ? selectedCup.getName() : LanguageModLoader.getValue("competitions_unnamed_cup"));
        titleLabel.setFontScale(UiTheme.fontScale("title"));
        contentTable.add(titleLabel).pad(UiTheme.space("sm")).row();
        
        contentTable.addSeparator().pad(UiTheme.space("xs")).row();
        
        // Cup Info Section
        contentTable.add(new VisLabel(LanguageModLoader.getValue("cup_information"))).pad(UiTheme.space("xs")).row();
        
        if (currentEdition != null) {
            contentTable.add(new VisLabel(LanguageModLoader.format("cup_season", currentEdition.getName()))).left().pad(UiTheme.space("xxs")).row();
            Integer participants = currentEdition.getParticipantClubs();
            String participantsText = participants != null ? String.valueOf(participants) : "0";
            contentTable.add(new VisLabel(LanguageModLoader.format("cup_participants", participantsText))).left().pad(UiTheme.space("xxs")).row();
        }
        
        contentTable.add().height(UiTheme.space("sm")).row();
        
        // Past Champions Section
        if (selectedCup.getEditions() != null && !selectedCup.getEditions().isEmpty()) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("competitions_past_champions"))).pad(UiTheme.space("xs")).row();
            contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
            
            // Show completed editions (those with champions)
            int completedCount = 0;
            for (CompetitionEdition edition : selectedCup.getEditions()) {
                if (edition.getChampionsId() != null) {
                    completedCount++;
                    Club champion = currentGame.getClubById(edition.getChampionsId());
                    Club runnerUp = edition.getRunnersUpId() != null ? currentGame.getClubById(edition.getRunnersUpId()) : null;
                    
                    String seasonInfo = edition.getName() != null ? edition.getName() : LanguageModLoader.format("competitions_edition_n", completedCount);
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
                contentTable.add(new VisLabel(LanguageModLoader.getValue("cup_no_completed"))).left().pad(UiTheme.space("xxs")).row();
            }
            
            contentTable.add().height(UiTheme.space("sm")).row();
        }
        
        // View Bracket Button
        VisTextButton bracketButton = new VisTextButton(LanguageModLoader.getValue("cup_view_bracket"));
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
        contentTable.add(bracketButton).pad(UiTheme.space("sm")).row();
        
        contentTable.addSeparator().pad(UiTheme.space("xs")).row();
        
        // Fixtures and Results - Show ALL rounds (complete schedule)
        List<Match> matches = getEditionMatches();
        if (!matches.isEmpty()) {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("cup_schedule"))).pad(UiTheme.space("xs")).row();
            contentTable.addSeparator().pad(UiTheme.space("xxs")).row();
            
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
                VisLabel roundLabel = new VisLabel(LanguageModLoader.format("cup_round_heading", roundName, round));
                roundLabel.setColor(UiTheme.color("text.heading"));
                contentTable.add(roundLabel).padTop(UiTheme.space("sm")).padBottom(UiTheme.space("xs")).row();
                
                // Matches in this round
                for (Match match : roundMatches) {
                    VisTable row = new VisTable(true);
                    
                    // Date
                    String dateStr = match.getMatchDateTime() != null ? match.getMatchDateTime().format(dateFormatter) : LanguageModLoader.getValue("competitions_tbd");
                    row.add(new VisLabel(dateStr)).width(UiTheme.size("col.number"));
                    
                    // Home team or "Winner of X"
                    String homeName = getTeamNameOrWinner(match, true);
                    row.add(new VisLabel(homeName)).width(UiTheme.size("col.label")).right();
                    
                    // Score or vs
                    String score = " " + LanguageModLoader.getValue("match_vs") + " ";
                    if (match.getIsPlayed() != null && match.getIsPlayed() && 
                        match.getHomeGoals() != null && match.getAwayGoals() != null) {
                        score = " " + match.getHomeGoals() + " - " + match.getAwayGoals() + " ";
                    }
                    row.add(new VisLabel(score)).width(UiTheme.size("col.score")).center();
                    
                    // Away team or "Winner of X"
                    String awayName = getTeamNameOrWinner(match, false);
                    row.add(new VisLabel(awayName)).width(UiTheme.size("col.label")).left();
                    
                    contentTable.add(row).pad(UiTheme.space("xxs")).row();
                }
            }
        } else {
            contentTable.add(new VisLabel(LanguageModLoader.getValue("cup_no_matches"))).pad(UiTheme.space("lg")).row();
        }
        
        contentTable.add().height(UiTheme.space("lg")).row();
        
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
                return LanguageModLoader.format("cup_winner_of", parentMatch.getBracketPath());
            }
            return LanguageModLoader.format("cup_winner_of_match", parentMatchId);
        }

        return LanguageModLoader.getValue("competitions_tbd");
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
        if (participants == 2) return LanguageModLoader.getValue("cup_round_final");
        if (participants == 4) return LanguageModLoader.getValue("cup_round_semi_final");
        if (participants == 8) return LanguageModLoader.getValue("cup_round_quarter_final");

        String[] ordinals = {"", LanguageModLoader.getValue("cup_round_first"), LanguageModLoader.getValue("cup_round_second"),
                LanguageModLoader.getValue("cup_round_third"), LanguageModLoader.getValue("cup_round_fourth"),
                LanguageModLoader.getValue("cup_round_fifth"), LanguageModLoader.getValue("cup_round_sixth")};
        if (roundNumber > 0 && roundNumber < ordinals.length) {
            return ordinals[roundNumber];
        }
        return LanguageModLoader.format("cup_round_n", roundNumber);
    }
}

