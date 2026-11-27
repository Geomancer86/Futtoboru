package com.rndmodgames.futtoboru.match.ecs.pitch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** E-12: every 1888-89 restart decided from a crossing and the last touch, plus era switches. */
class RestartDeciderTest {

    private static final double R = 0.11;
    private static final double EPS = 1e-9;

    private final PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
    private final RestartDecider laws = new RestartDecider(pitch, RestartLaws.laws1888());
    /** HOME defends WEST, so HOME attacks the EAST goal. */
    private final MatchEnds ends = new MatchEnds(Team.HOME);

    private LineCrossing east(double y, double z) {
        double hl = pitch.halfLength();
        LineCrossing c = pitch.sweep(BallSegment.of(hl - 5, y, z, hl + 2, y, z, R)).crossing();
        assertTrue(c != null && c.line() == LineCrossing.Line.GOAL_LINE);
        return c;
    }

    @Test
    void matchEndsAndHalfTimeChange() {
        assertEquals(Team.HOME, ends.defenderOf(PitchEnd.WEST));
        assertEquals(Team.HOME, ends.attackerOf(PitchEnd.EAST));
        assertEquals(PitchEnd.EAST, ends.endDefendedBy(Team.AWAY));
        RestartDecider.HalfTime ht = laws.halfTime(ends, Team.AWAY);
        assertEquals(Team.AWAY, ht.ends().defenderOf(PitchEnd.WEST), "ends change at half-time");
        assertEquals(Team.HOME, ht.kickOff().team(), "the other side kicks off the second half");
        assertEquals(RestartType.KICK_OFF, ht.kickOff().type());
    }

    @Test
    void kickOffIsFromTheCentreAndCannotScoreDirect() {
        Restart k = laws.kickOff(Team.HOME);
        assertEquals(RestartType.KICK_OFF, k.type());
        assertEquals(Team.HOME, k.team());
        assertEquals(pitch.centre(), k.spot());
        assertTrue(k.offsideApplies());
        assertFalse(k.goalDirectAllowed());
    }

    @Test
    void throwInGoesAgainstTheSideThatPutItOut() {
        LineCrossing c = pitch.sweep(BallSegment.of(12, -30, R, 13, -35, R, R)).crossing();
        LawDecision d = laws.decide(c, Team.HOME, null, ends);
        assertFalse(d.goal());
        assertFalse(d.disputed());
        assertNull(d.ifReversed());
        Restart t = d.restart();
        assertEquals(RestartType.THROW_IN, t.type());
        assertEquals(Team.AWAY, t.team());
        assertEquals(pitch.touchlineY(Flank.SOUTH), t.spot().y(), EPS);
        assertEquals(c.ballCentre().x(), t.spot().x(), EPS);
        assertTrue(t.twoHandsOverhead(), "two hands from above the head since 1883");
        assertTrue(t.offsideApplies(), "offside from a throw-in 1878-1920");
        assertFalse(t.goalDirectAllowed());
    }

    @Test
    void throwInSpotStaysOnTheTouchlineNearTheCorner() {
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        // Out over the touchline just past the goal-line extension (ball centre still within radius of the line).
        LineCrossing c = pitch.sweep(BallSegment.of(hl + 0.05, hw - 1, R, hl + 0.05, hw + 1, R, R)).crossing();
        assertEquals(LineCrossing.Line.TOUCHLINE, c.line());
        Restart t = laws.decide(c, Team.AWAY, null, ends).restart();
        assertEquals(Team.HOME, t.team());
        assertEquals(hl, t.spot().x(), EPS);
    }

    @Test
    void goalForTheAttackersThenKickOffByTheSideThatLostIt() {
        LawDecision d = laws.decide(east(0, 1), Team.HOME, null, ends);
        assertTrue(d.goal());
        assertEquals(Team.HOME, d.scoringTeam());
        assertFalse(d.disputed());
        assertEquals(RestartType.KICK_OFF, d.restart().type());
        assertEquals(Team.AWAY, d.restart().team());
        assertEquals(pitch.centre(), d.restart().spot());
    }

