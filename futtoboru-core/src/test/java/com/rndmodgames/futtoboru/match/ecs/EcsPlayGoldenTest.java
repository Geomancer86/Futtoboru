package com.rndmodgames.futtoboru.match.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * Golden hashes of {@code EcsMatchEngine.play} over full matches (162,000 ticks); see BallPhysicsTest for the rule.
 * Moved out of MatchWorldTest by E-15 when the match became a real one (about 2 s each).
 *
 * <p>History: E-13 (ecs-0.2) 22 standing bodies; E-14 (ecs-0.3) skills, action command, possession; E-16 (ecs-0.4)
 * officials and the laws' play state, nobody kicks off. Re-pinned by E-15 (ecs-0.5): the player AI plays the match
 * (TeamTacticsComponent on two TEAM entities and a PerceptionComponent per player appended to the hashed components),
 * so the hash now pins a full match with goals. Re-pinned by E-16b (ecs-0.6): AthleteComponent keeper flag hashed,
 * the ball bounces off posts and bars, forwards play up and run in behind, offsides are given. Re-pinned by E-DET
 * (ecs-0.7): RandomSource has its own Gaussian, so these hashes are the same on every JDK (17.0.2, 25, ...).
 */
@Tag("slow")
class EcsPlayGoldenTest {

    static final long GOLDEN_PLAY_DRY = 0x0fea6e1ba940f97fL;
    static final long GOLDEN_PLAY_RAIN = 0x77a5d7e4a8864a8bL;

    @Test
    void playIsDeterministicAndPinnedByAGoldenHash() {
        EcsMatchEngine engine = new EcsMatchEngine();
        RandomSource game = RandomSource.seeded(1888L);
        EcsMatchInput dry = new EcsMatchInput("m1", new UniformPitch(Surface.DRY, PitchConditions.DRY));
        EcsMatchInput rain = new EcsMatchInput("m2", new UniformPitch(Surface.WET, PitchConditions.RAIN));
        EcsMatchResult a = engine.play(dry, game.fork("match:m1"));
        EcsMatchResult b = engine.play(rain, game.fork("match:m2"));
        assertEquals(a, engine.play(dry, game.fork("match:m1")));
        assertEquals(162_000, a.ticks());
        assertEquals(EcsMatchEngine.ENGINE_VERSION, a.engineVersion());
        assertTrue(a.ballActions() > 1000, "a real match: " + a);
        // BKP §1.3 table, 400 g ball in rain: 476 g at half-time, 526 g at full time (dry ball stays 400 g).
        assertEquals(0.400, a.fullTimeBallMassKg(), 0);
        assertEquals(0.476, b.halfTimeBallMassKg(), 0.001);
        assertEquals(0.526, b.fullTimeBallMassKg(), 0.001);
        assertEquals(GOLDEN_PLAY_DRY, a.finalStateHash(), () -> "dry " + Long.toHexString(a.finalStateHash()) + " " + a);
        assertEquals(GOLDEN_PLAY_RAIN, b.finalStateHash(),
                () -> "rain " + Long.toHexString(b.finalStateHash()) + " " + b);
    }
}
