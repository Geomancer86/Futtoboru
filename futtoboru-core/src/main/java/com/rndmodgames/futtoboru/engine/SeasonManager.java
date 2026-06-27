package com.rndmodgames.futtoboru.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Owns simulated season identity and procedural rollover housekeeping.
 *
 * @see DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md
 */
public class SeasonManager {

    private final Futtoboru game;
    private final LeagueStandingsManager standingsManager = new LeagueStandingsManager();

    public SeasonManager(Futtoboru game) {
        this.game = game;
    }

    public void initializeNewGame(SaveGame save, long startSeasonId) {
        save.setStartSeasonId(startSeasonId);
        save.setSimulatedSeasonIndex(0);
    }

    public int getSimulatedSeasonIndex() {
        SaveGame save = game.getCurrentGame();
        return save != null && save.getSimulatedSeasonIndex() != null ? save.getSimulatedSeasonIndex() : 0;
    }

    public boolean isPrimaryLeagueComplete(SaveGame save) {
        if (save == null || save.getMainAuthority() == null || save.getMainAuthority().getLeagues() == null) {
            return false;
        }
        for (League league : save.getMainAuthority().getLeagues()) {
            if (league != null && !standingsManager.isLeagueComplete(league)) {
                return false;
            }
        }
        return !save.getMainAuthority().getLeagues().isEmpty();
    }

    public int countFinalizedLeagueEditions(SaveGame save) {
        if (save == null || save.getMainAuthority() == null || save.getMainAuthority().getLeagues() == null) {
            return 0;
        }
        int count = 0;
        for (League league : save.getMainAuthority().getLeagues()) {
            if (league == null || league.getEditions() == null) {
                continue;
            }
            for (CompetitionEdition edition : league.getEditions()) {
                if (edition != null && edition.getChampionsId() != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Archive played league fixtures and remove all league fixtures from schedules before a new edition.
     */
    public void archiveAndClearLeagueFixtures(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return;
        }
        for (Club club : league.getLeagueClubs()) {
            if (club == null || club.getScheduledMatches() == null) {
                continue;
            }
            Iterator<Match> iterator = club.getScheduledMatches().iterator();
            while (iterator.hasNext()) {
                Match match = iterator.next();
                if (match == null || match.getMatchType() == null
                        || match.getMatchType() != Match.LEAGUE_MATCH) {
                    continue;
                }
                if (Boolean.TRUE.equals(match.getIsPlayed())) {
                    if (club.getPlayedMatches() == null) {
                        club.setPlayedMatches(new ArrayList<>());
                    }
                    club.getPlayedMatches().add(match);
                }
                iterator.remove();
            }
        }
    }

    public void resetLeagueClubStats(League league) {
        if (league == null || league.getLeagueClubs() == null) {
            return;
        }
        for (Club club : league.getLeagueClubs()) {
            if (club == null) {
                continue;
            }
            club.setMatchesPlayed(0);
            club.setMatchesWon(0);
            club.setMatchesDrawn(0);
            club.setMatchesLost(0);
            club.setPoints(0);
            club.setGoalsScored(0);
            club.setGoalsConceded(0);
        }
    }

    public CompetitionRules snapshotRules(League league) {
        if (league == null || league.getRulesOrDefault() == null) {
            return CompetitionRules.createDefaultRules();
        }
        CompetitionRules source = league.getRulesOrDefault();
        CompetitionRules copy = new CompetitionRules();
        copy.setPointsForWin(source.getPointsForWin());
        copy.setPointsForDraw(source.getPointsForDraw());
        copy.setPointsForLoss(source.getPointsForLoss());
        copy.setMatchFormat(source.getMatchFormat());
        copy.getTieBreakingOrder().clear();
        copy.getTieBreakingOrder().addAll(source.getTieBreakingOrder());
        return copy;
    }

    public void onProceduralSeasonGenerated(League league) {
        SaveGame save = game.getCurrentGame();
        if (save == null) {
            return;
        }
        int nextIndex = getSimulatedSeasonIndex() + 1;
        save.setSimulatedSeasonIndex(nextIndex);
        Gdx.app.log("SeasonManager", "Simulated season index now " + nextIndex
                + " for league " + (league != null ? league.getName() : "?"));
    }
}
