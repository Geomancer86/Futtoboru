package com.rndmodgames.futtoboru.engine.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.StaffJudgement;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.recruitment.Knowledge;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.recruitment.Perception.Attr;
import com.rndmodgames.futtoboru.engine.recruitment.Perception.Range;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * R-04 (design §4): perceived ranges hold the truth, narrow with exposure and a better judge, a bias shifts them, the
 * club view weights skill x minutes, the same seed gives the same ranges, exposure comes from the matches attended and
 * survives a save. Hand-made objects, no new game (fast tier).
 */
class PerceptionTests extends BaseTest {

    private static final RandomSource ROOT = RandomSource.seeded(1888L);
    private static final Attr SPEED = Perception.byName("Speed");
    private static final Attr DRIBBLING = Perception.byName("Dribbling");

    private static Player man(long id, float value) {
        Player p = new Player();
        p.setId(id);
        Person person = new Person();
        person.setId(id + 100000);
        p.setPerson(person);
        for (Attr a : Perception.ATTRIBUTES) {
            set(p, a, value);
        }
        return p;
    }

    private static void set(Player p, Attr a, float v) {
        String setter = "set" + Character.toUpperCase(camel(a.id()).charAt(0)) + camel(a.id()).substring(1);
        try {
            Player.class.getMethod(setter, Float.class).invoke(p, v);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(setter, e);
        }
    }

    private static String camel(String id) {
        StringBuilder b = new StringBuilder();
        boolean up = false;
        for (char c : id.toCharArray()) {
            if (c == '_') {
                up = true;
            } else {
                b.append(up ? Character.toUpperCase(c) : c);
                up = false;
            }
        }
        return b.toString();
    }

    private static Observers.Observer judge(String key, int skill, String bias) {
        return new Observers.Observer(key, Observers.TRAINER, key,
                new StaffJudgement(skill, skill, skill, skill, bias, List.of("lancashire")));
    }

    @Test
    void everyAttributeHasAGetterAndASetter() {
        Player p = man(1, 9f);
        for (Attr a : Perception.ATTRIBUTES) {
            assertEquals(9f, a.of(p), a.id());
        }
        assertEquals(33, Perception.ATTRIBUTES.size());
    }

    @Test
    void rangesAlwaysHoldTheTruth() {
        RandomSource dice = RandomSource.seeded(7L);
        String[] biases = { null, StaffJudgement.BIAS_PACE, StaffJudgement.BIAS_SIZE, StaffJudgement.BIAS_FLAIR,
                StaffJudgement.BIAS_LOCAL, StaffJudgement.BIAS_SCOTS };
        float[] minutes = { 1f, 15f, 90f, 270f, 900f, 9000f };
        int checked = 0;
        for (int i = 0; i < 60; i++) {
            Player p = man(i, 3f + dice.nextInt(21));
            for (Attr a : Perception.ATTRIBUTES) {
                set(p, a, 3f + dice.nextFloat() * 20f);
            }
            Observers.Observer o = judge("p" + i, 3 + dice.nextInt(21), biases[i % biases.length]);
            for (float m : minutes) {
                List<Range> rs = new ArrayList<>();
                List<Double> ws = new ArrayList<>();
                for (Attr a : Perception.ATTRIBUTES) {
                    Range r = Perception.perceive(ROOT, o, p, a, m, i % 2 == 0, i % 3 == 0);
                    assertTrue(r.seen());
                    assertTrue(r.contains(a.of(p)), o + " " + a + " " + r + " truth " + a.of(p));
                    assertTrue(r.lo() >= Perception.MIN && r.hi() <= Perception.MAX, r.toString());
                    assertTrue(r.shownLo() <= a.of(p) && a.of(p) <= r.shownHi());
                    rs.add(r);
                    ws.add((double) m);
                    checked++;
                }
                // a club view of views that hold the truth holds it too
                Range all = Perception.consensus(rs.subList(0, 1), ws.subList(0, 1));
                assertTrue(all.contains(Perception.ATTRIBUTES.get(0).of(p)));
            }
        }
        assertTrue(checked > 10000);
    }

