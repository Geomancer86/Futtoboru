package com.rndmodgames.futtoboru.engine.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * G-01 on the real 1888-89 game: every AI club picks its own sheet at kick-off, the player's club plays his, the
 * sheet is frozen on the match and survives a save, and only the eleven score.
 */
class TeamSheetGameTests extends BaseTest {

    private static List<Club> clubs(SaveGame game) {
        Map<Club, Boolean> seen = new IdentityHashMap<>();
        List<Club> out = new ArrayList<>();
        for (Club c : game.getAllClubs()) {
            if (c != null && seen.put(c, Boolean.TRUE) == null) {
                out.add(c);
            }
        }
        return out;
    }

    private static Match friendly(long id, Club home, Club away) {
        Match m = new Match();
        m.setId(id);
        m.setMatchType(Match.FRIENDLY_MATCH);
        m.setHomeClubId(home.getId());
        m.setAwayClubId(away.getId());
        m.setMatchDateTime(LocalDateTime.of(1888, 9, 1, 15, 0));
        return m;
    }

    private static void assertValid(TeamSheet sheet, Club club) {
        assertNotNull(sheet, club.getName() + " has a sheet");
        assertEquals(club.getId(), sheet.getClubId());
        Map<Long, Player> squad = TeamSheets.squadById(club);
        assertEquals(Math.min(TeamShape.SLOTS, TeamSheets.available(club).size()), sheet.selectedCount(),
                club.getName() + ": eleven, or every fit man when short");
        assertEquals(sheet.selectedCount(), new java.util.HashSet<>(sheet.selectedIds()).size(), "no man twice");
        for (Long id : sheet.selectedIds()) {
            assertTrue(squad.containsKey(id), club.getName() + ": " + id + " is in the squad");
        }
        assertTrue(sheet.isSelected(sheet.getCaptainId()), club.getName() + ": the captain plays");
    }

    @Test
    void everyClubPicksItsOwnSheetAtKickOffAndOnlyTheElevenScore() {
        SaveGame game = NewGameFixture.newGame(application);
        List<Club> clubs = clubs(game);
        assertTrue(clubs.size() > 20, "clubs: " + clubs.size());
        Club preston = game.getCurrentClub();
        MatchSimulator simulator = new MatchSimulator(application);
        List<Match> matches = new ArrayList<>();
        Map<Club, String> lastSheet = new IdentityHashMap<>();
        for (int i = 0; i < clubs.size(); i++) {
            Club home = clubs.get(i);
            Club away = clubs.get((i + 1) % clubs.size());
            Match m = friendly(990_000L + i, home, away);
            assertTrue(simulator.simulateMatch(m));
            matches.add(m);
            lastSheet.put(home, m.getHomeSheet().toString());
            lastSheet.put(away, m.getAwaySheet().toString());
        }
        int goals = 0;
        for (Match m : matches) {
            Club home = game.getClubById(m.getHomeClubId());
            Club away = game.getClubById(m.getAwayClubId());
            assertValid(m.getHomeSheet(), home);
            assertValid(m.getAwaySheet(), away);
            for (Long scorer : m.getHomeScorerIds()) {
                assertTrue(m.getHomeSheet().isSelected(scorer), "home scorer " + scorer + " played");
            }
            for (Long scorer : m.getAwayScorerIds()) {
                assertTrue(m.getAwaySheet().isSelected(scorer), "away scorer " + scorer + " played");
            }
            assertEquals(m.getHomeGoals(), m.getHomeScorerIds().size());
            goals += m.getHomeGoals() + m.getAwayGoals();
        }
        assertTrue(goals > 10, "goals: " + goals);
        // an AI club's last pick is its standing sheet; every real squad puts a keeper in goal
        for (Club club : clubs) {
            if (club != preston) {
                assertNotNull(club.getTeamSheet(), club.getName());
                assertEquals(lastSheet.get(club), String.valueOf(club.getTeamSheet()),
                        club.getName() + " keeps the sheet of its last match");
            }
            TeamSheet sheet = club.getTeamSheet() != null ? club.getTeamSheet() : TeamSheets.standing(club);
            assertEquals(Position.GOALKEEPER, TeamSheets.squadById(club).get(sheet.getPlayerId(0)).getPosition(),
                    club.getName() + " has a keeper in goal");
        }
    }

