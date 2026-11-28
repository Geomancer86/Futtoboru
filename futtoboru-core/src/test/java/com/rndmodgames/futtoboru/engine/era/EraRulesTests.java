package com.rndmodgames.futtoboru.engine.era;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.match.ecs.laws.RulesProfile;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-11: rules code reads the era profile (goal nets, league points, the ECS laws), and the first-season
 * announcement (the points system, November 1888) reaches the inbox once, the same in every run.
 */
class EraRulesTests extends BaseTest {

    private static EngineTestWorld world() {
        EngineTestWorld world = new EngineTestWorld(application, 2);
        world.clubs.forEach(c -> c.getPlayers().forEach(p -> {
            p.getPerson().setBirthDate(LocalDateTime.of(1865, 1, 1, 0, 0));
            for (java.lang.reflect.Field f : Player.class.getDeclaredFields()) {
                if (f.getType() == Float.class) {
                    try {
                        f.setAccessible(true);
                        f.set(p, 10f);
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }));
        return world;
    }

    private static List<Message> era(SaveGame game) {
        return game.getAllMessages().stream().filter(m -> EraEventScheduler.MESSAGE_TYPE.equals(m.getMessageType()))
                .collect(Collectors.toList());
    }

    private static List<String> inbox(EngineTestWorld world, LocalDateTime from, int days) {
        world.game.setGameDate(from);
        for (int d = 0; d < days; d++) {
            world.engine.continueGame();
        }
        return era(world.game).stream().map(m -> m.getActionData() + "|" + m.getMessageTime().toLocalDate() + "|"
                + m.getTitle() + "|" + m.getPlainTextMessage()).collect(Collectors.toList());
    }

    @Test
    void thePointsSystemIsAnnouncedOnceOnItsDayInTheFirstSeason() {
        EngineTestWorld world = world();
        List<String> first = inbox(world, LocalDateTime.of(1888, 11, 19, 12, 0), 4); // 19 .. 22 November
        assertEquals(1, first.size(), first.toString());
        assertTrue(first.get(0).startsWith("league-1888-points-system|1888-11-21|"), first.get(0));
        Message m = era(world.game).get(0);
        assertEquals("football-league", m.getSender());
        assertFalse(m.getPlainTextMessage().contains("{"), m.getPlainTextMessage());

        // replaying the same days sends nothing again
        inbox(world, LocalDateTime.of(1888, 11, 19, 12, 0), 4);
        assertEquals(1, era(world.game).size());
    }

    @Test
    void theAnnouncementsAreTheSameInEveryRun() {
        List<String> a = inbox(world(), LocalDateTime.of(1891, 1, 10, 12, 0), 4);
        List<String> b = inbox(world(), LocalDateTime.of(1891, 1, 10, 12, 0), 4);
        assertEquals(List.of("press-1891-first-goal-nets"),
                a.stream().map(s -> s.substring(0, s.indexOf('|'))).collect(Collectors.toList()));
        assertEquals(a, b);
    }

    @Test
    void goalNetsFromTheEraProfile() {
        assertFalse(EraRules.goalNets(LocalDate.of(1888, 9, 8)));
        assertFalse(EraRules.goalNets(LocalDate.of(1891, 8, 31)));
        assertTrue(EraRules.goalNets(LocalDate.of(1891, 9, 1)));
        assertFalse(EraRules.goalNets(null), "no date: the 1888-89 baseline");
        // League clubs only: a competition outside the scope keeps the baseline
        assertTrue(EraRules.at(LocalDate.of(1892, 1, 1), "football-league").getBoolean("rules.goalNets"));
        assertFalse(EraRules.at(LocalDate.of(1892, 1, 1), "fa-cup").getBoolean("rules.goalNets"));
    }

    @Test
    void leagueRulesComeFromTheEra() {
        CompetitionRules r = CompetitionRules.fromEra(EraRules.at(LocalDate.of(1888, 9, 8), "football-league"));
        assertEquals(2, r.getPointsForWin());
        assertEquals(1, r.getPointsForDraw());
        assertEquals(0, r.getPointsForLoss());
        CompetitionRules historical = CompetitionRules.createHistoricalRules();
        assertEquals(historical.getTieBreakingOrder(), r.getTieBreakingOrder());
    }

    /** The ECS laws taken from the era data agree with the engine's season table, every season 1888-1939. */
    @Test
    void ecsLawsFromTheEraMatchTheSeasonTable() {
        for (int y = 1888; y <= 1939; y++) {
            RulesProfile fromEra = RulesProfile.fromEra(EraRules.at(LocalDate.of(y, 9, 15)), y);
            assertEquals(RulesProfile.forSeason(y), fromEra, "season " + y);
        }
        // penalties only from the sourced date (IFAB, June 1891, in force 1891-92)
        assertFalse(RulesProfile.fromEra(EraRules.at(LocalDate.of(1891, 8, 31)), 1890).penalties());
        assertTrue(RulesProfile.fromEra(EraRules.at(LocalDate.of(1891, 9, 1)), 1891).penalties());
    }
}
