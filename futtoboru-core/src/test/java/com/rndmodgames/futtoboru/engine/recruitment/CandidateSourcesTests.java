package com.rndmodgames.futtoboru.engine.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.recruitment.Advert;
import com.rndmodgames.futtoboru.data.recruitment.Agent;
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.save.LoadResult;
import com.rndmodgames.futtoboru.system.save.SaveCodec;

/**
 * R-05 (design §3.3) on the shipped data, hand-built clubs, no new game (fast tier): each source brings men at
 * plausible rates (letters more in the close season), an advert's cost is booked once and its replies come by letter,
 * an agent is paid once, the shortlist adds and removes, AI clubs list men by the same sources, the same seed gives
 * the same arrivals, and all of it survives a save.
 */
class CandidateSourcesTests extends BaseTest {

    private static final LocalDateTime MAY = LocalDateTime.of(1888, 5, 1, 0, 0);
    private static final String[] CLUBS = { "preston-north-end", "bolton-wanderers", "everton", "aston-villa",
            "stoke" };

    /** a game with five clubs (the first the player's) and its pools, on 1 May 1888 */
    private static SaveGame world(long seed) {
        SaveGame g = new SaveGame();
        for (String id : CLUBS) {
            Club c = new Club();
            c.setId(g.getIdAllocator().nextId());
            c.setDataId(id);
            c.setName(id);
            c.setClubBalance(new BigDecimal("300"));
            g.getAllClubs().add(c);
        }
        Person owner = new Person();
        owner.setName("Will");
        owner.setLastname("Sudell");
        g.assignId(owner);
        owner.setCurrentClubId(g.getAllClubs().get(0).getId());
        g.setOwner(owner);
        g.setGameDate(MAY);
        Recruitment.afterDay(g, MAY, RandomSource.seeded(seed));
        return g;
    }

    /** runs the sources (and the pools' days) from the day after the game's date to {@code until} */
    private static void run(SaveGame g, LocalDateTime until, RandomSource root) {
        for (LocalDateTime d = g.getGameDate().plusDays(1); !d.isAfter(until); d = d.plusDays(1)) {
            g.setGameDate(d);
            Recruitment.afterDay(g, d, root);
            CandidateSources.daily(g, d, root);
        }
    }

    private static List<Message> letters(SaveGame g, String type) {
        List<Message> out = new ArrayList<>();
        for (Message m : g.getAllMessages()) {
            if (type.equals(m.getMessageType())) {
                out.add(m);
            }
        }
        return out;
    }

    private static Player freeMan(SaveGame g, String last, Long lateClub) {
        Player p = new Player();
        p.setId(g.getIdAllocator().nextId());
        Person person = new Person();
        person.setName("Jack");
        person.setLastname(last);
        g.assignId(person);
        person.setPreviousClubId(lateClub);
        p.setPerson(person);
        p.setPosition(Position.values()[0]);
        g.getFreeAgents().add(p);
        return p;
    }

    private static String fingerprint(SaveGame g) {
        StringBuilder b = new StringBuilder();
        for (ShortlistEntry e : g.getShortlist()) {
            Player p = Shortlists.man(g, e.getPlayer());
            b.append(e.getClub()).append(' ').append(e.getPlayer()).append(' ').append(e.getSource()).append(' ')
                    .append(e.getStatus()).append(' ').append(p != null ? CandidateSources.name(p.getPerson()) : "?")
                    .append(' ').append(e.getSuggestedBy()).append(' ').append(e.getSince()).append('\n');
        }
        return b.toString();
    }