    @Test
    void ownGoalCountsForTheAttackers() {
        LawDecision d = laws.decide(east(1, 0.5), Team.AWAY, null, ends);
        assertTrue(d.goal());
        assertEquals(Team.HOME, d.scoringTeam());
        // Second half, ends changed: the EAST goal now belongs to HOME.
        LawDecision second = laws.decide(east(1, 0.5), Team.HOME, null, ends.changed());
        assertEquals(Team.AWAY, second.scoringTeam());
        assertEquals(Team.HOME, second.restart().team());
    }

    @Test
    void behindOffAnAttackerIsAGoalKickNearTheNearestPost() {
        LawDecision d = laws.decide(east(-10, 1), Team.HOME, null, ends);
        assertFalse(d.goal());
        Restart g = d.restart();
        assertEquals(RestartType.GOAL_KICK, g.type());
        assertEquals(Team.AWAY, g.team());
        Vec3 post = pitch.postBase(PitchEnd.EAST, Flank.SOUTH);
        assertTrue(g.spot().minus(post).length() <= 6 * PitchParams.YARD + EPS, "within 6 yd of the nearest post");
        assertTrue(g.spot().x() < pitch.halfLength(), "on the field");
        assertFalse(g.offsideApplies(), "no offside from a goal kick");
        assertTrue(g.goalDirectAllowed(), "allowed until 1890");
    }

    @Test
    void behindOffADefenderIsACornerWithinAYardOfTheFlag() {
        LawDecision d = laws.decide(east(12, 1), Team.AWAY, null, ends);
        Restart c = d.restart();
        assertEquals(RestartType.CORNER_KICK, c.type());
        assertEquals(Team.HOME, c.team());
        Vec3 flag = pitch.cornerFlag(PitchEnd.EAST, Flank.NORTH);
        double dist = c.spot().minus(flag).length();
        assertTrue(dist <= PitchParams.YARD && dist > 0, "within 1 yd of the flag: " + dist);
        assertTrue(c.spot().x() < pitch.halfLength() && c.spot().y() < pitch.halfWidth(), "inside the field");
        assertFalse(c.offsideApplies(), "no offside from a corner");
        assertFalse(c.goalDirectAllowed(), "no goal direct from a corner 1875-1924");
        // West end, south side, in the second half.
        double hl = pitch.halfLength();
        LineCrossing w = pitch.sweep(BallSegment.of(-hl + 2, -15, R, -hl - 2, -15, R, R)).crossing();
        Restart wc = laws.decide(w, Team.AWAY, null, ends.changed()).restart();
        assertEquals(RestartType.CORNER_KICK, wc.type());
        assertEquals(Team.HOME, wc.team());
        assertTrue(wc.spot().minus(pitch.cornerFlag(PitchEnd.WEST, Flank.SOUTH)).length() <= PitchParams.YARD);
    }

    @Test
    void noGoalDirectFromACornerOrKickOffIn1888() {
        LawDecision fromCorner = laws.decide(east(0, 1), Team.HOME, RestartType.CORNER_KICK, ends);
        assertFalse(fromCorner.goal());
        assertEquals(RestartType.GOAL_KICK, fromCorner.restart().type());
        assertEquals(Team.AWAY, fromCorner.restart().team());
        assertFalse(laws.decide(east(0, 1), Team.HOME, RestartType.KICK_OFF, ends).goal());
        assertFalse(laws.decide(east(0, 1), Team.HOME, RestartType.FREE_KICK, ends).goal(), "all free kicks indirect");
        assertTrue(laws.decide(east(0, 1), Team.HOME, RestartType.GOAL_KICK, ends).goal());
        // A defender's throw straight into his own goal: not a goal, a corner.
        LawDecision ownThrow = laws.decide(east(0, 1), Team.AWAY, RestartType.THROW_IN, ends);
        assertFalse(ownThrow.goal());
        assertEquals(RestartType.CORNER_KICK, ownThrow.restart().type());
    }

