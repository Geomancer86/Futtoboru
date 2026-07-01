package com.rndmodgames.futtoboru.tables.match.result;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.engine.simulation.PlayerConditionService;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Match Result Screen Table v1
 * 
 *  - This screen will be accessed immediately after a Match is played with the relevant Match Results
 *  
 *  - Displays match details: teams, score, date, attendance, venue
 * 
 * @author Geomancer86
 */
public class MatchResultScreenTable extends VisTable {

    //
    Futtoboru game;
    
    /** When set, shows this match instead of the latest played match (H-3). */
    private Match displayMatch;
    
    // Dynamic Components
    VisTable mainTable = new VisTable(true);
    
    //
    public MatchResultScreenTable(Game parent) {
        
        //
        super(true);
        
        this.game = (Futtoboru) parent;
    }

    public void setDisplayMatch(Match match) {
        this.displayMatch = match;
    }

    public void clearDisplayMatch() {
        this.displayMatch = null;
    }
    
    //
    public void updateDynamicComponents() {
        
        //
        this.clear();
        
        // Get current club
        Club currentClub = game.getCurrentGame().getCurrentClub();
        
        if (currentClub == null) {
            this.add("No match results available.");
            return;
        }

        Match match = resolveDisplayMatch(currentClub);
        if (match == null) {
            this.add("No match results available.");
            return;
        }
        
        // Get clubs
        Club homeClub = game.getCurrentGame().getClubById(match.getHomeClubId());
        Club awayClub = game.getCurrentGame().getClubById(match.getAwayClubId());
        
        if (homeClub == null || awayClub == null) {
            this.add("Error: Could not load match details.");
            Gdx.app.error("MatchResultScreenTable", "Home or away club not found for match");
            return;
        }
        
        // Format date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String matchDate = match.getMatchDateTime() != null ? match.getMatchDateTime().format(dateFormatter) : "Unknown";
        String matchTime = match.getMatchDateTime() != null ? match.getMatchDateTime().format(timeFormatter) : "Unknown";
        
        // Match Header
        this.row().padTop(20);
        VisLabel headerLabel = new VisLabel("MATCH RESULT");
        headerLabel.setFontScale(1.5f);
        this.add(headerLabel).colspan(2).padBottom(20);
        
        // Match Date and Time
        this.row();
        this.add("Date:").padRight(10);
        this.add(matchDate + " at " + matchTime).left();
        
        // Venue
        this.row().padTop(5);
        this.add("Venue:").padRight(10);
        String venueName = homeClub.getStadium() != null && homeClub.getStadium().getName() != null 
            ? homeClub.getStadium().getName() 
            : "Unknown Stadium";
        this.add(venueName).left();
        
        // Separator
        this.row().padTop(15).padBottom(15);
        this.addSeparator().colspan(2);
        
        // Teams and Score
        this.row().padTop(10);
        
        // Home Team
        VisTable homeTeamTable = new VisTable();
        homeTeamTable.add(new VisLabel(homeClub.getName())).row();
        homeTeamTable.add(new VisLabel(String.valueOf(match.getHomeGoals() != null ? match.getHomeGoals() : 0)))
            .padTop(5);
        this.add(homeTeamTable).expandX().center();
        
        // VS
        this.add(new VisLabel("vs")).padLeft(20).padRight(20);
        
        // Away Team
        VisTable awayTeamTable = new VisTable();
        awayTeamTable.add(new VisLabel(awayClub.getName())).row();
        awayTeamTable.add(new VisLabel(String.valueOf(match.getAwayGoals() != null ? match.getAwayGoals() : 0)))
            .padTop(5);
        this.add(awayTeamTable).expandX().center();
        
        // Result
        this.row().padTop(15);
        String result;
        if (match.getHomeGoals() != null && match.getAwayGoals() != null) {
            if (match.getHomeGoals() > match.getAwayGoals()) {
                result = homeClub.getName() + " won";
            } else if (match.getAwayGoals() > match.getHomeGoals()) {
                result = awayClub.getName() + " won";
            } else {
                result = "Draw";
            }
        } else {
            result = "Result not available";
        }
        VisLabel resultLabel = new VisLabel(result);
        resultLabel.setFontScale(1.2f);
        this.add(resultLabel).colspan(3).padBottom(20);
        
        // Separator
        this.row().padTop(15).padBottom(15);
        this.addSeparator().colspan(3);
        
        // Match Details
        this.row().padTop(10);
        this.add("Match Type:").padRight(10);
        String matchType = match.getMatchType() != null && match.getMatchType() == Match.FRIENDLY_MATCH 
            ? "Friendly" 
            : "Competition";
        this.add(matchType).left();
        
        // Attendance
        this.row().padTop(5);
        this.add("Attendance:").padRight(10);
        String attendance = match.getAttendance() != null ? String.valueOf(match.getAttendance()) : "Unknown";
        this.add(attendance).left();
        
        // Stadium capacity if available
        if (homeClub.getStadium() != null && homeClub.getStadium().getCapacity() != null) {
            this.row().padTop(5);
            this.add("Capacity:").padRight(10);
            this.add(String.valueOf(homeClub.getStadium().getCapacity())).left();
        }

        appendScorers(homeClub, awayClub, match);
        appendInjuries(currentClub, match);
        appendRatings(currentClub, match);
    }