    @Test
    void aSeasonOfSourcesAtPlausibleRatesForThePlayerAndTheAiClubs() {
        RandomSource root = RandomSource.seeded(1888L);
        SaveGame g = world(1888L);
        Club mine = g.getCurrentClub();
        Club bolton = g.getAllClubs().get(1);
        run(g, LocalDateTime.of(1888, 5, 7, 0, 0), root);
        Player a = freeMan(g, "Ross", bolton.getId());
        freeMan(g, "Graham", g.getAllClubs().get(2).getId());
        Player own = freeMan(g, "Drummond", mine.getId());
        run(g, LocalDateTime.of(1889, 4, 30, 0, 0), root);

        // 1. letters: a few a week in the close season, fewer in the season
        int close = 0;
        int season = 0;
        for (Message m : letters(g, CandidateSources.MSG_LETTER)) {
            assertEquals(MessageCategory.RECRUITMENT, m.getCategory());
            if (ContractCalendar.isCloseSeason(m.getMessageTime().toLocalDate())) {
                close++;
            } else {
                season++;
            }
        }
        double closeRate = close / 123.0; // May-August
        double seasonRate = season / 242.0;
        assertTrue(close + season >= 10 && close + season <= 60, "letters a season: " + (close + season));
        assertTrue(closeRate > seasonRate, "close season " + closeRate + " a day, season " + seasonRate);
        // 3. tips from the staff, with the tipper named and his view in the letter
        List<Message> tips = letters(g, CandidateSources.MSG_TIP);
        assertTrue(tips.size() >= 5 && tips.size() <= 35, "tips a season: " + tips.size());
        // 4. agents now and then
        assertTrue(letters(g, CandidateSources.MSG_AGENT).size() <= 10, "agents: " + letters(g, CandidateSources.MSG_AGENT).size());
        // 5. the "not retained" list: the other clubs' men, not our own
        List<Message> released = letters(g, CandidateSources.MSG_RELEASED);
        assertEquals(1, released.size(), "one list a season");
        List<ShortlistEntry> listed = Shortlists.ofLetter(g, released.get(0).getId());
        assertEquals(2, listed.size());
        assertTrue(released.get(0).getPlainTextMessage().contains("Ross"));
        assertFalse(released.get(0).getPlainTextMessage().contains("Drummond"), "our own man is not news to us");
        assertNotNull(g.knowledgeOf(Observers.personKey(g.getOwner()), a.getId(), false), "the list shows his form");
        assertNull(Shortlists.entry(g, mine.getId(), own.getId()));
        // every letter's men are offered (Add / Ignore), the player's own list is still empty
        for (ShortlistEntry e : g.getShortlist()) {
            if (mine.getId().equals(e.getClub())) {
                assertTrue(ShortlistEntry.OFFERED.equals(e.getStatus()) || ShortlistEntry.UNPAID.equals(e.getStatus()),
                        e.toString());
                assertNotNull(e.getLetter(), e.toString());
            }
        }
        // AI clubs: men listed at once by the same sources, at most AI_MAX each; an advert now and then
        Set<String> aiSources = new HashSet<>();
        for (Club c : g.getAllClubs()) {
            if (c == mine) {
                continue;
            }
            List<ShortlistEntry> list = Shortlists.listed(g, c);
            assertFalse(list.isEmpty(), c.getDataId());
            assertTrue(list.size() <= Shortlists.AI_MAX, c.getDataId() + " " + list.size());
            list.forEach(e -> aiSources.add(e.getSource()));
        }
        assertTrue(aiSources.contains(ShortlistEntry.LETTER) && aiSources.contains(ShortlistEntry.TIP), aiSources.toString());
        int aiAdverts = 0;
        for (Advert ad : g.getAdverts()) {
            aiAdverts += mine.getId().equals(ad.getClub()) ? 0 : 1;
        }
        assertTrue(aiAdverts >= 1 && aiAdverts <= 12, "AI adverts: " + aiAdverts);
        // no man twice on a club's list, every man exists
        Set<String> seen = new HashSet<>();
        Map<Long, Player> men = Shortlists.men(g);
        for (ShortlistEntry e : g.getShortlist()) {
            assertTrue(seen.add(e.getClub() + ":" + e.getPlayer()), e.toString());
            assertTrue(men.containsKey(e.getPlayer()), e.toString());
        }
    }

