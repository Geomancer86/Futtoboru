package com.rndmodgames.futtoboru.match.ecs.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.view.IndexActorSprites.Motion;

/**
 * E-21c sprite selection rules, pure (no art): direction from heading, animation from body state, keeper and official
 * sets, ball variants, mud, crowd moods, and the crisp scale choice.
 */
class SpriteSelectionTest {

    private static final double FALL_S = 4 / 6.0;

    @Test
    void directionsFollowTheIndexSouthIsTheCameraEastIsScreenRight() {
        assertEquals(0, IndexActorSprites.direction(-Math.PI / 2), "toward the camera (-y) = S");
        assertEquals(1, IndexActorSprites.direction(-Math.PI / 4), "SE");
        assertEquals(2, IndexActorSprites.direction(0), "screen right (+x) = E");
        assertEquals(3, IndexActorSprites.direction(Math.PI / 4), "NE");
        assertEquals(4, IndexActorSprites.direction(Math.PI / 2), "away (+y) = N");
        assertEquals(5, IndexActorSprites.direction(3 * Math.PI / 4), "NW");
        assertEquals(6, IndexActorSprites.direction(Math.PI), "W");
        assertEquals(6, IndexActorSprites.direction(-Math.PI), "W from the other side");
        assertEquals(7, IndexActorSprites.direction(-3 * Math.PI / 4), "SW");
        assertEquals(2, IndexActorSprites.direction(Math.toRadians(20)), "rounds to the nearest of 8");
        assertEquals(3, IndexActorSprites.direction(Math.toRadians(25)));
        assertEquals(2, IndexActorSprites.direction(2 * Math.PI), "wraps");
    }

    @Test
    void directionComesFromFacingThenMotionThenTheBall() {
        ActorSprites.Pose p = new ActorSprites.Pose().clear();
        p.hasFacing = true;
        p.facing = Math.PI;
        p.vx = 5;
        assertEquals(6, IndexActorSprites.direction(p), "a body's facing wins");
        p.hasFacing = false;
        assertEquals(2, IndexActorSprites.direction(p), "no facing: motion");
        p.vx = 0;
        p.x = 0;
        p.y = 0;
        p.ballX = 0;
        p.ballY = 10;
        assertEquals(4, IndexActorSprites.direction(p), "standing: faces the ball");
        p.ballY = 0.1;
        assertEquals(0, IndexActorSprites.direction(p), "on the ball: faces the camera");
    }

    @Test
    void animationFollowsBodyState() {
        assertEquals(Motion.IDLE, IndexActorSprites.motion(false, 0.1, false, 0, false, 0, FALL_S));
        assertEquals(Motion.RUN, IndexActorSprites.motion(false, 3, false, 0, false, 0, FALL_S));
        assertEquals(Motion.CHARGE, IndexActorSprites.motion(false, 3, false, 0, true, 0, FALL_S));
        assertEquals(Motion.FALL, IndexActorSprites.motion(false, 0, true, 0, false, 0.1, FALL_S), "going down");
        assertEquals(Motion.FALL, IndexActorSprites.motion(false, 0, true, 0, false, 5, FALL_S),
                "down, not hurt: holds the fall");
        assertEquals(Motion.INJURED, IndexActorSprites.motion(false, 0, true, 1, false, 5, FALL_S),
                "down and hurt: lies clutching a knee after the fall");
        assertEquals(Motion.FALL, IndexActorSprites.motion(false, 0, true, 2, false, 0.2, FALL_S),
                "hurt: the fall plays first");
        assertEquals(Motion.WALK, IndexActorSprites.motion(true, 2, true, 3, true, 9, FALL_S),
                "officials only walk or stand");
        assertEquals(Motion.IDLE, IndexActorSprites.motion(true, 0.1, false, 0, false, 0, FALL_S));
    }

    @Test
    void keeperAndOfficialsUseTheirOwnSets() {
        assertEquals("player/run", IndexActorSprites.animationName(Motion.RUN, false, false, 7));
        assertEquals("player/injured", IndexActorSprites.animationName(Motion.INJURED, false, false, 4));
        assertEquals("player/charge", IndexActorSprites.animationName(Motion.CHARGE, false, false, 4));
        assertEquals("keeper/run", IndexActorSprites.animationName(Motion.RUN, false, true, 0));
        assertEquals("keeper/run", IndexActorSprites.animationName(Motion.CHARGE, false, true, 0), "no keeper charge");
        assertEquals("keeper/fall", IndexActorSprites.animationName(Motion.INJURED, false, true, 0),
                "no keeper injured frames: the fall");
        assertEquals("keeper/idle", IndexActorSprites.animationName(Motion.IDLE, false, true, 0));
        assertEquals("referee/walk", IndexActorSprites.animationName(Motion.WALK, true, false, 0));
        assertEquals("umpire-a/idle", IndexActorSprites.animationName(Motion.IDLE, true, false, 1));
        assertEquals("umpire-b/walk", IndexActorSprites.animationName(Motion.WALK, true, false, 2));
    }

