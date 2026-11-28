package com.rndmodgames.futtoboru.engine.contracts;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.ContractBand;
import com.rndmodgames.futtoboru.data.ContractType;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.EngineTestWorld;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;
import com.rndmodgames.futtoboru.system.generators.PositionAssigner;

/**
 * G-04 on a small hand-built world (EngineTestWorld: one league of four clubs; the manager at club A, club B run
 * by its committee): engagements end on 30 April, a week's wages are booked once per club with summer pay only in
 * the close season, the retained list opens on 16 April and the men answer the next day, and the 1 May rollover
 * keeps the men who accepted, frees the others and fills every short squad.
 */
class ContractsTests extends BaseTest {

    private static final Position[] TWO_THREE_FIVE = { Position.GOALKEEPER, Position.FULL_BACK, Position.FULL_BACK,
            Position.HALF_BACK, Position.HALF_BACK, Position.HALF_BACK, Position.RIGHT_WING, Position.INSIDE_RIGHT,
            Position.CENTRE_FORWARD, Position.INSIDE_LEFT, Position.LEFT_WING };

    private EngineTestWorld world;
    private ContractManager contracts;
    private Club mine;
    private Club ai;

    @BeforeEach
    void setUp() {
        application.setSeed(1888L);
        world = new EngineTestWorld(application, 4);
        contracts = world.engine.getContractManager();
        mine = world.club(0);
        ai = world.club(1);
        // every man needs a birth date and attributes for the daily engine (attribute drift, ability)
        for (Club club : world.clubs) {
            for (Player p : club.getPlayers()) {
                p.getPerson().setBirthDate(LocalDateTime.of(1865, 1, 1, 0, 0));
                new PlayerAttributeGenerator().generatePlayerAttributes(p, p.getPerson(), world.game.getGameDate(),
                        RandomSource.seeded(p.getPerson().getId()));
            }
        }
        engage(mine, 1888);
        engage(ai, 1888);
        Person owner = new Person();
        owner.setId(world.game.getIdAllocator().nextId());
        owner.setCurrentClubId(mine.getId());
        world.game.setOwner(owner);
    }

    /** A 2-3-5 of men born in 1865, each a professional on 30s a week for the season, engaged from 1 April 1888 */
    private void engage(Club club, int season) {
        for (int i = 0; i < club.getPlayers().size(); i++) {
            Player p = club.getPlayers().get(i);
            p.setId(p.getPerson().getId());
            p.getPerson().setName("Man");
            p.getPerson().setLastname(club.getName() + " " + i);
            p.getPerson().setBirthDate(LocalDateTime.of(1865, 1, 1, 0, 0));
            p.getPerson().setCurrentClubId(club.getId());
            p.setPosition(TWO_THREE_FIVE[i]);
            club.addPlayerContract(contract(club, p, season, ContractType.PROFESSIONAL, 30, 0));
        }
    }

    private PlayerContract contract(Club club, Player p, int season, int type, int wageShillings, int summerShillings) {
        PlayerContract c = new PlayerContract();
        c.setId(world.game.getIdAllocator().nextId());
        c.setPlayerId(p.getId());
        c.setClubId(club.getId());
        c.setContractType(type);
        c.setSeason(season);
        c.setStartDate(LocalDateTime.of(season, 4, 1, 4, 0));
        c.setEndDate(ContractCalendar.seasonEnd(season));
        c.setWeeklyWage(ContractBand.ofShillings(wageShillings));
        c.setSummerWage(ContractBand.ofShillings(summerShillings));
        return c;
    }

    private PlayerContract contractOf(Club club, int index) {
        return club.getContractForPlayer(club.getPlayers().get(index).getId());
    }

    private void day(int year, int month, int day) {
        LocalDateTime date = LocalDateTime.of(year, month, day, 4, 0);
        world.game.setGameDate(date);
        contracts.afterDay(date);
    }