    @Test
    void laterSeasonsSwitchTheDirectAndOffsideRules() {
        RestartDecider l1890 = new RestartDecider(pitch, RestartLaws.forSeason(1890));
        assertFalse(l1890.decide(east(0, 1), Team.HOME, RestartType.GOAL_KICK, ends).goal());
        RestartDecider l1903 = new RestartDecider(pitch, RestartLaws.forSeason(1903));
        assertTrue(l1903.decide(east(0, 1), Team.HOME, RestartType.FREE_KICK, ends).goal());
        RestartDecider l1924 = new RestartDecider(pitch, RestartLaws.forSeason(1924));
        assertTrue(l1924.decide(east(0, 1), Team.HOME, RestartType.CORNER_KICK, ends).goal());
        assertFalse(RestartLaws.forSeason(1920).offsideFromThrowIn());
        assertTrue(RestartLaws.forSeason(1919).offsideFromThrowIn());
        assertEquals(RestartLaws.laws1888(), RestartLaws.forSeason(1888));
    }

    @Test
    void disputedGoalCarriesTheReversal() {
        LineCrossing close = east(pitch.goalMouthHalfWidth() - R - 0.03, 1);
        assertTrue(close.disputed());
        LawDecision d = laws.decide(close, Team.HOME, null, ends);
        assertTrue(d.goal());
        assertTrue(d.disputed());
        LawDecision rev = d.ifReversed();
        assertFalse(rev.goal());
        assertEquals(RestartType.GOAL_KICK, rev.restart().type());
        assertNull(rev.ifReversed());
        // Off a defender: reversed it is a corner.
        assertEquals(RestartType.CORNER_KICK, laws.decide(close, Team.AWAY, null, ends).ifReversed().restart().type());
    }

    @Test
    void disputedNearMissCanBeGivenAsAGoal() {
        LineCrossing over = east(0, pitch.crossbarUndersideZ() + 2 * pitch.params().crossbarRadiusM() + R + 0.02);
        LawDecision d = laws.decide(over, Team.HOME, null, ends);
        assertFalse(d.goal());
        assertTrue(d.disputed());
        assertEquals(RestartType.GOAL_KICK, d.restart().type());
        assertTrue(d.ifReversed().goal());
        assertEquals(Team.HOME, d.ifReversed().scoringTeam());
        assertEquals(Team.AWAY, d.ifReversed().restart().team());
    }

    @Test
    void voidDirectGoalIsNeverDisputed() {
        LineCrossing close = east(pitch.goalMouthHalfWidth() - R - 0.03, 1);
        LawDecision d = laws.decide(close, Team.HOME, RestartType.CORNER_KICK, ends);
        assertFalse(d.goal());
        assertFalse(d.disputed());
        assertNull(d.ifReversed());
    }

    @Test
    void sameInputsGiveEqualDecisions() {
        RestartDecider other = new RestartDecider(new PitchGeometry(PitchParams.defaults1888()), RestartLaws.laws1888());
        for (double y = -8; y <= 8; y += 0.37) {
            for (Team t : Team.values()) {
                double hl = pitch.halfLength();
                LineCrossing c = pitch.sweep(BallSegment.of(hl - 5, y, 1.2, hl + 2, y, 1.2, R)).crossing();
                if (c == null) {
                    continue; // hit a post
                }
                assertEquals(laws.decide(c, t, null, ends), other.decide(c, t, null, ends));
            }
        }
    }

    @Test
    void invalidRulesAndInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RestartLaws(false, true, false, false, false, true,
                false, false, true, true, 3, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new RestartLaws(false, true, false, false, false, true,
                false, false, true, true, 7, 0.5));
        assertThrows(IllegalArgumentException.class, () -> laws.decide(east(0, 1), null, null, ends));
        assertThrows(IllegalArgumentException.class, () -> new MatchEnds(null));
    }
}
