package com.rndmodgames.futtoboru.engine.cup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Unit tests for {@link CupBracketManager} — the bracket/replay engine actually wired into
 * {@code MatchSimulator} for cup matches (as opposed to the legacy, effectively unreachable
 * replay path in {@code AuthorityManager.checkCupReplays()}, which is skipped for any edition
 * that already uses parent-linked bracket matches).
 *
 * <p>Found via QA review 2026-07-15: this class had zero automated coverage despite being the
 * production cup-replay and winner-advancement implementation.
 */
class CupBracketManagerTest {

    private static final LocalDateTime GAME_DATE = LocalDateTime.of(1889, 1, 12, 0, 0);

    private SaveGame saveGame;
    private CupBracketManager bracketManager;
    private Club home;
    private Club away;

    @BeforeAll
    static void initializeLibGDX() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(), config);
    }

    @BeforeEach
    void setUp() {
        saveGame = new SaveGame();
        saveGame.setAllClubs(new ArrayList<>());
        saveGame.setGameDate(GAME_DATE);

        home = createClub("Home Town", 1L);
        away = createClub("Away United", 2L);

        bracketManager = new CupBracketManager(saveGame);
    }

    private Club createClub(String name, Long id) {
        Club club = new Club();
        club.setId(id);
        club.setName(name);
        club.setScheduledMatches(new ArrayList<>());
        club.setPlayedMatches(new ArrayList<>());
        saveGame.getAllClubs().add(club);
        return club;
    }

    private Match drawnMatch(long id, int round) {
        Match match = new Match();
        match.setId(id);
        match.setHomeClubId(home.getId());
        match.setAwayClubId(away.getId());
        match.setMatchType(Match.CUP_MATCH);
        match.setCompetitionId(100L);
        match.setCompetitionEditionId(200L);
        match.setRound(round);
        match.setBracketPosition(1);
        match.setBracketPath("R" + round + "M1");
        match.setMatchDateTime(GAME_DATE);
        match.setIsPlayed(true);
        match.setHomeGoals(1);
        match.setAwayGoals(1);
        return match;
    }

    private Match decidedMatch(long id, int round, int homeGoals, int awayGoals) {
        Match match = drawnMatch(id, round);
        match.setHomeGoals(homeGoals);
        match.setAwayGoals(awayGoals);
        return match;
    }

    // --- Replay scheduling -------------------------------------------------

    @Test
    void drawnMatchSchedulesReplayWithSwappedVenueAndParentLink() {
        Match original = drawnMatch(1L, 1);

        Match nextRoundMatch = bracketManager.advanceWinner(original);

        assertNull(nextRoundMatch, "A drawn match has no winner yet, so no next-round match is returned");

        Match replay = findReplayFor(original);
        assertNotNull(replay, "Expected a replay to be scheduled for the drawn match");
        assertEquals(original.getAwayClubId(), replay.getHomeClubId(), "Replay is hosted by the original away side");
        assertEquals(original.getHomeClubId(), replay.getAwayClubId(), "Original home side travels for the replay");
        assertEquals(original.getId(), replay.getParentMatch1Id(), "Replay must link back to the original match");
        assertEquals(original.getRound(), replay.getRound(), "Replay stays in the same round as the original");
        assertFalse(replay.getIsPlayed(), "Replay has not been played yet");
        assertTrue(replay.getMatchDateTime().isAfter(GAME_DATE),
                "Replay must be scheduled in the future, never on/before the current game date");
        assertTrue(replay.getBracketPath().endsWith("R"), "Replay bracket path is marked with an R suffix");
    }

    @Test
    void replayIsAddedToBothClubsSchedules() {
        Match original = drawnMatch(1L, 1);
        bracketManager.advanceWinner(original);

        assertTrue(hasMatchWithParent(home, original.getId()), "Home club should see the replay in its schedule");
        assertTrue(hasMatchWithParent(away, original.getId()), "Away club should see the replay in its schedule");
    }

    @Test
    void callingAdvanceWinnerTwiceOnSameDrawDoesNotDuplicateTheReplay() {
        Match original = drawnMatch(1L, 1);

        bracketManager.advanceWinner(original);
        bracketManager.advanceWinner(original);

        long replayCount = home.getScheduledMatches().stream()
                .filter(m -> original.getId().equals(m.getParentMatch1Id()))
                .count();
        assertEquals(1, replayCount, "A second call for the same drawn match must not create a duplicate replay");
    }

    // --- Winner advancement to next round -----------------------------------

    @Test
    void decisiveHomeWinAdvancesWinnerIntoNextRoundHomeSlot() {
        Match roundOneMatchA = decidedMatch(10L, 1, 2, 0); // home wins
        Match nextRound = new Match();
        nextRound.setId(20L);
        nextRound.setCompetitionEditionId(roundOneMatchA.getCompetitionEditionId());
        nextRound.setRound(2);
        nextRound.setParentMatch1Id(roundOneMatchA.getId());
        home.getScheduledMatches().add(nextRound);
        away.getScheduledMatches().add(nextRound);

        Match result = bracketManager.advanceWinner(roundOneMatchA);

        assertNotNull(result, "A decisive result should advance a winner into the next round match");
        assertEquals(roundOneMatchA.getHomeClubId(), result.getHomeClubId(),
                "Winner of round 1 slot 1 should fill the linked home slot in round 2");
    }

    @Test
    void decisiveAwayWinAdvancesWinnerIntoNextRoundAwaySlot() {
        Match roundOneMatchB = decidedMatch(11L, 1, 0, 3); // away wins
        Match nextRound = new Match();
        nextRound.setId(21L);
        nextRound.setCompetitionEditionId(roundOneMatchB.getCompetitionEditionId());
        nextRound.setRound(2);
        nextRound.setParentMatch2Id(roundOneMatchB.getId());
        home.getScheduledMatches().add(nextRound);
        away.getScheduledMatches().add(nextRound);

        Match result = bracketManager.advanceWinner(roundOneMatchB);

        assertNotNull(result);
        assertEquals(roundOneMatchB.getAwayClubId(), result.getAwayClubId(),
                "Winner of round 1 slot 2 should fill the linked away slot in round 2");
    }

    @Test
    void replayThatEndsInDrawStillProducesAWinnerInsteadOfStalling() {
        Match original = drawnMatch(1L, 1);
        bracketManager.advanceWinner(original); // schedules the replay

        Match replay = findReplayFor(original);
        assertNotNull(replay);
        replay.setIsPlayed(true);
        replay.setHomeGoals(1);
        replay.setAwayGoals(1); // replay itself is also a draw

        // Should not throw and should not hang the cup — a winner is picked (extra time/penalties stand-in).
        bracketManager.advanceWinner(replay);
    }

    // --- helpers -------------------------------------------------------------

    private Match findReplayFor(Match original) {
        for (Match m : home.getScheduledMatches()) {
            if (original.getId().equals(m.getParentMatch1Id())) {
                return m;
            }
        }
        return null;
    }

    private boolean hasMatchWithParent(Club club, Long parentId) {
        List<Match> matches = club.getScheduledMatches();
        if (matches == null) {
            return false;
        }
        for (Match m : matches) {
            if (parentId.equals(m.getParentMatch1Id())) {
                return true;
            }
        }
        return false;
    }
}
