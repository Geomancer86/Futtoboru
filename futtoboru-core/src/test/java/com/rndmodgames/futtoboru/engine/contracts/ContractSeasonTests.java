package com.rndmodgames.futtoboru.engine.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ContractBand;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.generators.PositionAssigner;
import com.rndmodgames.futtoboru.system.save.GameFingerprint;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * G-04 with the real 1888-89 data: a new game's engagements are one season to 30 April on their club's band; the
 * 1 May rollover leaves every club able to field eleven; a save keeps the engagements, the retained-list marks and
 * the free agents; a save made before G-04 loads with its engagements derived.
 */
@Tag("slow")
class ContractSeasonTests extends BaseTest {

    @AfterAll
    static void restore() {
        DatabaseLoader.resetDatabaseLoaderInstance(); // the new games mutated the shared database's clubs
    }

    @Test
    void aNewGamesEngagementsAreForOneSeasonOnTheClubsBand() {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        int professionals = 0;
        int withSummerPay = 0;
        int amateurs = 0;
        for (Club club : ContractManager.uniqueClubs(game)) {
            assertEquals(club.getPlayers().size(), club.getPlayerContracts().size(), club.getDataId());
            boolean league = java.util.List.of("preston-north-end", "aston-villa", "wolverhampton-wanderers",
                    "blackburn-rovers", "bolton-wanderers", "west-bromwich-albion", "accrington", "everton", "burnley",
                    "derby-county", "notts-county", "stoke").contains(club.getDataId());
            ContractBand band = ContractBand.forClub(DatabaseLoader.getContractBands(1888), club.getDataId(), league);
            for (Player p : club.getPlayers()) {
                PlayerContract c = club.getContractForPlayer(p.getId());
                assertNotNull(c, club.getDataId() + " " + p.getPerson().getDataId());
                assertEquals(Integer.valueOf(1888), c.getSeason());
                assertEquals(LocalDateTime.of(1888, 4, 1, 4, 0), c.getStartDate());
                assertEquals(LocalDateTime.of(1889, 4, 30, 23, 59, 59), c.getEndDate(), "D28 Q8: 30 April");
                if (c.isAmateur()) {
                    amateurs++;
                    assertEquals(0, c.getWeeklyWage().signum(), "an amateur takes no wage");
                    assertEquals(0, band.getAmateurExpenses().compareTo(c.getWeeklyExpenses()), "expenses only");
                    assertFalse(c.hasSummerPay());
                } else {
                    professionals++;
                    assertTrue(c.getWeeklyWage().compareTo(band.getSeasonWageMin()) >= 0
                            && c.getWeeklyWage().compareTo(band.getSeasonWageMax()) <= 0,
                            club.getDataId() + " wage " + c.getWeeklyWage() + " in " + band.getStanding());
                    if (c.hasSummerPay()) {
                        withSummerPay++;
                        assertTrue(c.getSummerWage().compareTo(c.getWeeklyWage()) < 0, "summer pay is lower");
                        assertTrue(c.getSummerWage().compareTo(band.getSummerWageMax()) <= 0);
                    }
                }
            }
            if ("old-carthusians".equals(club.getDataId())) {
                for (PlayerContract c : club.getPlayerContracts()) {
                    assertTrue(c.isAmateur(), "an old boys' club of amateurs");
                }
            }
        }
        assertTrue(professionals > amateurs, professionals + " professionals, " + amateurs + " amateurs");
        assertTrue(withSummerPay > 0 && withSummerPay < professionals, withSummerPay + " with summer pay");
    }

