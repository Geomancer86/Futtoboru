package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * The instant result (v1.0, moved out of {@link MatchSimulator} by E-17a; same draws in the same order, so seeded
 * results are unchanged):
 * - team strength = average of key player attributes, home side x 1.15
 * - each side x a random 0.7..1.3
 * - goals from a base distribution (30 % 0, 30 % 1, 25 % 2, 10 % 3, 5 % 4-5) shifted by the strength ratio, cap 5
 * - each goal's scorer is any player of the squad
 *
 * @author Geomancer86
 */
public final class InstantMatchEngine implements MatchEngine {

    private static final double HOME_ADVANTAGE_MULTIPLIER = 1.15; // 15% home advantage
    private static final double RANDOMNESS_MIN = 0.7;
    private static final double RANDOMNESS_MAX = 1.3;
    private static final int MAX_GOALS = 5; // Cap goals at 5 for v1.0

    @Override
    public String id() {
        return INSTANT;
    }

    @Override
    public MatchOutcome play(MatchFixture fixture, RandomSource random) {
        Club homeClub = fixture.home();
        Club awayClub = fixture.away();
        double homeStrength = calculateTeamStrength(homeClub) * HOME_ADVANTAGE_MULTIPLIER;
        double awayStrength = calculateTeamStrength(awayClub);

        double homeRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        double awayRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        double homeFinal = homeStrength * homeRandom;
        double awayFinal = awayStrength * awayRandom;

        int homeScore = generateScore(homeFinal, awayFinal, random);
        int awayScore = generateScore(awayFinal, homeFinal, random);

        List<Long> homeScorers = scorers(homeClub, homeScore, random);
        List<Long> awayScorers = scorers(awayClub, awayScore, random);
        return new MatchOutcome(homeScore, awayScore, homeScorers, awayScorers);
    }

    /** Any player of the squad scores each goal (future: weight by position and attributes). */
    private static List<Long> scorers(Club club, int goals, RandomSource random) {
        List<Long> ids = new ArrayList<>();
        List<Player> players = club.getPlayers();
        if (goals > 0 && players != null && !players.isEmpty()) {
            for (int i = 0; i < goals; i++) {
                Player scorer = players.get(random.nextInt(players.size()));
                if (scorer != null && scorer.getPerson() != null) {
                    ids.add(scorer.getPerson().getId());
                }
            }
        }
        return ids;
    }

    /** Average of physical, mental and technical key attributes over the squad; 50 without players. */
    static double calculateTeamStrength(Club club) {
        if (club == null || club.getPlayers() == null || club.getPlayers().isEmpty()) {
            return 50.0;
        }
        double totalStrength = 0.0;
        int playerCount = 0;
        for (Player player : club.getPlayers()) {
            if (player == null) {
                continue;
            }
            Float[] attributes = { player.getAcceleration(), player.getSpeed(), player.getStrength(),
                    player.getStamina(), player.getDetermination(), player.getPositioning(), player.getTeamwork(),
                    player.getPassing(), player.getKicking(), player.getTackling() };
            double playerStrength = 0.0;
            int attributeCount = 0;
            for (Float a : attributes) {
                if (a != null) {
                    playerStrength += a;
                    attributeCount++;
                }
            }
            if (attributeCount > 0) {
                totalStrength += (playerStrength / attributeCount);
                playerCount++;
            }
        }
        return playerCount == 0 ? 50.0 : totalStrength / playerCount;
    }

    /** Goals for one side from the base distribution, shifted by its share of the combined strength. */
    private static int generateScore(double teamStrength, double opponentStrength, RandomSource random) {
        double totalStrength = teamStrength + opponentStrength;
        double strengthRatio = totalStrength > 0 ? teamStrength / totalStrength : 0.5;

        double r = random.nextDouble();
        int baseGoals;
        if (r < 0.3) {
            baseGoals = 0;
        } else if (r < 0.6) {
            baseGoals = 1;
        } else if (r < 0.85) {
            baseGoals = 2;
        } else if (r < 0.95) {
            baseGoals = 3;
        } else {
            baseGoals = 4 + random.nextInt(2);
        }

        if (strengthRatio > 0.6) {
            baseGoals += 1;
        } else if (strengthRatio > 0.55) {
            if (random.nextDouble() < 0.5) {
                baseGoals += 1;
            }
        } else if (strengthRatio < 0.4) {
            baseGoals = Math.max(0, baseGoals - 1);
        } else if (strengthRatio < 0.45) {
            if (random.nextDouble() < 0.5) {
                baseGoals = Math.max(0, baseGoals - 1);
            }
        }
        return Math.min(MAX_GOALS, baseGoals);
    }
}