    @Test
    void neverSeenIsNoOpinionAndMoreMinutesOrABetterJudgeNarrowIt() {
        Player p = man(5, 12f);
        Observers.Observer poor = judge("p1", 5, null);
        Observers.Observer good = judge("p2", 20, null);
        assertFalse(Perception.perceive(ROOT, poor, p, SPEED, 0, false, false).seen());
        float last = Float.MAX_VALUE;
        for (float m : new float[] { 15f, 90f, 270f, 900f, 2700f }) {
            float w = Perception.perceive(ROOT, poor, p, SPEED, m, false, false).width();
            assertTrue(w <= last, m + " minutes: " + w + " after " + last);
            last = w;
            assertTrue(Perception.perceive(ROOT, good, p, SPEED, m, false, false).width() < w, "better judge");
        }
        assertTrue(last < 2.5f, "a season of looks by a poor judge: " + last);
        // character is slower to read; an outfield man is rarely seen in goal
        assertTrue(Perception.sigma(11, Perception.Group.MENTAL, false, 90)
                > Perception.sigma(11, Perception.Group.PHYSICAL, false, 90));
        assertTrue(Perception.sigma(11, Perception.Group.KEEPER, false, 90)
                > Perception.sigma(11, Perception.Group.KEEPER, true, 90));
    }

    @Test
    void aBiasShiftsTheRangeOnItsAttributesOnly() {
        Player p = man(6, 12f);
        Observers.Observer plain = judge("p9", 12, null);
        Observers.Observer pace = judge("p9", 12, StaffJudgement.BIAS_PACE); // same key: the same error
        Range a = Perception.perceive(ROOT, plain, p, SPEED, 900, false, false);
        Range b = Perception.perceive(ROOT, pace, p, SPEED, 900, false, false);
        assertEquals(a.centre() + 1f, b.centre(), 1e-4, "pace: speed looks a point better");
        assertEquals(Perception.perceive(ROOT, plain, p, DRIBBLING, 900, false, false).centre(),
                Perception.perceive(ROOT, pace, p, DRIBBLING, 900, false, false).centre(), 1e-6);
        // a committee man's local lad and the Scots: everything, only for such a man
        Observers.Observer local = judge("p9", 12, StaffJudgement.BIAS_LOCAL);
        assertEquals(a.centre() + 1f, Perception.perceive(ROOT, local, p, SPEED, 900, true, false).centre(), 1e-4);
        assertEquals(a.centre(), Perception.perceive(ROOT, local, p, SPEED, 900, false, false).centre(), 1e-6);
        Observers.Observer scots = judge("p9", 12, StaffJudgement.BIAS_SCOTS);
        assertEquals(Perception.perceive(ROOT, plain, p, DRIBBLING, 900, false, true).centre() + 1f,
                Perception.perceive(ROOT, scots, p, DRIBBLING, 900, false, true).centre(), 1e-4);
    }

    @Test
    void theClubViewWeightsSkillTimesMinutesAndHoldsTheTruth() {
        Range near = Perception.range(12f, 1.0, 0.0, 0);
        Range wide = Perception.range(12f, 4.0, 1.0, 1.0);
        Range heavy = Perception.consensus(List.of(near, wide), List.of(9.0, 1.0));
        Range light = Perception.consensus(List.of(near, wide), List.of(1.0, 9.0));
        assertTrue(Math.abs(heavy.centre() - near.centre()) < Math.abs(light.centre() - near.centre()));
        assertTrue(heavy.contains(12f) && light.contains(12f));
        assertTrue(heavy.width() < light.width());
        // nobody who saw him: no opinion; a weightless or unseen view does not count
        assertFalse(Perception.consensus(List.of(Perception.UNSEEN), List.of(5.0)).seen());
        Range one = Perception.consensus(List.of(near, Perception.UNSEEN, wide), List.of(2.0, 9.0, 0.0));
        assertEquals(near.lo(), one.lo(), 1e-6);
        assertEquals(near.hi(), one.hi(), 1e-6);
    }

    @Test
    void theSameSeedGivesTheSameRangesAndAnotherSeedOthers() {
        Player p = man(8, 11f);
        Observers.Observer o = judge("p3", 11, StaffJudgement.BIAS_FLAIR);
        Map<Attr, Range> first = view(RandomSource.seeded(42L), o, p);
        Map<Attr, Range> again = view(RandomSource.seeded(42L), o, p);
        Map<Attr, Range> other = view(RandomSource.seeded(43L), o, p);
        boolean differs = false;
        for (Attr a : Perception.ATTRIBUTES) {
            assertEquals(first.get(a).lo(), again.get(a).lo(), 0f);
            assertEquals(first.get(a).hi(), again.get(a).hi(), 0f);
            differs |= first.get(a).centre() != other.get(a).centre();
        }
        assertTrue(differs);
        // the error is the observer's own: another observer errs elsewhere
        assertNotEquals(Perception.error(ROOT, "p3", 8L, SPEED), Perception.error(ROOT, "p4", 8L, SPEED));
    }

