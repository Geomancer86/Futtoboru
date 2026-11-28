package com.rndmodgames.futtoboru.engine.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.team.TeamSelector;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.match.ecs.ai.TeamStyle;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.Surface;

/**
 * E-17a: both match engines satisfy the {@link MatchEngine} contract on the same fixture: a valid score, scorers who
 * play for the side they scored for (at most one per goal), the same outcome for the same seed. Plus the engine
 * flag ({@link MatchEngines}) and the weather draw. One ECS match takes about a second, hence slow.
 */
@Tag("slow")
class MatchEngineContractTests {

    private static final LocalDateTime KICK_OFF = LocalDateTime.of(1888, 9, 8, 15, 30);

    @AfterEach
    void restoreFlag() {
        MatchEngines.reset();
    }

    static Club club(long id, long firstPersonId, float level) {
        Club c = new Club();
        c.setId(id);
        c.setName("Club " + id);
        c.setDataId("club-" + id);
        Position[] squad = { Position.GOALKEEPER, Position.FULL_BACK, Position.FULL_BACK, Position.HALF_BACK,
                Position.HALF_BACK, Position.HALF_BACK, Position.RIGHT_WING, Position.INSIDE_RIGHT,
                Position.CENTRE_FORWARD, Position.INSIDE_LEFT, Position.LEFT_WING, Position.HALF_BACK,
                Position.CENTRE_FORWARD, null };
        for (int i = 0; i < squad.length; i++) {
            Person person = new Person();
            person.setId(firstPersonId + i);
            person.setBirthDate(LocalDateTime.of(1862 + i % 6, 3, 1, 0, 0));
            Player p = new Player();
            p.setId(firstPersonId + 5000 + i);
            p.setPerson(person);
            p.setPosition(squad[i]);
            float r = level + (i % 3) - 1;
            p.setSpeed(r);
            p.setAcceleration(r);
            p.setStamina(r);
            p.setStrength(r);
            p.setKicking(r);
            p.setPassing(r);
            p.setDribbling(r);
            p.setShotStopping(r);
            c.getPlayers().add(p);
        }
        return c;
    }

    /** The committee's best sheet for the club (G-01), as an AI club would field. */
    static TeamSheet sheet(Club club) {
        return TeamSelector.bestSheet(club.getId(), TeamSheets.available(club), null);
    }

    static MatchFixture fixture(String key, Club home, Club away, MatchWeather weather) {
        return new MatchFixture(key, home, away, KICK_OFF, weather, false, sheet(home), sheet(away));
    }

    static MatchFixture fixture(MatchWeather weather) {
        return fixture("contract-1", club(1, 100, 15), club(2, 200, 11), weather);
    }

    private static Set<Long> personIds(Club club) {
        Set<Long> ids = new HashSet<>();
        for (Player p : club.getPlayers()) {
            ids.add(p.getPerson().getId());
        }
        return ids;
    }

    private static void assertContract(MatchEngine engine, MatchFixture f) {
        RandomSource root = RandomSource.seeded(1888);
        MatchOutcome a = engine.play(f, root.fork("match:" + f.matchKey()));
        MatchOutcome b = engine.play(f, RandomSource.seeded(1888).fork("match:" + f.matchKey()));
        String label = engine.id() + " " + a;
        assertEquals(a, b, "same seed, same outcome: " + label);
        assertTrue(a.homeGoals() >= 0 && a.homeGoals() <= 30 && a.awayGoals() >= 0 && a.awayGoals() <= 30, label);
        assertTrue(a.homeScorerIds().size() <= a.homeGoals() && a.awayScorerIds().size() <= a.awayGoals(), label);
        assertTrue(personIds(f.home()).containsAll(a.homeScorerIds()), "home scorers play for home: " + label);
        assertTrue(personIds(f.away()).containsAll(a.awayScorerIds()), "away scorers play for away: " + label);
    }

    @Test
    void theInstantEngineKeepsTheContract() {
        MatchEngine engine = new InstantMatchEngine();
        assertContract(engine, fixture(MatchWeather.DRY));
        for (int seed = 0; seed < 200; seed++) {
            MatchFixture f = fixture("m" + seed, club(1, 100, 15), club(2, 200, 11), MatchWeather.DRY);
            MatchOutcome o = engine.play(f, RandomSource.seeded(seed));
            assertEquals(o.homeGoals(), o.homeScorerIds().size(), "every instant goal has a scorer");
            assertTrue(personIds(f.home()).containsAll(o.homeScorerIds()));
            assertTrue(personIds(f.away()).containsAll(o.awayScorerIds()));
        }
    }

