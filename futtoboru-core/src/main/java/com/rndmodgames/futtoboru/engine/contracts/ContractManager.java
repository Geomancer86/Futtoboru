package com.rndmodgames.futtoboru.engine.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ContractBand;
import com.rndmodgames.futtoboru.data.ContractType;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.generators.NameGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerProfessionAssigner;
import com.rndmodgames.futtoboru.system.generators.PositionAssigner;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-04: engagements through the season, for every club (D31: AI clubs get the same system).
 *
 * <ul>
 * <li><b>16 April</b> ({@link ContractCalendar#retainedListOpens}): the manager's club gets a letter and the retained
 * list opens (Squad, or the letter). He marks each man retained, with the terms offered for next season, or not
 * retained; terms sent are answered the next day by a seeded draw ({@link ContractTerms#acceptChance}).</li>
 * <li><b>1 May</b>, the rollover: every club's engagements of the season that ended are settled. The manager's marks
 * count for his club (undecided men are decided by the committee; terms not yet answered are answered now); every
 * other club's committee retains and offers by its own seeded rule ({@link #committeeDecides}). A man who accepts
 * gets a new one-season engagement; the others become free agents (before 1893-94 a man not re-engaged may join any
 * club). Then every club short of men (fewer than its band's minimum, or no 2-3-5) signs free agents, best first and
 * by the line it lacks, or a local man when the pool has none, so every club can field eleven.</li>
 * </ul>
 * Everything is keyed by season, club and player in the random forks, so the same seed gives the same outcome.
 */
public class ContractManager {

    public static final String MESSAGE_RETAINED_LIST = "RETAINED_LIST";
    public static final String MESSAGE_REPLIES = "RETAINED_REPLIES";
    public static final String MESSAGE_CLOSE_SEASON = "CLOSE_SEASON";

    /** A committee lets a man outside its best eleven go this often on a whim (placeholder, D30 A3) */
    static final double COMMITTEE_WHIM = 0.1;
    /** A committee does not retain men this old outside its best eleven (placeholder) */
    static final int COMMITTEE_VETERAN_AGE = 34;
    /** Local men a club may sign in one rollover when the pool is dry (a guard, never reached in play) */
    static final int MAX_SIGNINGS = 40;

    private final Futtoboru app;
    private final MessageManager messages;

    public ContractManager(Futtoboru app, MessageManager messages) {
        this.app = app;
        this.messages = messages;
    }

    private SaveGame game() {
        return app != null ? app.getCurrentGame() : null;
    }

    private RandomSource random() {
        return Futtoboru.randomOf(app);
    }

    // ------------------------------------------------------------------------------------------------- the day

    /** Called once a day after the date moved on: opens the retained list, answers terms, rolls the season over. */
    public void afterDay(LocalDateTime now) {
        SaveGame g = game();
        if (g == null || now == null) {
            return;
        }
        int running = runningSeason(g);
        LocalDate today = now.toLocalDate();
        if (today.isAfter(ContractCalendar.seasonEnd(running).toLocalDate())) {
            rollover(running, now);
            return;
        }
        if (!today.isBefore(ContractCalendar.retainedListOpens(running))
                && !Integer.valueOf(running).equals(g.getRetainedListSeason())) {
            g.setRetainedListSeason(running);
            openRetainedList(running);
        }
        answerOffers(today);
    }

    /**
     * The season of the engagements running now: saved in the game; the first time, the earliest season of any
     * club's engagements (1888 in a new game), else the season of the game date.
     */
    public int runningSeason(SaveGame g) {
        if (g.getContractsSeason() != null) {
            return g.getContractsSeason();
        }
        Integer earliest = null;
        for (Club club : uniqueClubs(g)) {
            for (PlayerContract c : club.getPlayerContracts()) {
                if (c != null && c.getSeason() != null && (earliest == null || c.getSeason() < earliest)) {
                    earliest = c.getSeason();
                }
            }
        }
        int season = earliest != null ? earliest
                : g.getGameDate() != null ? SeasonCalendar.forDate(g.getGameDate()).getStartYear() : 1888;
        g.setContractsSeason(season);
        return season;
    }

    /** @return true from the day the retained list opens to the season end (the manager may change it) */
    public boolean isRetainedListOpen() {
        SaveGame g = game();
        if (g == null || g.getGameDate() == null) {
            return false;
        }
        int running = runningSeason(g);
        LocalDate today = g.getGameDate().toLocalDate();
        return !today.isBefore(ContractCalendar.retainedListOpens(running))
                && !today.isAfter(ContractCalendar.seasonEnd(running).toLocalDate());
    }

    // ------------------------------------------------------------------------------------------- the manager

    /** The engagements of the club's men that end this season, by player id. */
    public List<PlayerContract> endingContracts(Club club) {
        List<PlayerContract> out = new ArrayList<>();
        SaveGame g = game();
        if (club == null || g == null) {
            return out;
        }
        int running = runningSeason(g);
        for (PlayerContract c : club.getPlayerContracts()) {
            if (c != null && Integer.valueOf(running).equals(c.getSeason())) {
                out.add(c);
            }
        }
        out.sort(Comparator.comparing(PlayerContract::getPlayerId, Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }

    /** Marks him retained; the terms start as his current ones (or the last offer) until changed. */
    public void retain(PlayerContract c) {
        if (c == null || c.getAnswer() == PlayerContract.Answer.ACCEPTED) {
            return;
        }
        c.setRetain(PlayerContract.Retain.RETAINED);
        if (c.getOfferedWage() == null) {
            c.setOfferedWage(c.getWeeklyWage());
            c.setOfferedSummerWage(c.getSummerWage());
        }
    }

    /** Marks him not retained (he leaves at the rollover). */
    public void release(PlayerContract c) {
        if (c == null || c.getAnswer() == PlayerContract.Answer.ACCEPTED) {
            return;
        }
        c.setRetain(PlayerContract.Retain.NOT_RETAINED);
        c.setOfferSentDate(null);
        c.setAnswer(null);
    }

    /** Raises or lowers the wage offered by whole shillings (never below nothing); a declined offer can be sent again. */
    public void changeOfferedWage(Club club, PlayerContract c, int shillings) {
        if (c == null || c.getRetain() != PlayerContract.Retain.RETAINED || c.isAmateur()
                || c.getAnswer() == PlayerContract.Answer.ACCEPTED || isSentAndWaiting(c)) {
            return;
        }
        int s = Math.max(0, ContractBand.shillings(c.getOfferedWage()) + shillings);
        c.setOfferedWage(ContractBand.ofShillings(s));
        if (c.getOfferedSummerWage() != null && c.getOfferedSummerWage().signum() > 0) {
            c.setOfferedSummerWage(ContractTerms.summerFor(c.getOfferedWage(), bandOf(club, runningSeason(game()) + 1)));
        }
        c.setAnswer(null); // a changed offer is a draft again: sent again, it is a new offer
        c.setOfferSentDate(null);
    }

    /** Offers summer pay (the band's share of the wage offered) or none. */
    public void setOfferedSummerPay(Club club, PlayerContract c, boolean on) {
        if (c == null || c.getRetain() != PlayerContract.Retain.RETAINED || c.isAmateur()
                || c.getAnswer() == PlayerContract.Answer.ACCEPTED || isSentAndWaiting(c)) {
            return;
        }
        c.setOfferedSummerWage(on ? ContractTerms.summerFor(c.getOfferedWage(), bandOf(club, runningSeason(game()) + 1))
                : BigDecimal.ZERO);
        c.setAnswer(null);
        c.setOfferSentDate(null);
    }

    /** @return true when the terms are with the man and he has not answered yet */
    public static boolean isSentAndWaiting(PlayerContract c) {
        return c.getOfferSentDate() != null && c.getAnswer() == null;
    }

    /**
     * Sends the terms of every retained man who has none to answer (a man who declined gets new terms only once they
     * are changed); they answer the next day.
     */
    public int sendTerms(Club club) {
        SaveGame g = game();
        if (club == null || g == null || !isRetainedListOpen()) {
            return 0;
        }
        int sent = 0;
        for (PlayerContract c : endingContracts(club)) {
            if (c.getRetain() == PlayerContract.Retain.RETAINED && c.getAnswer() == null && !isSentAndWaiting(c)) {
                c.setOfferSentDate(g.getGameDate());
                c.setAnswer(null);
                c.setOffers(c.getOffers() + 1);
                sent++;
            }
        }
        return sent;
    }

    /** The manager's club: terms sent before today are answered, in one letter. */
    void answerOffers(LocalDate today) {
        SaveGame g = game();
        Club club = g.getCurrentClub();
        if (club == null) {
            return;
        }
        int running = runningSeason(g);
        List<String> lines = new ArrayList<>();
        for (PlayerContract c : endingContracts(club)) {
            if (c.getRetain() == PlayerContract.Retain.RETAINED && isSentAndWaiting(c)
                    && c.getOfferSentDate().toLocalDate().isBefore(today)) {
                Player p = playerOf(club, c);
                answer(club, p, c, running, today);
                lines.add(replyLine(p, c));
            }
        }
        if (!lines.isEmpty()) {
            letter(club, MESSAGE_REPLIES, LanguageModLoader.format("contracts_replies_title", formatDate(today)),
                    LanguageModLoader.format("contracts_replies_body", String.join("\n", lines)), false);
        }
    }

    /**
     * His seeded answer to the terms offered. Each man has one fixed draw per season (his reluctance), so the same
     * terms always get the same answer and only better terms can turn a "no" into a "yes": sending again, or a
     * reload, never re-rolls him.
     */
    private void answer(Club club, Player p, PlayerContract c, int season, LocalDate today) {
        ContractBand band = bandOf(club, season + 1);
        int age = ContractTerms.age(p, today);
        BigDecimal asking = ContractTerms.askingWage(p, band, today);
        double chance = ContractTerms.acceptChance(c, c.getOfferedWage(), c.getOfferedSummerWage(), asking, age);
        RandomSource rng = random().fork("contracts:answer:" + season + ":" + c.getPlayerId());
        c.setAnswer(ContractTerms.accepts(chance, rng) ? PlayerContract.Answer.ACCEPTED : PlayerContract.Answer.DECLINED);
    }

    // ------------------------------------------------------------------------------------------- the rollover

    /** What the manager's club did at a rollover, for its letter. */
    private static final class Summary {
        final List<String> stayed = new ArrayList<>();
        final List<String> left = new ArrayList<>();
        final List<String> signed = new ArrayList<>();
    }

    /**
     * Settles every club's engagements of {@code season} and starts {@code season + 1}'s (see the class comment).
     * Clubs go in id order, their men in player id order.
     */
    public void rollover(int season, LocalDateTime now) {
        SaveGame g = game();
        if (g == null) {
            return;
        }
        int next = season + 1;
        LocalDateTime start = ContractCalendar.nextSeasonStart(season);
        LocalDate day = now != null ? now.toLocalDate() : start.toLocalDate();
        Club mine = g.getCurrentClub();
        Summary summary = new Summary();
        // clubs without engagements are not run by contracts (hand-built test clubs): left alone
        List<Club> settled = new ArrayList<>();
        // a club does not sign back a man it let go this rollover
        java.util.Map<Player, Club> releasedBy = new IdentityHashMap<>();
        int stayed = 0;
        int left = 0;

        for (Club club : uniqueClubs(g)) {
            boolean managed = mine != null && mine.getId() != null && mine.getId().equals(club.getId());
            List<PlayerContract> ending = new ArrayList<>();
            for (PlayerContract c : club.getPlayerContracts()) {
                if (c != null && (Integer.valueOf(season).equals(c.getSeason())
                        || (c.getSeason() == null && c.getEndDate() != null && c.getEndDate().isBefore(start)))) {
                    ending.add(c);
                }
            }
            if (ending.isEmpty()) {
                continue;
            }
            settled.add(club);
            ending.sort(Comparator.comparing(PlayerContract::getPlayerId, Comparator.nullsLast(Comparator.naturalOrder())));
            if (!managed) {
                for (PlayerContract c : ending) {
                    c.clearRetainedList(); // a manager who left the club: his marks no longer count
                }
            }
            committeeDecides(club, ending, season, day);

            ContractBand band = bandOf(club, next);
            for (PlayerContract c : ending) {
                Player p = playerOf(club, c);
                if (p == null) {
                    club.getPlayerContracts().remove(c);
                    continue;
                }
                if (c.getRetain() == PlayerContract.Retain.RETAINED && c.getAnswer() == null) {
                    if (c.getOfferSentDate() == null) {
                        c.setOffers(c.getOffers() + 1);
                    }
                    answer(club, p, c, season, day);
                }
                if (c.getRetain() == PlayerContract.Retain.RETAINED && c.getAnswer() == PlayerContract.Answer.ACCEPTED) {
                    PlayerContract renewed = renew(c, band, next, start);
                    club.getPlayerContracts().set(club.getPlayerContracts().indexOf(c), renewed);
                    stayed++;
                    if (managed) {
                        summary.stayed.add(LanguageModLoader.format("contracts_summary_stayed", name(p),
                                terms(renewed)));
                    }
                } else {
                    leave(g, club, p, c);
                    releasedBy.put(p, club);
                    left++;
                    if (managed) {
                        summary.left.add(LanguageModLoader.format(c.getRetain() == PlayerContract.Retain.RETAINED
                                ? "contracts_summary_declined" : "contracts_summary_not_retained", name(p)));
                    }
                }
            }
            // the season's money starts again from nothing (the Finances screen's "this season")
            club.setSeasonIncome(BigDecimal.ZERO);
            club.setSeasonExpenditure(BigDecimal.ZERO);
        }

        int signed = fillSquads(g, settled, next, start, day, mine, summary, releasedBy);
        g.setContractsSeason(next);
        Log.info(Log.ENGINE_GAME, "ContractManager", "Rollover to " + ContractCalendar.seasonName(next) + ": " + stayed
                + " re-engaged, " + left + " left their clubs, " + signed + " signed, " + g.getFreeAgents().size()
                + " free agents");

        if (mine != null && containsSame(settled, mine)) {
            String none = LanguageModLoader.getValue("contracts_summary_none");
            letter(mine, MESSAGE_CLOSE_SEASON,
                    LanguageModLoader.format("contracts_close_season_title", ContractCalendar.seasonName(next)),
                    LanguageModLoader.format("contracts_close_season_body", ContractCalendar.seasonName(next),
                            lines(summary.stayed, none), lines(summary.left, none), lines(summary.signed, none)),
                    true);
        }
    }

    /**
     * A committee's retained list for the men it has not decided (all of them at an AI club): it keeps its best men
     * (ability, ties by id) up to its band's squad size, but lets veterans and, on a seeded whim, a few others outside
     * its best eleven go; the retained get about their asking wage and keep summer pay if they had it.
     */
    void committeeDecides(Club club, List<PlayerContract> ending, int season, LocalDate day) {
        ContractBand band = bandOf(club, season + 1);
        List<Player> ranked = new ArrayList<>();
        for (PlayerContract c : ending) {
            Player p = playerOf(club, c);
            if (p != null) {
                ranked.add(p);
            }
        }
        ranked.sort(Comparator.comparingDouble((Player p) -> -ContractTerms.ability(p))
                .thenComparing(ContractManager::idOf, Comparator.nullsLast(Comparator.naturalOrder())));
        for (int i = 0; i < ranked.size(); i++) {
            Player p = ranked.get(i);
            PlayerContract c = contractOf(ending, p);
            if (c == null || c.getRetain() != null) {
                continue; // the manager decided him
            }
            RandomSource rng = random().fork("contracts:committee:" + season + ":" + club.getId() + ":" + idOf(p));
            boolean keep = i < band.getSquadSize();
            if (i >= 11 && ContractTerms.age(p, day) >= COMMITTEE_VETERAN_AGE) {
                keep = false;
            }
            if (i >= 11 && rng.nextDouble() < COMMITTEE_WHIM) {
                keep = false;
            }
            if (!keep) {
                c.setRetain(PlayerContract.Retain.NOT_RETAINED);
                continue;
            }
            c.setRetain(PlayerContract.Retain.RETAINED);
            if (c.isAmateur()) {
                c.setOfferedWage(BigDecimal.ZERO);
                c.setOfferedSummerWage(BigDecimal.ZERO);
            } else {
                BigDecimal asking = ContractTerms.askingWage(p, band, day);
                BigDecimal wage = com.rndmodgames.futtoboru.system.generators.PlayerContractGenerator
                        .withNoise(asking, 0.25 + 0.5 * rng.nextDouble(), band);
                c.setOfferedWage(wage);
                c.setOfferedSummerWage(c.hasSummerPay() ? ContractTerms.summerFor(wage, band) : BigDecimal.ZERO);
            }
        }
    }

    /** The accepted terms as a new one-season engagement (same man, club and type). */
    private PlayerContract renew(PlayerContract old, ContractBand band, int season, LocalDateTime start) {
        PlayerContract c = new PlayerContract();
        c.setId(game().getIdAllocator().nextId());
        c.setPlayerId(old.getPlayerId());
        c.setClubId(old.getClubId());
        c.setContractType(old.getContractType());
        c.setSeason(season);
        c.setStartDate(start);
        c.setEndDate(ContractCalendar.seasonEnd(season));
        c.setContractLengthMonths(12);
        if (old.isAmateur()) {
            c.setWeeklyWage(BigDecimal.ZERO);
            c.setSummerWage(BigDecimal.ZERO);
            c.setWeeklyExpenses(band.getAmateurExpenses());
        } else {
            c.setWeeklyWage(old.getOfferedWage() != null ? old.getOfferedWage() : old.getWeeklyWage());
            c.setSummerWage(old.getOfferedSummerWage() != null ? old.getOfferedSummerWage() : BigDecimal.ZERO);
        }
        c.setIsActive(true);
        return c;
    }

    /** He leaves the club: off its squad and its books, into the free-agent pool. */
    private static void leave(SaveGame g, Club club, Player p, PlayerContract c) {
        removeSame(club.getPlayers(), p);
        club.getPlayerContracts().remove(c);
        if (p.getPerson() != null && club.getId() != null && club.getId().equals(p.getPerson().getCurrentClubId())) {
            p.getPerson().setCurrentClubId(null);
        }
        if (!containsSame(g.getFreeAgents(), p)) {
            g.getFreeAgents().add(p);
        }
    }

    /**
     * Every club short of men signs them: while it has fewer than its band's minimum or cannot field a 2-3-5, the
     * best free agent of the line it lacks (any line when it only lacks numbers), else a local man.
     *
     * @return how many men were signed
     */
    int fillSquads(SaveGame g, List<Club> clubs, int season, LocalDateTime start, LocalDate day, Club mine,
            Summary summary, java.util.Map<Player, Club> releasedBy) {
        int signed = 0;
        for (Club club : clubs) {
            ContractBand band = bandOf(club, season);
            int locals = 0;
            for (int guard = 0; guard < MAX_SIGNINGS && needsMen(club, band); guard++) {
                Position.Line line = shortLine(club.getPlayers());
                Player p = bestFreeAgent(g, line, club, releasedBy);
                if (p != null) {
                    removeSame(g.getFreeAgents(), p);
                } else {
                    p = localMan(g, club, season, locals++, day);
                }
                sign(g, club, p, band, season, start);
                signed++;
                if (mine != null && mine.getId() != null && mine.getId().equals(club.getId())) {
                    PlayerContract c = club.getContractForPlayer(idOf(p));
                    summary.signed.add(LanguageModLoader.format("contracts_summary_signed", name(p),
                            p.getPosition() != null ? LanguageModLoader.getValue(p.getPosition().labelKey()) : "",
                            terms(c)));
                }
            }
        }
        return signed;
    }

    private static boolean needsMen(Club club, ContractBand band) {
        return club.getPlayers().size() < band.getMinimumSquad() || !PositionAssigner.canFieldTwoThreeFive(club.getPlayers());
    }

    /** The first line of the 2-3-5 the squad cannot fill (goal, backs, halves, forwards), or null */
    static Position.Line shortLine(List<Player> squad) {
        int[] need = { 1, 2, 3, 5 };
        int[] have = new int[Position.Line.values().length];
        for (Player p : squad) {
            if (p != null && p.getPosition() != null) {
                have[p.getPosition().getLine().ordinal()]++;
            }
        }
        for (Position.Line line : Position.Line.values()) {
            if (have[line.ordinal()] < need[line.ordinal()]) {
                return line;
            }
        }
        return null;
    }

    /** The best free agent (ability, then id) of {@code line}, or of any line when null; null when there is none */
    private static Player bestFreeAgent(SaveGame g, Position.Line line, Club club, java.util.Map<Player, Club> releasedBy) {
        Player best = null;
        for (Player p : g.getFreeAgents()) {
            if (p == null || p.getPerson() == null || releasedBy.get(p) == club || (line != null
                    && (p.getPosition() == null || p.getPosition().getLine() != line))) {
                continue;
            }
            if (best == null || ContractTerms.ability(p) > ContractTerms.ability(best)
                    || (ContractTerms.ability(p) == ContractTerms.ability(best) && idOf(p) < idOf(best))) {
                best = p;
            }
        }
        return best;
    }

    /** A local man answering the club's advertisement: generated, 18-29, of the club's country, no position yet */
    private Player localMan(SaveGame g, Club club, int season, int n, LocalDate day) {
        RandomSource rng = random().fork("contracts:local:" + season + ":" + club.getId() + ":" + n);
        Country country = club.getCountry();
        if (country == null || DatabaseLoader.getFirstNames(country).size() < 2
                || DatabaseLoader.getLastNames(country).size() < 2) {
            country = DatabaseLoader.getCountryByDataId("england");
        }
        Person person = new Person();
        person.setId(g.getIdAllocator().nextId());
        String[] names = NameGenerator.generateName(country, rng.fork("name"));
        person.setName(names[0]);
        person.setLastname(names[1]);
        person.setCountry(country);
        person.setCurrentCountry(country);
        person.setBirthDate(day.minusYears(18 + rng.nextInt(12)).minusDays(rng.nextInt(365)).atStartOfDay());
        Player player = new Player();
        player.setId(person.getId());
        player.setPerson(person);
        new PlayerAttributeGenerator().generatePlayerAttributes(player, person, day.atStartOfDay(),
                rng.fork("attributes"));
        return player;
    }

    /** Signs him for the season on his asking terms (a professional by the band's share, some with summer pay). */
    private void sign(SaveGame g, Club club, Player p, ContractBand band, int season, LocalDateTime start) {
        RandomSource rng = random().fork("contracts:sign:" + season + ":" + club.getId() + ":" + idOf(p));
        boolean professional = band.getProfessionalShare() > 0 && rng.nextDouble() < band.getProfessionalShare();
        PlayerContract c = new PlayerContract();
        c.setId(g.getIdAllocator().nextId());
        c.setPlayerId(idOf(p));
        c.setClubId(club.getId());
        c.setSeason(season);
        LocalDateTime now = g.getGameDate();
        c.setStartDate(now != null && now.isAfter(start) ? now : start);
        c.setEndDate(ContractCalendar.seasonEnd(season));
        c.setContractLengthMonths((int) Math.max(1, ChronoUnit.MONTHS.between(c.getStartDate(), c.getEndDate())));
        if (professional) {
            c.setContractType(ContractType.PROFESSIONAL);
            BigDecimal wage = ContractTerms.askingWage(p, band, start.toLocalDate());
            c.setWeeklyWage(wage);
            c.setSummerWage(rng.nextDouble() < band.getSummerPayShare() ? ContractTerms.summerFor(wage, band)
                    : BigDecimal.ZERO);
        } else {
            c.setContractType(ContractType.AMATEUR);
            c.setWeeklyWage(BigDecimal.ZERO);
            c.setSummerWage(BigDecimal.ZERO);
            c.setWeeklyExpenses(band.getAmateurExpenses());
        }
        c.setIsActive(true);
        club.getPlayers().add(p);
        club.addPlayerContract(c);
        if (p.getPerson() != null) {
            p.getPerson().setCurrentClubId(club.getId());
        }
        PlayerProfessionAssigner.assignProfession(p, club, c.getContractType(), rng.fork("profession"));
        if (p.getPosition() == null) {
            PositionAssigner.fillMissing(club.getPlayers()); // the slot the squad is shortest of
        }
    }

    // ------------------------------------------------------------------------------------------------ old saves

    /**
     * An engagement from a save made before G-04 has no season: it gets the season of its start (else of the game
     * date), the end on 30 April of the next year, no summer pay, and an amateur the band's expenses. The generated
     * wage stays.
     *
     * @return how many engagements were derived
     */
    public static int deriveOldContracts(SaveGame g) {
        int derived = 0;
        if (g == null) {
            return 0;
        }
        for (Club club : uniqueClubs(g)) {
            ContractBand band = null;
            for (PlayerContract c : club.getPlayerContracts()) {
                if (c == null || c.getSeason() != null) {
                    continue;
                }
                LocalDateTime from = c.getStartDate() != null ? c.getStartDate() : g.getGameDate();
                int season = from != null ? SeasonCalendar.forDate(from).getStartYear() : 1888;
                c.setSeason(season);
                c.setEndDate(ContractCalendar.seasonEnd(season));
                if (c.getStartDate() != null) {
                    c.setContractLengthMonths((int) Math.max(1, ChronoUnit.MONTHS.between(c.getStartDate(), c.getEndDate())));
                }
                if (c.getSummerWage() == null) {
                    c.setSummerWage(BigDecimal.ZERO);
                }
                if (c.isAmateur() && c.getWeeklyExpenses().signum() == 0) {
                    if (band == null) {
                        band = bandOf(g, club, season);
                    }
                    c.setWeeklyExpenses(band.getAmateurExpenses());
                }
                derived++;
            }
        }
        if (derived > 0) {
            Log.info(Log.SYSTEM_INIT, "ContractManager", derived + " engagements of an older save derived (G-04)");
        }
        return derived;
    }

    // ---------------------------------------------------------------------------------------------- helpers

    /** The wage band of a club for a season (league clubs: those of a league of the game's main authority). */
    public ContractBand bandOf(Club club, int season) {
        return bandOf(game(), club, season);
    }

    static ContractBand bandOf(SaveGame g, Club club, int season) {
        return ContractBand.forClub(DatabaseLoader.getContractBands(season), club != null ? club.getDataId() : null,
                isLeagueClub(g, club));
    }

    static boolean isLeagueClub(SaveGame g, Club club) {
        if (g == null || club == null || club.getId() == null || g.getMainAuthority() == null) {
            return false;
        }
        for (League league : g.getMainAuthority().getLeagues()) {
            if (league == null || league.getLeagueClubs() == null) {
                continue;
            }
            for (Club c : league.getLeagueClubs()) {
                if (c != null && club.getId().equals(c.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** The game's clubs once each (a new game lists some twice, P2-12/P2-13), in id order */
    public static List<Club> uniqueClubs(SaveGame g) {
        Set<Club> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<Club> out = new ArrayList<>();
        if (g == null || g.getAllClubs() == null) {
            return out;
        }
        for (Club c : g.getAllClubs()) {
            if (c != null && seen.add(c)) {
                out.add(c);
            }
        }
        out.sort(Comparator.comparing(Club::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        return out;
    }

    /** The club's player an engagement is for */
    public static Player playerOf(Club club, PlayerContract c) {
        if (club == null || c == null || c.getPlayerId() == null) {
            return null;
        }
        for (Player p : club.getPlayers()) {
            if (p != null && c.getPlayerId().equals(idOf(p))) {
                return p;
            }
        }
        return null;
    }

    /** A player's id (a Player shares its Person's id; an older test object may only have the person's) */
    static Long idOf(Player p) {
        if (p == null) {
            return null;
        }
        return p.getId() != null ? p.getId() : p.getPerson() != null ? p.getPerson().getId() : null;
    }

    private static PlayerContract contractOf(List<PlayerContract> contracts, Player p) {
        for (PlayerContract c : contracts) {
            if (c.getPlayerId() != null && c.getPlayerId().equals(idOf(p))) {
                return c;
            }
        }
        return null;
    }

    private static <T> void removeSame(List<T> list, T item) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == item) {
                list.remove(i);
                return;
            }
        }
    }

    private static <T> boolean containsSame(List<T> list, T item) {
        for (T t : list) {
            if (t == item) {
                return true;
            }
        }
        return false;
    }

    /** "45s a week, 20s in summer" style text of an engagement's pay (Money.format amounts) */
    public static String terms(PlayerContract c) {
        if (c == null) {
            return "";
        }
        if (c.isAmateur()) {
            return LanguageModLoader.format("contracts_terms_amateur", Money.format(c.getWeeklyExpenses()));
        }
        if (c.hasSummerPay()) {
            return LanguageModLoader.format("contracts_terms_summer", Money.format(c.getWeeklyWage()),
                    Money.format(c.getSummerWage()));
        }
        return LanguageModLoader.format("contracts_terms", Money.format(c.getWeeklyWage()));
    }

    private static String replyLine(Player p, PlayerContract c) {
        if (c.getAnswer() == PlayerContract.Answer.ACCEPTED) {
            String offered;
            if (c.isAmateur()) {
                offered = LanguageModLoader.getValue("contracts_terms_amateur_short");
            } else if (c.getOfferedSummerWage() != null && c.getOfferedSummerWage().signum() > 0) {
                offered = LanguageModLoader.format("contracts_terms_summer", Money.format(c.getOfferedWage()),
                        Money.format(c.getOfferedSummerWage()));
            } else {
                offered = LanguageModLoader.format("contracts_terms", Money.format(c.getOfferedWage()));
            }
            return LanguageModLoader.format("contracts_reply_accepted", name(p), offered);
        }
        return LanguageModLoader.format("contracts_reply_declined", name(p));
    }

    private static String name(Player p) {
        if (p == null || p.getPerson() == null) {
            return "?";
        }
        Person person = p.getPerson();
        return ((person.getName() != null ? person.getName() : "") + " "
                + (person.getLastname() != null ? person.getLastname() : "")).trim();
    }

    private static String lines(List<String> items, String none) {
        if (items.isEmpty()) {
            return "- " + none;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : items) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append("- ").append(s);
        }
        return sb.toString();
    }

    /** The date in the language's numeric pattern ({@code schedule_date_pattern}) */
    public static String formatDate(LocalDate day) {
        String pattern = LanguageModLoader.getValue("schedule_date_pattern");
        try {
            return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(day);
        } catch (RuntimeException e) { // a bad pattern in a language file
            return DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH).format(day);
        }
    }

    /** The April letter: the retained list is open. */
    private void openRetainedList(int season) {
        SaveGame g = game();
        Club club = g.getCurrentClub();
        if (club == null || endingContracts(club).isEmpty()) {
            return;
        }
        String next = ContractCalendar.seasonName(season + 1);
        String deadline = formatDate(ContractCalendar.seasonEnd(season).toLocalDate());
        String body = LanguageModLoader.format("contracts_retained_list_body", deadline, next)
                + (season + 1 < ContractCalendar.RETAIN_AND_TRANSFER_FROM
                        ? "\n\n" + LanguageModLoader.getValue("contracts_retained_list_free") : "");
        letter(club, MESSAGE_RETAINED_LIST, LanguageModLoader.format("contracts_retained_list_title", next), body, true);
    }

    private void letter(Club club, String type, String title, String body, boolean linkToList) {
        if (messages == null) {
            return;
        }
        Message m = new Message();
        m.setCategory(MessageCategory.SYSTEM);
        m.setMessageType(type);
        m.setPriority(MessagePriority.NORMAL);
        m.setTitle(title);
        m.setPlainTextMessage(body);
        m.setActionData(club.getId());
        if (linkToList) {
            m.setActionScreen(MainMenuManager.RETAINED_LIST_SCREEN);
        }
        m.setIsRead(false);
        m.setIsDeleted(false);
        messages.deliverMessage(m);
    }
}