    private static Map<Attr, Range> view(RandomSource root, Observers.Observer o, Player p) {
        Map<Attr, Range> out = new LinkedHashMap<>();
        for (Attr a : Perception.ATTRIBUTES) {
            out.put(a, Perception.perceive(root, o, p, a, 180, false, false));
        }
        return out;
    }

    @Test
    void periodWordsNameHisBestAndWorst() {
        Player p = man(9, 11f);
        Map<Attr, Range> ranges = new LinkedHashMap<>();
        for (Attr a : Perception.ATTRIBUTES) {
            ranges.put(a, Perception.exact(11f));
        }
        assertEquals(List.of("perceive_ordinary"), Perception.phraseKeys(ranges, false));
        ranges.put(SPEED, Perception.exact(21f));
        ranges.put(DRIBBLING, Perception.exact(16f));
        ranges.put(Perception.byName("Stamina"), Perception.exact(5f));
        ranges.put(Perception.byName("Shot Stopping"), Perception.exact(22f));
        assertEquals(List.of("perceive_speed_high", "perceive_dribbling_high", "perceive_stamina_low"),
                Perception.phraseKeys(ranges, false), "keeper attributes only for a keeper");
        assertEquals("perceive_band_5", Perception.bandKey(ranges.get(SPEED)));
        assertEquals("perceive_band_1", Perception.bandKey(Perception.exact(4f)));
        assertEquals("perceive_band_unknown", Perception.bandKey(Perception.UNSEEN));
        assertEquals("perceive_conf_sure", Perception.confidenceKey(ranges, false));
        Map<Attr, Range> none = new LinkedHashMap<>();
        none.put(SPEED, Perception.UNSEEN);
        assertEquals(List.of("perceive_not_seen"), Perception.phraseKeys(none, false));
        assertNull(Perception.confidenceKey(none, false));
        assertNotNull(p);
    }

    @Test
    void everyWordKeyIsInTheFourBundles() throws IOException {
        for (String bundle : List.of("en_us", "en_uk", "sp_sp", "sp_la")) {
            Properties props = new Properties();
            try (InputStream in = getClass().getResourceAsStream("/mods/language/BaseBundle_" + bundle + ".properties")) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
            for (String key : Perception.allWordKeys()) {
                String v = props.getProperty(key);
                assertTrue(v != null && !v.isBlank(), key + " missing in " + bundle);
            }
        }
    }

    // ------------------------------------------------------------------------------------------- the game

    /** The player's club (id 1, its secretary = the owner, a trainer, a captain) against club 2. */
    private static SaveGame game(Player captain, List<Player> opponents) {
        SaveGame g = new SaveGame();
        Club mine = new Club();
        mine.setId(1L);
        mine.setDataId("test-mine");
        Club other = new Club();
        other.setId(2L);
        other.setDataId("test-other");
        g.getAllClubs().add(mine);
        g.getAllClubs().add(other);
        Person owner = new Person();
        owner.setId(500L);
        owner.setCurrentClubId(1L);
        owner.setJudgement(new StaffJudgement(11, 11, 11, 11, null, List.of()));
        g.setOwner(owner);
        g.getAllPersons().add(owner);
        Person trainer = new Person();
        trainer.setId(501L);
        trainer.setCurrentClubId(1L);
        trainer.setJudgement(new StaffJudgement(15, 9, 10, 8, StaffJudgement.BIAS_PACE, List.of()));
        g.getAllPersons().add(trainer);
        mine.setStaffId(DatabaseLoader.getInstance().getProfessionByDataId(Profession.TRAINER).getId(), 501L);
        mine.getPlayers().add(captain);
        TeamSheet standing = new TeamSheet(1L, TeamShape.TWO_THREE_FIVE, ClubStyle.COMBINATION);
        standing.setPlayerId(0, captain.getId());
        standing.setCaptainId(captain.getId());
        mine.setTeamSheet(standing);
        captain.getPerson().setCurrentClubId(1L);
        for (Player p : opponents) {
            other.getPlayers().add(p);
            p.getPerson().setCurrentClubId(2L);
        }
        return g;
    }

