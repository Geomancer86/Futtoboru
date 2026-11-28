package com.rndmodgames.futtoboru.engine.random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;
import com.rndmodgames.futtoboru.engine.temporal.CompetitionScheduler;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.generators.PersonGenerator;

/**
 * P2-0: the same seed gives the same generated data and match results; another seed gives others.
 * (The full seeded-season test is P2-3.)
 */
@Tag("slow")
class SeededGameTests extends BaseTest {

    @AfterAll
    static void reloadDatabase() {
        // the tests rebuilt the shared database with fixed seeds; let the next class load a normal one
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    /** Generated attributes, contracts, occupations and completed birth dates of the whole database. */
    private static String databaseFingerprint(long seed) {
        DatabaseLoader.load(GameData.loadDefault(), RandomSource.seeded(seed));
        StringBuilder sb = new StringBuilder();
        for (Person person : DatabaseLoader.getPersons()) {
            sb.append(person.getId()).append(person.getBirthDate()).append(';');
        }
        for (Season season : DatabaseLoader.getInstance().getSeasons()) {
            for (Club club : season.getClubs()) {
                for (Player p : club.getPlayers()) {
                    sb.append(p.getId()).append(':').append(p.getAcceleration()).append(',').append(p.getSpeed())
                            .append(',').append(p.getPassing()).append(',').append(p.getTackling()).append(',')
                            .append(p.getShotStopping()).append(',').append(p.getCourage()).append(',')
                            .append(p.getPlayerProfession() != null ? p.getPlayerProfession().getName() : "-")
                            .append(';');
                }
                for (PlayerContract c : club.getPlayerContracts()) {
                    sb.append(c.getPlayerId()).append('/').append(c.getContractType()).append('/')
                            .append(c.getContractLengthMonths()).append('/').append(c.getWeeklyWage()).append('/')
                            .append(c.getSigningBonus()).append(';');
                }
            }
        }
        return sb.toString();
    }

    @Test
    void sameSeedBuildsTheSameDatabase() {
        String a = databaseFingerprint(1888);
        String b = databaseFingerprint(1888);
        String c = databaseFingerprint(1889);
        assertTrue(a.length() > 1000, "the database has generated players");
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    private static List<Club> clubsWithPlayers() {
        List<Club> clubs = new ArrayList<>();
        Season season = DatabaseLoader.getSeasonByDataId("1888-89");
        for (Club club : season.getClubs()) {
            if (club.getPlayers() != null && !club.getPlayers().isEmpty()) {
                clubs.add(club);
            }
        }
        return clubs;
    }

    /** Plays friendlies between the first clubs, in the given order of match ids. */
    private static List<String> playMatches(Futtoboru game, List<Club> clubs, boolean reversed) {
        SaveGame save = new SaveGame();
        save.getAllClubs().addAll(clubs);
        game.setCurrentGame(save);
        MatchSimulator simulator = new MatchSimulator(game);

        List<Match> matches = new ArrayList<>();
        for (int i = 0; i + 1 < clubs.size() && matches.size() < 6; i += 2) {
            Match m = new Match();
            m.setId(900_000L + i);
            m.setMatchType(Match.FRIENDLY_MATCH);
            m.setHomeClubId(clubs.get(i).getId());
            m.setAwayClubId(clubs.get(i + 1).getId());
            matches.add(m);
        }
        List<Match> order = new ArrayList<>(matches);
        if (reversed) {
            java.util.Collections.reverse(order);
        }
        for (Match m : order) {
            assertTrue(simulator.simulateMatch(m));
        }
        List<String> results = new ArrayList<>();
        for (Match m : matches) {
            results.add(m.getId() + " " + m.getHomeGoals() + "-" + m.getAwayGoals() + " gate " + m.getAttendance()
                    + " " + m.getHomeScorerIds() + m.getAwayScorerIds());
        }
        return results;
    }

    @Test
    void sameSeedPlaysTheSameMatchesInAnyOrder() {
        DatabaseLoader.load(GameData.loadDefault(), RandomSource.seeded(7));
        List<Club> clubs = clubsWithPlayers();
        assertTrue(clubs.size() >= 4);

        List<String> a = playMatches(new Futtoboru(2024L), clubs, false);
        List<String> b = playMatches(new Futtoboru(2024L), clubs, true);
        assertEquals(a, b, "a result depends on the seed and the match, not on the order of play");

        // another seed: some of the six results (goals, gates, scorers) differ
        List<String> c = playMatches(new Futtoboru(2025L), clubs, false);
        assertNotEquals(a, c);
    }

    @Test
    void sameSeedGeneratesTheSamePeopleAndDraws() {
        Country england = DatabaseLoader.getCountryByDataId("england");
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        PersonGenerator genA = new PersonGenerator(new Futtoboru(11L));
        PersonGenerator genB = new PersonGenerator(new Futtoboru(11L));
        PersonGenerator genC = new PersonGenerator(new Futtoboru(12L));
        List<String> c = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(1888, 9, 8, 15, 0);
        Season season = new Season();
        season.setStartDate(start);
        for (int i = 0; i < 20; i++) {
            Person pa = genA.generateUniquePerson(england, season, false);
            Person pb = genB.generateUniquePerson(england, season, false);
            Person pc = genC.generateUniquePerson(england, season, false);
            a.add(pa.getName() + " " + pa.getLastname() + " " + pa.getBirthDate());
            b.add(pb.getName() + " " + pb.getLastname() + " " + pb.getBirthDate());
            c.add(pc.getName() + " " + pc.getLastname() + " " + pc.getBirthDate());
        }
        assertEquals(a, b);
        assertNotEquals(a, c);

        // draws: same seed and entrants, same pairings
        List<Long> ids = new ArrayList<>();
        for (Club club : clubsWithPlayers()) {
            ids.add(club.getId());
        }
        com.rndmodgames.futtoboru.data.Competition comp = DatabaseLoader.getCompetitions().get(0);
        assertEquals(pairings(new CompetitionScheduler(new Futtoboru(3L)).competitionDraw(comp, ids)),
                pairings(new CompetitionScheduler(new Futtoboru(3L)).competitionDraw(comp, ids)));
        assertNotEquals(pairings(new CompetitionScheduler(new Futtoboru(3L)).competitionDraw(comp, ids)),
                pairings(new CompetitionScheduler(new Futtoboru(4L)).competitionDraw(comp, ids)));
    }

    private static List<String> pairings(List<Match> matches) {
        List<String> out = new ArrayList<>();
        for (Match m : matches) {
            out.add(m.getHomeClubId() + "v" + m.getAwayClubId());
        }
        return out;
    }

    @Test
    void theGameExposesItsSeed() {
        Futtoboru game = new Futtoboru(1888L);
        assertEquals(1888L, game.getSeed());
        assertEquals(1888L, game.random().getSeed());
        game.setSeed(99L);
        assertEquals(99L, game.getSeed());
    }
}