    @Test
    void frameRateFollowsSpeedWithinLimits() {
        assertEquals(1.0, IndexActorSprites.rate(6, 6), 1e-9);
        assertEquals(0.5, IndexActorSprites.rate(3, 6), 1e-9);
        assertEquals(0.4, IndexActorSprites.rate(0.5, 6), 1e-9);
        assertEquals(1.6, IndexActorSprites.rate(20, 6), 1e-9);
    }

    @Test
    void ballVariantsByHeightAndWaterAndMudByWetness() {
        assertEquals("ball/ground/dry", IndexActorSprites.ballAnimation(0.1, 0));
        assertEquals("ball/high/dry", IndexActorSprites.ballAnimation(3, 0.05));
        assertEquals("ball/ground/damp", IndexActorSprites.ballAnimation(0, 0.2));
        assertEquals("ball/ground/soaked", IndexActorSprites.ballAnimation(0, 0.5));
        assertEquals("ball/high/muddy", IndexActorSprites.ballAnimation(2, 0.9));
        assertEquals(0f, IndexActorSprites.mudAlpha(PitchConditions.DRY), 1e-6);
        assertTrue(IndexActorSprites.mudAlpha(PitchConditions.DAMP) < IndexActorSprites.mudAlpha(PitchConditions.DRIZZLE));
        assertEquals(IndexActorSprites.MUD_MAX_ALPHA, IndexActorSprites.mudAlpha(PitchConditions.RAIN), 1e-6);
        assertEquals(IndexActorSprites.MUD_MAX_ALPHA, IndexActorSprites.mudAlpha(PitchConditions.WATERLOGGED), 1e-6);
    }

    @Test
    void crowdMoodsFollowTheirThresholds() {
        assertEquals(CrowdPass.CALM, CrowdPass.mood(0, 0.3f));
        assertEquals(CrowdPass.CHEER, CrowdPass.mood(0.5, 0.3f));
        assertEquals(CrowdPass.CALM, CrowdPass.mood(0.5, 0.7f), "a calmer man needs more");
        assertEquals(CrowdPass.ANGRY, CrowdPass.mood(-0.5, 0.3f));
        assertEquals(CrowdPass.ENCROACH, CrowdPass.mood(-0.95, 0.3f), "the least patient encroach");
        assertEquals(CrowdPass.ANGRY, CrowdPass.mood(-0.95, 0.6f));
    }

    @Test
    void scalePicksTheClosestBakedSheetWithWholeScreenPixelTexels() {
        // magnification 1: middle and far side 1x, near touchline 2x
        int mid = SpriteScale.choose(5.0, 1, 2);
        assertEquals(1, SpriteScale.sheet(mid));
        assertEquals(1, SpriteScale.texel(mid));
        int far = SpriteScale.choose(3.3, 1, 2);
        assertEquals(1, SpriteScale.sheet(far), "smaller than 1x: still 1x, never a fractional texel");
        assertEquals(1, SpriteScale.texel(far));
        int near = SpriteScale.choose(10.3, 1, 2);
        assertEquals(2, SpriteScale.sheet(near));
        assertEquals(1, SpriteScale.texel(near));
        // in between: the closest (by ratio)
        assertEquals(1, SpriteScale.sheet(SpriteScale.choose(6.9, 1, 2)));
        assertEquals(2, SpriteScale.sheet(SpriteScale.choose(7.2, 1, 2)));
        // magnification 2: the 2x sheet at one screen pixel per texel (half size in art pixels)
        int zoom = SpriteScale.choose(5.0, 2, 2);
        assertEquals(2, SpriteScale.sheet(zoom));
        assertEquals(1, SpriteScale.texel(zoom));
        assertEquals(5.0, SpriteScale.drawnPixelsPerMetre(zoom, 2), 1e-9);
        // magnification 3 in the middle: 1x at three screen pixels per texel is exact
        int three = SpriteScale.choose(5.0, 3, 2);
        assertEquals(1, SpriteScale.sheet(three));
        assertEquals(3, SpriteScale.texel(three));
        // 1x-only sheets (crowd, props) scale by whole texels
        int crowdNear = SpriteScale.choose(10.3, 1, 1);
        assertEquals(1, SpriteScale.sheet(crowdNear));
        assertEquals(2, SpriteScale.texel(crowdNear));
        for (double p = 3; p < 14; p += 0.37) {
            for (int m = 1; m <= 6; m++) {
                int c = SpriteScale.choose(p, m, 2);
                assertTrue(SpriteScale.texel(c) >= 1 && SpriteScale.sheet(c) >= 1 && SpriteScale.sheet(c) <= 2);
                double drawn = SpriteScale.drawnPixelsPerMetre(c, m);
                assertTrue(drawn / p < 1.8 && p / drawn < 2.2, p + " @" + m + " drawn at " + drawn);
            }
        }
    }

    @Test
    void snapPutsPivotsOnTheScreenPixelGrid() {
        assertEquals(10.5f, SpriteScale.snap(10.4, 2), 1e-6);
        assertEquals(10f, SpriteScale.snap(10.2, 2), 1e-6);
        assertEquals(10.333333f, SpriteScale.snap(10.3, 3), 1e-5);
        assertEquals(10f, SpriteScale.snap(10.4, 1), 1e-6);
    }
}