    @Test
    void anAdvertIsPaidOnceAndItsRepliesComeByLetter() {
        RandomSource root = RandomSource.seeded(7L);
        SaveGame g = world(7L);
        Club mine = g.getCurrentClub();
        BigDecimal before = mine.getClubBalance();
        Advert ad = CandidateSources.placeAdvert(g, mine, "scottish-referee", Position.Line.FORWARD.name(), MAY, root);
        assertNotNull(ad);
        assertEquals(42, ad.getCostPence());
        assertEquals(0, before.subtract(Shortlists.pounds(42)).compareTo(mine.getClubBalance()));
        assertNull(CandidateSources.placeAdvert(g, mine, "scottish-referee", Advert.ANY, MAY.plusDays(3), root),
                "the advert still runs");
        assertNull(CandidateSources.placeAdvert(g, mine, "no-such-paper", Advert.ANY, MAY, root));
        int oneOff = 0;
        for (ClubExpenses e : mine.getExpensesHistory()) {
            oneOff += Shortlists.ONE_OFF.equals(e.getPeriodType()) ? 1 : 0;
        }
        assertEquals(1, oneOff, "booked once");
        assertEquals(0, before.subtract(Shortlists.pounds(42)).compareTo(mine.getClubBalance()));
        assertTrue(mine.getSeasonExpenditure().compareTo(Shortlists.pounds(42)) == 0);
        assertTrue(CandidateSources.advertWording(mine, ad.getWanted()).contains(mine.getName()));

        List<ShortlistEntry> due = new ArrayList<>();
        for (ShortlistEntry e : g.getShortlist()) {
            if (ShortlistEntry.ADVERT.equals(e.getSource())) {
                due.add(e);
                assertEquals(ShortlistEntry.DUE, e.getStatus());
                assertTrue(!e.getDue().isBefore(MAY.plusDays(5)) && !e.getDue().isAfter(MAY.plusDays(10)),
                        "a Scottish paper: about a week: " + e.getDue());
                Candidate c = Perception.candidateOf(g, Shortlists.man(g, e.getPlayer()));
                assertTrue(c.getRegion().equals("scotland-west"), c.getRegion());
            }
        }
        assertEquals(ad.getReplies(), due.size());
        assertEquals(0, CandidateSources.repliesIn(g, ad));
        run(g, MAY.plusDays(11), root);
        assertEquals(ad.getReplies(), letters(g, CandidateSources.MSG_ADVERT).size());
        assertEquals(ad.getReplies(), CandidateSources.repliesIn(g, ad));
        for (ShortlistEntry e : due) {
            assertEquals(ShortlistEntry.OFFERED, e.getStatus());
        }
        assertNotNull(CandidateSources.placeAdvert(g, mine, "scottish-referee", Advert.ANY, MAY.plusDays(15), root),
                "a fortnight later a new advert");
    }

    @Test
    void theShortlistAddsRemovesAndAnswersLetters() {
        SaveGame g = world(11L);
        Club mine = g.getCurrentClub();
        Candidate c = g.getCandidates().get(0);
        Player p = c.getPlayer();
        assertTrue(Shortlists.add(g, mine, p, MAY));
        assertFalse(Shortlists.add(g, mine, p, MAY), "already on it");
        assertEquals(1, Shortlists.listed(g, mine).size());
        assertEquals(ShortlistEntry.OWN, Shortlists.listed(g, mine).get(0).getSource());
        assertTrue(Shortlists.remove(g, mine, p.getId()));
        assertFalse(Shortlists.remove(g, mine, p.getId()));
        assertTrue(Shortlists.listed(g, mine).isEmpty());
        assertTrue(Shortlists.add(g, mine, p, MAY), "back on the list");
        assertEquals(1, g.getShortlist().size(), "one entry per man");

        Candidate d = g.getCandidates().get(1);
        ShortlistEntry offered = Shortlists.arrive(g, mine, d.getPlayer(), ShortlistEntry.LETTER,
                ShortlistEntry.OFFERED, MAY);
        assertNull(Shortlists.arrive(g, mine, d.getPlayer(), ShortlistEntry.TIP, ShortlistEntry.OFFERED, MAY),
                "a second source makes no second entry");
        assertTrue(Shortlists.accept(offered, MAY));
        assertFalse(Shortlists.ignore(offered), "listed, not offered");
        ShortlistEntry other = Shortlists.arrive(g, mine, g.getCandidates().get(2).getPlayer(), ShortlistEntry.LETTER,
                ShortlistEntry.OFFERED, MAY);
        assertTrue(Shortlists.ignore(other));
        assertFalse(Shortlists.accept(other, MAY));
        assertEquals(2, Shortlists.listed(g, mine).size());

        // a shortlisted man stays in the pools through the close season; an ignored one may go
        Set<Long> held = Shortlists.held(g);
        assertTrue(held.contains(p.getId()) && !held.contains(other.getPlayer()));
        CandidatePools.build(g, 1889, MAY.plusYears(1), RandomSource.seeded(11L));
        assertNotNull(Shortlists.man(g, p.getId()), "kept over the rebuild");
        assertEquals(1, Shortlists.prune(g), "the ignored man left the pools, his entry goes");
    }

