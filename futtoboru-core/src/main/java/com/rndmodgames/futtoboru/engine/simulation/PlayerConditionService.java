package com.rndmodgames.futtoboru.engine.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerCondition;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Daily ticks and post-match updates for player condition (B5 / B5.1).
 */
public final class PlayerConditionService {

    public static final int LOW_FITNESS_WARNING_THRESHOLD = 60;
    public static final int HIGH_FATIGUE_THRESHOLD = 50;

    private static final int MATCH_FITNESS_COST = 5;
    private static final int REST_FITNESS_GAIN = 2;
    private static final int REST_FITNESS_GAIN_TIRED = 1;
    private static final int MATCH_SHARPNESS_GAIN = 5;
    private static final int MATCH_FATIGUE_GAIN = 8;
    private static final int DAILY_FATIGUE_RECOVERY = 3;
    private static final int GOAL_MORALE_BONUS = 2;

    private PlayerConditionService() {
    }

    public static PlayerCondition conditionOf(Player player) {
        if (player == null) {
            return PlayerCondition.createFitDefault();
        }
        if (player.getCondition() == null) {
            player.setCondition(PlayerCondition.createFitDefault());
        }
        return player.getCondition();
    }

    public static boolean isAvailableForLineup(Player player, LocalDate today) {
        PlayerCondition condition = conditionOf(player);
        if (condition.isInjured(today)) {
            if (condition.getInjuryReturnDate() != null && condition.getInjuryReturnDate().isBefore(today)) {
                condition.clearInjury();
                return true;
            }
            return false;
        }
        return true;
    }

    public static boolean isLowFitness(Player player) {
        return conditionOf(player).getFitnessPct() < LOW_FITNESS_WARNING_THRESHOLD;
    }

    public static double fitnessStrengthFactor(Player player) {
        int fitness = conditionOf(player).getFitnessPct();
        return 0.5 + (fitness / 200.0);
    }

    public static double sharpnessStrengthFactor(Player player) {
        int sharpness = conditionOf(player).getSharpnessPct();
        return 0.7 + (sharpness / 333.0);
    }

    public static void tickDaily(SaveGame game) {
        if (game == null || game.getAllClubs() == null) {
            return;
        }
        LocalDate today = game.getGameDate() != null ? game.getGameDate().toLocalDate() : LocalDate.now();
        for (Club club : game.getAllClubs()) {
            if (club == null || club.getPlayers() == null) {
                continue;
            }
            boolean playedYesterday = clubHadMatchOnDate(club, today.minusDays(1));
            for (Player player : club.getPlayers()) {
                if (player == null) {
                    continue;
                }
                PlayerCondition condition = conditionOf(player);
                if (condition.getInjuryReturnDate() != null
                        && condition.getInjuryReturnDate().isBefore(today)) {
                    condition.clearInjury();
                }
                if (!playedYesterday) {
                    int gain = condition.getFatiguePct() > HIGH_FATIGUE_THRESHOLD
                            ? REST_FITNESS_GAIN_TIRED : REST_FITNESS_GAIN;
                    condition.setFitnessPct(clampPct(condition.getFitnessPct() + gain));
                    int sharpness = condition.getSharpnessPct() - 1;
                    condition.setSharpnessPct(Math.max(40, sharpness));
                }
                condition.setFatiguePct(clampPct(condition.getFatiguePct() - DAILY_FATIGUE_RECOVERY));
            }
        }
    }

    public static void applyPostMatchEffects(Match match, MatchLineup homeLineup, Club homeClub,
            MatchLineup awayLineup, Club awayClub) {
        applyPostMatchEffects(match, homeLineup, homeClub, awayLineup, awayClub, null, null, null);
    }

