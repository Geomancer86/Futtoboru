package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.injuries.InjuryRules;
import com.rndmodgames.futtoboru.engine.injuries.MatchInjuries;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.engine.team.TeamStrength;

/**
 * The instant result (v1.0 with G-01 team sheets; moved out of {@link MatchSimulator} by E-17a with the same draws in
 * the same order, so seeded results are unchanged):
 * - strength comes from the eleven on each side's frozen sheet ({@link TeamStrength}: attack and defence by place,
 *   out of position and playing short cost, small shape / style / captain modifiers)
 * - home side x 1.15 (not at a neutral ground, P2-14)
 * - each side x a random 0.7..1.3, x its hurt factor (G-02: men hurt in the match)
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

        // G-02 (D19-3): a passenger is worth less from his minute on, a man carried off nothing while off
        double homeHurt = hurtFactor(onField(fixture.homeSheet(), homeClub).size(), fixture.homeInjuries(),
                fixture.injuryRules());
        double awayHurt = hurtFactor(onField(fixture.awaySheet(), awayClub).size(), fixture.awayInjuries(),
                fixture.injuryRules());

        double homeAdvantage = fixture.neutral() ? 1.0 : HOME_ADVANTAGE_MULTIPLIER;

        double homeRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));
        double awayRandom = RANDOMNESS_MIN + (random.nextDouble() * (RANDOMNESS_MAX - RANDOMNESS_MIN));

        double homeAttack = homeStrength.attack() * homeAdvantage * homeRandom * homeHurt;
        double homeDefence = homeStrength.defence() * homeAdvantage * homeRandom * homeHurt;
        double awayAttack = awayStrength.attack() * awayRandom * awayHurt;
        double awayDefence = awayStrength.defence() * awayRandom * awayHurt;

        int homeScore = generateScore(homeAttack, awayDefence, random);
        int awayScore = generateScore(awayAttack, homeDefence, random);

        List<Long> homeScorers = scorers(fixture.homeSheet(), homeClub, fixture.homeInjuries(), homeScore, random);
        List<Long> awayScorers = scorers(fixture.awaySheet(), awayClub, fixture.awayInjuries(), awayScore, random);
        return new MatchOutcome(homeScore, awayScore, homeScorers, awayScorers);
    }

    /**
     * One draw per goal: a man of the sheet ({@link TeamStrength#pickScorer}); G-02: a man carried off for good
     * scores no more (kept simple: not at all in this match).
     */
    private static List<Long> scorers(TeamSheet sheet, Club club, List<MatchInjury> injuries, int goals,
            RandomSource random) {
        List<Long> ids = new ArrayList<>();
        if (goals > 0 && club.getPlayers() != null && !club.getPlayers().isEmpty()) {
            for (int i = 0; i < goals; i++) {
                Map<Long, Player> squad = TeamSheets.squadById(club);
                if (injuries != null && !injuries.isEmpty()) {
                    squad.values().removeIf(p -> p.getPerson() != null && injuries.stream().anyMatch(
                            in -> MatchInjuries.leftForGood(in) && p.getPerson().getId().equals(in.getPersonId())));
                }
                Player scorer = TeamStrength.pickScorer(sheet, squad, random);
                if (scorer != null && scorer.getPerson() != null) {
                    ids.add(scorer.getPerson().getId());
                }
            }
        }
        return ids;
    }

    /** G-02: the men on the sheet's places, in slot order (empty places: the side plays short). */
    static List<Player> onField(TeamSheet sheet, Club club) {
        List<Player> out = new ArrayList<>();
        if (sheet == null) {
            return out;
        }
        Map<Long, Player> squad = TeamSheets.squadById(club);
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            Player p = sheet.getPlayerId(s) != null ? squad.get(sheet.getPlayerId(s)) : null;
            if (p != null) {
                out.add(p);
            }
        }
        return out;
    }

    /**
     * G-02: what the side is worth over the match next to the same men unhurt (1 when nobody is hurt; playing short
     * from kick-off is already in {@link TeamStrength}).
     */
    static double hurtFactor(int menOnField, List<MatchInjury> injuries, InjuryRules rules) {
        if (menOnField == 0 || injuries == null || injuries.isEmpty()) {
            return 1.0;
        }
        return MatchInjuries.effectiveMen(menOnField, injuries, rules) / menOnField;
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