    @Test
    void anAiClubPicksTheSameSheetForTheSameMatchInAnyOrder() {
        SaveGame game = NewGameFixture.newGame(application);
        List<Club> clubs = clubs(game);
        Club a = clubs.get(1);
        Club b = clubs.get(2);
        Match m1 = friendly(991_001L, a, b);
        Match m2 = friendly(991_001L, a, b);
        TeamSheet preview = TeamSheets.preview(m1, a, true, game, application.random());
        TeamSheet first = TeamSheets.kickOff(m1, a, true, game, application.random());
        TeamSheets.kickOff(friendly(991_002L, b, a), b, true, game, application.random()); // another match between
        TeamSheet second = TeamSheets.kickOff(m2, a, true, game, application.random());
        assertEquals(first.toString(), second.toString());
        assertEquals(first.toString(), preview.toString(), "the preview shows the pick of kick-off");
    }

    @Test
    void thePlayersSheetIsFrozenAtKickOffAndSavedWithTheMatch() {
        SaveGame game = NewGameFixture.newGame(application);
        Club preston = game.getCurrentClub();
        assertEquals("preston-north-end", preston.getDataId());
        assertNull(preston.getTeamSheet(), "nothing picked before it is needed");
        TeamSheet standing = TeamSheets.standing(preston);
        assertValid(standing, preston);

        // the player puts his centre forward in goal and leaves inside left empty
        Long centreForward = standing.getPlayerId(8);
        Long keeper = standing.getPlayerId(0);
        standing.setPlayerId(0, centreForward);
        standing.setPlayerId(9, null);
        assertEquals(10, standing.selectedCount());
        String edited = standing.toString();

        List<Club> others = clubs(game);
        others.remove(preston);
        Club other = others.get(5);
        Club idle = others.get(7);
        Match m = friendly(992_000L, preston, other);
        game.getClubById(preston.getId()).getScheduledMatches().add(m);
        other.getScheduledMatches().add(m);
        assertTrue(new MatchSimulator(application).simulateMatch(m));
        TeamSheet played = m.getHomeSheet();
        assertEquals(centreForward, played.getPlayerId(0), "his choice is played as he made it");
        assertEquals(keeper, played.getPlayerId(8));
        assertNotNull(played.getPlayerId(9), "the committee fills the empty place at kick-off");
        assertValid(played, preston);
        assertEquals(edited, standing.toString(), "the standing sheet is left as he made it");

        // later changes do not touch the sheet of the match
        String frozen = played.toString();
        TeamSheets.autoPick(preston);
        assertEquals(frozen, m.getHomeSheet().toString());
        String standingNow = preston.getTeamSheet().toString();

        LoadResult loaded = SaveCodec.decode(SaveCodec.encode(game, "G-01", LocalDateTime.of(2000, 1, 1, 0, 0)));
        assertTrue(loaded.isLoaded(), loaded.toString());
        SaveGame back = loaded.getGame();
        Match again = null;
        for (Match x : back.getClubById(preston.getId()).getScheduledMatches()) {
            if (x.getId() == 992_000L) {
                again = x;
            }
        }
        assertNotNull(again, "the match is saved");
        assertEquals(frozen, String.valueOf(again.getHomeSheet()), "the home sheet as played");
        assertEquals(String.valueOf(m.getAwaySheet()), String.valueOf(again.getAwaySheet()), "the away sheet");
        assertEquals(standingNow, String.valueOf(back.getClubById(preston.getId()).getTeamSheet()),
                "the standing sheet");
        Club otherBack = back.getClubById(other.getId());
        assertEquals(String.valueOf(other.getTeamSheet()), String.valueOf(otherBack.getTeamSheet()));

        // a club that never needed one has none in the save (an optional field) and picks it on first use
        Club idleBack = back.getClubById(idle.getId());
        assertNull(idle.getTeamSheet());
        assertNull(idleBack.getTeamSheet());
        assertValid(TeamSheets.standing(idleBack), idleBack);
    }
}
