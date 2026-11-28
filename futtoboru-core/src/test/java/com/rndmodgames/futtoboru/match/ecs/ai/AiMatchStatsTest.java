package com.rndmodgames.futtoboru.match.ecs.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchEngine;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchInput;
import com.rndmodgames.futtoboru.match.ecs.EcsMatchResult;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.body.TeamSheet;

/**
 * E-15 first rough check (calibration proper is E-20): N seeded full matches between stub sides; goals per game and
 * home / draw / away shares against the 1888-89 Football League (4.45 goals per game; 59 / 17 / 24 %). Only sanity
 * bounds are asserted; the numbers are logged for the report. {@code -Dai.stats.matches=N} plays more.
 */
@Tag("slow")
class AiMatchStatsTest {

    @Test
    void seededMatchesGiveAFirstScoreline() {
        int n = Integer.getInteger("ai.stats.matches", 12);
        EcsMatchEngine engine = new EcsMatchEngine();
        RandomSource game = RandomSource.seeded(1888L);
        int goals = 0;
        int home = 0;
        int draw = 0;
        int away = 0;
        long actions = 0;
        long restarts = 0;
        long t0 = System.nanoTime();
        StringBuilder scores = new StringBuilder();
        for (int m = 0; m < n; m++) {
            boolean wet = m % 3 == 2;
            TeamStyle hs = m % 2 == 0 ? TeamStyle.DRIBBLING : TeamStyle.COMBINATION;
            TeamStyle as = m % 4 < 2 ? TeamStyle.DRIBBLING : TeamStyle.COMBINATION;
            EcsMatchInput in = new EcsMatchInput("s" + m,
                    wet ? new UniformPitch(Surface.WET, PitchConditions.RAIN) : new UniformPitch(Surface.DRY, 0), 0,
                    45, new EcsMatchInput.Side(TeamSheet.stub(Shape.TWO_THREE_FIVE), hs),
                    new EcsMatchInput.Side(TeamSheet.stub(Shape.TWO_THREE_FIVE), as));
            EcsMatchResult r = engine.play(in, game.fork("match:s" + m));
            goals += r.homeGoals() + r.awayGoals();
            home += r.homeGoals() > r.awayGoals() ? 1 : 0;
            draw += r.homeGoals() == r.awayGoals() ? 1 : 0;
            away += r.homeGoals() < r.awayGoals() ? 1 : 0;
            actions += r.ballActions();
            restarts += r.offsides() + r.fouls();
            scores.append(r.homeGoals()).append('-').append(r.awayGoals()).append(' ');
        }
        double ms = (System.nanoTime() - t0) / 1e6 / n;
        String line = String.format(Locale.ROOT,
                "E-15 %d matches: %.2f goals/game (target 4.45), H/D/A %.0f/%.0f/%.0f %% (target 59/17/24),"
                        + " %d ball actions and %d offsides+fouls given per match, %.0f ms per match (%.2f us/tick); scores %s",
                n, goals / (double) n, 100.0 * home / n, 100.0 * draw / n, 100.0 * away / n, actions / n,
                restarts / n, ms, ms * 1000 / 162_000, scores);
        System.out.println(line);
        assertTrue(goals > 0, line);
        assertTrue(actions / n > 300, line);
    }
}
