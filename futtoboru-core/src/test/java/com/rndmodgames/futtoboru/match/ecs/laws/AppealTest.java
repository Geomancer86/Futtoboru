package com.rndmodgames.futtoboru.match.ecs.laws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.action.ActionParams;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchEnd;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/** E-16: rules by season, umpire bias and the referee, fouls, keeper charges, disputed goals, half-time, officials. */
class AppealTest {

    static final double HL = Laws.PITCH.halfLength();
    static final double HW = Laws.PITCH.halfWidth();

    @Test
    void rulesBySeason() {
        RulesProfile r = RulesProfile.season1888();
        assertEquals(3, r.offsideOpponents());
        assertFalse(r.offsideOwnHalfExempt() || r.refereeOnField() || r.penalties() || r.keeperChargeProtected()
                || r.directFreeKicks() || r.goalDirectFromCorner());
        assertTrue(r.umpiresDecide());
        assertTrue(r.goalDirectFromGoalKick(), "until IFAB 1890");
        assertTrue(r.offsideFrom(RestartType.THROW_IN));
        assertFalse(r.offsideFrom(RestartType.GOAL_KICK) || r.offsideFrom(RestartType.CORNER_KICK));
        assertEquals(ActionParams.KeeperHandling.OWN_HALF, r.keeperHandling());

        assertTrue(RulesProfile.forSeason(1891).refereeOnField() && RulesProfile.forSeason(1891).penalties());
        assertFalse(RulesProfile.forSeason(1890).penalties());
        assertTrue(RulesProfile.forSeason(1892).keeperChargeProtected());
        assertTrue(RulesProfile.forSeason(1903).directFreeKicks());
        assertTrue(RulesProfile.forSeason(1907).offsideOwnHalfExempt());
        assertEquals(ActionParams.KeeperHandling.PENALTY_AREA, RulesProfile.forSeason(1912).keeperHandling());
        assertFalse(RulesProfile.forSeason(1920).offsideFrom(RestartType.THROW_IN));
        assertTrue(RulesProfile.forSeason(1924).goalDirectFromCorner());
        assertEquals(2, RulesProfile.forSeason(1925).offsideOpponents());
        assertEquals(3, RulesProfile.forSeason(1924).offsideOpponents());
    }

    // ---- umpires and the referee ------------------------------------------------------------------------------------

    static OfficialComponent[] crew(double partisanship) {
        return new OfficialComponent[] {
                new OfficialComponent(OfficialComponent.REFEREE, null, 0.7, 0, 0, -HW - 1.5),
                new OfficialComponent(OfficialComponent.UMPIRE, Team.HOME, 1, partisanship, -10, -HW + 3),
                new OfficialComponent(OfficialComponent.UMPIRE, Team.AWAY, 1, partisanship, 10, HW - 3) };
    }

    /** Over seeded trials of 50/50 claims: the yes-rate of each umpire by who appeals, and referee tie-breaks. */
    static double[] trials(double partisanship, long seed) {
        Adjudicator a = new Adjudicator(RandomSource.seeded(seed).fork("officials:noise"));
        OfficialComponent[] crew = crew(partisanship);
        Adjudicator.Noise n = new Adjudicator.Noise(0.5, 0.02, 0, 0.8);
        LawRecord r = new LawRecord();
        int trials = 4000;
        double homeYesHomeAppeal = 0, homeYesAwayAppeal = 0, awayYesAwayAppeal = 0, awayYesHomeAppeal = 0;
        double givenHome = 0, givenAway = 0;
        int ties = 0;
        for (int t = 0; t < trials; t++) {
            int appealing = t % 2;
            r.clear();
            boolean given = a.judge(crew, 3, false, r, appealing, 0, n, 0, 0, Double.NaN);
            boolean hy = r.homeUmpireVote == LawRecord.VOTE_YES;
            boolean ay = r.awayUmpireVote == LawRecord.VOTE_YES;
            if (appealing == 0) {
                homeYesHomeAppeal += hy ? 1 : 0;
                awayYesHomeAppeal += ay ? 1 : 0;
                givenHome += given ? 1 : 0;
            } else {
                homeYesAwayAppeal += hy ? 1 : 0;
                awayYesAwayAppeal += ay ? 1 : 0;
                givenAway += given ? 1 : 0;
            }
            if (hy != ay) {
                ties++;
                assertTrue(r.refereeCalled, "umpires differ: the referee decides");
                assertEquals(given ? LawRecord.VOTE_YES : LawRecord.VOTE_NO, r.refereeVote);
            } else {
                assertFalse(r.refereeCalled);
                assertEquals(hy, given, "umpires agree: their decision");
            }
        }
        double half = trials / 2.0;
        return new double[] {homeYesHomeAppeal / half, homeYesAwayAppeal / half, awayYesAwayAppeal / half,
                awayYesHomeAppeal / half, givenHome / half, givenAway / half, ties};
    }

