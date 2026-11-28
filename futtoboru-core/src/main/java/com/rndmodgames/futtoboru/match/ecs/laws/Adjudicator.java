package com.rndmodgames.futtoboru.match.ecs.laws;

import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * How an appeal is decided (E-16; ECS §4.2, V2 §2.4). The truth of the claim is a signed {@code evidence} (> 0: the
 * claim is right). Each official sees it with Gaussian noise that grows with his distance from the incident (and, for
 * offside, with how far he stands from being level with the line) and adds his lean toward his club:
 * <pre>
 *   vote = evidence + sigma * N(0,1) + partisanship * lean * (his club appeals ? +1 : -1) > 0
 *   sigma = perception * (base + perM * distance + perLineM * |x - lineX|)
 * </pre>
 * Until 1891 the two club umpires vote; if they agree that is the decision, if not the referee (off the field,
 * neutral) decides from his own view. From 1891 ({@link RulesProfile#refereeOnField()}) the referee decides alone.
 * With no officials on the ground the truth decides. Every judgement draws exactly three numbers from the
 * {@code officials:noise} fork, whoever votes, so the stream never depends on the outcome.
 */
public final class Adjudicator {

    private final RandomSource noise;

    /** Noise of one kind of appeal: base, per metre of distance, per metre off the line, lean at partisanship 1. */
    public record Noise(double base, double perM, double perLineM, double lean) {
    }

    public Adjudicator(RandomSource noise) {
        if (noise == null) {
            throw new IllegalArgumentException("noise");
        }
        this.noise = noise;
    }

    /**
     * Decides one appeal and writes the votes into {@code record}.
     *
     * @param officials      the officials on the ground ({@code [0..count)})
     * @param appealingTeam  {@code Team.ordinal()} of the side making the claim
     * @param lineX          offside only: x of the offside line (the umpire's angle on it), NaN otherwise
     * @return true if the claim is upheld
     */
    public boolean judge(OfficialComponent[] officials, int count, boolean refereeOnField, LawRecord record,
            int appealingTeam, double evidence, Noise n, double x, double y, double lineX) {
        double g0 = noise.nextGaussian();
        double g1 = noise.nextGaussian();
        double g2 = noise.nextGaussian();
        OfficialComponent referee = null;
        OfficialComponent home = null;
        OfficialComponent away = null;
        for (int i = 0; i < count; i++) {
            OfficialComponent o = officials[i];
            if (o.isReferee()) {
                referee = referee == null ? o : referee;
            } else if (o.club == 0) {
                home = home == null ? o : home;
            } else {
                away = away == null ? o : away;
            }
        }
        record.appealed = true;
        record.appealingTeam = appealingTeam;
        record.evidence = evidence;
        boolean given;
        if (referee == null && home == null && away == null) {
            given = evidence > 0;
        } else if (refereeOnField || home == null || away == null) {
            OfficialComponent alone = referee != null ? referee : (home != null ? home : away);
            boolean v = vote(alone, g2, appealingTeam, evidence, n, x, y, lineX);
            setVote(record, alone, v);
            given = v;
        } else {
            boolean vh = vote(home, g0, appealingTeam, evidence, n, x, y, lineX);
            boolean va = vote(away, g1, appealingTeam, evidence, n, x, y, lineX);
            record.homeUmpireVote = vh ? LawRecord.VOTE_YES : LawRecord.VOTE_NO;
            record.awayUmpireVote = va ? LawRecord.VOTE_YES : LawRecord.VOTE_NO;
            if (vh == va) {
                given = vh;
            } else {
                record.refereeCalled = true;
                if (referee == null) {
                    given = evidence > 0;
                } else {
                    given = vote(referee, g2, appealingTeam, evidence, n, x, y, lineX);
                    record.refereeVote = given ? LawRecord.VOTE_YES : LawRecord.VOTE_NO;
                }
            }
        }
        record.given = given;
        return given;
    }

    private static void setVote(LawRecord record, OfficialComponent o, boolean v) {
        int code = v ? LawRecord.VOTE_YES : LawRecord.VOTE_NO;
        if (o.isReferee()) {
            record.refereeCalled = true;
            record.refereeVote = code;
        } else if (o.club == 0) {
            record.homeUmpireVote = code;
        } else {
            record.awayUmpireVote = code;
        }
    }

    /** One official's vote with the standard normal draw {@code g}. */
    static boolean vote(OfficialComponent o, double g, int appealingTeam, double evidence, Noise n, double x, double y,
            double lineX) {
        double dx = o.x - x;
        double dy = o.y - y;
        double sigma = n.base() + n.perM() * Math.sqrt(dx * dx + dy * dy);
        if (!Double.isNaN(lineX)) {
            sigma += n.perLineM() * Math.abs(o.x - lineX);
        }
        sigma *= o.perception;
        double lean = o.club < 0 ? 0 : o.partisanship * n.lean() * (o.club == appealingTeam ? 1 : -1);
        return evidence + sigma * g + lean > 0;
    }
}
