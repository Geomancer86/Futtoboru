package com.rndmodgames.futtoboru.engine.finances;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.BaseTest;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.NewGameClubs;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.GameData;

/**
 * G-12a: one seeded 1888-89 financial year (1 May 1888 to 1 May 1889) played headless with the real data, measuring
 * every club's balance and its income and expense categories. Before the fix every club outside the League ended the
 * season in debt; most of them should now roughly break even, and every penny of a balance change is explained by
 * exactly one booked category (nothing counted twice, nothing booked off the books).
 */
@Tag("slow")
@Tag("season")
class EconomySeasonTests extends BaseTest {

    static final long SEED = 1888L;
    static final LocalDate FROM = LocalDate.of(1888, 5, 1);
    static final LocalDate TO = LocalDate.of(1889, 5, 1);
    static final String NL = System.lineSeparator();

    static Ledger first;
    static Ledger second;

    @BeforeAll
    static void playTwice() {
        first = Ledger.play(SEED);
        second = Ledger.play(SEED);
        System.out.println(first.report());
    }

    @AfterAll
    static void restore() {
        DatabaseLoader.resetDatabaseLoaderInstance();
    }

    @Test
    void everyBalanceChangeIsExplainedOnceByItsCategories() {
        for (Row r : first.rows.values()) {
            BigDecimal explained = r.income().subtract(r.expenses());
            assertEquals(0, r.change().compareTo(explained), r.club + ": balance change " + r.change()
                    + " but income - expenses " + explained + NL + first.report());
            assertEquals(0, r.weekTotals.compareTo(r.expenseCategories()), r.club
                    + ": the weekly totals differ from the sum of their categories");
        }
        // a gate is booked by one club only (or none, at the Association's neutral ties)
        for (Map.Entry<Long, BigDecimal> e : first.gateByMatch.entrySet()) {
            BigDecimal revenue = first.revenueByMatch.getOrDefault(e.getKey(), BigDecimal.ZERO);
            assertTrue(e.getValue().compareTo(revenue) <= 0, "match " + e.getKey() + " booked " + e.getValue()
                    + " of a gate of " + revenue);
        }
    }

    @Test
    void mostClubsOutsideTheLeagueRoughlyBreakEven() {
        int outside = 0;
        int even = 0;
        for (Row r : first.rows.values()) {
            if (!r.league) {
                outside++;
                if (r.breaksEven()) {
                    even++;
                }
            }
        }
        assertTrue(outside >= 10, "clubs outside the League: " + outside);
        // before G-12a every one of them lost money all year and ended in debt
        assertTrue(even * 5 >= outside * 3, even + " of " + outside + " clubs outside the League break even"
                + NL + first.report());
        List<BigDecimal> results = new ArrayList<>();
        for (Row r : first.rows.values()) {
            if (!r.league) {
                results.add(r.change());
            }
        }
        results.sort(null);
        BigDecimal median = results.get(results.size() / 2);
        assertTrue(median.abs().compareTo(new BigDecimal("40")) <= 0, "median year outside the League " + median);
    }

    @Test
    void aiClubsArrangeFriendliesAndThePlayersClubOnlyHisOwn() {
        int outside = 0;
        int playing = 0;
        for (Row r : first.rows.values()) {
            if ("preston-north-end".equals(r.club)) {
                assertEquals(0, r.friendlies, "the player arranged no friendly, so his club played none");
            } else if (!r.league) {
                outside++;
                if (r.friendlies >= 10) {
                    playing++;
                }
            }
        }
        assertTrue(playing * 3 >= outside * 2, playing + " of " + outside + " clubs outside the League played 10+ friendlies");
    }

    @Test
    void leagueClubsVary() {
        int gained = 0;
        int lost = 0;
        for (Row r : first.rows.values()) {
            if (r.league) {
                if (r.change().signum() > 0) {
                    gained++;
                } else {
                    lost++;
                }
            }
        }
        assertEquals(12, gained + lost, "the 12 founder members");
        assertTrue(gained >= 2 && lost >= 2, gained + " League clubs gained, " + lost + " lost" + NL + first.report());
    }

    @Test
    void theSameSeedKeepsTheSameBooks() {
        assertEquals(first.report(), second.report());
    }

    /** One club's year: balances and the flows booked between them (pounds). */
    static final class Row {
        String club;
        boolean league;
        BigDecimal start;
        BigDecimal end;
        String standing;
        int friendlies;
        BigDecimal gates = BigDecimal.ZERO;
        BigDecimal subscriptions = BigDecimal.ZERO;
        BigDecimal prizes = BigDecimal.ZERO;
        BigDecimal weekTotals = BigDecimal.ZERO;
        Map<String, BigDecimal> expense = new LinkedHashMap<>();

        BigDecimal change() {
            return end.subtract(start);
        }

