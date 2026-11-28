package com.rndmodgames.futtoboru.engine.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.language.LanguageSkill;
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.Origin;
import com.rndmodgames.futtoboru.data.recruitment.PoolRegion;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.LanguageData;
import com.rndmodgames.futtoboru.system.data.RecruitmentData;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;

/**
 * R-03 headless and seeded, on the shipped data without a new game: pools sized per region, real men first, ages,
 * origin nudges and upbringing tendencies applied, foreign-born at the region's rates, the same pools for the same
 * seed, and the close-season / in-season turnover.
 */
class CandidatePoolsTests extends BaseTest {

    private static final LocalDateTime START = LocalDateTime.of(1888, 9, 8, 0, 0);

    private static RecruitmentData data() {
        return DatabaseLoader.getRecruitmentData();
    }

    private static SaveGame build(long seed, int season, LocalDateTime now) {
        SaveGame game = new SaveGame();
        CandidatePools.build(game, season, now, RandomSource.seeded(seed));
        return game;
    }

    @Test
    void everyRegionHasItsSizeAndOnlyMenOfSeventeenOrMore() {
        SaveGame game = build(1888L, 1888, START);
        int total = 0;
        for (PoolRegion region : data().getPools().values()) {
            List<Candidate> men = CandidatePools.inRegion(game, region.dataId());
            assertEquals(region.size(), men.size(), region.dataId());
            total += men.size();
            for (Candidate c : men) {
                int age = CandidatePools.age(c.getPlayer().getPerson(), START);
                assertTrue(age >= CandidatePools.MIN_AGE, c.getPlayer().getPerson().getLastname() + " is " + age);
                if (!c.isReal()) {
                    int[] band = CandidatePools.ageBand(c.getSourceKind());
                    assertTrue(age >= band[0] && age <= band[1], c.getSourceKind() + " aged " + age);
                    assertTrue(region.sourceKinds().contains(c.getSourceKind()));
                }
                assertNotNull(c.getPlayer().getPosition());
                assertNotNull(c.getPlayer().getId());
                assertFalse(c.getPlayer().getPerson().getLanguages().isEmpty(), "languages (R-11a)");
            }
        }
        assertEquals(total, game.getCandidates().size());
        assertEquals(Integer.valueOf(1888), game.getCandidatePoolSeason());
    }

    @Test
    void realMenComeFirstWhenListedOldEnoughAndInNoSquad() {
        SaveGame game = build(1888L, 1888, START);
        List<Candidate> birmingham = CandidatePools.inRegion(game, "birmingham");
        Candidate devey = birmingham.get(0);
        assertTrue(devey.isReal());
        assertEquals("john-devey-1866", devey.getPlayer().getPerson().getDataId());
        assertEquals("Mitchell St George's", devey.getPlaysFor());
        assertEquals("centre-forward", devey.getPlayer().getPosition().getDataId());
        assertNotNull(devey.getPlayer().getStrength(), "his ability is generated (D28 Q2)");
        // Needham is 15 in 1888 (D28 Q9) and Wharton is listed from 1889
        assertTrue(realIds(game).equals(Set.of("john-devey-1866")), realIds(game).toString());

        SaveGame in1889 = build(1888L, 1889, LocalDateTime.of(1889, 5, 1, 0, 0));
        assertEquals("arthur-wharton-1865", CandidatePools.inRegion(in1889, "sheffield").get(0).getPlayer().getPerson()
                .getDataId());
        assertFalse(realIds(in1889).contains("ernest-needham-1873"), "still 16 on 1 May 1889");
        SaveGame in1890 = build(1888L, 1890, LocalDateTime.of(1890, 5, 1, 0, 0));
        assertEquals(Set.of("ernest-needham-1873", "arthur-wharton-1865", "john-devey-1866"), realIds(in1890));
        assertNull(build(1888L, 1891, LocalDateTime.of(1891, 5, 1, 0, 0)).getCandidates().stream()
                .filter(c -> "john-devey-1866".equals(c.getPlayer().getPerson().getDataId())).findFirst().orElse(null),
                "Devey is listed until 1890");

        // a real man of the same seed is the same man every season (his own fork; only his age moves him)
        Player a = devey.getPlayer();
        Player b = build(1888L, 1889, START).getCandidates().stream()
                .filter(c -> "john-devey-1866".equals(c.getPlayer().getPerson().getDataId())).findFirst().get()
                .getPlayer();
        assertEquals(a.getSpeed(), b.getSpeed());
    }