    private void appendScorers(Club homeClub, Club awayClub, Match match) {
        List<Long> homeScorers = match.getHomeScorerIds();
        List<Long> awayScorers = match.getAwayScorerIds();
        if ((homeScorers == null || homeScorers.isEmpty()) && (awayScorers == null || awayScorers.isEmpty())) {
            return;
        }

        this.row().padTop(15).padBottom(10);
        this.addSeparator().colspan(3);
        this.row().padTop(10);
        VisLabel scorersHeader = new VisLabel("SCORERS");
        scorersHeader.setFontScale(1.1f);
        this.add(scorersHeader).colspan(3).padBottom(8);

        if (homeScorers != null && !homeScorers.isEmpty()) {
            this.row();
            this.add(homeClub.getName() + ":").padRight(10);
            this.add(formatScorerList(homeClub, homeScorers)).colspan(2).left();
        }
        if (awayScorers != null && !awayScorers.isEmpty()) {
            this.row().padTop(4);
            this.add(awayClub.getName() + ":").padRight(10);
            this.add(formatScorerList(awayClub, awayScorers)).colspan(2).left();
        }
    }

    private void appendInjuries(Club currentClub, Match match) {
        if (currentClub == null || match == null || currentClub.getId() == null) {
            return;
        }
        java.util.List<Long> injuredIds;
        if (currentClub.getId().equals(match.getHomeClubId())) {
            injuredIds = match.getHomeInjuredPlayerIds();
        } else if (currentClub.getId().equals(match.getAwayClubId())) {
            injuredIds = match.getAwayInjuredPlayerIds();
        } else {
            return;
        }
        if (injuredIds == null || injuredIds.isEmpty()) {
            return;
        }
        java.time.LocalDate asOf = match.getMatchDateTime() != null
                ? match.getMatchDateTime().toLocalDate() : java.time.LocalDate.now();

        this.row().padTop(15).padBottom(10);
        this.addSeparator().colspan(3);
        this.row().padTop(10);
        VisLabel header = new VisLabel("INJURIES");
        header.setFontScale(1.1f);
        this.add(header).colspan(3).padBottom(8);

        for (Long playerId : injuredIds) {
            Player player = findPlayer(currentClub, playerId);
            if (player == null) {
                continue;
            }
            PlayerCondition condition = PlayerConditionService.conditionOf(player);
            String label = condition.getInjuryDisplayName() != null
                    ? condition.getInjuryDisplayName() : "Injury";
            int weeks = condition.estimateWeeksOut(asOf);
            this.row().padTop(2);
            this.add(playerName(currentClub, playerId)).padRight(10);
            this.add(label + " (~" + weeks + " wk)").colspan(2).left();
        }
    }

    private Player findPlayer(Club club, Long playerId) {
        if (club == null || playerId == null || club.getPlayers() == null) {
            return null;
        }
        for (Player player : club.getPlayers()) {
            if (player != null && playerId.equals(player.getId())) {
                return player;
            }
        }
        return null;
    }

    private void appendRatings(Club currentClub, Match match) {
        if (currentClub == null || match == null) {
            return;
        }
        Map<Long, Float> ratings = null;
        Club ratedClub = currentClub;
        if (currentClub.getId().equals(match.getHomeClubId())) {
            ratings = match.getHomePlayerRatings();
        } else if (currentClub.getId().equals(match.getAwayClubId())) {
            ratings = match.getAwayPlayerRatings();
        }
        if (ratings == null || ratings.isEmpty()) {
            return;
        }

        this.row().padTop(15).padBottom(10);
        this.addSeparator().colspan(3);
        this.row().padTop(10);
        VisLabel ratingsHeader = new VisLabel("PLAYER RATINGS");
        ratingsHeader.setFontScale(1.1f);
        this.add(ratingsHeader).colspan(3).padBottom(8);

        for (Map.Entry<Long, Float> entry : ratings.entrySet()) {
            this.row().padTop(2);
            this.add(playerName(ratedClub, entry.getKey())).padRight(10);
            this.add(String.format("%.1f", entry.getValue())).left();
        }
    }

    private String formatScorerList(Club club, List<Long> scorerIds) {
        StringBuilder builder = new StringBuilder();
        for (Long scorerId : scorerIds) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(playerName(club, scorerId));
        }
        return builder.toString();
    }

    private String playerName(Club club, Long playerId) {
        if (club == null || playerId == null || club.getPlayers() == null) {
            return "?";
        }
        for (Player player : club.getPlayers()) {
            if (player != null && playerId.equals(player.getId()) && player.getPerson() != null) {
                return player.getPerson().getName() + " " + player.getPerson().getLastname();
            }
        }
        return "Player#" + playerId;
    }

    private Match resolveDisplayMatch(Club currentClub) {
        if (displayMatch != null) {
            return displayMatch;
        }
        if (currentClub.getPlayedMatches() == null || currentClub.getPlayedMatches().isEmpty()) {
            return null;
        }
        return currentClub.getPlayedMatches().get(currentClub.getPlayedMatches().size() - 1);
    }
}