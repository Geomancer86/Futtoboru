package com.rndmodgames.futtoboru.engine.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.StaffJudgement;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * R-09 + R-03 in a new 1888-89 game: every club (AI too) has a trainer and a secretary, the player is his club's
 * secretary, everyone judging has skills and a bias; the pools exist from the first screen; all of it survives a save
 * and a save without it gets it on load.
 */
@Tag("slow")
class RecruitmentGameTests extends BaseTest {

    @AfterAll
    static void restore() {
        DatabaseLoader.resetDatabaseLoaderInstance(); // the new games mutated the shared database's clubs
    }

    private static SaveGame newGame(long seed) {
        application.setSeed(seed);
        return NewGameFixture.newGame(application);
    }

    @Test
    void everyClubHasATrainerAndASecretaryAndThePlayerIsHisClubsSecretary() {
        SaveGame game = newGame(1888L);
        Club mine = game.getCurrentClub();
        Profession secretaryPost = DatabaseLoader.getProfessionByDataId(Profession.SECRETARY);
        Set<Long> holders = new HashSet<>();
        for (Club club : ContractManager.uniqueClubs(game)) {
            Person trainer = StaffRoles.trainerOf(game, club);
            Person secretary = StaffRoles.secretaryOf(game, club);
            assertNotNull(trainer, club.getDataId());
            assertNotNull(secretary, club.getDataId());
            assertTrue(trainer.getPrimaryProfession().is(Profession.TRAINER));
            assertEquals(club.getId(), trainer.getCurrentClubId());
            assertTrue(holders.add(trainer.getId()), "one post each");
            if (club == mine) {
                assertSame(game.getOwner(), secretary, "the player is his club's secretary");
                assertNull(club.getStaffId(secretaryPost.getId()), "never stored: he holds his own post only");
            } else {
                // the club's own secretary until the job market appoints a secretary-manager (G-05's secretary)
                assertTrue(secretary.getPrimaryProfession().is(Profession.SECRETARY)
                        || secretary.getPrimaryProfession().is(Profession.MANAGER), club.getDataId());
                assertEquals(club.getId(), secretary.getCurrentClubId());
                assertTrue(holders.add(secretary.getId()), "one post each");
            }
            assertJudges(trainer, Profession.TRAINER);
            assertJudges(secretary, Profession.SECRETARY);
            assertFalse(trainer.getLanguages().isEmpty(), "languages (R-11a)");
        }
        assertTrue(game.getOwner().getJudgement() != null, "the player judges too");
        assertEquals(0, StaffRoles.ensure(game, game.getGameDate(), application.random()), "idempotent");
        assertFalse(game.getCandidates().isEmpty(), "the pools exist from the first screen");
        assertEquals(Integer.valueOf(1888), game.getCandidatePoolSeason());
        assertEquals("lancashire", CandidatePools.regionOf(mine).dataId());

        // G-05's committee men (made on the first day) judge too, after the same day's staff step
        application.getGameEngine().continueGame();
        assertNotNull(mine.getCommittee(), "G-05 committee");
        assertFalse(mine.getCommittee().getMembers().isEmpty());
        for (com.rndmodgames.futtoboru.data.CommitteeMember m : mine.getCommittee().getMembers()) {
            StaffJudgement j = m.getJudgement();
            assertNotNull(j, m.getFullName());
            assertTrue(j.getMental() >= StaffJudgement.MIN && j.getMental() <= StaffJudgement.MAX);
            assertEquals("lancashire", j.getNetwork().get(0));
        }

        // the player leaves: the AI club gets a secretary of its own the next time
        game.getOwner().setCurrentClubId(null);
        mine.setStaffId(DatabaseLoader.getProfessionByDataId(Profession.MANAGER).getId(), null);
        assertEquals(1, StaffRoles.ensure(game, game.getGameDate().plusDays(1), application.random()));
        assertTrue(StaffRoles.secretaryOf(game, mine).getPrimaryProfession().is(Profession.SECRETARY));
    }

