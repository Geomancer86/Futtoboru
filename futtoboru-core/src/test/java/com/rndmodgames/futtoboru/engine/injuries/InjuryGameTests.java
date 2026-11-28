package com.rndmodgames.futtoboru.engine.injuries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.rndmodgames.futtoboru.data.InjuryKind;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerInjury;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.simulation.MatchResultRecorder;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-02 on the real 1888-89 game: injured men are left out of every club's sheet (the player's and the AI's, one
 * path), a club short of fit men plays short, injuries happen in simulated matches of every club, the recorder gives
 * days out and the player's club a letter, the daily recovery brings men back, and the same seed gives the same.
 */
class InjuryGameTests extends BaseTest {

    private static final LocalDateTime DAY = LocalDateTime.of(1888, 9, 1, 15, 0);

    private static List<Club> clubs(SaveGame game) {
        Map<Club, Boolean> seen = new IdentityHashMap<>();
        List<Club> out = new ArrayList<>();
        for (Club c : game.getAllClubs()) {
            if (c != null && c.getPlayers().size() >= 11 && seen.put(c, Boolean.TRUE) == null) {
                out.add(c);
            }
        }
        return out;
    }

    private static Club otherClub(SaveGame game) {
        for (Club c : clubs(game)) {
            if (c != game.getCurrentClub()) {
                return c;
            }
        }
        throw new AssertionError("no AI club");
    }

    private static Match friendly(long id, Club home, Club away) {
        Match m = new Match();
        m.setId(id);
        m.setMatchType(Match.FRIENDLY_MATCH);
        m.setHomeClubId(home.getId());
        m.setAwayClubId(away.getId());
        m.setMatchDateTime(DAY);
        return m;
    }

    private static void injure(Player p, int days) {
        p.setInjury(new PlayerInjury(InjuryKind.CARRIED_OFF, DAY.minusDays(1), days, null));
    }

    @Test
    void injuredMenAreLeftOutOfEverySheetAndAShortClubPlaysShort() {
        SaveGame game = NewGameFixture.newGame(application);
        Club mine = game.getCurrentClub();
        Club ai = otherClub(game);
        // the player's own XI names two men who are then hurt: the committee replaces them at kick-off
        TeamSheet standing = TeamSheets.standing(mine);
        Player myHurt = TeamSheets.squadById(mine).get(standing.getPlayerId(0));
        Player myHurt2 = TeamSheets.squadById(mine).get(standing.getPlayerId(5));
        injure(myHurt, 10);
        injure(myHurt2, 10);
        assertFalse(TeamSheets.isAvailable(myHurt));
        // the AI club can raise only nine
        List<Player> aiMen = new ArrayList<>(ai.getPlayers());
        for (int i = 9; i < aiMen.size(); i++) {
            injure(aiMen.get(i), 20);
        }
        assertEquals(9, TeamSheets.available(ai).size());

        Match m = friendly(995_001L, mine, ai);
        assertTrue(new MatchSimulator(application).simulateMatch(m));
        TeamSheet home = m.getHomeSheet();
        TeamSheet away = m.getAwaySheet();
        assertFalse(home.isSelected(myHurt.getId()));
        assertFalse(home.isSelected(myHurt2.getId()));
        assertEquals(Math.min(11, TeamSheets.available(mine).size()), home.selectedCount(), "replaced from the fit men");
        assertEquals(9, away.selectedCount(), "nine fit men: the side plays short");
        for (int i = 9; i < aiMen.size(); i++) {
            assertFalse(away.isSelected(aiMen.get(i).getId()));
        }
        for (Long id : m.getAwayScorerIds()) {
            assertTrue(away.isSelected(id));
        }
    }

