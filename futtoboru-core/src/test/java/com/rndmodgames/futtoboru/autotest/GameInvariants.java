package com.rndmodgames.futtoboru.autotest;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerContract;
import com.rndmodgames.futtoboru.data.PlayerInjury;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.engine.committee.CommitteeManager;
import com.rndmodgames.futtoboru.engine.contracts.ContractTerms;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * A-01: what must hold in any game on any day. Each check throws an {@link AssertionError} naming the seed, the
 * date and what is wrong; {@code where} carries the seed and the date.
 */
final class GameInvariants {

    /** Attributes are on the 3d6 + bonus scale (PlayerAttributeGenerator). */
    static final float ATTRIBUTE_MIN = 3f;
    static final float ATTRIBUTE_MAX = 23f;
    static final int AGE_MIN = 14;
    static final int AGE_MAX = 60;
    /** 1888 pounds: the richest club of the era turned over a few thousand a year */
    static final BigDecimal BALANCE_LIMIT = new BigDecimal("100000");
    /** an injured man is fit again the day after his last day out (InjuryManager.recoverDaily) */
    static final int INJURY_GRACE_DAYS = 1;

    private GameInvariants() {
    }

    static void fail(String where, String what) {
        throw new AssertionError(where + ": " + what);
    }

    static void check(boolean ok, String where, String what) {
        if (!ok) {
            fail(where, what);
        }
    }

    // --------------------------------------------------------------------------------------------- matches

    /**
     * Every match of today was played once, each side by 11 men, or by all its fit men when it had fewer (the
     * documented short side, G-01 kickOff).
     */
    static void todaysSides(String where, SaveGame game, Map<String, Integer> fitBefore, LocalDate day) {
        Map<Long, Match> played = playedById(game);
        // one match a day per club
        Map<Long, Match> todays = new HashMap<>();
        for (Match m : played.values()) {
            if (m.getMatchDateTime() != null && m.getMatchDateTime().toLocalDate().equals(day)) {
                for (Long club : new Long[] { m.getHomeClubId(), m.getAwayClubId() }) {
                    Match other = todays.put(club, m);
                    if (other != null) {
                        Club c = game.getClubById(club);
                        fail(where, (c != null ? c.getName() : club) + " played two matches on " + day + ": "
                                + describe(game, other) + " and " + describe(game, m));
                    }
                }
            }
        }
        for (Map.Entry<String, Integer> e : fitBefore.entrySet()) {
            String[] k = e.getKey().split(":");
            Long id = Long.valueOf(k[0]);
            Match m = played.get(id);
            if (m == null) {
                if (scheduledById(game).containsKey(id)) {
                    fail(where, "match " + id + " of " + day + " was not played on its day");
                }
                continue; // cancelled (a friendly that clashed), no longer anywhere
            }
            TeamSheet sheet = "H".equals(k[1]) ? m.getHomeSheet() : m.getAwaySheet();
            check(sheet != null, where, "match " + id + " has no " + k[1] + " team sheet");
            int expected = Math.min(TeamShape.SLOTS, e.getValue());
            check(sheet.selectedCount() == expected, where, describe(game, m) + " side " + k[1] + " fielded "
                    + sheet.selectedCount() + " men with " + e.getValue() + " fit (expected " + expected + "), sheet "
                    + sheet.getPlayerIds() + " " + sheet.getShape() + ", managed club "
                    + (game.getCurrentClub() != null ? game.getCurrentClub().getName() : null));
        }
    }

