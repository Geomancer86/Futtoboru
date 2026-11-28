package com.rndmodgames.futtoboru.tables.match.history;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Match History Screen Table v1
 * 
 * Displays all played matches with scores and allows clicking to view details
 * 
 * sources:
 *      - https://cdn.footballmanager.com/site/inline-images/Watford_%20Supporters.png
 *      - https://content.invisioncic.com/Msigames/monthly_2021_01/Example01.jpg.657daeda224e917f2763020b2c0e443e.jpg
 *      - https://fminside.net/resources/uploads/img/Home-screen-2-1536x932.png
 * 
 * @author Geomancer86
 */
public class MatchHistoryScreenTable extends VisTable {

    //
    Futtoboru game;
    private MainMenuManager mainMenuManager;
    
    // Dynamic Club
    private Club currentClub;
    
    // Selected match for viewing details
    private Match selectedMatch;
    
    // Dynamic Components
    VisTable mainTable = new VisTable(true);
    
    public MatchHistoryScreenTable(Game parent) {
        
        //
        super(true);
        
        this.game = (Futtoboru) parent;
    }
    
    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }
    
    // 
    public void updateDynamicComponents() {
        
        // Refresh current club reference from game state
        if (game != null && game.getSession().getGame() != null) {
            Club refreshedClub = game.getSession().getGame().getCurrentClub();
            if (refreshedClub != null && (currentClub == null || !refreshedClub.getId().equals(currentClub.getId()))) {
                currentClub = refreshedClub;
            }
        }
        
        if (currentClub == null) {
            this.clear();
            this.add(LanguageModLoader.getValue("match_no_club"));
            return;
        }
        
        Log.info(Log.UI_MATCH, "SHOWING MATCH HISTORY SCREEN - PLAYED MATCHES: " + currentClub.getPlayedMatches().size());
        
        this.clear();
        
        if (currentClub.getPlayedMatches() == null || currentClub.getPlayedMatches().isEmpty()) {
            this.row();
            this.add(LanguageModLoader.getValue("match_none_played"));
            return;
        }
        
        // Sort matches by date (most recent first)
        java.util.List<Match> sortedMatches = new java.util.ArrayList<>(currentClub.getPlayedMatches());
        Collections.sort(sortedMatches, (match1, match2) -> {
            if (match1.getMatchDateTime() == null || match2.getMatchDateTime() == null) {
                return 0;
            }
            return match2.getMatchDateTime().compareTo(match1.getMatchDateTime()); // Descending (newest first)
        });
        
        // Header
        this.row().padTop(UiTheme.space("sm")).padBottom(UiTheme.space("sm"));
        VisLabel headerLabel = new VisLabel(LanguageModLoader.getValue("match_history_title"));
        headerLabel.setFontScale(UiTheme.fontScale("title"));
        this.add(headerLabel).colspan(4).padBottom(UiTheme.space("md"));
        
        // Column headers
        this.row().padBottom(UiTheme.space("xs"));
        this.add(new VisLabel(LanguageModLoader.getValue("history_date"))).width(UiTheme.size("col.number"));
        this.add(new VisLabel(LanguageModLoader.getValue("history_home"))).expandX().left();
        this.add(new VisLabel(LanguageModLoader.getValue("history_score"))).width(UiTheme.size("col.value"));
        this.add(new VisLabel(LanguageModLoader.getValue("history_away"))).expandX().left();
        
        // Separator
        this.row();
        this.addSeparator().colspan(4);
        
        // Date formatter
        DateTimeFormatter dateFormatter = GameFormat.formatter(GameFormat.MEDIUM); // U-12: the game language, not the machine's
        
        // Display matches
        for (Match match : sortedMatches) {
            
            // Get clubs (use SaveGame, not DatabaseLoader for current game state)
            Club homeClub = game.getSession().getGame().getClubById(match.getHomeClubId());
            Club awayClub = game.getSession().getGame().getClubById(match.getAwayClubId());
            
            if (homeClub == null || awayClub == null) {
                Log.error(Log.UI_MATCH, "MatchHistoryScreenTable", "Could not find clubs for match");
                continue;
            }
            
            // Date
            this.row().padTop(UiTheme.space("xs")).padBottom(UiTheme.space("xs"));
            String matchDate = match.getMatchDateTime() != null 
                ? match.getMatchDateTime().format(dateFormatter) 
                : LanguageModLoader.getValue("unknown");
            this.add(new VisLabel(matchDate)).width(UiTheme.size("col.number"));
            
            // Home team, score and away team open the match result and report (G-03)
            this.add(matchLink(homeClub.getName(), match)).expandX().left();
            int homeGoals = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
            int awayGoals = match.getAwayGoals() != null ? match.getAwayGoals() : 0;
            this.add(matchLink(homeGoals + " - " + awayGoals, match)).width(UiTheme.size("col.value"));
            this.add(matchLink(awayClub.getName(), match)).expandX().left();
        }
    }
    
    /**
     * A link that opens the match. A LinkLabel without its own listener opens its text as a web address (VisUI's
     * default), so the row's links used to send "Preston North End" to the browser as well.
     */
    private LinkLabel matchLink(String text, Match match) {
        LinkLabel link = new LinkLabel(text);
        link.setListener(new LinkLabel.LinkLabelListener() {
            @Override
            public void clicked(String url) {
                viewMatchDetails(match);
            }
        });
        return link;
    }

    /**
     * View match details by navigating to match result screen
     */
    private void viewMatchDetails(Match match) {
        if (match == null || mainMenuManager == null) {
            return;
        }
        
        selectedMatch = match;
        Log.info(Log.UI_MATCH, "MatchHistoryScreenTable", "Match clicked: " + match.getId());

        // G-03: the result screen shows this match, its goals with minutes and its report
        mainMenuManager.showMatchResult(match);
    }

    public Club getCurrentClub() {
        return currentClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.currentClub = currentClub;
    }
    
    public Match getSelectedMatch() {
        return selectedMatch;
    }
}