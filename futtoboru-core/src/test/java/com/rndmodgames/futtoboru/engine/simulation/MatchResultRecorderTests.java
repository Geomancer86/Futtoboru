package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;

/**
 * P2-2: one result path for league and cup matches.
 */
public class MatchResultRecorderTests extends BaseTest {

    private EngineTestWorld world;
    private MatchResultRecorder recorder;

    @BeforeEach
    void setUp() {
        world = new EngineTestWorld(application, 4);
        recorder = new MatchResultRecorder(application, world.engine.getMessageManager());
    }

    @Test
    void leagueResultUpdatesTableOnce() {
        Club home = world.club(0);
        Club away = world.club(1);
        Match match = world.leagueMatch(home, away);
        EngineTestWorld.score(match, 2, 1);

        recorder.record(match);
        recorder.record(match); // a second record of the same match must not count again

        assertEquals(1, home.getMatchesPlayed());
        assertEquals(1, away.getMatchesPlayed());
        assertEquals(1, home.getMatchesWon());
        assertEquals(1, away.getMatchesLost());
        assertEquals(2, home.getGoalsScored());
        assertEquals(1, home.getGoalsConceded());
        assertEquals(2, home.getPoints(), "1888 rules: 2 points for a win");
        assertEquals(0, away.getPoints());
        assertFalse(home.getScheduledMatches().contains(match));
        assertEquals(1, home.getPlayedMatches().size());
        assertEquals(1, away.getPlayedMatches().size());
        assertEquals(1, countMessages("MATCH_RESULT"), "one result message");
    }

    @Test
    void simulatedLeagueMatchGoesThroughThePathOnce() {
        Club home = world.club(0);
        Club away = world.club(1);
        Match match = world.leagueMatch(home, away);

        assertTrue(recorder.play(match, new MatchSimulator(application)));
        assertFalse(recorder.play(match, new MatchSimulator(application)), "a played match is not played again");

        assertEquals(1, home.getMatchesPlayed());
        assertEquals(1, away.getMatchesPlayed());
        assertEquals(1, home.getMatchesWon() + home.getMatchesDrawn() + home.getMatchesLost());
        assertEquals(match.getHomeGoals(), home.getGoalsScored());
        assertEquals(home.getPoints() + away.getPoints(), 2, "2-1-0 rules hand out 2 points per match");
    }

    @Test
    void cupResultDoesNotTouchLeagueTable() {
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        Match tie = cup.byPath("R1M1");
        EngineTestWorld.score(tie, 3, 0);
        recorder.record(tie);

        for (Club c : world.clubs) {
            assertEquals(0, c.getMatchesPlayed());
            assertEquals(0, c.getGoalsScored());
            assertEquals(0, c.getMatchesWon());
            assertEquals(0, c.getPoints());
        }
    }

    @Test
    void cupResultAdvancesWinner() {
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        Match tie = cup.byPath("R1M1");
        Match next = cup.byPath("R2M1");
        assertNotNull(next);
        assertNull(next.getHomeClubId());

        EngineTestWorld.score(tie, 0, 2);
        recorder.record(tie);

        assertEquals(tie.getAwayClubId(), next.getHomeClubId(), "winner of R1M1 fills the home slot of R2M1");
    }

    @Test
    void cupDrawSchedulesReplayThenReplayWinnerAdvances() {
        EngineTestWorld.Cup cup = world.cup(world.clubs);
        Match tie = cup.byPath("R1M1");
        Club home = world.game.getClubById(tie.getHomeClubId());
        Club away = world.game.getClubById(tie.getAwayClubId());

        EngineTestWorld.score(tie, 1, 1);
        recorder.record(tie);

        Match replay = null;
        for (Match m : home.getScheduledMatches()) {
            if (tie.getId().equals(m.getParentMatch1Id()) && "R1M1R".equals(m.getBracketPath())) {
                replay = m;
            }
        }
        assertNotNull(replay, "a drawn tie gets a replay");
        assertTrue(away.getScheduledMatches().contains(replay));
        assertEquals(away.getId(), replay.getHomeClubId(), "the replay is at the other ground");
        assertNull(cup.byPath("R2M1").getHomeClubId(), "nobody advances from a draw");

        EngineTestWorld.score(replay, 2, 0);
        recorder.record(replay);
        assertEquals(away.getId(), cup.byPath("R2M1").getHomeClubId());
    }

    @Test
    void leagueMatchBlockedByPendingDrawButCupMatchIsNot() {
        Message draw = new Message();
        draw.setMessageType("LEAGUE_DRAW");
        draw.setIsMandatory(true);
        draw.setScheduledDate(world.game.getGameDate().plusDays(3));
        world.game.getScheduledMessages().add(draw);

        Club home = world.club(0);
        Club away = world.club(1);
        Match league = world.leagueMatch(home, away);

        assertFalse(recorder.play(league, new MatchSimulator(application)));
        assertFalse(league.getIsPlayed());
        assertEquals(0, home.getMatchesPlayed());
        assertTrue(home.getScheduledMatches().contains(league));

        EngineTestWorld.Cup cup = world.cup(world.clubs);
        assertTrue(recorder.isPlayable(cup.byPath("R1M1")), "the draw blocker only holds back league matches");

        // once the draw has been read, the league match can be played
        world.game.getScheduledMessages().clear();
        assertTrue(recorder.play(league, new MatchSimulator(application)));
        assertEquals(1, home.getMatchesPlayed());
    }

    @Test
    void blockerIgnoresReadOrNonMandatoryMessages() {
        Message read = new Message();
        read.setMessageType("CUP_DRAW");
        read.setIsMandatory(true);
        read.setIsRead(true);
        world.game.getAllMessages().add(read);
        Message optional = new Message();
        optional.setMessageType("LEAGUE_DRAW");
        optional.setIsMandatory(false);
        world.game.getAllMessages().add(optional);

        assertFalse(MatchResultRecorder.hasMandatoryDrawBlocker(world.game));
    }

    private int countMessages(String type) {
        int n = 0;
        for (Message m : world.game.getAllMessages()) {
            if (type.equals(m.getMessageType())) {
                n++;
            }
        }
        return n;
    }
}