    /** Played matches are in exactly the two clubs' lists once; nothing before today is left scheduled. */
    static void matchesPlayedOnce(String where, SaveGame game, LocalDate today) {
        Map<Long, Integer> seen = new HashMap<>();
        for (Club c : game.getAllClubs()) {
            Set<Long> own = new HashSet<>();
            for (Match m : c.getPlayedMatches()) {
                check(own.add(m.getId()), where, c.getName() + " lists match " + m.getId() + " twice as played");
                check(c.getId().equals(m.getHomeClubId()) || c.getId().equals(m.getAwayClubId()), where,
                        c.getName() + " lists match " + m.getId() + " it did not play");
                check(Boolean.TRUE.equals(m.getIsPlayed()), where, "match " + m.getId() + " in the played list is not played");
                check(m.getHomeGoals() != null && m.getAwayGoals() != null && m.getHomeGoals() >= 0
                        && m.getAwayGoals() >= 0, where, "match " + m.getId() + " has no score");
                seen.merge(m.getId(), 1, Integer::sum);
            }
            for (Match m : c.getScheduledMatches()) {
                check(!own.contains(m.getId()), where, "match " + m.getId() + " both played and scheduled at " + c.getName());
                if (m.getMatchDateTime() != null && m.getHomeClubId() != null && m.getAwayClubId() != null
                        && m.getMatchDateTime().toLocalDate().isBefore(today)) {
                    fail(where, describe(game, m) + " was never played (listed by " + c.getName() + ")");
                }
            }
        }
        for (Map.Entry<Long, Integer> e : seen.entrySet()) {
            check(e.getValue() == 2, where, "match " + e.getKey() + " is in " + e.getValue() + " played lists, not 2");
        }
    }

    /** A match in words: id, kind, clubs, date, competition, round, score, played flag. */
    static String describe(SaveGame game, Match m) {
        Club h = m.getHomeClubId() != null ? game.getClubById(m.getHomeClubId()) : null;
        Club a = m.getAwayClubId() != null ? game.getClubById(m.getAwayClubId()) : null;
        String kind = m.getMatchType() == null ? "?" : switch (m.getMatchType()) {
            case Match.LEAGUE_MATCH -> "league";
            case Match.CUP_MATCH -> "cup";
            case Match.FRIENDLY_MATCH -> "friendly";
            default -> "type " + m.getMatchType();
        };
        return kind + " match " + m.getId() + " " + (h != null ? h.getName() : m.getHomeClubId()) + " v "
                + (a != null ? a.getName() : m.getAwayClubId()) + " on " + m.getMatchDateTime() + " (competition "
                + m.getCompetitionId() + "/" + m.getCompetitionEditionId() + ", round " + m.getRound() + " "
                + m.getBracketPath() + ", played " + m.getIsPlayed() + " " + m.getHomeGoals() + "-" + m.getAwayGoals()
                + ")";
    }

    static Map<Long, Match> playedById(SaveGame game) {
        Map<Long, Match> out = new HashMap<>();
        for (Club c : game.getAllClubs()) {
            for (Match m : c.getPlayedMatches()) {
                out.put(m.getId(), m);
            }
        }
        return out;
    }

    static Map<Long, Match> scheduledById(SaveGame game) {
        Map<Long, Match> out = new HashMap<>();
        for (Club c : game.getAllClubs()) {
            for (Match m : c.getScheduledMatches()) {
                out.put(m.getId(), m);
            }
        }
        return out;
    }

    // --------------------------------------------------------------------------------------------- tables