    @Test
    void eachUmpireLeansToHisClubAndTheRefereeBreaksTies() {
        double[] partisan = trials(0.8, 1888);
        assertTrue(partisan[0] > 0.65, "home umpire backs home claims: " + partisan[0]);
        assertTrue(partisan[1] < 0.35, "home umpire refuses away claims: " + partisan[1]);
        assertTrue(partisan[2] > 0.65 && partisan[3] < 0.35, "away umpire likewise");
        // both lean the same amount: the referee settles most and neither side gains overall
        assertTrue(partisan[6] > 2000, "many disagreements: " + partisan[6]);
        assertEquals(partisan[4], partisan[5], 0.05);

        double[] neutral = trials(0, 1888);
        assertEquals(neutral[0], neutral[1], 0.05, "partisanship 0: no side bias");
        assertEquals(0.5, neutral[0], 0.05);
    }

    @Test
    void theTruthWinsWithSharpEyesAndTheRefereeAloneFrom1891() {
        Adjudicator a = new Adjudicator(RandomSource.seeded(3).fork("officials:noise"));
        OfficialComponent[] crew = crew(0.5);
        Adjudicator.Noise sharp = new Adjudicator.Noise(0.01, 0, 0, 0.0);
        LawRecord r = new LawRecord();
        assertTrue(a.judge(crew, 3, false, r, 0, 1, sharp, 0, 0, Double.NaN));
        assertFalse(a.judge(crew, 3, false, r, 0, -1, sharp, 0, 0, Double.NaN));
        r.clear();
        a.judge(crew, 3, true, r, 0, 1, sharp, 0, 0, Double.NaN);
        assertEquals(LawRecord.NO_VOTE, r.homeUmpireVote);
        assertEquals(LawRecord.NO_VOTE, r.awayUmpireVote);
        assertEquals(LawRecord.VOTE_YES, r.refereeVote);
        r.clear();
        assertTrue(a.judge(crew, 0, false, r, 0, 0.1, sharp, 0, 0, Double.NaN), "no officials: the truth");
    }

    // ---- fouls ------------------------------------------------------------------------------------------------------

    /** A home charger at (-1, 0) running at 6 m/s into an away player at (0, 0) facing {@code victimFacing}. */
    static MatchWorld chargeScene(PhysicsParams p, Position victimPos, double victimFacing, Entity[] out) {
        MatchWorld w = Laws.world(p, 11);
        Entity a = Laws.player(w, Team.HOME, Position.CENTRE_FORWARD, -1.0, 0, 0);
        Entity b = Laws.player(w, Team.AWAY, victimPos, 0, 0, victimFacing);
        Entity ball = Laws.ball(w, 20, 20, 45);
        w.tick();
        Laws.state(ball).phase = PlayStateComponent.IN_PLAY; // as if the kick-off had been taken
        Laws.body(a).vx = 6;
        a.getComponent(MoveCommand.class).set(6, 0).charge = true;
        out[0] = a;
        out[1] = b;
        out[2] = ball;
        return w;
    }

