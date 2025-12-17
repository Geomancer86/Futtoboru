package com.rndmodgames.futtoboru.tables.match.history;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;

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
        if (game != null && game.getCurrentGame() != null) {
            Club refreshedClub = game.getCurrentGame().getCurrentClub();
            if (refreshedClub != null && (currentClub == null || !refreshedClub.getId().equals(currentClub.getId()))) {
                currentClub = refreshedClub;
            }
        }
        
        if (currentClub == null) {
            this.clear();
            this.add("No club selected.");
            return;
        }
        
        System.out.println("SHOWING MATCH HISTORY SCREEN - PLAYED MATCHES: " + currentClub.getPlayedMatches().size());
        
        this.clear();
        
        if (currentClub.getPlayedMatches() == null || currentClub.getPlayedMatches().isEmpty()) {
            this.row();
            this.add("No matches played yet.");
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
        this.row().padTop(10).padBottom(10);
        VisLabel headerLabel = new VisLabel("MATCH HISTORY");
        headerLabel.setFontScale(1.3f);
        this.add(headerLabel).colspan(4).padBottom(15);
        
        // Column headers
        this.row().padBottom(5);
        this.add(new VisLabel("Date")).width(100);
        this.add(new VisLabel("Home")).expandX().left();
        this.add(new VisLabel("Score")).width(80);
        this.add(new VisLabel("Away")).expandX().left();
        
        // Separator
        this.row();
        this.addSeparator().colspan(4);
        
        // Date formatter
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        
        // Display matches
        for (Match match : sortedMatches) {
            
            // Get clubs (use SaveGame, not DatabaseLoader for current game state)
            Club homeClub = game.getCurrentGame().getClubById(match.getHomeClubId());
            Club awayClub = game.getCurrentGame().getClubById(match.getAwayClubId());
            
            if (homeClub == null || awayClub == null) {
                Gdx.app.error("MatchHistoryScreenTable", "Could not find clubs for match");
                continue;
            }
            
            // Date
            this.row().padTop(5).padBottom(5);
            String matchDate = match.getMatchDateTime() != null 
                ? match.getMatchDateTime().format(dateFormatter) 
                : "Unknown";
            this.add(new VisLabel(matchDate)).width(100);
            
            // Home team (clickable)
            String homeTeamName = homeClub.getName();
            LinkLabel homeTeamLink = new LinkLabel(homeTeamName);
            homeTeamLink.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    viewMatchDetails(match);
                }
            });
            this.add(homeTeamLink).expandX().left();
            
            // Score (clickable)
            int homeGoals = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
            int awayGoals = match.getAwayGoals() != null ? match.getAwayGoals() : 0;
            String scoreText = homeGoals + " - " + awayGoals;
            LinkLabel scoreLink = new LinkLabel(scoreText);
            scoreLink.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    viewMatchDetails(match);
                }
            });
            this.add(scoreLink).width(80);
            
            // Away team (clickable)
            String awayTeamName = awayClub.getName();
            LinkLabel awayTeamLink = new LinkLabel(awayTeamName);
            awayTeamLink.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    viewMatchDetails(match);
                }
            });
            this.add(awayTeamLink).expandX().left();
        }
    }
    
    /**
     * View match details by navigating to match result screen
     */
    private void viewMatchDetails(Match match) {
        if (match == null || mainMenuManager == null) {
            return;
        }
        
        // Store selected match (we'll need to add a setSelectedMatch method to MatchResultScreenTable)
        selectedMatch = match;
        
        // TODO: For now, we can only show the most recent match in MatchResultScreenTable
        // In the future, we should add setSelectedMatch() to MatchResultScreenTable
        Gdx.app.log("MatchHistoryScreenTable", "Match clicked: " + 
            (match.getHomeGoals() != null ? match.getHomeGoals() : 0) + " - " + 
            (match.getAwayGoals() != null ? match.getAwayGoals() : 0));
        
        // For now, just log - we'll need to enhance MatchResultScreenTable to accept a specific match
        // mainMenuManager.setActiveMainScreen(MainMenuManager.MATCH_RESULT_SCREEN);
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