    /**
     * The live table of each league adds up (W+D+L=P, points from the rules, goals for = goals against) and equals
     * the league matches of the running edition played so far.
     */
    static void tables(String where, SaveGame game) {
        if (game.getMainAuthority() == null) {
            return;
        }
        Map<Long, Match> played = playedById(game);
        for (League league : game.getMainAuthority().getLeagues()) {
            if (league.getEditions().isEmpty() || league.getLeagueClubs() == null) {
                continue;
            }
            CompetitionEdition edition = league.getEditions().get(league.getEditions().size() - 1);
            if (!edition.getFinalStandings().isEmpty()) {
                continue; // between the final table and the next season's start
            }
            CompetitionRules rules = league.getRulesOrDefault();
            Map<Long, int[]> fromMatches = new HashMap<>();
            Set<Long> members = new HashSet<>();
            for (Club c : league.getLeagueClubs()) {
                members.add(c.getId());
            }
            for (Match m : played.values()) {
                // league fixtures carry no edition id: the edition's dates and the league's clubs place them
                if (m.getMatchType() != null && m.getMatchType() == Match.LEAGUE_MATCH && inEdition(m, edition)
                        && members.contains(m.getHomeClubId()) && members.contains(m.getAwayClubId())) {
                    add(fromMatches, m.getHomeClubId(), m.getHomeGoals(), m.getAwayGoals());
                    add(fromMatches, m.getAwayClubId(), m.getAwayGoals(), m.getHomeGoals());
                }
            }
            int gf = 0;
            int ga = 0;
            int n = league.getLeagueClubs().size();
            for (Club c : league.getLeagueClubs()) {
                String w = where + " " + league.getName() + " " + c.getName();
                int p = nz(c.getMatchesPlayed());
                check(p == nz(c.getMatchesWon()) + nz(c.getMatchesDrawn()) + nz(c.getMatchesLost()), w, "W+D+L != P");
                check(nz(c.getPoints()) == nz(c.getMatchesWon()) * rules.getPointsForWin()
                        + nz(c.getMatchesDrawn()) * rules.getPointsForDraw()
                        + nz(c.getMatchesLost()) * rules.getPointsForLoss(), w, "points do not follow the rules");
                check(p <= 2 * (n - 1), w, "played " + p + " of " + 2 * (n - 1));
                int[] r = fromMatches.getOrDefault(c.getId(), new int[5]);
                String rec = p + "p " + nz(c.getMatchesWon()) + "w " + nz(c.getMatchesDrawn()) + "d "
                        + nz(c.getGoalsScored()) + "-" + nz(c.getGoalsConceded());
                String mat = r[0] + "p " + r[1] + "w " + r[2] + "d " + r[3] + "-" + r[4];
                check(rec.equals(mat), w, "table row " + rec + " but the played league matches give " + mat);
                gf += nz(c.getGoalsScored());
                ga += nz(c.getGoalsConceded());
            }
            check(gf == ga, where + " " + league.getName(), "goals for " + gf + " != goals against " + ga);
        }
    }

    static boolean inEdition(Match m, CompetitionEdition edition) {
        return m.getMatchDateTime() != null && edition.getStartDate() != null
                && !m.getMatchDateTime().toLocalDate().isBefore(edition.getStartDate().toLocalDate())
                && (edition.getEndDate() == null
                        || !m.getMatchDateTime().toLocalDate().isAfter(edition.getEndDate().toLocalDate()));
    }

    private static void add(Map<Long, int[]> rows, Long club, Integer scored, Integer conceded) {
        int[] r = rows.computeIfAbsent(club, k -> new int[5]);
        r[0]++;
        int s = nz(scored);
        int c = nz(conceded);
        if (s > c) {
            r[1]++;
        } else if (s == c) {
            r[2]++;
        }
        r[3] += s;
        r[4] += c;
    }

    static int nz(Integer i) {
        return i == null ? 0 : i;
    }

    /** A cup edition that has its winner leaves no tie unplayed in any club's fixtures. */
    static void finishedCupsLeaveNoTies(String where, SaveGame game) {
        Set<Long> finished = new HashSet<>();
        for (Competition c : game.getAllCups()) {
            for (CompetitionEdition e : c.getEditions()) {
                if (e.getChampionsId() != null) {
                    finished.add(e.getId());
                }
            }
        }
        for (Club c : game.getAllClubs()) {
            for (Match m : c.getScheduledMatches()) {
                if (m.getCompetitionEditionId() != null && finished.contains(m.getCompetitionEditionId())) {
                    fail(where, describe(game, m) + " is still in " + c.getName()
                            + "'s fixtures after its cup was won");
                }
            }
        }
    }