    private static Match played(long id, boolean home, Player captain, List<Player> opponents) {
        Match m = new Match();
        m.setId(id);
        m.setHomeClubId(home ? 1L : 2L);
        m.setAwayClubId(home ? 2L : 1L);
        m.setIsPlayed(true);
        m.setMatchDateTime(LocalDateTime.of(1888, 9, 8 + (int) id, 15, 0));
        TeamSheet ours = new TeamSheet(1L, TeamShape.TWO_THREE_FIVE, ClubStyle.COMBINATION);
        ours.setPlayerId(0, captain.getId());
        ours.setCaptainId(captain.getId());
        TeamSheet theirs = new TeamSheet(2L, TeamShape.TWO_THREE_FIVE, ClubStyle.COMBINATION);
        for (int i = 0; i < opponents.size(); i++) {
            theirs.setPlayerId(i, opponents.get(i).getId());
        }
        m.setHomeSheet(home ? ours : theirs);
        m.setAwaySheet(home ? theirs : ours);
        return m;
    }

    @Test
    void theMatchesAttendedGiveExposureAndOwnMenAreKnown() {
        Player captain = man(10, 14f);
        List<Player> them = List.of(man(20, 9f), man(21, 17f));
        SaveGame g = game(captain, them);
        Club mine = g.getCurrentClub();
        assertSame(mine, g.getAllClubs().get(0));
        // a match played before R-04 counted: the first record counts the club's played matches once
        Match before = played(1, true, captain, them);
        mine.getPlayedMatches().add(before);
        Match next = played(2, false, captain, them);
        mine.getPlayedMatches().add(next);
        Perception.afterMatch(g, next);
        assertTrue(g.isKnowledgeCounted());
        for (Player p : them) {
            assertEquals(180f, g.knowledgeOf("p500", p.getId(), false).getMinutes(), "secretary at both");
            assertEquals(180f, g.knowledgeOf("p501", p.getId(), false).getMinutes(), "trainer at both");
            assertEquals(180f, g.knowledgeOf("p" + captain.getPerson().getId(), p.getId(), false).getMinutes(),
                    "the captain played both");
        }
        Match third = played(3, true, captain, them);
        mine.getPlayedMatches().add(third);
        Perception.afterMatch(g, third);
        assertEquals(270f, g.knowledgeOf("p500", 20L, false).getMinutes());
        assertNull(g.knowledgeOf("p500", captain.getId(), false), "nobody scouts his own men");

        Perception.Report report = Perception.report(g, them.get(1), null, ROOT);
        assertFalse(report.known());
        assertTrue(report.seen());
        assertEquals(3, report.observers().size(), "secretary, trainer, captain");
        for (Attr a : Perception.ATTRIBUTES) {
            assertTrue(report.consensus().get(a).contains(17f), a.id());
        }
        Perception.Report own = Perception.report(g, captain, null, ROOT);
        assertTrue(own.known());
        assertEquals(14f, own.consensus().get(SPEED).lo(), 0f);
        assertEquals(0f, own.consensus().get(SPEED).width(), 0f);
    }

    @Test
    void exposureSurvivesASave() {
        SaveGame g = new SaveGame();
        Perception.watch(g, "p7", man(30, 10f), 90f);
        Perception.watch(g, "p7", man(30, 10f), 45f);
        Perception.watch(g, "c:test:John Smith", man(31, 10f), 90f);
        g.setKnowledgeCounted(true);
        LocalDateTime at = LocalDateTime.of(2000, 1, 1, 0, 0);
        String text = SaveCodec.encode(g, "R-04", at);
        LoadResult loaded = SaveCodec.decode(text);
        assertTrue(loaded.isLoaded(), loaded.toString());
        SaveGame copy = loaded.getGame();
        assertTrue(copy.isKnowledgeCounted());
        assertEquals(2, copy.getKnowledge().size());
        Knowledge k = copy.knowledgeOf("p7", 30L, false);
        assertEquals(135f, k.getMinutes(), 0f);
        assertEquals(90f, copy.knowledgeOf("c:test:John Smith", 31L, false).getMinutes(), 0f);
        assertEquals(text, SaveCodec.encode(copy, "R-04", at), "a second save is the same text");
    }
}
