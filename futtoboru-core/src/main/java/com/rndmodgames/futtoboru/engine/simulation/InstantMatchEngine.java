package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.engine.team.TeamStrength;

/**
 * The instant result (v1.0 with G-01 team sheets; moved out of {@link MatchSimulator} by E-17a with the same draws in
 * the same order, so seeded results are unchanged):
 * - strength comes from the eleven on each side's frozen sheet ({@link TeamStrength}: attack and defence by place,
 *   out of position and playing short cost, small shape / style / captain modifiers)
 * - home side x 1.15 (not at a neutral ground, P2-14)
 * - each side x a random 0.7..1.3
 * - goals from a base distribution (30 % 0, 30 % 1, 25 % 2, 10 % 3, 5 % 4-5), each side's attack against the other's
 *   defence, shifted by the strength ratio, cap 5
 * - each goal's scorer is one of the eleven, weighted by place and finishing ({@link TeamStrength#pickScorer})
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
        TeamStrength homeStrength = TeamStrength.of(fixture.homeSheet(), TeamSheets.squadById(homeClub));
        TeamStrength awayStrength = TeamStrength.of(fixture.awaySheet(), TeamSheets.squadById(awayClub));

        double homeAdvantage = fixture.neutral() ? 1.0 : HOME_ADVANTAGE_MULTIPLIER;

        double homeRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        double awayRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));

        double homeAttack = homeStrength.attack() * homeAdvantage * homeRandom;
        double homeDefence = homeStrength.defence() * homeAdvantage * homeRandom;
        double awayAttack = awayStrength.attack() * awayRandom;
        double awayDefence = awayStrength.defence() * awayRandom;

        int homeScore = generateScore(homeAttack, awayDefence, random);
        int awayScore = generateScore(awayAttack, homeDefence, random);

        List<Long> homeScorers = scorers(fixture.homeSheet(), homeClub, homeScore, random);
        List<Long> awayScorers = scorers(fixture.awaySheet(), awayClub, awayScore, random);
        return new MatchOutcome(homeScore, awayScore, homeScorers, awayScorers);
    }

    /** One draw per goal: a man of the sheet ({@link TeamStrength#pickScorer}). */
    private static List<Long> scorers(TeamSheet sheet, Club club, int goals, RandomSource random) {
        List<Long> ids = new ArrayList<>();
        if (goals > 0 && club.getPlayers() != null && !club.getPlayers().isEmpty()) {
            Map<Long, Player> squad = TeamSheets.squadById(club);
            for (int i = 0; i < goals; i++) {
                Player scorer = TeamStrength.pickScorer(sheet, squad, random);
                if (scorer != null && scorer.getPerson() != null) {
                    ids.add(scorer.getPerson().getId());
                }
            }
        }
        return ids;
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