    @Test
    void aRealManInASquadIsNotACandidate() {
        SaveGame game = new SaveGame();
        PoolRegion birmingham = data().getPools().get("birmingham");
        List<Candidate> men = CandidatePools.buildRegion(game, birmingham, 1888, START, RandomSource.seeded(1L),
                Set.of("john-devey-1866"));
        assertEquals(birmingham.size(), men.size());
        assertTrue(men.stream().noneMatch(Candidate::isReal));
    }

    private static Set<String> realIds(SaveGame game) {
        Set<String> out = new java.util.HashSet<>();
        for (Candidate c : game.getCandidates()) {
            if (c.isReal()) {
                out.add(c.getPlayer().getPerson().getDataId());
            }
        }
        return out;
    }

    @Test
    void originNudgesAndUpbringingTendenciesAreApplied() {
        PoolRegion lancashire = data().getPools().get("lancashire");
        SaveGame game = new SaveGame();
        RandomSource root = RandomSource.seeded(7L);
        int checked = 0;
        int tendencies = 0;
        PlayerAttributeGenerator attributes = new PlayerAttributeGenerator();
        for (int n = 0; n < 40; n++) {
            RandomSource fork = root.fork("recruit:cand:1888:lancashire:" + n);
            Candidate c = CandidatePools.generated(game, lancashire, 1888, START, fork);
            Origin origin = data().origin(c.getOrigin());
            if (origin == null) {
                continue;
            }
            // the same man without his nudges: the attribute dice are his own fork's
            Player base = new Player();
            base.setPerson(c.getPlayer().getPerson());
            base.setPlayerProfession(c.getPlayer().getPlayerProfession());
            attributes.generatePlayerAttributes(base, base.getPerson(), START,
                    root.fork("recruit:cand:1888:lancashire:" + n).fork("attributes"));
            Map<String, Integer> nudges = origin.getNudges();
            for (String attr : List.of("strength", "endurance", "stamina", "speed", "acceleration", "jumping",
                    "dexterity", "concentration", "courage", "determination", "leadership", "perception",
                    "positioning", "teamwork")) {
                int expected = Math.round(value(base, attr));
                if (nudges.containsKey(attr)) {
                    expected = clamp(expected + nudges.get(attr));
                }
                if (c.getTendencies().contains(attr)) {
                    expected = clamp(expected + lancashire.upbringing().tendencies().get(attr));
                    tendencies++;
                }
                assertEquals(expected, Math.round(value(c.getPlayer(), attr)), origin.getDataId() + " " + attr);
            }
            checked++;
        }
        assertTrue(checked > 20, "most men have a backstory: " + checked);
        assertTrue(tendencies > 0, "some men took an upbringing tendency (D28 Q14)");
    }

    private static int clamp(int v) {
        return Math.max(3, Math.min(23, v));
    }

    private static float value(Player p, String attr) {
        switch (attr) {
        case "strength": return p.getStrength();
        case "endurance": return p.getEndurance();
        case "stamina": return p.getStamina();
        case "speed": return p.getSpeed();
        case "acceleration": return p.getAcceleration();
        case "jumping": return p.getJumping();
        case "dexterity": return p.getDexterity();
        case "concentration": return p.getConcentration();
        case "courage": return p.getCourage();
        case "determination": return p.getDetermination();
        case "leadership": return p.getLeadership();
        case "perception": return p.getPerception();
        case "positioning": return p.getPositioning();
        default: return p.getTeamwork();
        }
    }

    @Test
    void foreignBornMenComeAtTheRegionsRatesWithTheirLanguages() {
        PoolRegion liverpool = data().getPools().get("liverpool");
        double irishRate = liverpool.foreignBorn().get("ireland");
        SaveGame game = new SaveGame();
        RandomSource root = RandomSource.seeded(1889L);
        int men = 0;
        int irish = 0;
        int asChildren = 0;
        for (int season = 1888; season < 1918; season++) {
            for (Candidate c : CandidatePools.buildRegion(game, liverpool, season, START.withYear(season), root,
                    Collections.emptySet())) {
                men++;
                Person p = c.getPlayer().getPerson();
                if (!"england".equals(p.getCountry().getDataId())) {
                    assertNotNull(c.getArrivedAge(), "a foreign-born man has the age he came over at");
                    if ("ireland".equals(p.getCountry().getDataId())) {
                        irish++;
                    }
                    if (c.getArrivedAge() <= CandidatePools.CHILD_MAX_AGE) {
                        asChildren++;
                        LanguageSkill english = p.getLanguage(LanguageData.ENGLISH);
                        assertNotNull(english);
                        assertEquals(LanguageSkill.MAX, english.getSpeak(), "schooled here: native English");
                        assertEquals(liverpool.upbringing().style(), c.getUpbringing(), "raised here");
                    } else {
                        assertNull(c.getUpbringing(), "came over grown: learned the game elsewhere");
                    }
                    assertTrue(p.getHomeLanguage() != null);
                } else {
                    assertNull(c.getArrivedAge());
                }
            }
        }
        double rate = irish / (double) men;
        assertTrue(Math.abs(rate - irishRate) < 0.035, "Irish-born " + rate + " of " + men + " vs " + irishRate);
        assertTrue(asChildren > 0);
    }