    @Test
    void everyClubGetsInjuriesTheSameWayAndTheSameSeedGivesTheSame() {
        SaveGame game = NewGameFixture.newGame(application);
        List<Club> clubs = clubs(game);
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        int mine = 0;
        int ai = 0;
        for (int run = 0; run < 2; run++) {
            MatchSimulator sim = new MatchSimulator(application, RandomSource.seeded(1888));
            for (int r = 0; r < 6; r++) {
                for (int i = 0; i < clubs.size(); i++) {
                    Club home = clubs.get(i);
                    Club away = clubs.get((i + 1 + r) % clubs.size());
                    if (home == away) {
                        continue;
                    }
                    Match m = friendly(996_000L + r * 1000 + i, home, away);
                    assertTrue(sim.simulateMatch(m));
                    for (boolean isHome : new boolean[] { true, false }) {
                        Club club = isHome ? home : away;
                        TeamSheet sheet = isHome ? m.getHomeSheet() : m.getAwaySheet();
                        for (MatchInjury inj : isHome ? m.getHomeInjuries() : m.getAwayInjuries()) {
                            Player p = InjuryManager.playerOf(club, inj.getPersonId());
                            assertNotNull(p);
                            assertTrue(sheet.isSelected(p.getId()), "only a man who played is hurt");
                            (run == 0 ? first : second).add(m.getId() + ":" + inj.getPersonId() + ":"
                                    + inj.getKind() + ":" + inj.getMinute() + ":" + inj.getDaysOut());
                            if (run == 0) {
                                if (club == game.getCurrentClub()) {
                                    mine++;
                                } else {
                                    ai++;
                                }
                            }
                        }
                    }
                }
            }
        }
        assertTrue(first.size() > 20, "injuries in " + clubs.size() * 6 + " matches: " + first.size());
        assertTrue(ai > 0, "AI clubs get hurt");
        assertEquals(first, second, "same seed, same injuries");
    }

    @Test
    void theRecorderGivesDaysOutAndALetterAndRecoveryBringsHimBack() {
        SaveGame game = NewGameFixture.newGame(application);
        game.setGameDate(DAY);
        Club mine = game.getCurrentClub();
        Club ai = otherClub(game);
        Player hurt = mine.getPlayers().get(0);
        Player knocked = mine.getPlayers().get(1);
        Player aiHurt = ai.getPlayers().get(0);
        Match m = friendly(997_001L, mine, ai);
        m.setHomeGoals(1);
        m.setAwayGoals(0);
        m.setIsPlayed(true);
        m.getHomeInjuries().add(new MatchInjury(hurt.getPerson().getId(), InjuryKind.PASSENGER, 30, null, 12));
        m.getHomeInjuries().add(new MatchInjury(knocked.getPerson().getId(), InjuryKind.KNOCK, 50, null, 0));
        m.getAwayInjuries().add(new MatchInjury(aiHurt.getPerson().getId(), InjuryKind.CARRIED_OFF, 70, null, 30));
        mine.getScheduledMatches().add(m);
        ai.getScheduledMatches().add(m);

        new MatchResultRecorder(application, gameEngine.getMessageManager()).record(m);

        assertNotNull(hurt.getInjury());
        assertEquals(DAY.toLocalDate().plusDays(12), hurt.getInjury().getUntil().toLocalDate());
        assertNull(knocked.getInjury(), "a knock of no days keeps him fit");
        assertNotNull(aiHurt.getInjury(), "an AI club's man gets his days out too");
        assertEquals(InjuryKind.CARRIED_OFF, aiHurt.getInjury().getKindEnum());
        Message letter = last(game, InjuryManager.MSG_INJURY);
        assertNotNull(letter, "the player's club gets the injury letter");
        assertTrue(letter.getPlainTextMessage().contains(hurt.getPerson().getLastname()));
        assertFalse(letter.getPlainTextMessage().contains(aiHurt.getPerson().getLastname() + ":"),
                "only his own men");
        assertFalse(letter.getTitle().contains("msg_"), letter.getTitle());

        InjuryManager.recoverDaily(game, DAY.plusDays(11), gameEngine.getMessageManager());
        assertNotNull(hurt.getInjury());
        assertFalse(TeamSheets.isAvailable(hurt));
        InjuryManager.recoverDaily(game, DAY.plusDays(12), gameEngine.getMessageManager());
        assertNull(hurt.getInjury(), "fit again on his day");
        assertTrue(TeamSheets.isAvailable(hurt));
        assertNotNull(aiHurt.getInjury());
        assertNotNull(last(game, InjuryManager.MSG_FIT_AGAIN));
        InjuryManager.recoverDaily(game, DAY.plusDays(30), gameEngine.getMessageManager());
        assertNull(aiHurt.getInjury(), "AI clubs recover the same way");
    }

    private static Message last(SaveGame game, String type) {
        Message out = null;
        for (Message m : game.getAllMessages()) {
            if (m != null && type.equals(m.getMessageType())) {
                out = m;
            }
        }
        return out;
    }
}