    private List<Message> messages(String type) {
        List<Message> out = new ArrayList<>();
        for (Message m : world.game.getAllMessages()) {
            if (type.equals(m.getMessageType())) {
                out.add(m);
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------------------------------- dates

    @Test
    void anEngagementEndsOn30AprilAndTheCloseSeasonIsMayToAugust() {
        assertEquals(LocalDateTime.of(1889, 4, 30, 23, 59, 59), ContractCalendar.seasonEnd(1888));
        assertEquals(LocalDateTime.of(1889, 5, 1, 0, 0), ContractCalendar.nextSeasonStart(1888));
        assertEquals(LocalDate.of(1889, 4, 16), ContractCalendar.retainedListOpens(1888));
        assertEquals(1888, ContractCalendar.seasonOf(LocalDate.of(1889, 4, 30)));
        assertEquals(1889, ContractCalendar.seasonOf(LocalDate.of(1889, 5, 1)));
        assertEquals("1889-90", ContractCalendar.seasonName(1889));
        assertTrue(ContractCalendar.isCloseSeason(LocalDate.of(1888, 5, 1)));
        assertTrue(ContractCalendar.isCloseSeason(LocalDate.of(1888, 8, 31)));
        assertFalse(ContractCalendar.isCloseSeason(LocalDate.of(1888, 9, 1)));
        assertFalse(ContractCalendar.isCloseSeason(LocalDate.of(1889, 4, 30)));
    }

    // ------------------------------------------------------------------------------------------------- wages

    @Test
    void aWeeksWagesAreBookedOncePerClubAndSummerPayOnlyInTheCloseSeason() {
        // 45s with 20s summer pay (Holt's 1888 terms), 30s without, an amateur on 2s expenses, the rest 30s
        mine.getPlayerContracts().clear();
        List<Player> men = mine.getPlayers();
        mine.addPlayerContract(contract(mine, men.get(0), 1888, ContractType.PROFESSIONAL, 45, 20));
        PlayerContract amateur = contract(mine, men.get(1), 1888, ContractType.AMATEUR, 0, 0);
        amateur.setWeeklyExpenses(ContractBand.ofShillings(2));
        mine.addPlayerContract(amateur);
        for (int i = 2; i < men.size(); i++) {
            mine.addPlayerContract(contract(mine, men.get(i), 1888, ContractType.PROFESSIONAL, 30, 0));
        }
        BigDecimal season = ContractBand.ofShillings(45 + 2 + 30 * 9);
        BigDecimal summer = ContractBand.ofShillings(20);
        assertEquals(0, season.compareTo(ContractTerms.wageBill(mine, false)));
        assertEquals(0, summer.compareTo(ContractTerms.wageBill(mine, true)));

        world.game.setGameDate(LocalDateTime.of(1888, 9, 1, 4, 0));
        for (int d = 0; d < 8; d++) {
            world.engine.continueGame();
        }
        List<ClubExpenses> weeks = mine.getExpensesHistory();
        assertEquals(2, weeks.size(), "two weeks booked in eight days, once each: " + weeks.size());
        assertEquals(7, java.time.temporal.ChronoUnit.DAYS.between(weeks.get(0).getPeriodStart(),
                weeks.get(1).getPeriodStart()));
        for (ClubExpenses week : weeks) {
            assertEquals(0, season.compareTo(week.getPlayerWages()), "the playing season's wage bill");
        }

        // the close season: only the man with summer pay is paid
        mine.getExpensesHistory().clear();
        for (Club c : world.clubs) {
            c.getExpensesHistory().clear();
        }
        world.game.setGameDate(LocalDateTime.of(1888, 6, 1, 4, 0));
        world.engine.continueGame();
        assertEquals(1, mine.getExpensesHistory().size());
        assertEquals(0, summer.compareTo(mine.getExpensesHistory().get(0).getPlayerWages()), "summer pay only");

        // after the engagement's end nothing is due
        assertEquals(0, ContractTerms.weeklyPay(mine.getPlayerContracts().get(0), LocalDate.of(1889, 5, 1)).signum());
        assertEquals(0, ContractTerms.weeklyPay(mine.getPlayerContracts().get(0), LocalDate.of(1888, 3, 31)).signum());
    }

    // ------------------------------------------------------------------------------------------ retained list

    @Test
    void theRetainedListOpensOn16AprilAndTheMenAnswerTheNextDay() {
        day(1889, 4, 15);
        assertFalse(contracts.isRetainedListOpen());
        assertEquals(0, contracts.sendTerms(mine), "nothing can be sent before the list opens");
        assertTrue(messages(ContractManager.MESSAGE_RETAINED_LIST).isEmpty());

        day(1889, 4, 16);
        assertTrue(contracts.isRetainedListOpen());
        List<Message> letters = messages(ContractManager.MESSAGE_RETAINED_LIST);
        assertEquals(1, letters.size(), "one April letter");
        assertEquals(GameScreenId.RETAINED_LIST, letters.get(0).getActionScreen());
        assertEquals(mine.getId(), letters.get(0).getActionData());
        assertTrue(letters.get(0).getTitle().contains("1889-90"), letters.get(0).getTitle());
        assertEquals(11, contracts.endingContracts(mine).size());

        PlayerContract generous = contractOf(mine, 0);
        PlayerContract stingy = contractOf(mine, 1);
        PlayerContract released = contractOf(mine, 2);
        contracts.retain(generous);
        contracts.changeOfferedWage(mine, generous, 170); // 200s a week
        contracts.retain(stingy);
        contracts.changeOfferedWage(mine, stingy, -30); // nothing
        contracts.release(released);
        assertEquals(0, ContractBand.ofShillings(200).compareTo(generous.getOfferedWage()));
        assertEquals(0, stingy.getOfferedWage().signum());

        assertEquals(2, contracts.sendTerms(mine), "the two retained men");
        assertEquals(0, contracts.sendTerms(mine), "terms already with the men are not sent twice");
        assertTrue(ContractManager.isSentAndWaiting(generous));
        contracts.changeOfferedWage(mine, generous, -100);
        assertEquals(0, ContractBand.ofShillings(200).compareTo(generous.getOfferedWage()), "locked while waiting");

        day(1889, 4, 16); // the same day again: no answers yet
        assertNull(generous.getAnswer());

        day(1889, 4, 17);
        assertEquals(PlayerContract.Answer.ACCEPTED, generous.getAnswer(), "200s a week is accepted");
        assertEquals(PlayerContract.Answer.DECLINED, stingy.getAnswer(), "nothing is declined");
        assertEquals(1, messages(ContractManager.MESSAGE_REPLIES).size(), "one letter with both replies");
        assertEquals(1, messages(ContractManager.MESSAGE_RETAINED_LIST).size(), "the April letter comes once");

        // a man who declined can be offered more and answers again
        contracts.changeOfferedWage(mine, stingy, 200);
        assertNull(stingy.getAnswer());
        assertEquals(1, contracts.sendTerms(mine));
        assertEquals(2, stingy.getOffers());
        day(1889, 4, 18);
        assertNotNull(stingy.getAnswer());
    }

    @Test
    void theRolloverKeepsTheMenWhoAcceptedAndFreesTheRest() {
        day(1889, 4, 16);
        PlayerContract generous = contractOf(mine, 0);
        PlayerContract stingy = contractOf(mine, 1);
        PlayerContract released = contractOf(mine, 2);
        Player keeper = mine.getPlayers().get(0);
        Player stingyMan = mine.getPlayers().get(1);
        Player releasedMan = mine.getPlayers().get(2);
        contracts.retain(generous);
        contracts.changeOfferedWage(mine, generous, 170);
        contracts.setOfferedSummerPay(mine, generous, true);
        contracts.retain(stingy);
        contracts.changeOfferedWage(mine, stingy, -30);
        contracts.release(released);
        contracts.sendTerms(mine);
        day(1889, 4, 17);
        assertEquals(PlayerContract.Answer.DECLINED, stingy.getAnswer());

        day(1889, 4, 30);
        assertEquals(Integer.valueOf(1888), world.game.getContractsSeason(), "30 April: still the 1888-89 engagements");
        day(1889, 5, 1);
        assertEquals(Integer.valueOf(1889), world.game.getContractsSeason());

        // he accepted: a new one-season engagement on the terms offered
        PlayerContract renewed = mine.getContractForPlayer(keeper.getId());
        assertEquals(Integer.valueOf(1889), renewed.getSeason());
        assertEquals(LocalDateTime.of(1889, 5, 1, 0, 0), renewed.getStartDate());
        assertEquals(LocalDateTime.of(1890, 4, 30, 23, 59, 59), renewed.getEndDate());
        assertEquals(0, ContractBand.ofShillings(200).compareTo(renewed.getWeeklyWage()));
        assertTrue(renewed.hasSummerPay(), "summer pay offered and accepted");
        assertNull(renewed.getRetain(), "a new engagement starts without retained-list marks");

        // he declined, he was not retained: both gone from the club, free to join anyone (not their old club)
        for (Player gone : new Player[] { stingyMan, releasedMan }) {
            assertFalse(mine.getPlayers().contains(gone), name(gone) + " left");
            assertNull(mine.getContractForPlayer(gone.getId()), name(gone) + " has no engagement at his old club");
            boolean free = world.game.getFreeAgents().contains(gone);
            boolean atAi = ai.getPlayers().contains(gone);
            assertTrue(free ^ atAi, name(gone) + " is a free agent or signed by the other club");
            if (free) {
                assertNull(gone.getPerson().getCurrentClubId());
            } else {
                assertEquals(ai.getId(), gone.getPerson().getCurrentClubId());
            }
        }

        // every club run by contracts starts 1889-90 with at least its band's minimum and a full 2-3-5
        for (Club club : new Club[] { mine, ai }) {
            ContractBand band = contracts.bandOf(club, 1889);
            assertEquals(ContractBand.LEAGUE, band.getStanding());
            assertTrue(club.getPlayers().size() >= band.getMinimumSquad(), club.getName() + ": " + club.getPlayers().size());
            assertTrue(PositionAssigner.canFieldTwoThreeFive(club.getPlayers()), club.getName() + " fields a 2-3-5");
            assertEquals(club.getPlayers().size(), club.getPlayerContracts().size(), club.getName() + ": one engagement each");
            for (Player p : club.getPlayers()) {
                PlayerContract c = club.getContractForPlayer(p.getId());
                assertNotNull(c, name(p));
                assertEquals(Integer.valueOf(1889), c.getSeason(), name(p));
                assertEquals(club.getId(), p.getPerson().getCurrentClubId(), name(p));
            }
        }
        // clubs outside the contract system (no engagements) are left alone
        assertEquals(11, world.club(2).getPlayers().size());

        // nobody is in two places
        Set<Player> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Club club : world.clubs) {
            for (Player p : club.getPlayers()) {
                assertTrue(seen.add(p), name(p) + " twice");
            }
        }
        for (Player p : world.game.getFreeAgents()) {
            assertTrue(seen.add(p), name(p) + " both signed and free");
        }

        List<Message> summary = messages(ContractManager.MESSAGE_CLOSE_SEASON);
        assertEquals(1, summary.size(), "one close-season letter");
        assertTrue(summary.get(0).getPlainTextMessage().contains(name(keeper)));
        assertTrue(summary.get(0).getPlainTextMessage().contains(name(releasedMan)));

        // another day: nothing rolls over twice
        int size = mine.getPlayers().size();
        day(1889, 5, 2);
        assertEquals(size, mine.getPlayers().size());
        assertEquals(1, messages(ContractManager.MESSAGE_CLOSE_SEASON).size());
    }

    @Test
    void theRolloverWaitsForTheReElectionVoteAndEmptiesTheSheetPlacesOfMenWhoLeft() {
        // G-08: the League's annual meeting is still to come on 1 May (a late last League day)
        com.rndmodgames.futtoboru.data.CompetitionEdition edition = new com.rndmodgames.futtoboru.data.CompetitionEdition();
        com.rndmodgames.futtoboru.data.ReElection vote = new com.rndmodgames.futtoboru.data.ReElection();
        vote.setMeeting(LocalDateTime.of(1889, 5, 4, 0, 0));
        edition.setReElection(vote);
        world.league.setEditions(new ArrayList<>(List.of(edition)));
        // G-01: the manager's standing sheet names the man he will not retain, as captain
        Player releasedMan = mine.getPlayers().get(2);
        com.rndmodgames.futtoboru.data.TeamSheet sheet = com.rndmodgames.futtoboru.engine.team.TeamSheets.standing(mine);
        int slot = -1;
        for (int s = 0; s < com.rndmodgames.futtoboru.data.TeamShape.SLOTS; s++) {
            if (releasedMan.getId().equals(sheet.getPlayerId(s))) {
                slot = s;
            }
        }
        assertTrue(slot >= 0, "the released man is on the sheet");
        sheet.setCaptainId(releasedMan.getId());

        day(1889, 4, 16);
        contracts.release(contractOf(mine, 2));
        day(1889, 5, 1);
        assertEquals(Integer.valueOf(1888), world.game.getContractsSeason(), "1 May: waits for the vote");
        assertTrue(mine.getPlayers().contains(releasedMan));
        vote.setHeld(true);
        day(1889, 5, 4);
        assertEquals(Integer.valueOf(1889), world.game.getContractsSeason(), "rolled over after the vote");
        assertFalse(mine.getPlayers().contains(releasedMan));
        assertNull(mine.getTeamSheet().getPlayerId(slot), "his place on the sheet is empty");
        assertNull(mine.getTeamSheet().getCaptainId(), "and the captaincy with it");
    }

    @Test
    void undecidedMenAreDecidedByTheCommitteeAndTheSameSeedGivesTheSameRollover() {
        String first = rolloverDigest();
        setUp();
        String second = rolloverDigest();
        assertEquals(first, second);
        // the manager left every man undecided: the committee kept and offered terms, nobody keeps old marks
        for (PlayerContract c : mine.getPlayerContracts()) {
            assertNull(c.getRetain());
        }
    }

    private String rolloverDigest() {
        day(1889, 5, 1);
        StringBuilder sb = new StringBuilder();
        for (Club club : new Club[] { mine, ai }) {
            for (PlayerContract c : club.getPlayerContracts()) {
                sb.append(club.getName()).append(' ').append(contractsName(club, c)).append(' ')
                        .append(c.getWeeklyWage()).append('/')
                        .append(c.getSummerWage()).append(';');
            }
        }
        sb.append(world.game.getFreeAgents().size());
        return sb.toString();
    }

    private static String contractsName(Club club, PlayerContract c) {
        Player p = ContractManager.playerOf(club, c);
        return p != null ? name(p) : "?";
    }

    private static String name(Player p) {
        return p.getPerson().getName() + " " + p.getPerson().getLastname();
    }

    // ------------------------------------------------------------------------------------------------ terms

    @Test
    void theAnswerFollowsTheWageAgainstHisValueAndAge() {
        ContractBand league = ContractBand.forClub(ContractBand.defaults(), null, true);
        Player p = mine.getPlayers().get(5);
        PlayerContract c = contractOf(mine, 5);
        LocalDate day = LocalDate.of(1889, 4, 20);
        BigDecimal asking = ContractTerms.askingWage(p, league, day);
        assertTrue(asking.compareTo(league.getSeasonWageMin()) >= 0 && asking.compareTo(league.getSeasonWageMax()) <= 0,
                "asks inside the band: " + asking);
        double fair = ContractTerms.acceptChance(c, asking, BigDecimal.ZERO, asking, 24);
        double more = ContractTerms.acceptChance(c, asking.multiply(BigDecimal.valueOf(2)), BigDecimal.ZERO, asking, 24);
        double less = ContractTerms.acceptChance(c, asking.multiply(BigDecimal.valueOf(0.7)), BigDecimal.ZERO, asking, 24);
        double summer = ContractTerms.acceptChance(c, asking, ContractBand.ofShillings(20), asking, 24);
        double veteran = ContractTerms.acceptChance(c, asking.multiply(BigDecimal.valueOf(0.7)), BigDecimal.ZERO, asking, 32);
        assertTrue(more > fair && fair > less, more + " > " + fair + " > " + less);
        assertTrue(summer > fair, "summer pay makes an offer better");
        assertTrue(veteran > less, "an older man is more willing");
        assertTrue(fair >= 0.85, "a fair offer is mostly accepted: " + fair);
        assertTrue(less < 0.5, "30% below his value is mostly declined: " + less);

        // youth and veterans ask less than a man in his prime
        assertTrue(ContractTerms.ageFactor(19) < ContractTerms.ageFactor(25));
        assertTrue(ContractTerms.ageFactor(35) < ContractTerms.ageFactor(25));
        // an amateur band pays nothing
        assertEquals(0, ContractTerms.askingWage(p, ContractBand.forClub(
                com.rndmodgames.futtoboru.system.DatabaseLoader.getInstance().getContractBands(1888), "old-carthusians", false),
                day).signum());
    }
}