        BigDecimal income() {
            return gates.add(subscriptions).add(prizes);
        }

        BigDecimal expenses() {
            return weekTotals;
        }

        BigDecimal expenseCategories() {
            return expense.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        /** roughly break even: the year's result within a quarter of the year's turnover, or within £40 */
        boolean breaksEven() {
            BigDecimal turnover = income().max(expenses());
            BigDecimal band = turnover.multiply(new BigDecimal("0.25")).max(new BigDecimal("40"));
            return change().abs().compareTo(band) <= 0;
        }
    }

    static final class Ledger {
        Futtoboru game;
        SaveGame save;
        FuttoboruGameEngine engine;
        Map<Long, Row> rows = new LinkedHashMap<>();
        Map<Long, BigDecimal> gateByMatch = new HashMap<>();
        Map<Long, BigDecimal> revenueByMatch = new HashMap<>();
        Map<Long, BigDecimal> gatesAtStart = new HashMap<>();
        Map<Long, Integer> expensesAtStart = new HashMap<>();

        static Ledger play(long seed) {
            Ledger l = new Ledger();
            l.newGame(seed);
            l.playTo(FROM);
            l.open();
            l.playTo(TO);
            l.close();
            return l;
        }

        void newGame(long seed) {
            DatabaseLoader.load(GameData.loadDefault(), RandomSource.seeded(seed));
            game = new Futtoboru(seed);
            save = new SaveGame();
            game.setCurrentGame(save);
            ScriptsManager scripts = new ScriptsManager(game);
            AuthorityManager authority = new AuthorityManager(game);
            engine = new FuttoboruGameEngine(game, scripts, authority);
            game.setScriptsManager(scripts);
            game.setGameEngine(engine);
            Season season = DatabaseLoader.getSeasonByDataId("1888-89");
            Person owner = new Person();
            owner.setName("Test");
            owner.setLastname("Secretary");
            owner.setBirthDate(LocalDateTime.of(1850, 1, 1, 0, 0));
            save.setOwner(owner);
            save.setMainAuthority(DatabaseLoader.getMainAuthority());
            save.setSelectedCountries(new ArrayList<>(season.getCountries()));
            save.assignId(owner);
            save.getAllPersons().add(owner);
            owner.setPrimaryProfession(DatabaseLoader.getProfessionByDataId(Profession.MANAGER));
            owner.setCurrentCountry(season.getCountries().get(0));
            save.setGameStartDate(season.getStartDate());
            save.setGameDate(season.getStartDate());
            save.setAllClubs(new ArrayList<>());
            NewGameClubs.addTo(save, DatabaseLoader.getCompetitions(), save.getSelectedCountries());
            owner.setCurrentClubId(DatabaseLoader.getClubByDataId("preston-north-end").getId());
            save.getGameScripts().addAll(season.getSeasonScripts());
            game.initializeJobSystem();
            game.getJobManager().initializeJobOpenings();
            com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator attributes =
                    new com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator();
            for (Club club : save.getAllClubs()) {
                for (com.rndmodgames.futtoboru.data.Player player : club.getPlayers()) {
                    if (player != null && player.getPerson() != null && player.getAcceleration() == null) {
                        attributes.generatePlayerAttributes(player, player.getPerson(), save.getGameDate(),
                                game.random().fork("attributes:" + player.getPerson().getId()));
                    }
                }
            }
        }

        /** Presses the buttons the player would until the game date is {@code day} (nothing of that day played). */
        void playTo(LocalDate day) {
            for (int step = 0; step < 2000 && save.getGameDate().toLocalDate().isBefore(day); step++) {
                readDraws();
                if (engine.getNextGameAction() == FuttoboruGameEngine.MATCH_PREVIEW_ACTION) {
                    engine.getMatchResult();
                }
                engine.continueGame();
            }
            assertEquals(day, save.getGameDate().toLocalDate(), "reached " + day);
        }

        void readDraws() {
            if (save.getScheduledMessages() != null) {
                for (Message m : new ArrayList<>(save.getScheduledMessages())) {
                    if (m != null && Boolean.TRUE.equals(m.getIsMandatory())) {
                        engine.getMessageManager().deliverMessage(m);
                    }
                }
            }
            for (Message m : save.getAllMessages()) {
                if (m != null && Boolean.TRUE.equals(m.getIsMandatory())) {
                    m.setIsRead(true);
                    m.setIsMandatory(false);
                }
            }
        }

        void open() {
            for (Club c : save.getAllClubs()) {
                Row r = new Row();
                r.club = c.getDataId();
                r.start = balance(c);
                rows.put(c.getId(), r);
                gatesAtStart.put(c.getId(), gates(c));
                expensesAtStart.put(c.getId(), c.getExpensesHistory().size());
            }
        }

