package com.rndmodgames.futtoboru.engine.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * Goal minutes for the instant result (G-03), a stand-in until the ECS engine (E-04) times its own events.
 *
 * Runs after the score and the scorers are decided, on its own stream ({@code fork("minutes:" + match)}), so it never
 * changes a score, a scorer or a gate. Every goal gets a different whole minute from 1 to 90 (two halves of 45; the
 * 1888 laws kept no added time). Goals come a little more often late in each half and late in the match, as tired
 * men in heavy boots tire: the weight of a minute rises from 1.0 at the kick-off to 1.4 at the end, with an extra 0.2
 * in the last five minutes of each half. Each side's minutes are stored ascending, so goal i of a side belongs to its
 * scorer i.
 */
public final class GoalMinutes {

    public static final int HALF = 45;
    public static final int FULL_TIME = 90;

    private static final double[] WEIGHTS = new double[FULL_TIME + 1];
    static {
        for (int m = 1; m <= FULL_TIME; m++) {
            double w = 1.0 + 0.4 * (m - 1) / (FULL_TIME - 1);
            if ((m > HALF - 5 && m <= HALF) || m > FULL_TIME - 5) {
                w += 0.2;
            }
            WEIGHTS[m] = w;
        }
    }

    private GoalMinutes() {
    }

    /**
     * Gives every goal of a played match its minute (replacing any minutes it had).
     *
     * @param random the match's own minutes stream
     */
    public static void assign(Match match, RandomSource random) {
        int home = goals(match.getHomeGoals());
        int away = goals(match.getAwayGoals());
        boolean[] taken = new boolean[FULL_TIME + 1];
        match.setHomeGoalMinutes(draw(home, random, taken));
        match.setAwayGoalMinutes(draw(away, random, taken));
    }

    private static int goals(Integer goals) {
        // more goals than minutes can't happen in a real result; clamp so the draw always ends
        return goals == null ? 0 : Math.max(0, Math.min(goals, FULL_TIME / 2));
    }

    /** {@code count} distinct minutes not yet taken, by weight, ascending. */
    private static List<Integer> draw(int count, RandomSource random, boolean[] taken) {
        List<Integer> minutes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double total = 0;
            for (int m = 1; m <= FULL_TIME; m++) {
                if (!taken[m]) {
                    total += WEIGHTS[m];
                }
            }
            double pick = random.nextDouble() * total;
            int chosen = -1;
            for (int m = 1; m <= FULL_TIME; m++) {
                if (taken[m]) {
                    continue;
                }
                chosen = m;
                pick -= WEIGHTS[m];
                if (pick < 0) {
                    break;
                }
            }
            taken[chosen] = true;
            minutes.add(chosen);
        }
        Collections.sort(minutes);
        return minutes;
    }

    /**
     * True when the match carries a minute for each of its goals (a match played before G-03 has none; a goalless
     * match needs none).
     */
    public static boolean hasMinutes(Match match) {
        return match.getHomeGoalMinutes().size() == goals(match.getHomeGoals())
                && match.getAwayGoalMinutes().size() == goals(match.getAwayGoals());
    }
}