    @Test
    void everyOriginHasItsLabelInTheFourBundles() throws java.io.IOException {
        for (String id : data().getOrigins().keySet()) {
            com.rndmodgames.futtoboru.ui.LanguageBundleKeysTest.assertKeyInAllBundles(
                    com.rndmodgames.futtoboru.tables.recruitment.CandidatesScreenTable.originKey(id));
        }
    }

    @Test
    void theSameSeedGivesTheSamePoolsAndAnotherSeedOthers() {
        assertEquals(fingerprint(build(1888L, 1888, START)), fingerprint(build(1888L, 1888, START)));
        assertFalse(fingerprint(build(1888L, 1888, START)).equals(fingerprint(build(1889L, 1888, START))));
    }

    static List<String> fingerprint(SaveGame game) {
        List<String> out = new ArrayList<>();
        for (Candidate c : game.getCandidates()) {
            Player p = c.getPlayer();
            Person person = p.getPerson();
            out.add(c.getRegion() + "|" + person.getName() + " " + person.getLastname() + "|" + person.getBirthDate()
                    + "|" + person.getCountry().getDataId() + "|" + c.getSourceKind() + "|" + c.getOrigin() + "|"
                    + c.getPlaysFor() + "|" + c.getTendencies() + "|" + c.getArrivedAge() + "|" + p.getPosition() + "|"
                    + p.getStrength() + "/" + p.getSpeed() + "/" + p.getTeamwork() + "/" + p.getPassing() + "|"
                    + person.getLanguages());
        }
        return out;
    }

    @Test
    void thePoolsTurnOverSlowlyInSeasonAndRefreshAtTheCloseSeason() {
        SaveGame game = new SaveGame();
        RandomSource root = RandomSource.seeded(5L);
        assertTrue(CandidatePools.afterDay(game, START, root), "the first day builds the pools");
        List<String> before = fingerprint(game);
        assertFalse(CandidatePools.afterDay(game, START.plusDays(1), root), "nothing on an ordinary day");

        assertTrue(CandidatePools.afterDay(game, LocalDateTime.of(1888, 10, 1, 0, 0), root), "October's arrivals");
        assertFalse(CandidatePools.afterDay(game, LocalDateTime.of(1888, 10, 1, 0, 0), root), "once a month");
        int changed = 0;
        for (PoolRegion region : data().getPools().values()) {
            List<Candidate> men = CandidatePools.inRegion(game, region.dataId());
            assertEquals(region.size(), men.size(), "as many leave as arrive: " + region.dataId());
            for (Candidate c : men) {
                changed += c.getSince().getMonthValue() == 10 ? 1 : 0;
            }
            assertEquals(region.monthly(),
                    men.stream().filter(c -> c.getSince().getMonthValue() == 10).count(), region.dataId());
        }
        assertTrue(changed > 0 && changed < before.size() / 4, "slowly: " + changed);
        assertTrue(game.getCandidates().stream().anyMatch(Candidate::isReal), "a real man stays in season");

        SaveGame next = new SaveGame();
        CandidatePools.afterDay(next, START, root);
        assertTrue(CandidatePools.afterDay(next, LocalDateTime.of(1889, 5, 1, 0, 0), root), "1 May: a new season");
        assertEquals(Integer.valueOf(1889), next.getCandidatePoolSeason());
        assertTrue(next.getCandidates().stream().allMatch(c -> c.getSeason() == 1889), "last season's men are gone");
        assertFalse(CandidatePools.afterDay(next, LocalDateTime.of(1889, 6, 1, 0, 0), root), "no arrivals in summer");
    }
}