    @Test
    void afterTheRolloverEveryClubFieldsElevenAndASaveKeepsTheEngagementsAndTheFreeAgents() throws IOException {
        application.setSeed(1888L);
        SaveGame game = NewGameFixture.newGame(application);
        ContractManager contracts = application.getGameEngine().getContractManager();
        Club preston = game.getCurrentClub();

        LocalDateTime april = LocalDateTime.of(1889, 4, 16, 4, 0);
        game.setGameDate(april);
        contracts.afterDay(april);
        List<PlayerContract> ending = contracts.endingContracts(preston);
        assertEquals(preston.getPlayers().size(), ending.size());
        contracts.retain(ending.get(0));
        contracts.changeOfferedWage(preston, ending.get(0), 5);
        contracts.release(ending.get(1));
        assertEquals(1, contracts.sendTerms(preston));

        // a save in the middle of the retained list keeps the marks
        roundTrip(game);

        LocalDateTime may = LocalDateTime.of(1889, 5, 1, 4, 0);
        game.setGameDate(may);
        contracts.afterDay(may);
        assertEquals(Integer.valueOf(1889), game.getContractsSeason());
        assertFalse(game.getFreeAgents().isEmpty(), "men not retained or who declined are free agents");

        Set<Player> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<String> short11 = new ArrayList<>();
        for (Club club : ContractManager.uniqueClubs(game)) {
            if (!PositionAssigner.canFieldTwoThreeFive(club.getPlayers())
                    || club.getPlayers().size() < contracts.bandOf(club, 1889).getMinimumSquad()) {
                short11.add(club.getDataId() + " (" + club.getPlayers().size() + ")");
            }
            assertEquals(club.getPlayers().size(), club.getPlayerContracts().size(), club.getDataId());
            for (Player p : club.getPlayers()) {
                assertTrue(seen.add(p), p.getPerson().getName() + " in two squads");
                assertEquals(Integer.valueOf(1889), club.getContractForPlayer(p.getId()).getSeason());
            }
        }
        assertEquals(List.of(), short11, "every club can field eleven in 1889-90");
        for (Player p : game.getFreeAgents()) {
            assertTrue(seen.add(p), "a free agent is in no squad");
            assertEquals(null, p.getPerson().getCurrentClubId());
        }

        // after the rollover a save keeps the new engagements and the free agents
        roundTrip(game);
    }

    private static void roundTrip(SaveGame game) {
        GameFingerprint before = GameFingerprint.of(game);
        String text = SaveCodec.encode(game, "G-04", LocalDateTime.of(2000, 1, 1, 0, 0));
        LoadResult loaded = SaveCodec.decode(text);
        assertTrue(loaded.isLoaded(), loaded.toString());
        assertEquals(new ArrayList<String>(), loaded.getProblems());
        assertEquals(before.toString(), GameFingerprint.of(loaded.getGame()).toString());
        assertEquals(text, SaveCodec.encode(loaded.getGame(), "G-04", LocalDateTime.of(2000, 1, 1, 0, 0)));
        NewGameFixture.bind(application, loaded.getGame());
        // the copy is not the game played on: bind the original back
        NewGameFixture.bind(application, game);
    }

    @Test
    void aSaveFromBeforeG04LoadsWithItsEngagementsDerived() throws IOException {
        String text;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("saves/v1_1888-89_new_game.json")) {
            assertNotNull(in);
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertFalse(text.contains("summerWage"), "a save made before G-04");
        LoadResult result = SaveCodec.decode(text);
        assertTrue(result.isLoaded(), result.toString());
        SaveGame game = result.getGame();
        int contracts = 0;
        for (Club club : ContractManager.uniqueClubs(game)) {
            for (PlayerContract c : club.getPlayerContracts()) {
                assertEquals(Integer.valueOf(1888), c.getSeason(), "derived from the 1 April 1888 start");
                assertEquals(LocalDateTime.of(1889, 4, 30, 23, 59, 59), c.getEndDate(), "derived end");
                assertEquals(0, c.getSummerWage().signum(), "no summer pay derived");
                if (c.isAmateur()) {
                    assertTrue(c.getWeeklyExpenses().signum() > 0, "an amateur gets his band's expenses");
                }
                contracts++;
            }
        }
        assertTrue(contracts > 500, "contracts: " + contracts);
        assertTrue(game.getFreeAgents().isEmpty());

        // and it plays on: the first day sets the running season from its engagements
        NewGameFixture.bind(application, game);
        NewGameFixture.play(application, game, 2, true);
        assertEquals(Integer.valueOf(1888), game.getContractsSeason());
    }
}