    static LawRecord runToFoulAppeal(MatchWorld w) {
        for (int t = 0; t < 30; t++) {
            w.tick();
            LawRecord r = Laws.find(w, LawRecord.FOUL);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    @Test
    void aChargeFromBehindIsAFreeKick() {
        Entity[] e = new Entity[3];
        MatchWorld w = chargeScene(Laws.params(1888), Position.HALF_BACK, 0, e);
        LawRecord r = runToFoulAppeal(w);
        assertNotNull(r);
        assertEquals(LawRecord.FOUL_FROM_BEHIND, r.foulKind);
        assertTrue(r.given);
        assertEquals(Laws.index(e[0]), r.offender);
        assertEquals(Laws.index(e[1]), r.victim);
        assertEquals(Team.AWAY.ordinal(), r.restartTeam);
        assertEquals(RestartType.FREE_KICK.ordinal(), r.restartType);
        PlayStateComponent s = Laws.state(e[2]);
        assertTrue(s.stopped());
        assertEquals(1, s.foulsGiven);
        BallComponent ball = Laws.ballOf(e[2]);
        assertEquals(s.restartX, ball.x, 1e-12, "the ball is put on the spot");
    }

    @Test
    void chargingTheKeeperIsFairIn1888AndAFoulOnceHeIsProtected() {
        Entity[] e = new Entity[3];
        MatchWorld w1888 = chargeScene(Laws.params(1888), Position.GOALKEEPER, StrictMath.PI, e);
        LawRecord r1888 = runToFoulAppeal(w1888);
        assertTrue(r1888 == null || !r1888.given, "1888-89: a fair charge");
        assertEquals(0, Laws.state(e[2]).foulsGiven);
        assertTrue(Laws.state(e[2]).inPlay());

        MatchWorld w1894 = chargeScene(Laws.params(1894), Position.GOALKEEPER, StrictMath.PI, e);
        LawRecord r1894 = runToFoulAppeal(w1894);
        assertNotNull(r1894);
        assertEquals(LawRecord.FOUL_KEEPER_CHARGE, r1894.foulKind);
        assertTrue(r1894.given);
    }

    @Test
    void noPenaltyIn1888ButAPenaltyFrom1891NearTheGoal() {
        for (int season : new int[] {1888, 1891}) {
            PhysicsParams p = Laws.params(season);
            MatchWorld w = Laws.world(p, 11);
            Laws.laws(w).setPitch(new PitchGeometry(PitchParams.forSeason(season, 110, 70)));
            // home defends west: an away forward charged from behind by a home back near the west goal
            Entity a = Laws.player(w, Team.HOME, Position.FULL_BACK, -HL + 6, 0, StrictMath.PI);
            Entity b = Laws.player(w, Team.AWAY, Position.CENTRE_FORWARD, -HL + 5, 0, StrictMath.PI);
            Entity ball = Laws.ball(w, 0, 20, 45);
            w.tick();
            Laws.state(ball).phase = PlayStateComponent.IN_PLAY;
            Laws.body(a).vx = -6;
            a.getComponent(MoveCommand.class).set(-6, 0).charge = true;
            LawRecord r = runToFoulAppeal(w);
            assertNotNull(r, "season " + season);
            assertTrue(r.given);
            RestartType expect = season >= 1891 ? RestartType.PENALTY_KICK : RestartType.FREE_KICK;
            assertEquals(expect.ordinal(), r.restartType, "season " + season);
            assertTrue(Laws.index(b) == r.victim);
        }
    }

    // ---- disputed goals ---------------------------------------------------------------------------------------------

    /** A shot at the east goal at height 1 m crossing at {@code y}; returns the record of the crossing. */
    static LawRecord shot(PhysicsParams p, double y, boolean partisanHomeUmpireOnly, Entity[] ballOut) {
        MatchWorld w = Laws.world(p, 5);
        Laws.player(w, Team.HOME, Position.CENTRE_FORWARD, 0, 10, 0);
        Laws.player(w, Team.AWAY, Position.GOALKEEPER, HL - 1, -10, StrictMath.PI);
        Entity ball = Laws.ball(w, 0, 0, 45);
        if (partisanHomeUmpireOnly) {
            w.spawn(SpawnRole.OFFICIAL).add(new OfficialComponent(OfficialComponent.UMPIRE, Team.HOME, 1, 1, HL - 5,
                    HW - 3));
        }
        w.tick();
        PlayStateComponent s = Laws.state(ball);
        s.phase = PlayStateComponent.IN_PLAY;
        BallComponent b = Laws.ballOf(ball);
        b.x = HL - 1;
        b.y = y;
        b.z = 1;
        b.vx = 25;
        b.vy = 0;
        b.vz = 0;
        b.state = BallComponent.AIR;
        s.prevBallX = b.x;
        s.prevBallY = b.y;
        s.prevBallZ = b.z;
        ballOut[0] = ball;
        for (int t = 0; t < 10; t++) {
            w.tick();
            for (int type : new int[] {LawRecord.DISPUTED_GOAL, LawRecord.GOAL, LawRecord.OUT_OF_PLAY}) {
                LawRecord r = Laws.find(w, type);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    @Test
    void aCloseCallIsAppealedAndTheUmpiresDecide() {
        double r = new PhysicsParams().ballRadiusM;
        double inner = Laws.PITCH.goalMouthHalfWidth();
        double outer = inner + 2 * Laws.PITCH.params().postRadiusM();
        Entity[] ball = new Entity[1];

        // clearly in: an undisputed goal, kick-off to the side that lost it
        LawRecord clear = shot(Laws.params(1888), 0, false, ball);
        assertEquals(LawRecord.GOAL, clear.type);
        assertEquals(Team.HOME.ordinal(), clear.scoringTeam);
        assertEquals(RestartType.KICK_OFF.ordinal(), clear.restartType);
        assertEquals(Team.AWAY.ordinal(), clear.restartTeam);
        assertEquals(1, Laws.state(ball[0]).homeGoals);

        // just inside the post (no nets): the defenders claim it went outside; with no officials the truth stands
        LawRecord in = shot(Laws.params(1888), inner - r - 0.04, false, ball);
        assertEquals(LawRecord.DISPUTED_GOAL, in.type);
        assertTrue(in.appealed && !in.given);
        assertEquals(Team.AWAY.ordinal(), in.appealingTeam);
        assertEquals(Team.HOME.ordinal(), in.scoringTeam);
        assertEquals(1, Laws.state(ball[0]).disputedGoals);

        // just outside: the attackers claim a goal; refused -> goal kick
        LawRecord out = shot(Laws.params(1888), outer + r + 0.04, false, ball);
        assertEquals(LawRecord.DISPUTED_GOAL, out.type);
        assertEquals(Team.HOME.ordinal(), out.appealingTeam);
        assertFalse(out.given);
        assertEquals(-1, out.scoringTeam);
        assertEquals(RestartType.GOAL_KICK.ordinal(), out.restartType);

        // just outside, but the only official is the home club's strongly partisan umpire: given
        PhysicsParams p = Laws.params(1888);
        p.laws.goalLeanM = 1.0;
        LawRecord robbed = shot(p, outer + r + 0.04, true, ball);
        assertEquals(LawRecord.DISPUTED_GOAL, robbed.type);
        assertTrue(robbed.given);
        assertEquals(LawRecord.VOTE_YES, robbed.homeUmpireVote);
        assertEquals(Team.HOME.ordinal(), robbed.scoringTeam);
        assertEquals(1, Laws.state(ball[0]).homeGoals);
    }

    // ---- clock ------------------------------------------------------------------------------------------------------

    @Test
    void halfTimeChangesEndsAndFullTimeEndsPlay() {
        PhysicsParams p = Laws.params(1888);
        MatchWorld w = Laws.world(p, 9);
        Entity home = Laws.player(w, Team.HOME, Position.CENTRE_FORWARD, -10, 5, 0);
        Entity away = Laws.player(w, Team.AWAY, Position.CENTRE_FORWARD, 10, -5, StrictMath.PI);
        Entity ball = Laws.ball(w, 0, 0, 0.05); // 3 s halves = 90 ticks
        Entity[] crew = Officials.spawn(w, Laws.PITCH, Laws.state(ball).ends());
        AthleteComponent ath = home.getComponent(AthleteComponent.class);
        ath.stamina = 0.4;
        ath.reserve = 0.5;
        PlayStateComponent s = Laws.state(ball);
        assertEquals(90, s.halfTicks);
        w.runTicks(89);
        assertEquals(1, s.half);
        double umpireX = crew[1].getComponent(OfficialComponent.class).x;
        w.tick();
        assertNotNull(Laws.find(w, LawRecord.HALF_TIME));
        assertEquals(2, s.half);
        assertEquals(Team.AWAY.ordinal(), s.westDefender, "ends changed");
        assertEquals(Team.AWAY, Laws.laws(w).ends().westDefender());
        assertEquals(PitchEnd.EAST, Laws.laws(w).ends().endDefendedBy(Team.HOME));
        assertEquals(10, Laws.body(home).x, 1e-9, "players change ends");
        assertEquals(-10, Laws.body(away).x, 1e-9);
        assertEquals(-umpireX, crew[1].getComponent(OfficialComponent.class).x, 0.5);
        assertEquals(1, ath.stamina, 0);
        assertTrue(ath.reserve > 0.5);
        assertEquals(RestartType.KICK_OFF, s.restartType());
        assertEquals(Team.AWAY, s.restartTeam(), "the other side kicks off the second half");
        assertEquals(-1, s.attackSign(Team.HOME));
        w.runTicks(90);
        assertTrue(s.fullTime());
        assertEquals(180, w.tickCount());
        w.runTicks(5);
        assertTrue(s.fullTime());
    }

    // ---- officials ---------------------------------------------------------------------------------------------------

    @Test
    void officialsKeepAViewOfPlayFromTheirPlaces() {
        PhysicsParams p = Laws.params(1888);
        MatchWorld w = Laws.world(p, 2);
        Laws.player(w, Team.HOME, Position.CENTRE_FORWARD, -10, 5, 0);
        Entity ball = Laws.ball(w, 0, 0, 45);
        Entity[] crew = Officials.spawn(w, Laws.PITCH, Laws.state(ball).ends());
        OfficialComponent ref = crew[0].getComponent(OfficialComponent.class);
        OfficialComponent homeU = crew[1].getComponent(OfficialComponent.class);
        OfficialComponent awayU = crew[2].getComponent(OfficialComponent.class);
        assertTrue(ref.isReferee() && ref.partisanship == 0);
        assertEquals(Team.HOME, homeU.clubTeam());
        w.tick();
        Laws.state(ball).phase = PlayStateComponent.IN_PLAY;
        BallComponent b = Laws.ballOf(ball);
        double[] ballX = {-40, 30, 5};
        for (double bx : ballX) {
            for (int t = 0; t < 600; t++) {
                b.x = bx;
                b.y = 10;
                b.vx = b.vy = 0;
                w.tick();
                assertTrue(Math.abs(ref.y) > HW, "the referee stays off the field (1888-89)");
                assertTrue(Math.abs(ref.y) < HW + 3, "but not in the crowd");
                assertTrue(Math.abs(ref.x) <= p.laws.refereeRoamM + 1e-9);
                assertTrue(Math.abs(homeU.y) < HW && Math.abs(awayU.y) < HW, "umpires on the field");
                assertTrue(homeU.speed() <= p.laws.umpireSpeedMps + 1e-9);
            }
            assertTrue(homeU.x <= 0 && awayU.x >= 0, "each umpire in the half his club defends");
            if (bx < 0) {
                assertEquals(bx, homeU.x, 1.0, "home umpire level with play in his half");
            } else {
                assertEquals(bx, awayU.x, 1.0, "away umpire level with play in his half");
            }
        }
    }
}