    @Test
    void theEcsEngineKeepsTheContract() {
        assertContract(new EcsMatchEngineAdapter(), fixture(new MatchWeather(Surface.WET, 0.36)));
    }

    @Test
    void theEcsSideIsTheFrozenSheetWithItsShapeAndStyle() {
        MatchFixture f = fixture(MatchWeather.DRY);
        for (TeamShape shape : TeamShape.values()) {
            TeamSheet sheet = sheet(f.home());
            sheet.reshape(shape);
            sheet.setStyle(ClubStyle.COMBINATION);
            EcsMatchEngineAdapter.Eleven xi = EcsMatchEngineAdapter.Eleven.of(sheet, f.home(), f);
            assertEquals(shape.label(), xi.sheet.shape().label());
            assertEquals(TeamStyle.COMBINATION, xi.style);
            for (int s = 0; s < TeamShape.SLOTS; s++) {
                assertEquals(shape.slot(s), xi.sheet.players().get(s).position(), shape + " slot " + s);
                Long playerId = sheet.getPlayerId(s);
                Long expected = null;
                for (Player p : f.home().getPlayers()) {
                    if (p.getId().equals(playerId)) {
                        expected = p.getPerson().getId();
                    }
                }
                assertEquals(expected, xi.personIds.get(s), "slot " + s + " is the sheet's man");
            }
        }

        // E-20: home advantage on the home ground only, none at a neutral one
        EcsMatchEngineAdapter.Eleven h = EcsMatchEngineAdapter.Eleven.of(f.homeSheet(), f.home(), f);
        EcsMatchEngineAdapter.Eleven a = EcsMatchEngineAdapter.Eleven.of(f.awaySheet(), f.away(), f);
        assertTrue(!EcsMatchEngineAdapter.input(f, h, a).neutral(), "home ground");
        MatchFixture neutral = new MatchFixture("n", f.home(), f.away(), KICK_OFF, MatchWeather.DRY, true,
                f.homeSheet(), f.awaySheet());
        assertTrue(EcsMatchEngineAdapter.input(neutral, h, a).neutral(), "neutral ground");

        TeamSheet shortSheet = sheet(f.away());
        shortSheet.setPlayerId(9, null);
        shortSheet.setPlayerId(10, null);
        EcsMatchEngineAdapter.Eleven gaps = EcsMatchEngineAdapter.Eleven.of(shortSheet, f.away(), f);
        assertEquals(11, gaps.sheet.players().size());
        assertEquals(2, gaps.personIds.stream().filter(id -> id == null).count(), "two stand-ins");
        assertEquals(EcsMatchEngineAdapter.STAND_IN_RATING, gaps.sheet.players().get(10).speed());
    }

    @Test
    void theFlagDefaultsToInstantAndSelectsEcs() {
        assertEquals(MatchEngine.INSTANT, MatchEngines.parse(null));
        assertEquals(MatchEngine.INSTANT, MatchEngines.parse(""));
        assertEquals(MatchEngine.ECS, MatchEngines.parse(" ECS "));
        assertEquals(MatchEngine.INSTANT, MatchEngines.parse("instant"));
        assertEquals(MatchEngine.INSTANT, MatchEngines.parse("box2d"));
        if (System.getProperty(MatchEngines.PROPERTY) == null) {
            assertEquals(MatchEngine.INSTANT, MatchEngines.selectedId(), "default unchanged");
        }
        MatchEngines.select("ecs");
        assertTrue(MatchEngines.selected() instanceof EcsMatchEngineAdapter);
        MatchEngines.select("instant");
        assertTrue(MatchEngines.selected() instanceof InstantMatchEngine);
    }

    @Test
    void weatherDependsOnDateAndGroundOnly() {
        RandomSource root = RandomSource.seeded(1888);
        LocalDate day = LocalDate.of(1889, 1, 12);
        assertEquals(MatchWeather.of(root, day, "deepdale"), MatchWeather.of(RandomSource.seeded(1888), day,
                "deepdale"));
        Set<Surface> seen = new HashSet<>();
        for (int d = 0; d < 300; d++) {
            seen.add(MatchWeather.of(root, day.plusDays(d % 90), "ground-" + d).surface());
        }
        assertTrue(seen.containsAll(List.of(Surface.DRY, Surface.WET, Surface.MUDDY, Surface.FROZEN)), "" + seen);
        assertEquals(MatchWeather.DRY, MatchWeather.of(root, null, "x"));
    }
}
