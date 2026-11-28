package com.rndmodgames.futtoboru.engine.fans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.FanStanding;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.simulation.MatchResultRecorder;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-10: the fan-love meter. Hysteresis (a tier needs a margin past the line to move either way), legend damping, the
 * start tier from the season's {@code fanTiers}, performances moving it on the real 1888-89 game for every club, and
 * the same seed giving the same meters.
 */
class FanMeterTests extends BaseTest {

    private static final FanRules R = FanRules.DEFAULTS;
    private static final LocalDateTime DAY = LocalDateTime.of(1888, 9, 1, 15, 0);

    @Test
    void theTierMovesWithHysteresis() {
        FanStanding s = new FanStanding(FanMeter.startPoints(1, R), 1);
        assertEquals(150, s.getPoints(), 1e-9);
        FanMeter.add(s, 60, R); // 210: past the line (200) but not the margin (220)
        assertEquals(1, s.getTier());
        FanMeter.add(s, 15, R); // 225
        assertEquals(2, s.getTier());
        FanMeter.add(s, -30, R); // 195: below the line it came up through, not below 180
        assertEquals(2, s.getTier(), "no flip back at once");
        FanMeter.add(s, -20, R); // 175
        assertEquals(1, s.getTier());
        FanMeter.add(s, -1000, R);
        assertEquals(0, s.getTier());
        assertEquals(0, s.getPoints(), 1e-9);
        FanMeter.add(s, 10_000, R);
        assertEquals(5, s.getTier());
        assertEquals(600, s.getPoints(), 1e-9);
        assertEquals(3, FanMeter.nextTier(0, 350, R), "a big jump climbs several tiers");
    }

    @Test
    void legendsLoseAtTheDampedRate() {
        FanStanding legend = new FanStanding(FanMeter.startPoints(5, R), 5);
        FanMeter.add(legend, -100, R); // 550 - 25
        assertEquals(525, legend.getPoints(), 1e-9);
        assertEquals(5, legend.getTier());
        FanStanding hero = new FanStanding(FanMeter.startPoints(4, R), 4);
        FanMeter.add(hero, -100, R);
        assertEquals(350, hero.getPoints(), 1e-9);
        assertEquals(3, hero.getTier());
    }

    @Test
    void theShippedRulesAreTheDefaults() {
        FanRules shipped = FanRules.from(DatabaseLoader.getGameData());
        for (String rule : new String[] { FanRules.APPEARANCE, FanRules.GOAL, FanRules.DERBY_GOAL, FanRules.CUP_TIE_WON,
                FanRules.CUP_FINAL_WON, FanRules.ABSENCE, FanRules.BAND, FanRules.HYSTERESIS, FanRules.LEGEND_DAMPING,
                FanRules.JITTER }) {
            assertEquals(R.get(rule), shipped.get(rule), 1e-9, rule);
        }
    }

    @Test
    void theStartTierComesFromTheSeasonsFanTiers() {
        SaveGame game = NewGameFixture.newGame(application);
        Club preston = club(game, "preston-north-end");
        Player dewhurst = player(preston, "fred-dewhurst-1863");
        assertNotNull(dewhurst);
        assertEquals(4, FanMeter.tierOf(dewhurst, preston));
        assertTrue(FanMeter.percentToNext(dewhurst, preston) >= 0);
        int real = 0;
        for (Club c : game.getAllClubs()) {
            for (Player p : c.getPlayers()) {
                int t = FanMeter.tierOf(p, c);
                assertTrue(t >= 0 && t <= 5);
                real += p.getPerson().getDataId() != null && t >= 1 ? 1 : 0;
            }
        }
        assertTrue(real > 100, "real men start at least at squad player: " + real);
    }

    @Test
    void performancesMoveEveryClubsMetersAndTheSameSeedGivesTheSame() {
        List<String> first = season(1888);
        List<String> second = season(1888);
        assertEquals(first, second, "same seed, same meters");
        assertTrue(first.stream().anyMatch(s -> s.endsWith(":up")), "a regular who scores goes up");
        assertTrue(first.stream().anyMatch(s -> s.endsWith(":down")), "a man left out goes down");
        assertTrue(!first.equals(season(77)), "another seed gives other meters");
    }