    public static void applyPostMatchEffects(Match match, MatchLineup homeLineup, Club homeClub,
            MatchLineup awayLineup, Club awayClub, ClubStaffManager staffManager,
            MessageManager messageManager, Long humanManagedClubId) {
        if (match == null) {
            return;
        }
        match.setHomeInjuredPlayerIds(new java.util.ArrayList<>());
        match.setAwayInjuredPlayerIds(new java.util.ArrayList<>());
        applyForSide(match, homeLineup, homeClub, match.getHomePlayerRatings(), true,
                staffManager, messageManager, humanManagedClubId);
        applyForSide(match, awayLineup, awayClub, match.getAwayPlayerRatings(), false,
                staffManager, messageManager, humanManagedClubId);
    }

    private static void applyForSide(Match match, MatchLineup lineup, Club club,
            Map<Long, Float> ratings, boolean isHome, ClubStaffManager staffManager,
            MessageManager messageManager, Long humanManagedClubId) {
        if (lineup == null || club == null) {
            return;
        }
        int homeGoals = match.getHomeGoals() != null ? match.getHomeGoals() : 0;
        int awayGoals = match.getAwayGoals() != null ? match.getAwayGoals() : 0;
        boolean won = isHome ? homeGoals > awayGoals : awayGoals > homeGoals;
        boolean lost = isHome ? homeGoals < awayGoals : awayGoals < homeGoals;
        LocalDate today = match.getMatchDateTime() != null
                ? match.getMatchDateTime().toLocalDate() : LocalDate.now();
        Random injuryRng = new Random(match.getId() != null ? match.getId() * 31L : 42L);
        List<Long> scorers = isHome ? match.getHomeScorerIds() : match.getAwayScorerIds();

        for (MatchLineupBuilder.PlayerRef ref : MatchLineupBuilder.playersInLineup(lineup, club)) {
            if (ref.player == null) {
                continue;
            }
            PlayerCondition condition = conditionOf(ref.player);
            condition.setFitnessPct(clampPct(condition.getFitnessPct() - MATCH_FITNESS_COST));
            condition.setSharpnessPct(clampPct(condition.getSharpnessPct() + MATCH_SHARPNESS_GAIN));
            condition.setFatiguePct(clampPct(condition.getFatiguePct() + MATCH_FATIGUE_GAIN));

            float rating = ratings != null && ref.player.getId() != null
                    ? ratings.getOrDefault(ref.player.getId(), MatchRatingsCalculator.BASE_RATING)
                    : MatchRatingsCalculator.BASE_RATING;
            int morale = condition.getMorale();
            if (won) {
                morale += 3;
            } else if (lost) {
                morale -= 2;
            }
            if (scorers != null && ref.player.getId() != null && scorers.contains(ref.player.getId())) {
                morale += GOAL_MORALE_BONUS;
            }
            if (rating >= 8.0f) {
                morale += 1;
            } else if (rating < 5.0f) {
                morale -= 1;
            }
            condition.setMorale(clampPct(morale));

            if (InjuryGenerator.applyInjury(ref.player, condition, club, staffManager, injuryRng, today).isApplied()
                    && ref.player.getId() != null) {
                if (isHome) {
                    match.getHomeInjuredPlayerIds().add(ref.player.getId());
                } else {
                    match.getAwayInjuredPlayerIds().add(ref.player.getId());
                }
                notifyInjuryInbox(club, ref.player, condition, messageManager, humanManagedClubId);
            }
        }
    }

    private static void notifyInjuryInbox(Club club, Player player, PlayerCondition condition,
            MessageManager messageManager, Long humanManagedClubId) {
        if (messageManager == null || club == null || player == null || condition == null) {
            return;
        }
        if (humanManagedClubId == null || !humanManagedClubId.equals(club.getId())) {
            return;
        }
        if (condition.getInjurySeverityEnum() == null
                || condition.getInjurySeverityEnum() == com.rndmodgames.futtoboru.data.InjurySeverity.MINOR) {
            return;
        }
        messageManager.createInjuryNewsMessage(player, condition);
    }

    private static boolean clubHadMatchOnDate(Club club, LocalDate date) {
        if (club.getPlayedMatches() == null || date == null) {
            return false;
        }
        for (Match match : club.getPlayedMatches()) {
            if (match != null && match.getMatchDateTime() != null
                    && match.getMatchDateTime().toLocalDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    static int clampPct(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