        void close() {
            for (Club c : save.getAllClubs()) {
                Row r = rows.get(c.getId());
                assertNotNull(r, "club " + c.getDataId() + " joined the game during the year");
                r.end = balance(c);
                r.standing = com.rndmodgames.futtoboru.engine.contracts.ContractManager.standingOf(save, c, 1888);
                r.league = com.rndmodgames.futtoboru.data.ContractBand.LEAGUE.equals(r.standing);
                r.gates = gates(c).subtract(gatesAtStart.get(c.getId()));
                List<ClubExpenses> weeks = c.getExpensesHistory();
                for (ClubExpenses w : weeks.subList(expensesAtStart.get(c.getId()), weeks.size())) {
                    r.weekTotals = r.weekTotals.add(w.getTotalExpenses());
                    r.subscriptions = r.subscriptions.add(w.getSubscriptions());
                    add(r, "player wages", w.getPlayerWages());
                    add(r, "staff wages", w.getStaffWages());
                    add(r, "ground upkeep", w.getStadiumMaintenance().add(w.getPitchMaintenance()));
                    add(r, "kit and balls", w.getMaterialsEquipment());
                    add(r, "travel", w.getTravelExpenses());
                    add(r, "match day", w.getMatchdayExpenses());
                    add(r, "office", w.getAdministrativeExpenses());
                    add(r, "rent", w.getStadiumRent());
                    add(r, "other", w.getOtherExpenses());
                }
                for (MatchIncome mi : c.getMatchIncomes()) {
                    gateByMatch.merge(mi.getMatchId(), mi.getTicketRevenue(), BigDecimal::add);
                }
                for (Match m : c.getPlayedMatches()) {
                    if (m != null && m.getMatchType() != null && m.getMatchType() == Match.FRIENDLY_MATCH) {
                        r.friendlies++;
                    }
                    if (m != null && m.getId() != null && m.getMatchRevenue() != null) {
                        revenueByMatch.put(m.getId(), m.getMatchRevenue());
                    }
                }
                r.prizes = prizeOf(c);
            }
        }

        /** The League's prize money (AuthorityManager): champion and runner-up of an edition completed this year */
        BigDecimal prizeOf(Club c) {
            BigDecimal sum = BigDecimal.ZERO;
            for (com.rndmodgames.futtoboru.data.League league : save.getMainAuthority().getLeagues()) {
                for (com.rndmodgames.futtoboru.data.CompetitionEdition e : league.getEditions()) {
                    if (e.getFinalStandings() == null || e.getFinalStandings().isEmpty()) {
                        continue;
                    }
                    if (c.getId().equals(e.getChampionsId())) {
                        sum = sum.add(AuthorityManager.LEAGUE_CHAMPION_PRIZE);
                    }
                    if (c.getId().equals(e.getRunnersUpId())) {
                        sum = sum.add(AuthorityManager.LEAGUE_RUNNER_UP_PRIZE);
                    }
                }
            }
            return sum;
        }

        static void add(Row r, String category, BigDecimal amount) {
            r.expense.merge(category, amount != null ? amount : BigDecimal.ZERO, BigDecimal::add);
        }

        static BigDecimal balance(Club c) {
            return c.getClubBalance() != null ? c.getClubBalance() : BigDecimal.ZERO;
        }

        static BigDecimal gates(Club c) {
            BigDecimal sum = BigDecimal.ZERO;
            for (MatchIncome mi : c.getMatchIncomes()) {
                if (mi != null && mi.getTicketRevenue() != null) {
                    sum = sum.add(mi.getTicketRevenue());
                }
            }
            return sum;
        }

        String report() {
            StringBuilder sb = new StringBuilder("G-12a economy, seed " + SEED + ", " + FROM + " to " + TO
                    + " (pounds)" + NL);
            sb.append(String.format(java.util.Locale.ROOT, "%-26s %-7s %9s %9s %8s %7s %7s %8s", "club", "stand",
                    "start", "end", "gates", "subs", "prizes", "expenses"));
            Row any = rows.values().iterator().next();
            for (String cat : any.expense.keySet()) {
                sb.append(String.format(java.util.Locale.ROOT, " %8s", cat.length() > 8 ? cat.substring(0, 8) : cat));
            }
            sb.append(NL);
            for (Row r : rows.values()) {
                sb.append(String.format(java.util.Locale.ROOT, "%-26s %-7s %9.2f %9.2f %8.2f %7.2f %7.2f %8.2f", r.club,
                        r.standing, r.start, r.end, r.gates, r.subscriptions, r.prizes, r.expenses()));
                for (BigDecimal v : r.expense.values()) {
                    sb.append(String.format(java.util.Locale.ROOT, " %8.2f", v));
                }
                sb.append(r.breaksEven() ? "  even" : "").append(NL);
            }
            return sb.toString();
        }
    }
}
