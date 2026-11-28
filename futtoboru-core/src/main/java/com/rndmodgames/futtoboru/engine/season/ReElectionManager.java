package com.rndmodgames.futtoboru.engine.season;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.ReElection;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-08: the re-election vote at the end of a League season (Phase 5).
 *
 * <ol>
 *   <li><b>Open</b> ({@link #open}), when the season completes: if the League's rule ({@link ReElectionRule}, from the
 *       data) covers the season, its bottom clubs must retire and seek re-election; clubs outside the League apply
 *       (each decides from its own record, gates and strength, seeded). The meeting date is set; the next season is
 *       not generated until the vote ({@link #isPending}).</li>
 *   <li><b>Meeting</b> ({@link #holdDueMeetings}): every member club that is not up for re-election votes for as many
 *       candidates as there are places, weighted by each candidate's standing (record, gates, strength, League
 *       membership) and its own seeded preference. The most votes fill the places; ties go to the better standing,
 *       then the lower id. Losers leave the League (no other competition or level exists in the data yet, so they
 *       play friendlies and the cup); the elected clubs join for the next season.</li>
 * </ol>
 * Real outcomes (1889: all four re-elected) are reference data only (D17): nothing here reads them. All draws are
 * forks of the game's seeded source, so the same seed holds the same vote. AI clubs and the player's club go through
 * the same path; the player's club also gets letters (notice + application, canvass report, result).
 */
public class ReElectionManager {

    public static final String MSG_CALL = "REELECTION_CALL";
    public static final String MSG_NOTICE = "REELECTION_NOTICE";
    public static final String MSG_CANVASS = "REELECTION_CANVASS";
    public static final String MSG_MEETING = "REELECTION_MEETING";
    public static final String MSG_RESULT = "REELECTION_RESULT";

    /** Base chance that an eligible club outside the League applies, raised by its standing. */
    static final double APPLY_BASE = 0.10;
    static final double APPLY_PER_STANDING = 0.60;
    /** League members up for re-election start ahead: the members knew them (1889: all four re-elected). */
    static final double MEMBER_BONUS = 0.25;
    /** A voter's own liking for a candidate: standing times a seeded factor in [1 - x, 1 + x]. */
    static final double VOTER_SPREAD = 0.35;

    private final Futtoboru game;
    private Function<League, ReElectionRule> rules = this::ruleFromData;

    public ReElectionManager(Futtoboru game) {
        this.game = game;
    }

    /** Tests: the rule of a league (default: its competition record in the game data). */
    public void setRules(Function<League, ReElectionRule> rules) {
        this.rules = rules;
    }

    private SaveGame save() {
        return game.getCurrentGame();
    }

    // ---------------------------------------------------------------------------------------------------- open

    /**
     * Opens the re-election after a completed season (no-op when the rule does not cover it).
     *
     * @param standings the final table, champion first
     * @return true when a vote is now pending
     */
    public boolean open(League league, CompetitionEdition season, List<Club> standings, LocalDateTime now) {
        if (league == null || season == null || season.getReElection() != null || standings == null || now == null) {
            return false;
        }
        ReElectionRule rule = rules.apply(league);
        int startYear = season.getStartDate() != null ? season.getStartDate().getYear() : now.getYear() - 1;
        if (rule == null || !rule.appliesTo(startYear)) {
            return false;
        }
        int places = Math.min(rule.getPlaces(), standings.size() - 1);
        if (places <= 0) {
            return false;
        }
        ReElection vote = new ReElection();
        vote.setPlaces(places);
        vote.setMeeting(now.toLocalDate().plusDays(rule.getMeetingAfterDays()).atStartOfDay());
        for (int i = standings.size() - places; i < standings.size(); i++) {
            vote.getRetiringIds().add(standings.get(i).getId());
        }
        for (Club club : eligibleApplicants(league)) {
            double chance = APPLY_BASE + APPLY_PER_STANDING * Math.min(1.0, standing(club, league, season, now, false));
            RandomSource dice = game.random().fork("reelection:apply:" + season.getId() + ":" + club.getId());
            if (dice.nextDouble() < chance) {
                vote.getApplicantIds().add(club.getId());
            }
        }
        season.setReElection(vote);
        Log.info(Log.ENGINE_AUTHORITY, "ReElectionManager", league.getName() + ": " + places + " clubs seek re-election, "
                + vote.getApplicantIds().size() + " applicants, meeting " + vote.getMeeting().toLocalDate());

        MessageManager messages = game.getGameEngine().getMessageManager();
        messages.deliverMessage(callMessage(league, season, vote));
        Club own = ownClub();
        if (own != null && vote.candidates().contains(own.getId())) {
            boolean member = vote.getRetiringIds().contains(own.getId());
            messages.deliverMessage(noticeMessage(league, season, vote, own, member));
            Message canvass = canvassMessage(league, season, vote, own, now);
            LocalDateTime canvassDay = now.plusDays(Math.max(1, rule.getMeetingAfterDays() / 2));
            if (canvassDay.isBefore(vote.getMeeting())) {
                canvass.setScheduledDate(canvassDay);
                messages.scheduleMessage(canvass);
            } else {
                messages.deliverMessage(canvass);
            }
        }
        return true;
    }

    /** Clubs outside the League, in its country, with a squad that can field an XI; by id. */
    List<Club> eligibleApplicants(League league) {
        Set<Long> members = new LinkedHashSet<>();
        Long countryId = null;
        for (Club c : league.getLeagueClubs()) {
            if (c != null) {
                members.add(c.getId());
                if (countryId == null && c.getCountry() != null) {
                    countryId = c.getCountry().getId();
                }
            }
        }
        Map<Long, Club> out = new LinkedHashMap<>();
        if (save() == null || save().getAllClubs() == null) {
            return new ArrayList<>();
        }
        for (Club c : save().getAllClubs()) {
            if (c == null || c.getId() == null || members.contains(c.getId()) || out.containsKey(c.getId())) {
                continue;
            }
            if (countryId != null && (c.getCountry() == null || !countryId.equals(c.getCountry().getId()))) {
                continue;
            }
            if (c.getPlayers() == null || c.getPlayers().size() < 11 || c.getStadium() == null) {
                continue;
            }
            out.put(c.getId(), c);
        }
        List<Club> list = new ArrayList<>(out.values());
        list.sort(Comparator.comparing(Club::getId));
        return list;
    }

    // ------------------------------------------------------------------------------------------------- meeting

    /** @return true while a completed season of this league waits for its annual meeting */
    public boolean isPending(League league) {
        return pendingSeason(league) != null;
    }

    /** G-04: the same, for callers without the manager (the contract rollover waits for the vote) */
    public static boolean hasPendingMeeting(League league) {
        return pendingSeason(league) != null;
    }

    private static CompetitionEdition pendingSeason(League league) {
        if (league == null || league.getEditions() == null) {
            return null;
        }
        for (CompetitionEdition e : league.getEditions()) {
            if (e != null && e.getReElection() != null && !e.getReElection().isHeld()) {
                return e;
            }
        }
        return null;
    }

    /**
     * Holds every annual meeting that is due today.
     *
     * @return the leagues whose membership was voted on (their next season can now be generated)
     */
    public List<League> holdDueMeetings(List<League> leagues, LocalDateTime now) {
        List<League> held = new ArrayList<>();
        if (leagues == null || now == null) {
            return held;
        }
        for (League league : leagues) {
            CompetitionEdition season = pendingSeason(league);
            if (season != null && !now.isBefore(season.getReElection().getMeeting())) {
                hold(league, season, now);
                held.add(league);
            }
        }
        return held;
    }

    void hold(League league, CompetitionEdition season, LocalDateTime now) {
        ReElection vote = season.getReElection();
        List<Long> candidates = vote.candidates();
        Map<Long, Club> clubs = new LinkedHashMap<>();
        double[] standing = new double[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            Club c = save().getClubById(candidates.get(i));
            clubs.put(candidates.get(i), c);
            standing[i] = c == null ? 0.0
                    : standing(c, league, season, now, vote.getRetiringIds().contains(c.getId()));
        }
        List<Club> voters = new ArrayList<>();
        for (Club c : league.getLeagueClubs()) {
            if (c != null && !vote.getRetiringIds().contains(c.getId())) {
                voters.add(c);
            }
        }
        int[] votes = tally(candidates, standing, voters, vote.getPlaces(), season.getId());

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            order.add(i);
        }
        order.sort((a, b) -> votes[a] != votes[b] ? Integer.compare(votes[b], votes[a])
                : standing[a] != standing[b] ? Double.compare(standing[b], standing[a])
                : Long.compare(candidates.get(a), candidates.get(b)));
        vote.getVotes().clear();
        for (int v : votes) {
            vote.getVotes().add(v);
        }
        vote.getElectedIds().clear();
        for (int k = 0; k < Math.min(vote.getPlaces(), order.size()); k++) {
            vote.getElectedIds().add(candidates.get(order.get(k)));
        }
        vote.setHeld(true);

        // membership for the next season: the losers leave, the elected applicants join
        for (Long id : vote.getRetiringIds()) {
            if (!vote.getElectedIds().contains(id)) {
                Club out = clubs.get(id);
                league.getLeagueClubs().removeIf(c -> c != null && id.equals(c.getId()));
                if (out != null && out.getCurrentLeague() == league) {
                    out.setCurrentLeague(null);
                }
                Log.info(Log.ENGINE_AUTHORITY, "ReElectionManager", (out != null ? out.getName() : id)
                        + " not re-elected: leaves " + league.getName());
            }
        }
        for (Long id : vote.getElectedIds()) {
            Club in = clubs.get(id);
            if (in != null && !vote.getRetiringIds().contains(id)) {
                league.getLeagueClubs().add(in);
                in.setCurrentLeague(league);
                Log.info(Log.ENGINE_AUTHORITY, "ReElectionManager", in.getName() + " elected to " + league.getName());
            }
        }

        MessageManager messages = game.getGameEngine().getMessageManager();
        messages.deliverMessage(meetingMessage(league, season, vote, clubs, order));
        Club own = ownClub();
        if (own != null && candidates.contains(own.getId())) {
            messages.deliverMessage(resultMessage(league, season, vote, own));
        }
    }

    /**
     * The votes: each voter names {@code places} different candidates, drawn one after another with weight
     * (standing x the voter's seeded liking)^2 among those it has not named yet.
     */
    int[] tally(List<Long> candidates, double[] standing, List<Club> voters, int places, Long seasonId) {
        int[] votes = new int[candidates.size()];
        for (Club voter : voters) {
            RandomSource dice = game.random().fork("reelection:vote:" + seasonId + ":" + voter.getId());
            double[] weight = new double[candidates.size()];
            for (int i = 0; i < candidates.size(); i++) {
                double liking = 1.0 + VOTER_SPREAD * (2.0 * dice.nextDouble() - 1.0);
                double w = Math.max(0.01, standing[i] * liking);
                weight[i] = w * w;
            }
            for (int k = 0; k < Math.min(places, candidates.size()); k++) {
                double total = 0;
                for (double w : weight) {
                    total += w;
                }
                double r = dice.nextDouble() * total;
                int pick = -1;
                for (int i = 0; i < weight.length; i++) {
                    if (weight[i] <= 0) {
                        continue;
                    }
                    pick = i;
                    r -= weight[i];
                    if (r < 0) {
                        break;
                    }
                }
                if (pick < 0) {
                    break;
                }
                votes[pick]++;
                weight[pick] = 0;
            }
        }
        return votes;
    }

    // ------------------------------------------------------------------------------------------------ standing

    /**
     * How strong a candidate's case is, about 0..1.3: this season's record (the final table for a member, its
     * played matches for an applicant), gates (average home attendance, else ground size), playing strength (squad
     * attributes), money, and League membership.
     */
    double standing(Club club, League league, CompetitionEdition season, LocalDateTime now, boolean member) {
        double record = member ? tableRecord(club, league, season) : matchRecord(club, season, now);
        double gates = gates(club, season);
        double strength = strength(club);
        double money = club.getClubBalance() == null ? 0.0
                : Math.max(0.0, Math.min(1.0, club.getClubBalance().doubleValue() / 1000.0));
        return 0.05 + 0.35 * record + 0.25 * gates + 0.20 * strength + 0.10 * money + (member ? MEMBER_BONUS : 0.0);
    }

    private static double tableRecord(Club club, League league, CompetitionEdition season) {
        int perWin = league.getRulesOrDefault().getPointsForWin() != null
                ? league.getRulesOrDefault().getPointsForWin() : 2;
        for (LeagueStandingRow row : season.getFinalStandings()) {
            if (club.getId().equals(row.getClubId()) && row.getPlayed() != null && row.getPlayed() > 0) {
                return Math.min(1.0, (row.getPoints() != null ? row.getPoints() : 0) / (double) (perWin * row.getPlayed()));
            }
        }
        return 0.25;
    }

    /** Wins and half the draws of this season's played matches (friendlies and cup ties too). */
    private static double matchRecord(Club club, CompetitionEdition season, LocalDateTime now) {
        int played = 0;
        double points = 0;
        if (club.getPlayedMatches() != null) {
            for (Match m : club.getPlayedMatches()) {
                if (m == null || !Boolean.TRUE.equals(m.getIsPlayed()) || m.getHomeGoals() == null
                        || m.getAwayGoals() == null || !inSeason(m.getMatchDateTime(), season, now)) {
                    continue;
                }
                boolean home = club.getId().equals(m.getHomeClubId());
                int us = home ? m.getHomeGoals() : m.getAwayGoals();
                int them = home ? m.getAwayGoals() : m.getHomeGoals();
                played++;
                points += us > them ? 1.0 : us == them ? 0.5 : 0.0;
            }
        }
        return played == 0 ? 0.25 : points / played;
    }

    private static boolean inSeason(LocalDateTime date, CompetitionEdition season, LocalDateTime now) {
        if (date == null) {
            return false;
        }
        LocalDateTime from = season.getStartDate() != null ? season.getStartDate().minusMonths(6) : null;
        return (from == null || !date.isBefore(from)) && (now == null || !date.isAfter(now));
    }

    /** Average home gate this season against 8,000; without gates the ground's size against 20,000 (scaled). */
    private static double gates(Club club, CompetitionEdition season) {
        long total = 0;
        int n = 0;
        if (club.getMatchIncomes() != null) {
            for (MatchIncome income : club.getMatchIncomes()) {
                if (income != null && income.getAttendance() != null && income.getAttendance() > 0
                        && club.getId().equals(income.getClubId())
                        && inSeason(income.getMatchDate(), season, null)) {
                    total += income.getAttendance();
                    n++;
                }
            }
        }
        if (n > 0) {
            return Math.min(1.0, total / (double) n / 8000.0);
        }
        if (club.getStadium() != null && club.getStadium().getCapacity() != null) {
            return 0.7 * Math.min(1.0, club.getStadium().getCapacity() / 20000.0);
        }
        return 0.1;
    }

    /** Average of the main attributes over the squad, on the 3-23 scale, as 0..1. */
    private static double strength(Club club) {
        double sum = 0;
        int n = 0;
        for (Player p : club.getPlayers()) {
            if (p == null) {
                continue;
            }
            Number[] values = { p.getAcceleration(), p.getSpeed(), p.getStamina(), p.getStrength(), p.getPassing(),
                    p.getKicking(), p.getTackling(), p.getPositioning() };
            for (Number v : values) {
                if (v != null) {
                    sum += v.doubleValue();
                    n++;
                }
            }
        }
        return n == 0 ? 0.3 : Math.max(0.0, Math.min(1.0, (sum / n - 3.0) / 20.0));
    }

    // ------------------------------------------------------------------------------------------------- the rule

    /** The rule from the data: the League's competition record (by the game's competition of the same name). */
    private ReElectionRule ruleFromData(League league) {
        String dataId = null;
        if (save() != null && save().getAllLeagues() != null) {
            for (Competition c : save().getAllLeagues()) {
                if (c != null && c.getDataId() != null && c.getName() != null && c.getName().equals(league.getName())) {
                    dataId = c.getDataId();
                    break;
                }
            }
        }
        if (dataId == null) {
            for (Competition c : DatabaseLoader.getCompetitions()) {
                if (c != null && c.getDataId() != null && c.getName() != null && c.getName().equals(league.getName())) {
                    dataId = c.getDataId();
                    break;
                }
            }
        }
        return ReElectionRule.of(DatabaseLoader.getGameData(), dataId);
    }

    // ------------------------------------------------------------------------------------------------ messages

    private Club ownClub() {
        SaveGame save = save();
        return save != null ? save.getCurrentClub() : null;
    }

    private String names(List<Long> ids) {
        List<String> out = new ArrayList<>();
        for (Long id : ids) {
            Club c = save().getClubById(id);
            out.add(c != null ? c.getName() : String.valueOf(id));
        }
        return out.isEmpty() ? LanguageModLoader.getValue("msg_reelection_none") : String.join(", ", out);
    }

    private static String seasonName(League league, CompetitionEdition season) {
        return season.getName() != null ? season.getName() : league.getName();
    }

    private static String day(LocalDateTime date) {
        return MessageManager.formatDay(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private static Message message(MessageCategory category, String type, MessagePriority priority, String title,
            String text) {
        Message m = new Message();
        m.setCategory(category);
        m.setMessageType(type);
        m.setPriority(priority);
        m.setTitle(title);
        m.setPlainTextMessage(text);
        m.setIsRead(false);
        m.setIsDeleted(false);
        return m;
    }

    private Message callMessage(League league, CompetitionEdition season, ReElection vote) {
        String text = LanguageModLoader.format("msg_reelection_call_text", league.getName(), vote.getPlaces(),
                names(vote.getRetiringIds()), day(vote.getMeeting())) + "\n\n"
                + LanguageModLoader.format("msg_reelection_applicants", names(vote.getApplicantIds()));
        return message(MessageCategory.AUTHORITY, MSG_CALL, MessagePriority.NORMAL,
                LanguageModLoader.format("msg_reelection_call_title", seasonName(league, season)), text);
    }

    private Message noticeMessage(League league, CompetitionEdition season, ReElection vote, Club own,
            boolean member) {
        String text = member
                ? LanguageModLoader.format("msg_reelection_notice_member", own.getName(), league.getName(),
                        day(vote.getMeeting()))
                : LanguageModLoader.format("msg_reelection_notice_applicant", own.getName(), league.getName(),
                        day(vote.getMeeting()));
        return message(MessageCategory.AUTHORITY, MSG_NOTICE, MessagePriority.URGENT,
                LanguageModLoader.format("msg_reelection_notice_title", own.getName(), seasonName(league, season)),
                text);
    }

    /** The secretary's canvass: how our standing ranks among the candidates (no dice, no peeking at the vote). */
    private Message canvassMessage(League league, CompetitionEdition season, ReElection vote, Club own,
            LocalDateTime now) {
        List<Long> candidates = vote.candidates();
        double ours = standing(own, league, season, now, vote.getRetiringIds().contains(own.getId()));
        int ahead = 0;
        for (Long id : candidates) {
            Club c = save().getClubById(id);
            if (c != null && !id.equals(own.getId())
                    && standing(c, league, season, now, vote.getRetiringIds().contains(id)) > ours) {
                ahead++;
            }
        }
        String outlook = ahead < vote.getPlaces() - 1 ? "msg_reelection_canvass_good"
                : ahead < vote.getPlaces() + 1 ? "msg_reelection_canvass_close" : "msg_reelection_canvass_poor";
        String text = LanguageModLoader.format("msg_reelection_canvass_text", candidates.size(), vote.getPlaces())
                + "\n\n" + LanguageModLoader.getValue(outlook);
        return message(MessageCategory.AUTHORITY, MSG_CANVASS, MessagePriority.NORMAL,
                LanguageModLoader.format("msg_reelection_canvass_title", own.getName(), seasonName(league, season)),
                text);
    }

    private Message meetingMessage(League league, CompetitionEdition season, ReElection vote, Map<Long, Club> clubs,
            List<Integer> order) {
        List<Long> candidates = vote.candidates();
        StringBuilder text = new StringBuilder(LanguageModLoader.format("msg_reelection_meeting_text",
                league.getName(), names(vote.getElectedIds()))).append("\n\n");
        for (int i : order) {
            Club c = clubs.get(candidates.get(i));
            text.append(LanguageModLoader.format("msg_reelection_votes_row", c != null ? c.getName() : candidates.get(i),
                    vote.getVotes().get(i))).append("\n");
        }
        return message(MessageCategory.AUTHORITY, MSG_MEETING, MessagePriority.NORMAL,
                LanguageModLoader.format("msg_reelection_meeting_title", seasonName(league, season)), text.toString());
    }

    private Message resultMessage(League league, CompetitionEdition season, ReElection vote, Club own) {
        boolean member = vote.getRetiringIds().contains(own.getId());
        boolean elected = vote.getElectedIds().contains(own.getId());
        String key = member ? (elected ? "msg_reelection_result_reelected" : "msg_reelection_result_out")
                : (elected ? "msg_reelection_result_elected" : "msg_reelection_result_rejected");
        int i = vote.candidates().indexOf(own.getId());
        String text = LanguageModLoader.format(key, own.getName(), league.getName()) + "\n\n"
                + LanguageModLoader.format("msg_reelection_result_votes", vote.getVotes().get(i));
        return message(MessageCategory.AUTHORITY, MSG_RESULT, MessagePriority.URGENT,
                LanguageModLoader.format("msg_reelection_result_title", own.getName(), seasonName(league, season)),
                text);
    }
}
