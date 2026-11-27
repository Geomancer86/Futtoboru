package com.rndmodgames.futtoboru.system.save;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * A comparable summary of a game's state (P2-5): key -> text, sorted, so a failing comparison shows which part
 * differs. Covers the date, the id allocator, every club's balance, record, squad, contracts and fixtures, the
 * league tables, the competition editions, messages, jobs, scripts and the random seed.
 *
 * P2-0 can extend it (subclass and {@link #put} more keys, or compare two games after "continue N days").
 */
public class GameFingerprint {

    private final Map<String, String> values = new TreeMap<>();

    protected GameFingerprint() {
    }

    public static GameFingerprint of(SaveGame game) {
        GameFingerprint f = new GameFingerprint();
        f.read(game);
        return f;
    }

    protected void put(String key, Object value) {
        values.put(key, String.valueOf(value));
    }

    protected void read(SaveGame game) {
        put("date", game.getGameDate());
        put("startDate", game.getGameStartDate());
        put("allocator.next", game.getIdAllocator().peek());
        put("seed", game.getSeed());
        put("owner", game.getOwner() == null ? null
                : game.getOwner().getId() + " at " + game.getOwner().getCurrentClubId());

        int contracts = 0;
        List<String> played = new ArrayList<>();
        Map<Club, Boolean> seen = new IdentityHashMap<>();
        for (Club club : game.getAllClubs()) {
            if (seen.put(club, Boolean.TRUE) != null) {
                continue;
            }
            String k = "club." + club.getId() + ".";
            put(k + "balance", plain(club.getClubBalance()));
            put(k + "record", club.getMatchesPlayed() + "p " + club.getMatchesWon() + "w " + club.getMatchesDrawn()
                    + "d " + club.getMatchesLost() + "l " + club.getGoalsScored() + "-" + club.getGoalsConceded() + " "
                    + club.getPoints() + "pts");
            List<Long> squad = new ArrayList<>();
            for (Player p : club.getPlayers()) {
                squad.add(p.getId());
            }
            Collections.sort(squad);
            put(k + "squad", squad);
            put(k + "contracts", club.getPlayerContracts().size());
            contracts += club.getPlayerContracts().size();
            put(k + "fixtures", club.getProposedMatches().size() + " proposed, " + club.getScheduledMatches().size()
                    + " scheduled, " + club.getPlayedMatches().size() + " played");
            put(k + "staff", new TreeMap<>(club.getStaff()));
            put(k + "finances", club.getFinancialHistory().size() + " snapshots, " + club.getExpensesHistory().size()
                    + " expenses, " + club.getMatchIncomes().size() + " incomes");
            for (Match m : club.getPlayedMatches()) {
                if (club.getId().equals(m.getHomeClubId())) {
                    played.add(m.getId() + ":" + m.getHomeClubId() + "-" + m.getAwayClubId() + " " + m.getHomeGoals()
                            + "-" + m.getAwayGoals() + " @" + m.getMatchDateTime());
                }
            }
        }
        Collections.sort(played);
        put("clubs", seen.size());
        put("contracts", contracts);
        put("matches.played", played);

        LeagueStandingsManager standings = new LeagueStandingsManager();
        for (League league : game.getMainAuthority().getLeagues()) {
            StringJoiner table = new StringJoiner(", ");
            for (Club c : standings.calculateStandings(league)) {
                table.add(c.getId() + ":" + c.getPoints());
            }
            put("league." + league.getId() + ".table", table);
            put("league." + league.getId() + ".editions", editions(league.getEditions()));
        }
        Map<Long, Competition> competitions = new TreeMap<>();
        for (Competition c : game.getAllCups()) {
            competitions.put(c.getId(), c);
        }
        for (Competition c : game.getAllLeagues()) {
            competitions.put(c.getId(), c);
        }
        for (Competition c : competitions.values()) {
            put("competition." + c.getId() + ".editions", editions(c.getEditions()));
        }

        put("messages", game.getAllMessages().size() + " delivered, " + game.getScheduledMessages().size() + " scheduled");
        put("persons", game.getAllPersons().size());
        put("jobs", game.getActiveJobOpenings().size() + " openings, " + game.getAllApplications().size()
                + " applications, " + game.getPendingOffers().size() + " offers");
        put("snapshots", game.getPlayerAttributeSnapshots().size());
        StringJoiner scripts = new StringJoiner(", ");
        for (BasicScript s : game.getGameScripts()) {
            scripts.add(s.getName() + "=" + s.getIsExecuted());
        }
        put("scripts", scripts);
    }

    private static String editions(List<CompetitionEdition> editions) {
        StringJoiner out = new StringJoiner("; ");
        for (CompetitionEdition e : editions) {
            out.add(e.getId() + " " + e.getName() + " " + e.getParticipantClubsIds().size() + " clubs, "
                    + e.getStartDate() + ".." + e.getEndDate() + ", champions " + e.getChampionsId() + ", runners-up "
                    + e.getRunnersUpId());
        }
        return out.toString();
    }

    private static String plain(BigDecimal n) {
        return n == null ? null : n.toPlainString();
    }

    /** @return key -> value, sorted by key */
    public Map<String, String> values() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GameFingerprint && ((GameFingerprint) o).values.equals(values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : values.entrySet()) {
            sb.append(e.getKey()).append(" = ").append(e.getValue()).append('\n');
        }
        return sb.toString();
    }
}