    @Test
    void anAgentIsPaidOnceAndHisMenGoOnTheList() {
        RandomSource root = RandomSource.seeded(5L);
        SaveGame g = world(5L);
        Club mine = g.getCurrentClub();
        Agent agent = DatabaseLoader.getInstance().getRecruitmentData().getAgents().get("agent-glasgow-mcintyre");
        List<ShortlistEntry> men = CandidateSources.agentWrites(g, mine, agent, MAY, root.fork("agent"));
        assertEquals(agent.names(), men.size());
        Message letter = letters(g, CandidateSources.MSG_AGENT).get(0);
        for (ShortlistEntry e : men) {
            assertEquals(ShortlistEntry.UNPAID, e.getStatus());
            assertFalse(letter.getPlainTextMessage().contains(CandidateSources.name(Shortlists.man(g, e.getPlayer())
                    .getPerson())), "no names before the fee");
        }
        BigDecimal before = mine.getClubBalance();
        int paid = CandidateSources.payAgent(g, mine, letter.getId(), MAY, root);
        assertEquals(agent.feePence() * men.size(), paid);
        assertEquals(0, CandidateSources.payAgent(g, mine, letter.getId(), MAY, root), "paid once");
        assertEquals(0, before.subtract(Shortlists.pounds(paid)).compareTo(mine.getClubBalance()));
        assertEquals(men.size(), Shortlists.listed(g, mine).size());
        Message names = letters(g, CandidateSources.MSG_AGENT_NAMES).get(0);
        for (ShortlistEntry e : men) {
            assertTrue(names.getPlainTextMessage().contains(CandidateSources.name(Shortlists.man(g, e.getPlayer())
                    .getPerson())));
        }
        // an AI club that cannot afford him does not take him
        Club poor = g.getAllClubs().get(1);
        poor.setClubBalance(BigDecimal.ZERO);
        assertTrue(CandidateSources.agentWrites(g, poor, agent, MAY, root.fork("poor")).isEmpty());
    }

    @Test
    void theSameSeedBringsTheSameMen() {
        SaveGame a = world(42L);
        SaveGame b = world(42L);
        run(a, MAY.plusDays(60), RandomSource.seeded(42L));
        run(b, MAY.plusDays(60), RandomSource.seeded(42L));
        assertFalse(a.getShortlist().isEmpty());
        assertEquals(fingerprint(a), fingerprint(b));
        SaveGame c = world(43L);
        run(c, MAY.plusDays(60), RandomSource.seeded(43L));
        assertFalse(fingerprint(a).equals(fingerprint(c)), "another seed, other men");
    }

    @Test
    void shortlistsAndAdvertsSurviveASave() {
        RandomSource root = RandomSource.seeded(3L);
        SaveGame g = world(3L);
        Club mine = g.getCurrentClub();
        CandidateSources.placeAdvert(g, mine, "athletic-news", Advert.ANY, MAY, root);
        run(g, MAY.plusDays(20), root);
        Shortlists.add(g, mine, g.getCandidates().get(0).getPlayer(), MAY.plusDays(20));
        Player free = freeMan(g, "Ross", g.getAllClubs().get(1).getId());
        g.setNotRetainedListSeason(1888);
        String before = fingerprint(g);
        Map<Long, Integer> adverts = new HashMap<>();
        g.getAdverts().forEach(ad -> adverts.put(ad.getClub(), ad.getCostPence()));

        LocalDateTime at = LocalDateTime.of(2000, 1, 1, 0, 0);
        String text = SaveCodec.encode(g, "R-05", at);
        LoadResult loaded = SaveCodec.decode(text);
        assertTrue(loaded.isLoaded(), loaded.toString());
        SaveGame copy = loaded.getGame();
        assertEquals(before, fingerprint(copy));
        assertEquals(g.getAdverts().size(), copy.getAdverts().size());
        assertEquals(adverts.get(mine.getId()), Integer.valueOf(copy.getAdverts().get(0).getCostPence()));
        assertEquals(Integer.valueOf(1888), copy.getNotRetainedListSeason());
        assertEquals(g.getAllClubs().get(1).getId(), Shortlists.man(copy, free.getId()).getPerson().getPreviousClubId());
        assertEquals(text, SaveCodec.encode(copy, "R-05", at), "a second save is the same text");
    }
}