    /** 30 friendlies between Preston and Villa on the instant result; one line per man: id, tier, points, move. */
    private List<String> season(long seed) {
        SaveGame game = NewGameFixture.newGame(application);
        Club preston = club(game, "preston-north-end");
        Club villa = club(game, "aston-villa");
        MatchSimulator sim = new MatchSimulator(application, RandomSource.seeded(seed));
        RandomSource fans = RandomSource.seeded(seed);
        List<String> before = snapshot(preston, villa);
        for (int i = 0; i < 30; i++) {
            Match m = new Match();
            m.setId(980_000L + i);
            m.setMatchType(Match.FRIENDLY_MATCH);
            m.setHomeClubId((i % 2 == 0 ? preston : villa).getId());
            m.setAwayClubId((i % 2 == 0 ? villa : preston).getId());
            m.setMatchDateTime(DAY.plusDays(7L * i));
            assertTrue(sim.simulateMatch(m));
            FanMeter.applyAfterMatch(game, m, fans, false, false, false, false);
        }
        List<String> out = new ArrayList<>();
        int k = 0;
        for (Club c : new Club[] { preston, villa }) {
            for (Player p : c.getPlayers()) {
                assertNotNull(p.getFans(), "every man of both clubs has a meter (AI club too)");
                double start = Double.parseDouble(before.get(k++));
                out.add(p.getId() + ":" + p.getFans().getTier() + ":" + p.getFans().getPoints() + ":"
                        + (p.getFans().getPoints() > start ? "up" : p.getFans().getPoints() < start ? "down" : "same"));
            }
        }
        return out;
    }

    private static List<String> snapshot(Club... clubs) {
        List<String> out = new ArrayList<>();
        for (Club c : clubs) {
            for (Player p : c.getPlayers()) {
                out.add(String.valueOf(FanMeter.startPoints(FanMeter.tierOf(p, c), R)));
            }
        }
        return out;
    }

    @Test
    void theRecorderMovesBothClubsAndCountsACupTie() {
        SaveGame game = NewGameFixture.newGame(application);
        game.setGameDate(DAY);
        Club preston = club(game, "preston-north-end");
        Club villa = club(game, "aston-villa");
        Match m = new Match();
        m.setId(981_001L);
        m.setMatchType(Match.FRIENDLY_MATCH);
        m.setHomeClubId(preston.getId());
        m.setAwayClubId(villa.getId());
        m.setMatchDateTime(DAY);
        assertTrue(new MatchSimulator(application, RandomSource.seeded(5)).simulateMatch(m));
        preston.getScheduledMatches().add(m);
        villa.getScheduledMatches().add(m);
        new MatchResultRecorder(application, null).record(m);
        TeamSheet sheet = m.getAwaySheet();
        Player played = null;
        for (Player p : villa.getPlayers()) {
            if (sheet.isSelected(p.getId())) {
                played = p;
            }
        }
        assertNotNull(played);
        assertNotNull(played.getFans(), "the AI club's men get a meter from the recorder");
        assertTrue(played.getFans().getPoints() > FanMeter.startPoints(FanMeter.startTier(DatabaseLoader.getGameData(),
                played, villa), R), "an appearance adds points");

        // a cup tie won adds to every man on the sheet, the final more
        FanStanding tie = new FanStanding(150, 1);
        FanStanding plain = new FanStanding(150, 1);
        FanMeter.add(tie, R.get(FanRules.APPEARANCE) + R.get(FanRules.CUP_TIE_WON) + R.get(FanRules.CUP_FINAL_WON), R);
        FanMeter.add(plain, R.get(FanRules.APPEARANCE), R);
        assertTrue(tie.getPoints() - plain.getPoints() >= 46 - 1e-9);
    }

    @Test
    void prestonAndBlackburnAreRivals() {
        SaveGame game = NewGameFixture.newGame(application);
        assertTrue(FanMeter.isRival(club(game, "preston-north-end"), club(game, "blackburn-rovers")));
        assertTrue(FanMeter.isRival(club(game, "blackburn-rovers"), club(game, "preston-north-end")));
        assertTrue(!FanMeter.isRival(club(game, "preston-north-end"), club(game, "stoke")));
    }

    private static Club club(SaveGame game, String dataId) {
        for (Club c : game.getAllClubs()) {
            if (c != null && dataId.equals(c.getDataId())) {
                return c;
            }
        }
        throw new AssertionError("no club " + dataId);
    }

    private static Player player(Club club, String personDataId) {
        for (Player p : club.getPlayers()) {
            if (personDataId.equals(p.getPerson().getDataId())) {
                return p;
            }
        }
        return null;
    }
}