    private static void assertJudges(Person p, String role) {
        StaffJudgement j = p.getJudgement();
        assertNotNull(j, p.getLastname());
        for (int v : new int[] { j.getPhysical(), j.getTechnical(), j.getMental(), j.getKeeper() }) {
            assertTrue(v >= StaffJudgement.MIN && v <= StaffJudgement.MAX, j.toString());
        }
        assertTrue(StaffJudgement.BIASES.contains(j.getBias()), j.toString());
        if (Profession.TRAINER.equals(role)) {
            assertTrue(j.getBias().equals(StaffJudgement.BIAS_PACE) || j.getBias().equals(StaffJudgement.BIAS_SIZE));
            assertTrue(j.getPhysical() >= 12, "a trainer judges physique well: " + j);
        }
    }

    @Test
    void judgementProfilesPerRole() {
        int trainerPhysical = 0;
        int committeeMental = 0;
        int committeeLocal = 0;
        int n = 200;
        for (int i = 0; i < n; i++) {
            StaffJudgement t = StaffRoles.judgementFor(Profession.TRAINER, List.of("lancashire"),
                    com.rndmodgames.futtoboru.engine.random.RandomSource.seeded(i).fork("judge:" + i));
            StaffJudgement c = StaffRoles.judgementFor(Profession.COMMITTEE_MEMBER, List.of("lancashire"),
                    com.rndmodgames.futtoboru.engine.random.RandomSource.seeded(i).fork("judge:c" + i));
            trainerPhysical += t.getPhysical() - t.getTechnical();
            committeeMental += c.getMental() - c.getPhysical();
            committeeLocal += StaffJudgement.BIAS_LOCAL.equals(c.getBias()) ? 1 : 0;
            assertEquals("lancashire", t.getNetwork().get(0));
        }
        assertTrue(trainerPhysical / (double) n > 4, "the trainer judges physique best");
        assertTrue(committeeMental / (double) n > 2, "a committee man judges character best");
        assertTrue(committeeLocal > n / 2, "and likes a local lad");
    }

    @Test
    void sameSeedSameStaffAndPools() {
        List<String> a = staffAndPools(newGame(42L));
        List<String> b = staffAndPools(newGame(42L));
        assertEquals(a, b);
    }

    private static List<String> staffAndPools(SaveGame game) {
        List<String> out = new ArrayList<>(CandidatePoolsTests.fingerprint(game));
        for (Club club : ContractManager.uniqueClubs(game)) {
            Person t = StaffRoles.trainerOf(game, club);
            Person s = StaffRoles.secretaryOf(game, club);
            out.add(club.getDataId() + "|" + t.getName() + " " + t.getLastname() + "|" + t.getJudgement() + "|"
                    + s.getName() + " " + s.getLastname() + "|" + s.getJudgement());
        }
        return out;
    }

    @Test
    void staffAndPoolsSurviveASaveAndAnOlderSaveGetsThemOnLoad() {
        SaveGame game = newGame(1888L);
        // the fixture's manager has no languages (R-11a fills them on load): give them now so the text compares
        com.rndmodgames.futtoboru.system.generators.LanguageGenerator.fillMissing(game.getAllPersons(),
                DatabaseLoader.getLanguageData(), DatabaseLoader.getGameData(), DatabaseLoader.getRandom());
        List<String> before = staffAndPools(game);
        LocalDateTime at = LocalDateTime.of(2000, 1, 1, 0, 0);
        String text = SaveCodec.encode(game, "R-03", at);
        LoadResult loaded = SaveCodec.decode(text);
        assertTrue(loaded.isLoaded(), loaded.toString());
        assertEquals(new ArrayList<String>(), loaded.getProblems());
        SaveGame copy = loaded.getGame();
        // the secretary of the player's club is the loaded owner
        NewGameFixture.bind(application, copy);
        assertEquals(before, staffAndPools(copy));
        assertEquals(text, SaveCodec.encode(copy, "R-03", at), "a second save is the same text");

        // a save without them (made before R-03, no format change): the same pools and staff on load
        JsonValue root = new JsonReader().parse(text);
        JsonValue g = root.get("game");
        g.remove("candidates");
        g.remove("candidatePoolSeason");
        g.remove("candidateArrivalsMonth");
        LoadResult older = SaveCodec.decode(root.prettyPrint(OutputType.json, 100));
        assertTrue(older.isLoaded(), older.toString());
        NewGameFixture.bind(application, older.getGame());
        assertEquals(CandidatePoolsTests.fingerprint(game), CandidatePoolsTests.fingerprint(older.getGame()));
        assertEquals(Integer.valueOf(1888), older.getGame().getCandidatePoolSeason());
        NewGameFixture.bind(application, game);
    }
}
