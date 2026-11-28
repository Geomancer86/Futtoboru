package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.InjuryKind;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchInjury;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerInjury;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-02 and saves: a player's injury and a match's injuries are optional fields (no saveVersion bump). They survive a
 * save and load; the v1 fixture save (made before G-02) loads with every man fit.
 */
class InjurySaveTests {

    @BeforeAll
    static void preload() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        new HeadlessApplication(new Futtoboru(SeasonMemo.SEED), config);
        BaseTest.awaitHeadlessCreate();
    }

    private static SaveGame load(String text) {
        LoadResult result = SaveCodec.decode(text);
        assertTrue(result.isLoaded(), result.toString());
        return result.getGame();
    }

    @Test
    void injuriesSurviveSaveAndLoadAndAnOldSaveIsAllFit() throws IOException {
        SaveGame game;
        try (InputStream in = InjurySaveTests.class.getClassLoader().getResourceAsStream(SaveGameTests.FIXTURE)) {
            assertNotNull(in, SaveGameTests.FIXTURE);
            game = load(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        Club home = game.getCurrentClub();
        for (Club c : game.getAllClubs()) {
            for (Player p : c.getPlayers()) {
                assertNull(p.getInjury(), "a save made before G-02 has every man fit");
            }
        }
        Club away = null;
        for (Club c : game.getAllClubs()) {
            if (c != home && !c.getPlayers().isEmpty()) {
                away = c;
                break;
            }
        }
        assertNotNull(away);
        Player hurt = home.getPlayers().get(0);
        LocalDateTime day = LocalDateTime.of(1888, 9, 8, 0, 0);
        hurt.setInjury(new PlayerInjury(InjuryKind.CARRIED_OFF, day, 21, 4242L));
        Match m = new Match();
        m.setId(game.getIdAllocator().nextId());
        m.setMatchType(Match.LEAGUE_MATCH);
        m.setHomeClubId(home.getId());
        m.setAwayClubId(away.getId());
        m.setMatchDateTime(day.withHour(15));
        m.setIsPlayed(true);
        m.setHomeGoals(0);
        m.setAwayGoals(0);
        m.getHomeInjuries().add(new MatchInjury(hurt.getPerson().getId(), InjuryKind.CARRIED_OFF, 34, 55, 21));
        home.getPlayedMatches().add(m);
        away.getPlayedMatches().add(m);

        SaveGame again = load(SaveCodec.encode(game, "G-02", LocalDateTime.of(2026, 11, 28, 12, 0)));
        Player hurtAgain = null;
        for (Player p : again.getCurrentClub().getPlayers()) {
            if (p.getId().equals(hurt.getId())) {
                hurtAgain = p;
            }
        }
        assertNotNull(hurtAgain);
        PlayerInjury injury = hurtAgain.getInjury();
        assertNotNull(injury);
        assertEquals(InjuryKind.CARRIED_OFF, injury.getKindEnum());
        assertEquals(day, injury.getSince());
        assertEquals(day.plusDays(21), injury.getUntil());
        assertEquals(21, injury.getDaysOut());
        assertEquals(4242L, injury.getMatchId());

        Match mAgain = null;
        for (Match x : again.getCurrentClub().getPlayedMatches()) {
            if (x.getId().equals(m.getId())) {
                mAgain = x;
            }
        }
        assertNotNull(mAgain);
        assertEquals(1, mAgain.getHomeInjuries().size());
        MatchInjury i = mAgain.getHomeInjuries().get(0);
        assertEquals(hurt.getPerson().getId(), i.getPersonId());
        assertEquals("carried-off", i.getKind());
        assertEquals(34, i.getMinute());
        assertEquals(55, i.getReturnedMinute());
        assertEquals(21, i.getDaysOut());
        assertTrue(mAgain.getAwayInjuries().isEmpty());
    }
}
