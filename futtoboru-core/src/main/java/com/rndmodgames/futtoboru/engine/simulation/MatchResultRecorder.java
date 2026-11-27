package com.rndmodgames.futtoboru.engine.simulation;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.engine.cup.CupBracketManager;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DebugLogManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * The single match-result path (P2-2).
 *
 * {@link MatchSimulator#simulateMatch(Match)} only decides the result and writes it into the {@link Match}
 * (goals, scorers, attendance). Everything that follows from a result goes through {@link #record(Match)}:
 * league table (played, won, drawn, lost, goals, points from the league rules), cup advancement / replays /
 * completion, moving the match to both clubs' played lists and the result message.
 *
 * {@link #play(Match, MatchSimulator)} is the one entry point used for every match (the player's match and the
 * day's other matches): it refuses league matches while a mandatory draw message is pending.
 */
public class MatchResultRecorder {

    private final Futtoboru gameInstance;
    private final MessageManager messageManager;

    public MatchResultRecorder(Futtoboru gameInstance, MessageManager messageManager) {
        this.gameInstance = gameInstance;
        this.messageManager = messageManager;
    }

    private SaveGame game() {
        return gameInstance.getCurrentGame();
    }

    /**
     * True when a mandatory, unread draw message (league, cup or fixture draw) is delivered or scheduled.
     * League matches must not be played until the draw has been shown on the draw screen.
     */
    public static boolean hasMandatoryDrawBlocker(SaveGame game) {
        if (game == null) {
            return false;
        }
        if (game.getAllMessages() != null) {
            for (Message message : game.getAllMessages()) {
                if (message == null) {
                    continue;
                }
                boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                boolean isUnread = message.getIsRead() == null || !message.getIsRead();
                boolean isNotDeleted = message.getIsDeleted() == null || !message.getIsDeleted();
                if (isMandatory && isUnread && isNotDeleted && isDrawMessage(message)) {
                    return true;
                }
            }
        }
        // Scheduled (not yet delivered) mandatory draws block too, whatever their date
        if (game.getScheduledMessages() != null) {
            for (Message message : game.getScheduledMessages()) {
                if (message != null && message.getIsMandatory() != null && message.getIsMandatory()
                        && isDrawMessage(message)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isDrawMessage(Message message) {
        String type = message.getMessageType();
        return "LEAGUE_DRAW".equals(type) || "CUP_DRAW".equals(type) || "FIXTURE_DRAW".equals(type);
    }

    /**
     * True when the match can be played now: both clubs known, not played yet, and (for a league match)
     * no mandatory draw message pending.
     */
    public boolean isPlayable(Match match) {
        if (match == null || match.getHomeClubId() == null || match.getAwayClubId() == null) {
            return false;
        }
        if (match.getIsPlayed() != null && match.getIsPlayed()) {
            return false;
        }
        if (isLeagueMatch(match) && hasMandatoryDrawBlocker(game())) {
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "MatchResultRecorder",
                "Not playing league match " + match.getId() + ": a mandatory draw message is pending");
            return false;
        }
        return true;
    }

    /**
     * Simulate and record one match: the one result path for every match.
     *
     * @return true if the match was played and recorded
     */
    public boolean play(Match match, MatchSimulator simulator) {
        if (!isPlayable(match)) {
            return false;
        }
        if (!simulator.simulateMatch(match)) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_MATCH, "MatchResultRecorder",
                "Failed to simulate match " + match.getId());
            return false;
        }
        record(match);
        return true;
    }

    /**
     * Apply a played match to the game state. Recording the same match twice has no effect
     * (a match already in both clubs' played lists is not counted again).
     */
    public void record(Match match) {
        if (match == null || match.getIsPlayed() == null || !match.getIsPlayed()
                || match.getHomeGoals() == null || match.getAwayGoals() == null) {
            return;
        }
        SaveGame game = game();
        Club homeClub = game.getClubById(match.getHomeClubId());
        Club awayClub = game.getClubById(match.getAwayClubId());
        if (homeClub == null || awayClub == null) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_ENGINE_MATCH, "MatchResultRecorder",
                "Cannot record match " + match.getId() + ": club not found");
            return;
        }
        if (isRecorded(match, homeClub) && isRecorded(match, awayClub)) {
            return; // already recorded
        }

        updateLeagueTable(match, homeClub, awayClub);

        if (match.getMatchType() != null && match.getMatchType() == Match.CUP_MATCH
                && match.getCompetitionEditionId() != null) {
            Match next = new CupBracketManager(game, gameInstance).advanceWinner(match);
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_CUP, "MatchResultRecorder",
                "Cup match " + match.getBracketPath() + " recorded"
                + (next != null ? ", winner goes to " + next.getBracketPath() : ""));
        }

        moveToPlayed(match, homeClub);
        moveToPlayed(match, awayClub);

        if (messageManager != null) {
            Message resultMessage = messageManager.createMatchResultMessage(match, homeClub, awayClub);
            if (resultMessage != null) {
                messageManager.deliverMessage(resultMessage);
            }
        }

        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_ENGINE_MATCH, "MatchResultRecorder",
            "Recorded " + homeClub.getName() + " " + match.getHomeGoals() + " - " + match.getAwayGoals()
            + " " + awayClub.getName());
    }

    private static boolean isRecorded(Match match, Club club) {
        if (club.getPlayedMatches() == null) {
            return false;
        }
        for (Match m : club.getPlayedMatches()) {
            if (m == match || (m != null && m.getId() != null && m.getId().equals(match.getId()))) {
                return true;
            }
        }
        return false;
    }

    private static void moveToPlayed(Match match, Club club) {
        if (club.getScheduledMatches() != null) {
            club.getScheduledMatches().remove(match);
        }
        if (!isRecorded(match, club)) {
            club.getPlayedMatches().add(match);
        }
    }

    private static boolean isLeagueMatch(Match match) {
        return match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH;
    }

    /**
     * The Club statistics are the league table: only league matches count (cup ties and friendlies
     * used to add goals and wins/draws/losses, so W+D+L did not add up to P). Points come from the league rules.
     */
    private void updateLeagueTable(Match match, Club homeClub, Club awayClub) {
        if (!isLeagueMatch(match)) {
            return;
        }
        int homeScore = match.getHomeGoals();
        int awayScore = match.getAwayGoals();

        League league = findLeagueForMatch(homeClub, awayClub);
        CompetitionRules rules = league != null ? league.getRulesOrDefault() : CompetitionRules.createDefaultRules();

        homeClub.setMatchesPlayed(homeClub.getMatchesPlayed() + 1);
        awayClub.setMatchesPlayed(awayClub.getMatchesPlayed() + 1);
        homeClub.setGoalsScored(homeClub.getGoalsScored() + homeScore);
        homeClub.setGoalsConceded(homeClub.getGoalsConceded() + awayScore);
        awayClub.setGoalsScored(awayClub.getGoalsScored() + awayScore);
        awayClub.setGoalsConceded(awayClub.getGoalsConceded() + homeScore);

        if (homeScore > awayScore) {
            homeClub.setMatchesWon(homeClub.getMatchesWon() + 1);
            awayClub.setMatchesLost(awayClub.getMatchesLost() + 1);
            homeClub.setPoints(homeClub.getPoints() + rules.getPointsForWin());
            awayClub.setPoints(awayClub.getPoints() + rules.getPointsForLoss());
        } else if (awayScore > homeScore) {
            awayClub.setMatchesWon(awayClub.getMatchesWon() + 1);
            homeClub.setMatchesLost(homeClub.getMatchesLost() + 1);
            awayClub.setPoints(awayClub.getPoints() + rules.getPointsForWin());
            homeClub.setPoints(homeClub.getPoints() + rules.getPointsForLoss());
        } else {
            homeClub.setMatchesDrawn(homeClub.getMatchesDrawn() + 1);
            awayClub.setMatchesDrawn(awayClub.getMatchesDrawn() + 1);
            homeClub.setPoints(homeClub.getPoints() + rules.getPointsForDraw());
            awayClub.setPoints(awayClub.getPoints() + rules.getPointsForDraw());
        }
    }

    /**
     * The league that has both clubs (null if none)
     */
    private League findLeagueForMatch(Club homeClub, Club awayClub) {
        SaveGame game = game();
        if (game.getMainAuthority() == null || game.getMainAuthority().getLeagues() == null) {
            return null;
        }
        for (League league : game.getMainAuthority().getLeagues()) {
            if (league == null || league.getLeagueClubs() == null) {
                continue;
            }
            boolean hasHome = false;
            boolean hasAway = false;
            for (Club club : league.getLeagueClubs()) {
                if (club != null && club.getId() != null) {
                    hasHome |= club.getId().equals(homeClub.getId());
                    hasAway |= club.getId().equals(awayClub.getId());
                }
            }
            if (hasHome && hasAway) {
                return league;
            }
        }
        return null;
    }
}
