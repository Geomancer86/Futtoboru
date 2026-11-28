package com.rndmodgames.futtoboru.engine.injuries;

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.InjuryKind;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * G-02 (D19-3) on the instant result: who is hurt in a match, how, when and for how long, and what it costs the side.
 * Drawn on its own stream ({@code fork("injuries:<match>")}, one fork per side), so the rest of the result draws the
 * same numbers whether or not anyone is hurt.
 */
public final class MatchInjuries {

    public static final int MINUTES = 90;
    /** A carried-off man who comes back is off this many minutes at least (and at most this + {@link #RETURN_SPAN}). */
    static final int RETURN_AFTER = 10;
    static final int RETURN_SPAN = 20;

    private MatchInjuries() {
    }

    /**
     * One draw per man in side order: carried off, passenger, knock or nothing by the kinds' chances; then the minute,
     * a carried-off man's return, and the days out from the kind's range.
     */
    public static List<MatchInjury> roll(List<Player> side, InjuryRules rules, RandomSource rng) {
        List<MatchInjury> out = new ArrayList<>();
        InjuryKind[] order = { InjuryKind.CARRIED_OFF, InjuryKind.PASSENGER, InjuryKind.KNOCK };
        for (Player p : side) {
            double u = rng.nextDouble();
            double cum = 0;
            InjuryKind hit = null;
            for (InjuryKind k : order) {
                cum += rules.get(k).chancePerMatch;
                if (u < cum) {
                    hit = k;
                    break;
                }
            }
            if (hit == null) {
                continue;
            }
            InjuryRules.Rule rule = rules.get(hit);
            int minute = 1 + rng.nextInt(MINUTES);
            Integer back = null;
            if (hit == InjuryKind.CARRIED_OFF && rng.nextDouble() < rule.returnChance) {
                int b = minute + RETURN_AFTER + rng.nextInt(RETURN_SPAN + 1);
                back = b < MINUTES ? b : null; // too late to come back on
            }
            int days = rule.minDays + rng.nextInt(rule.maxDays - rule.minDays + 1);
            Long personId = p != null && p.getPerson() != null ? p.getPerson().getId() : null;
            out.add(new MatchInjury(personId, hit, minute, back, days));
        }
        return out;
    }

    /**
     * Men the side is worth over the match: those who took the field less what the hurt ones lost (a passenger counts
     * {@code strengthWhileHurt} from his minute on; a carried-off man nothing while off, then as a passenger).
     */
    public static double effectiveMen(int menOnField, List<MatchInjury> injuries, InjuryRules rules) {
        double men = menOnField;
        for (MatchInjury i : injuries) {
            InjuryKind k = i.getKindEnum();
            if (k == null || k == InjuryKind.KNOCK) {
                continue;
            }
            double s = rules.get(k).strengthWhileHurt;
            int from = i.getMinute();
            if (k == InjuryKind.PASSENGER) {
                men -= (1 - s) * (MINUTES - from) / (double) MINUTES;
            } else {
                int back = i.getReturnedMinute() != null ? i.getReturnedMinute() : MINUTES;
                men -= (back - from) / (double) MINUTES;
                men -= (1 - s) * (MINUTES - back) / (double) MINUTES;
            }
        }
        return Math.max(0, men);
    }

    /** True when the man was carried off and did not come back (he cannot score after it: kept simple, not at all). */
    public static boolean leftForGood(MatchInjury i) {
        return i.getKindEnum() == InjuryKind.CARRIED_OFF && i.getReturnedMinute() == null;
    }
}
