package com.rndmodgames.futtoboru.engine.recruitment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubExpenses;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * R-05 (docs/design/RECRUITMENT_AND_TRIALS.md §3.3): every club's shortlist and the men who reached it. One
 * {@link ShortlistEntry} per (club, man); the shortlist is the {@link ShortlistEntry#LISTED} ones. A man on a list
 * is a candidate of the pools or a free agent (a "not retained" man); one who is neither any more (signed, gone at
 * the close season) leaves every list ({@link #prune}). The men a club still has an open entry for stay in the pools
 * through the monthly turnover and the close-season rebuild ({@link #held}).
 */
public final class Shortlists {

    /** An AI club keeps this many men on its list at most; the longest-listed go first (placeholder). */
    public static final int AI_MAX = 12;
    /** One-off bookings ({@link #book}) carry this period type, so the weekly expenses' 7-day clock ignores them. */
    public static final String ONE_OFF = "ONE_OFF";
    static final BigDecimal PENCE_A_POUND = BigDecimal.valueOf(240);

    private Shortlists() {
    }

    // ------------------------------------------------------------------------------------------- lookups

    /** @return the club's entry for this man, whatever its status, or null */
    public static ShortlistEntry entry(SaveGame game, Long club, Long player) {
        if (game == null || club == null || player == null) {
            return null;
        }
        for (ShortlistEntry e : game.getShortlist()) {
            if (club.equals(e.getClub()) && player.equals(e.getPlayer())) {
                return e;
            }
        }
        return null;
    }

    /** @return the club's shortlist ({@link ShortlistEntry#LISTED}), oldest first */
    public static List<ShortlistEntry> listed(SaveGame game, Club club) {
        List<ShortlistEntry> out = new ArrayList<>();
        if (game == null || club == null || club.getId() == null) {
            return out;
        }
        for (ShortlistEntry e : game.getShortlist()) {
            if (club.getId().equals(e.getClub()) && e.isListed()) {
                out.add(e);
            }
        }
        return out;
    }

    /** @return the entries that came in one inbox letter */
    public static List<ShortlistEntry> ofLetter(SaveGame game, Long letter) {
        List<ShortlistEntry> out = new ArrayList<>();
        if (game == null || letter == null) {
            return out;
        }
        for (ShortlistEntry e : game.getShortlist()) {
            if (letter.equals(e.getLetter())) {
                out.add(e);
            }
        }
        return out;
    }

    /** @return the ids of the men some club still has an open (not ignored) entry for */
    public static Set<Long> held(SaveGame game) {
        Set<Long> out = new HashSet<>();
        if (game == null) {
            return out;
        }
        for (ShortlistEntry e : game.getShortlist()) {
            if (e.getPlayer() != null && !ShortlistEntry.IGNORED.equals(e.getStatus())) {
                out.add(e.getPlayer());
            }
        }
        return out;
    }

    /** @return the men who may be on a list by id: the pools' candidates and the free agents */
    public static Map<Long, Player> men(SaveGame game) {
        Map<Long, Player> out = new HashMap<>();
        for (Candidate c : game.getCandidates()) {
            if (c != null && c.getPlayer() != null && c.getPlayer().getId() != null) {
                out.put(c.getPlayer().getId(), c.getPlayer());
            }
        }
        for (Player p : game.getFreeAgents()) {
            if (p != null && p.getId() != null) {
                out.putIfAbsent(p.getId(), p);
            }
        }
        return out;
    }

    /** @return the man of an entry (candidate or free agent), or null */
    public static Player man(SaveGame game, Long player) {
        if (game == null || player == null) {
            return null;
        }
        for (Candidate c : game.getCandidates()) {
            if (c != null && c.getPlayer() != null && player.equals(c.getPlayer().getId())) {
                return c.getPlayer();
            }
        }
        for (Player p : game.getFreeAgents()) {
            if (p != null && player.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    /** @return true when this club is the one the player runs (letters to the inbox, not straight to the list) */
    public static boolean isHuman(SaveGame game, Club club) {
        Club mine = game != null ? game.getCurrentClub() : null;
        return mine != null && club != null && mine.getId() != null && mine.getId().equals(club.getId());
    }

    // ------------------------------------------------------------------------------------------- changes

    /**
     * A man reached the club: a new entry, unless the club has one for him already.
     *
     * @return the new entry, or null when he had one
     */
    public static ShortlistEntry arrive(SaveGame game, Club club, Player man, String source, String status,
            LocalDateTime now) {
        if (game == null || club == null || club.getId() == null || man == null || man.getId() == null
                || entry(game, club.getId(), man.getId()) != null) {
            return null;
        }
        ShortlistEntry e = new ShortlistEntry(club.getId(), man.getId(), source, status, now);
        game.getShortlist().add(e);
        if (ShortlistEntry.LISTED.equals(status) && !isHuman(game, club)) {
            capAi(game, club);
        }
        return e;
    }

    /** The secretary puts a man on the list himself (or back on it): true when the list changed. */
    public static boolean add(SaveGame game, Club club, Player man, LocalDateTime now) {
        if (game == null || club == null || man == null || man.getId() == null) {
            return false;
        }
        ShortlistEntry e = entry(game, club.getId(), man.getId());
        if (e == null) {
            return arrive(game, club, man, ShortlistEntry.OWN, ShortlistEntry.LISTED, now) != null;
        }
        if (e.isListed() || ShortlistEntry.DUE.equals(e.getStatus()) || ShortlistEntry.UNPAID.equals(e.getStatus())) {
            return false;
        }
        e.setStatus(ShortlistEntry.LISTED);
        e.setSince(now);
        return true;
    }

    /** Takes a man off the club's list (the entry stays, ignored, so the same letter does not bring him back). */
    public static boolean remove(SaveGame game, Club club, Long player) {
        ShortlistEntry e = club == null ? null : entry(game, club.getId(), player);
        if (e == null || !e.isListed()) {
            return false;
        }
        e.setStatus(ShortlistEntry.IGNORED);
        return true;
    }

    /** "Add to shortlist" on a letter: an offered man goes on the list. */
    public static boolean accept(ShortlistEntry e, LocalDateTime now) {
        if (e == null || !ShortlistEntry.OFFERED.equals(e.getStatus())) {
            return false;
        }
        e.setStatus(ShortlistEntry.LISTED);
        e.setSince(now);
        return true;
    }

    /** "Ignore" on a letter: an offered (or an agent's unpaid) man is let go. */
    public static boolean ignore(ShortlistEntry e) {
        if (e == null || !(ShortlistEntry.OFFERED.equals(e.getStatus())
                || ShortlistEntry.UNPAID.equals(e.getStatus()))) {
            return false;
        }
        e.setStatus(ShortlistEntry.IGNORED);
        return true;
    }

    /**
     * Drops the entries whose man is no longer a candidate or a free agent (signed, or gone at the close season).
     *
     * @return how many entries went
     */
    public static int prune(SaveGame game) {
        if (game == null || game.getShortlist().isEmpty()) {
            return 0;
        }
        Map<Long, Player> men = men(game);
        int before = game.getShortlist().size();
        game.getShortlist().removeIf(e -> e.getPlayer() == null || e.getClub() == null || !men.containsKey(e.getPlayer()));
        return before - game.getShortlist().size();
    }

    private static void capAi(SaveGame game, Club club) {
        List<ShortlistEntry> list = listed(game, club);
        int over = list.size() - AI_MAX;
        for (Iterator<ShortlistEntry> it = list.iterator(); it.hasNext() && over > 0; over--) {
            game.getShortlist().remove(it.next());
        }
    }

    // ------------------------------------------------------------------------------------------- money

    /** @return pence as pounds (240 to the pound) */
    public static BigDecimal pounds(int pence) {
        return BigDecimal.valueOf(pence).divide(PENCE_A_POUND, 4, RoundingMode.HALF_UP);
    }

    /**
     * Books a one-off cost (an advert, an agent's fee) once: off the balance, into the season's and month's
     * expenditure, and as a record in the club's expense history (administration: printing, postage, advertising),
     * marked {@link #ONE_OFF}.
     */
    public static void book(Club club, int pence, LocalDateTime now) {
        if (club == null || pence <= 0) {
            return;
        }
        BigDecimal cost = pounds(pence);
        ClubExpenses e = new ClubExpenses(club.getId(), now, now, ONE_OFF);
        e.setAdministrativeExpenses(cost);
        e.calculateTotal();
        club.addExpense(e);
        BigDecimal balance = club.getClubBalance() != null ? club.getClubBalance() : BigDecimal.ZERO;
        club.setClubBalance(balance.subtract(cost));
        club.setSeasonExpenditure(club.getSeasonExpenditure().add(cost));
        club.setMonthExpenditure(club.getMonthExpenditure().add(cost));
    }
}