    /** Each competition has one edition per football year, and no two at once. */
    static void oneEditionPerSeason(String where, SaveGame game) {
        List<Competition> all = new ArrayList<>(game.getAllCups());
        all.addAll(game.getAllLeagues());
        for (Competition c : all) {
            Set<Integer> years = new HashSet<>();
            Set<Long> ids = new HashSet<>();
            for (CompetitionEdition e : c.getEditions()) {
                if (!ids.add(e.getId())) {
                    continue; // the same edition listed by the league and the competition
                }
                if (e.getStartDate() != null) {
                    int y = CommitteeManager.footballYear(e.getStartDate());
                    check(years.add(y), where, c.getName() + " has two editions in " + y + "-" + (y + 1));
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------- clubs

    /**
     * At the start of a season (1 September): every club has a ground, a secretary (or his post advertised) and
     * at least 11 registered men.
     */
    static void clubsAtSeasonStart(String where, SaveGame game, List<JobOpening> openings) {
        Profession secretary = DatabaseLoader.getProfessionByDataId(Profession.MANAGER);
        Set<Long> advertised = new HashSet<>();
        for (JobOpening o : openings) {
            if (secretary.getId().equals(o.getProfessionId())) {
                advertised.add(o.getClubId());
            }
        }
        for (Club c : game.getAllClubs()) {
            String w = where + " " + c.getName();
            check(c.getStadium() != null, w, "no ground");
            Long sec = c.getStaff().get(secretary.getId());
            check(sec != null || advertised.contains(c.getId()), w, "no secretary and the post is not advertised");
            check(c.getPlayers().size() >= TeamShape.SLOTS, w, "only " + c.getPlayers().size() + " registered men");
        }
    }

    /** Each man is registered once, at one club (not twice in a squad, not at two clubs, not also a free agent). */
    static void oneClubPerMan(String where, SaveGame game) {
        Map<Long, String> at = new HashMap<>();
        for (Club c : game.getAllClubs()) {
            for (Player p : c.getPlayers()) {
                check(p != null && p.getId() != null, where + " " + c.getName(), "a squad man without an id");
                String before = at.put(p.getId(), c.getName());
                check(before == null, where, "player " + p.getId() + " is registered at " + before + " and at "
                        + c.getName());
            }
        }
        for (Player p : game.getFreeAgents()) {
            check(!at.containsKey(p.getId()), where, "free agent " + p.getId() + " is also at " + at.get(p.getId()));
        }
    }

    /** Engagements: one running engagement per squad man, none for a man who left, no negative pay. */
    static void contracts(String where, SaveGame game, int runningSeason) {
        for (Club c : game.getAllClubs()) {
            String w = where + " " + c.getName();
            Set<Long> squad = new HashSet<>();
            for (Player p : c.getPlayers()) {
                squad.add(p.getId());
            }
            Set<Long> engaged = new HashSet<>();
            for (PlayerContract k : c.getPlayerContracts()) {
                if (k == null || !Boolean.TRUE.equals(k.getIsActive())) {
                    continue;
                }
                check(k.getWeeklyWage() != null && k.getWeeklyWage().signum() >= 0, w, "engagement of "
                        + k.getPlayerId() + " has wage " + k.getWeeklyWage());
                check(k.getSummerWage() != null && k.getSummerWage().signum() >= 0, w, "engagement of "
                        + k.getPlayerId() + " has summer pay " + k.getSummerWage());
                check(squad.contains(k.getPlayerId()), w, "pays an engagement to " + k.getPlayerId()
                        + ", who is not in the squad");
                if (k.getSeason() != null && k.getSeason() == runningSeason) {
                    check(engaged.add(k.getPlayerId()), w, "two running engagements for " + k.getPlayerId());
                }
            }
            for (Long id : squad) {
                check(engaged.contains(id), w, "squad man " + id + " has no engagement for " + runningSeason);
            }
            BigDecimal bill = ContractTerms.wageBill(c, false);
            check(bill.signum() >= 0, w, "wage bill " + bill);
        }
    }

    /** Finite, plausible numbers: attributes 3-23, ages, balances. */
    static void values(String where, SaveGame game, LocalDate today) {
        for (Club c : game.getAllClubs()) {
            String w = where + " " + c.getName();
            BigDecimal b = c.getClubBalance();
            check(b != null, w, "no balance");
            check(b.abs().compareTo(BALANCE_LIMIT) < 0, w, "balance " + b + " beyond +-" + BALANCE_LIMIT);
            check(c.getSeasonIncome() == null || c.getSeasonIncome().signum() >= 0, w, "season income "
                    + c.getSeasonIncome());
            check(c.getSeasonExpenditure() == null || c.getSeasonExpenditure().signum() >= 0, w,
                    "season expenditure " + c.getSeasonExpenditure());
            for (Player p : c.getPlayers()) {
                attributes(w, p);
                if (p.getPerson() != null && p.getPerson().getBirthDate() != null) {
                    int age = ContractTerms.age(p, today);
                    check(age >= AGE_MIN && age <= AGE_MAX, w, "player " + p.getId() + " is " + age);
                }
            }
        }
    }

    private static final List<Method> ATTRIBUTES = new ArrayList<>();

    static {
        for (Method m : Player.class.getMethods()) {
            if (m.getName().startsWith("get") && m.getReturnType() == Float.class && m.getParameterCount() == 0) {
                ATTRIBUTES.add(m);
            }
        }
    }

    static void attributes(String where, Player p) {
        for (Method m : ATTRIBUTES) {
            Float v;
            try {
                v = (Float) m.invoke(p);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
            if (v == null) {
                continue;
            }
            check(!v.isNaN() && !v.isInfinite() && v >= ATTRIBUTE_MIN && v <= ATTRIBUTE_MAX, where,
                    "player " + p.getId() + " " + m.getName().substring(3) + " = " + v);
        }
    }

    /** No man at a club is still out after his last day. */
    static void injuriesHeal(String where, SaveGame game, LocalDate today) {
        for (Club c : game.getAllClubs()) {
            for (Player p : c.getPlayers()) {
                PlayerInjury i = p.getInjury();
                if (i != null && i.getUntil() != null) {
                    check(!i.getUntil().toLocalDate().plusDays(INJURY_GRACE_DAYS).isBefore(today), where + " "
                            + c.getName(), "player " + p.getId() + " still injured, out until " + i.getUntil());
                }
            }
        }
    }

    /** Re-election (G-08): each league's new season has as many clubs as the season before. */
    static void reElectionFillsPlaces(String where, SaveGame game) {
        for (League league : game.getMainAuthority().getLeagues()) {
            List<CompetitionEdition> eds = league.getEditions();
            for (int i = 1; i < eds.size(); i++) {
                int before = eds.get(i - 1).getParticipantClubsIds().size();
                int now = eds.get(i).getParticipantClubsIds().size();
                check(now == before, where + " " + league.getName(), eds.get(i).getName() + " has " + now
                        + " clubs, the season before " + before);
                check(new HashSet<>(eds.get(i).getParticipantClubsIds()).size() == now,
                        where + " " + league.getName(), eds.get(i).getName() + " lists a club twice");
            }
            check(league.getLeagueClubs().size() == eds.get(eds.size() - 1).getParticipantClubsIds().size(),
                    where + " " + league.getName(), "the league has " + league.getLeagueClubs().size()
                            + " clubs, its edition " + eds.get(eds.size() - 1).getParticipantClubsIds().size());
        }
    }

    /** All the dismissals so far (G-05), every club. */
    static int dismissals(SaveGame game) {
        int n = 0;
        for (Club c : game.getAllClubs()) {
            if (c.getCommittee() != null) {
                n += c.getCommittee().getDismissedIds().size();
            }
        }
        return n;
    }
}
