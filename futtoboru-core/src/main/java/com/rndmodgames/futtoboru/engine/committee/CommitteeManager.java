package com.rndmodgames.futtoboru.engine.committee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.ClubCommittee;
import com.rndmodgames.futtoboru.data.CommitteeMember;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.LeagueStandingRow;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.MatchIncome;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.recruitment.WageRecord;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.generators.NameGenerator;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * G-05: every club's committee, every game day (D31: AI clubs the same as the player's).
 *
 * <ul>
 * <li>A club gets its committee on its first game day: a president, a treasurer and two to four committee men,
 * seeded per club ({@code fork("committee:<club>")}).</li>
 * <li>At the start of each season (the calendar's April turn once the League table is final, or the first game
 * day) it sets the season's expectations from
 * the club's size (ground and balance, ranked in its League), last season's place and its balance: finish in the
 * top N, keep the balance in credit (or reduce the debt), a Cup tie for the leading clubs. The player's club gets
 * the letter.</li>
 * <li>Each week the confidence in the secretary(-manager) moves with the week's results, the League place against
 * the target, the gates and the balance (a little seeded mood, {@code fork("committee-week:<club>:<day>")}). When
 * the League season is over (a club outside the League: when the calendar turns) the expectations are judged.</li>
 * <li>Low confidence brings a warning, then a final warning, then dismissal: the secretary leaves his post (the
 * J-01 job market advertises it and an AI club appoints at the advert's deadline); the player becomes a job
 * seeker.</li>
 * </ul>
 * All thresholds and weights are placeholders (D30): nothing about committee tempers is sourced yet.
 */
public final class CommitteeManager {

    public static final String MSG_EXPECTATIONS = "COMMITTEE_EXPECTATIONS";
    public static final String MSG_PRAISE = "COMMITTEE_PRAISE";
    public static final String MSG_WARNING = "COMMITTEE_WARNING";
    public static final String MSG_FINAL_WARNING = "COMMITTEE_FINAL_WARNING";
    public static final String MSG_RELIEVED = "COMMITTEE_RELIEVED";
    public static final String MSG_REVIEW = "COMMITTEE_REVIEW";
    public static final String MSG_DISMISSAL = "COMMITTEE_DISMISSAL";

    /** Placeholders (D30). */
    static final int START_CONFIDENCE = 60;
    /** Each week the confidence moves 1/{@link #DRIFT_DIVISOR} of the way back to this. */
    static final int NEUTRAL_CONFIDENCE = 50;
    static final int DRIFT_DIVISOR = 8;
    static final int REVIEW_DAYS = 7;
    static final int WARNING_BELOW = 40;
    static final int FINAL_WARNING_BELOW = 32;
    static final int FINAL_WARNING_AFTER_DAYS = 21;
    static final int DISMISS_BELOW = 25;
    static final int DISMISS_AFTER_DAYS = 14;
    static final int RELIEVED_AT = 45;
    static final int PRAISE_AT = 80;
    static final int PRAISE_EVERY_DAYS = 56;
    static final int LEAGUE_GAMES_BEFORE_PLACE_COUNTS = 4;
    /** A League season still running when the calendar turns is waited for until this month (July). */
    static final int LAST_MONTH_TO_WAIT = 7;
    /** A club outside the League sets its season's expectations on the first of this month (August). */
    static final int MEETING_MONTH_OUTSIDE_LEAGUE = 8;

    static final String[] OCCUPATIONS = { "mill_owner", "publican", "brewer", "solicitor", "schoolmaster", "doctor",
            "merchant", "manufacturer", "clergyman", "shopkeeper", "colliery_agent", "builder" };

    private CommitteeManager() {
    }

    /** One game day for every club (after the day's competitions, before the job market's day). */
    public static void daily(SaveGame game, RandomSource root, ClubStaffManager staff, MessageManager messages) {
        if (game == null || game.getGameDate() == null || game.getAllClubs() == null || root == null) {
            return;
        }
        Profession secretaryPost = DatabaseLoader.getProfessionByDataId(Profession.MANAGER);
        Map<Long, List<Club>> tables = new HashMap<>();
        for (Club club : new ArrayList<>(game.getAllClubs())) {
            if (club != null && club.getId() != null) {
                day(game, club, root, staff, secretaryPost, messages, tables);
            }
        }
    }

    static void day(SaveGame game, Club club, RandomSource root, ClubStaffManager staff, Profession secretaryPost,
            MessageManager messages, Map<Long, List<Club>> tables) {
        LocalDateTime today = game.getGameDate();
        ClubCommittee c = ensure(game, club, root);
        boolean ours = club == game.getCurrentClub();
        int year = footballYear(today);
        League league = leagueOf(game, club);

        Long holder = secretaryPost != null ? club.getStaffId(secretaryPost.getId()) : null;
        if (!Objects.equals(holder, c.getSecretaryId())) {
            // a new secretary starts afresh
            c.setSecretaryId(holder);
            c.setSecretarySince(holder != null ? today : null);
            c.setConfidence(START_CONFIDENCE);
            c.setWarnings(0);
            c.setLastWarning(null);
        }

        boolean newSeason = c.getSeasonYear() == null || c.getSeasonYear() < year;
        if (newSeason && c.getSeasonYear() != null && !Objects.equals(c.getReviewedYear(), c.getSeasonYear())) {
            if (leagueSeasonPending(league, c.getSeasonYear()) && today.getMonthValue() < LAST_MONTH_TO_WAIT) {
                newSeason = false; // the League season runs into April: judge it when its table is final
            } else {
                review(game, club, c, today, ours, messages);
            }
        }
        if (newSeason && readyForExpectations(league, year, today)) {
            if (c.getSeasonYear() != null) {
                c.setConfidence(50 + (c.getConfidence() - 50) / 2); // a new season tempers the mood
            }
            setExpectations(game, club, c, year, today);
            c.setLastReview(today);
            if (ours && messages != null) {
                messages.deliverMessage(expectationsLetter(game, club, c, today));
            }
        } else if (!newSeason && !Objects.equals(c.getReviewedYear(), c.getSeasonYear())
                && leagueSeasonOver(league, c.getSeasonYear())) {
            review(game, club, c, today, ours, messages);
        }

        if (c.getLastReview() == null) {
            c.setLastReview(today);
        }
        if (ChronoUnit.DAYS.between(c.getLastReview(), today) < REVIEW_DAYS) {
            return;
        }
        LocalDateTime from = c.getLastReview();
        c.setLastReview(today);
        if (holder == null) {
            return; // nobody to judge
        }
        int delta = weeklyDelta(club, league, c, from, today, tables);
        int mood = root.fork("committee-week:" + key(club) + ":" + today.toLocalDate()).nextInt(3) - 1;
        // a quiet week: the committee drifts back towards an even temper
        int drift = (int) Math.round((NEUTRAL_CONFIDENCE - c.getConfidence()) / (double) DRIFT_DIVISOR);
        c.setConfidence(clamp(c.getConfidence() + delta + mood + drift));
        judge(game, club, c, today, ours, staff, secretaryPost, messages);
    }

    // ------------------------------------------------------------------------------------------ the committee

    /** @return the club's committee, made on its first day (seeded members) */
    public static ClubCommittee ensure(SaveGame game, Club club, RandomSource root) {
        if (club.getCommittee() != null) {
            return club.getCommittee();
        }
        ClubCommittee c = new ClubCommittee();
        RandomSource rng = root.fork("committee:" + key(club));
        Country country = club.getCountry();
        if (country == null || DatabaseLoader.getFirstNames(country).size() < 2
                || DatabaseLoader.getLastNames(country).size() < 2) {
            country = DatabaseLoader.getCountryByDataId("england");
        }
        int thisYear = game.getGameDate().getYear();
        int men = 4 + rng.nextInt(3); // president, treasurer, 2-4 committee men
        for (int i = 0; i < men; i++) {
            CommitteeMember m = new CommitteeMember();
            String[] name = NameGenerator.generateName(country, rng);
            m.setName(name[0]);
            m.setLastname(name[1]);
            m.setRole(i == 0 ? CommitteeMember.PRESIDENT : i == 1 ? CommitteeMember.TREASURER : CommitteeMember.MEMBER);
            m.setOccupation(OCCUPATIONS[rng.nextInt(OCCUPATIONS.length)]);
            m.setBirthYear(thisYear - 32 - rng.nextInt(34));
            m.setPatience(round2(rng.nextDouble()));
            m.setGenerosity(round2(rng.nextDouble()));
            c.getMembers().add(m);
        }
        c.setConfidence(START_CONFIDENCE);
        c.setWarnings(0);
        club.setCommittee(c);
        return c;
    }

    /** The season a day belongs to, as the calendar has it (April turns it): 1888 = the 1888-89 season. */
    public static int footballYear(LocalDateTime day) {
        return SeasonCalendar.startYearOf(day.toLocalDate());
    }

    // ------------------------------------------------------------------------------------------ expectations

    static void setExpectations(SaveGame game, Club club, ClubCommittee c, int year, LocalDateTime today) {
        c.setSeasonYear(year);
        BigDecimal balance = balance(club);
        c.setStartBalance(balance);
        c.setKeepBalancePositive(balance.signum() >= 0);

        League league = leagueOf(game, club);
        List<Club> members = league != null && league.getLeagueClubs() != null ? league.getLeagueClubs() : List.of();
        if (league == null) {
            c.setTargetPosition(null);
            c.setLeagueSize(null);
            c.setStanding("small");
            c.setCupTiesExpected(0);
        } else {
            int n = members.size();
            int sizeRank = 1;
            double size = size(club);
            for (Club other : members) {
                if (other != null && !club.getId().equals(other.getId()) && size(other) > size) {
                    sizeRank++;
                }
            }
            Integer last = lastPosition(league, club, year - 1);
            int base = last != null ? (int) Math.round((sizeRank + last) / 2.0) : sizeRank;
            c.setTargetPosition(Math.max(1, Math.min(n, base + 2)));
            c.setLeagueSize(n);
            c.setStanding(sizeRank * 3 <= n ? "leading" : sizeRank * 3 <= 2 * n ? "middling" : "small");
            c.setCupTiesExpected("leading".equals(c.getStanding()) ? 1 : 0);
        }
        WageRecord band = DatabaseLoader.getRecruitmentData() != null
                ? DatabaseLoader.getRecruitmentData().band(c.getStanding(), today.toLocalDate()) : null;
        c.setWageBandPence(band != null && band.getMaxWeeklyPence() != null ? band.getMaxWeeklyPence()
                : Committee.DEFAULT_BAND_PENCE);
        Log.info(Log.ENGINE_GAME, "CommitteeManager", club.getName() + " " + Season.label(year) + ": top "
                + c.getTargetPosition() + ", balance " + balance + ", cup " + c.getCupTiesExpected());
    }

    /**
     * The committee meets for a season once the club's League season of that year exists (the League is formed,
     * or the next season drawn up after the annual meeting), or, for a club outside the League, on 1 August.
     */
    static boolean readyForExpectations(League league, int year, LocalDateTime today) {
        return edition(league, year) != null
                || !today.toLocalDate().isBefore(java.time.LocalDate.of(year, MEETING_MONTH_OUTSIDE_LEAGUE, 1));
    }

    /** The club's size for its expectations: its ground and its balance (placeholder weights, D30). */
    static double size(Club club) {
        int capacity = club.getStadium() != null && club.getStadium().getCapacity() != null
                ? club.getStadium().getCapacity() : 0;
        return capacity + 20.0 * balance(club).doubleValue();
    }

    static Integer lastPosition(League league, Club club, int year) {
        CompetitionEdition edition = edition(league, year);
        if (edition == null) {
            return null;
        }
        for (LeagueStandingRow row : edition.getFinalStandings()) {
            if (row != null && club.getId().equals(row.getClubId())) {
                return row.getPosition();
            }
        }
        return null;
    }

    static CompetitionEdition edition(League league, int year) {
        if (league == null || league.getEditions() == null) {
            return null;
        }
        for (CompetitionEdition e : league.getEditions()) {
            if (e != null && e.getStartDate() != null && footballYear(e.getStartDate()) == year) {
                return e;
            }
        }
        return null;
    }

    static boolean leagueSeasonOver(League league, int year) {
        CompetitionEdition e = edition(league, year);
        return e != null && !e.getFinalStandings().isEmpty();
    }

    /** @return true while the club's League season of that year has no final table yet */
    static boolean leagueSeasonPending(League league, int year) {
        CompetitionEdition e = edition(league, year);
        return e != null && e.getFinalStandings().isEmpty();
    }

    // ------------------------------------------------------------------------------------------ the week

    /** Results, the League place, gates and the balance since {@code from} (placeholder weights, D30). */
    static int weeklyDelta(Club club, League league, ClubCommittee c, LocalDateTime from, LocalDateTime to,
            Map<Long, List<Club>> tables) {
        double up = 0;
        double down = 0;
        for (Match m : club.getPlayedMatches()) {
            if (m == null || m.getMatchDateTime() == null || !m.getMatchDateTime().isAfter(from)
                    || m.getMatchDateTime().isAfter(to) || m.getHomeGoals() == null || m.getAwayGoals() == null) {
                continue;
            }
            boolean home = club.getId().equals(m.getHomeClubId());
            int margin = home ? m.getHomeGoals() - m.getAwayGoals() : m.getAwayGoals() - m.getHomeGoals();
            int weight = m.getMatchType() != null && m.getMatchType() == Match.FRIENDLY_MATCH ? 1 : 3;
            if (margin > 0) {
                up += weight;
            } else if (margin < 0) {
                down += weight + (margin <= -4 ? 2 : 0);
            }
        }
        // the League place against the target
        if (c.getTargetPosition() != null && league != null && club.getMatchesPlayed() != null
                && club.getMatchesPlayed() >= LEAGUE_GAMES_BEFORE_PLACE_COUNTS) {
            List<Club> table = tables.computeIfAbsent(league.getId(),
                    id -> new LeagueStandingsManager().calculateStandings(league));
            int place = 0;
            for (int i = 0; i < table.size(); i++) {
                if (club.getId().equals(table.get(i).getId())) {
                    place = i + 1;
                }
            }
            if (place > c.getTargetPosition()) {
                down += Math.min(2, place - c.getTargetPosition());
            } else if (place > 0) {
                up += 1;
            }
        }
        // home gates against the ground
        int capacity = club.getStadium() != null && club.getStadium().getCapacity() != null
                ? club.getStadium().getCapacity() : 0;
        for (MatchIncome income : club.getMatchIncomes()) {
            if (income == null || income.getAttendance() == null || income.getMatchDate() == null || capacity <= 0
                    || !club.getId().equals(income.getClubId()) || !income.getMatchDate().isAfter(from)
                    || income.getMatchDate().isAfter(to)) {
                continue;
            }
            if (income.getAttendance() / (double) capacity >= 0.6) {
                up += 1; // a big gate pleases; a small one is not held against him (grounds were rarely full)
            }
        }
        // the balance
        BigDecimal balance = balance(club);
        if (c.isKeepBalancePositive() && balance.signum() < 0) {
            down += 1; // a club in debt from the start is judged on the debt at the season's end only
        }
        return (int) Math.round(up - down * severity(c));
    }

    /** Impatient committees take bad news harder: 0.75 (all patient) to 1.25. */
    static double severity(ClubCommittee c) {
        if (c.getMembers().isEmpty()) {
            return 1.0;
        }
        double patience = 0;
        for (CommitteeMember m : c.getMembers()) {
            patience += m.getPatience();
        }
        return 1.25 - 0.5 * patience / c.getMembers().size();
    }

    /** Praise, warnings and dismissal after the week's confidence. */
    static void judge(SaveGame game, Club club, ClubCommittee c, LocalDateTime today, boolean ours,
            ClubStaffManager staff, Profession secretaryPost, MessageManager messages) {
        int confidence = c.getConfidence();
        int warnings = c.getWarnings();
        long sinceWarning = c.getLastWarning() != null ? ChronoUnit.DAYS.between(c.getLastWarning(), today)
                : Long.MAX_VALUE;
        Person secretary = person(game, c.getSecretaryId());
        String name = secretary != null ? fullName(secretary) : "";

        if (warnings >= 2 && confidence < DISMISS_BELOW && sinceWarning >= DISMISS_AFTER_DAYS) {
            dismiss(game, club, c, today, ours, staff, secretaryPost, secretary, messages);
            return;
        }
        if (warnings == 1 && confidence < FINAL_WARNING_BELOW && sinceWarning >= FINAL_WARNING_AFTER_DAYS) {
            c.setWarnings(2);
            c.setLastWarning(today);
            letterIf(ours, messages, game, club, c, MSG_FINAL_WARNING, today, "msg_committee_final_warning", name);
            return;
        }
        if (warnings == 0 && confidence < WARNING_BELOW) {
            c.setWarnings(1);
            c.setLastWarning(today);
            letterIf(ours, messages, game, club, c, MSG_WARNING, today, "msg_committee_warning", name);
            return;
        }
        if (warnings > 0 && confidence >= RELIEVED_AT) {
            c.setWarnings(0);
            c.setLastWarning(null);
            letterIf(ours, messages, game, club, c, MSG_RELIEVED, today, "msg_committee_relieved", name);
            return;
        }
        if (confidence >= PRAISE_AT && (c.getLastPraise() == null
                || ChronoUnit.DAYS.between(c.getLastPraise(), today) >= PRAISE_EVERY_DAYS)) {
            c.setLastPraise(today);
            letterIf(ours, messages, game, club, c, MSG_PRAISE, today, "msg_committee_praise", name);
        }
    }

    /** The secretary leaves his post: out of work, a candidate for other clubs; the post goes to the job market. */
    static void dismiss(SaveGame game, Club club, ClubCommittee c, LocalDateTime today, boolean ours,
            ClubStaffManager staff, Profession secretaryPost, Person secretary, MessageManager messages) {
        Message letter = ours && messages != null ? letter(game, club, c, MSG_DISMISSAL, today,
                "msg_committee_dismissal", secretary != null ? fullName(secretary) : "") : null;
        if (letter != null) {
            letter.setPriority(MessagePriority.URGENT);
        }
        if (staff != null && secretaryPost != null) {
            staff.fireStaff(club, secretaryPost);
        } else {
            club.setStaffId(secretaryPost != null ? secretaryPost.getId() : null, null);
            if (secretary != null) {
                secretary.setCurrentClubId(null);
            }
        }
        if (c.getSecretaryId() != null && !c.getDismissedIds().contains(c.getSecretaryId())) {
            c.getDismissedIds().add(c.getSecretaryId());
        }
        Log.info(Log.ENGINE_JOBS, "CommitteeManager", "The committee of " + club.getName() + " dismissed "
                + (secretary != null ? fullName(secretary) : c.getSecretaryId()) + " (confidence "
                + c.getConfidence() + ")");
        c.setSecretaryId(null);
        c.setSecretarySince(null);
        c.setConfidence(START_CONFIDENCE);
        c.setWarnings(0);
        c.setLastWarning(null);
        if (letter != null) {
            messages.deliverMessage(letter);
        }
    }

    // ------------------------------------------------------------------------------------------ the season

    /** The season's expectations judged: confidence moves, the player's club gets the review. */
    static void review(SaveGame game, Club club, ClubCommittee c, LocalDateTime today, boolean ours,
            MessageManager messages) {
        int year = c.getSeasonYear();
        c.setReviewedYear(year);
        LocalDateTime from = LocalDateTime.of(year, 4, 1, 0, 0); // the calendar's season turn (CalendarRules)
        StringBuilder lines = new StringBuilder();
        int delta = 0;

        if (c.getTargetPosition() != null) {
            Integer place = lastPosition(leagueOf(game, club), club, year);
            if (place != null) {
                boolean met = place <= c.getTargetPosition();
                delta += met ? 8 : -Math.min(16, 6 + 2 * (place - c.getTargetPosition()));
                lines.append(LanguageModLoader.format(met ? "committee_review_league_met" : "committee_review_league_missed",
                        place, c.getLeagueSize() != null ? c.getLeagueSize() : place, c.getTargetPosition()))
                        .append('\n');
            }
        }
        BigDecimal balance = balance(club);
        boolean balanceMet = c.isKeepBalancePositive() ? balance.signum() >= 0
                : balance.compareTo(c.getStartBalance()) >= 0;
        delta += balanceMet ? 3 : -5;
        lines.append(LanguageModLoader.format(balanceMet ? "committee_review_balance_met" : "committee_review_balance_missed",
                Money.format(balance))).append('\n');
        if (c.getCupTiesExpected() > 0) {
            int played = 0;
            int won = 0;
            for (Match m : club.getPlayedMatches()) {
                if (m == null || m.getMatchType() == null || m.getMatchType() != Match.CUP_MATCH
                        || m.getMatchDateTime() == null || m.getMatchDateTime().isBefore(from)
                        || m.getMatchDateTime().isAfter(today) || m.getHomeGoals() == null || m.getAwayGoals() == null) {
                    continue;
                }
                played++;
                boolean home = club.getId().equals(m.getHomeClubId());
                if (home ? m.getHomeGoals() > m.getAwayGoals() : m.getAwayGoals() > m.getHomeGoals()) {
                    won++;
                }
            }
            if (played > 0) {
                boolean met = won >= c.getCupTiesExpected();
                delta += met ? 3 : -4;
                lines.append(LanguageModLoader.format(met ? "committee_review_cup_met" : "committee_review_cup_missed",
                        won)).append('\n');
            }
        }
        c.setConfidence(clamp(c.getConfidence() + delta));
        Log.info(Log.ENGINE_GAME, "CommitteeManager", club.getName() + " " + Season.label(year) + " judged: "
                + delta + ", confidence " + c.getConfidence() + ", balance " + c.getStartBalance() + " -> " + balance);
        if (ours && messages != null) {
            Message m = letter(game, club, c, MSG_REVIEW, today, "msg_committee_review", Season.label(year),
                    lines.toString().trim(), LanguageModLoader.getValue(Committee.confidenceKey(c.getConfidence())));
            messages.deliverMessage(m);
        }
    }

    // ------------------------------------------------------------------------------------------ letters

    /** The expectations, one line each, in the game language. */
    public static List<String> expectationLines(SaveGame game, ClubCommittee c, Club club) {
        List<String> lines = new ArrayList<>();
        if (c == null || c.getSeasonYear() == null) {
            return lines;
        }
        League league = club != null ? leagueOf(game, club) : null;
        if (c.getTargetPosition() == null) {
            lines.add(LanguageModLoader.getValue("committee_exp_no_league"));
        } else if (c.getLeagueSize() != null && c.getTargetPosition() >= c.getLeagueSize()) {
            lines.add(LanguageModLoader.getValue("committee_exp_league_any"));
        } else {
            lines.add(LanguageModLoader.format("committee_exp_league", c.getTargetPosition(),
                    league != null ? league.getName() : ""));
        }
        lines.add(LanguageModLoader.format(c.isKeepBalancePositive() ? "committee_exp_balance_positive"
                : "committee_exp_balance_debt", Money.format(c.getStartBalance())));
        if (c.getCupTiesExpected() > 0) {
            lines.add(LanguageModLoader.getValue("committee_exp_cup"));
        }
        lines.add(LanguageModLoader.format("committee_exp_wage_band",
                Money.format(Committee.bandWeekly(club))));
        return lines;
    }

    static Message expectationsLetter(SaveGame game, Club club, ClubCommittee c, LocalDateTime today) {
        StringBuilder lines = new StringBuilder();
        for (String line : expectationLines(game, c, club)) {
            lines.append("- ").append(line).append('\n');
        }
        Person secretary = person(game, c.getSecretaryId());
        return letter(game, club, c, MSG_EXPECTATIONS, today, "msg_committee_expectations",
                secretary != null ? fullName(secretary) : "", Season.label(c.getSeasonYear()),
                lines.toString().trim());
    }

    private static void letterIf(boolean ours, MessageManager messages, SaveGame game, Club club, ClubCommittee c,
            String type, LocalDateTime today, String key, String name) {
        if (ours && messages != null) {
            messages.deliverMessage(letter(game, club, c, type, today, key, name));
        }
    }

    /**
     * A committee letter: {@code <key>_title} ({0} club, {1} date) and {@code <key>_text} ({0} club, {1} president,
     * then {@code args} from {2}).
     */
    static Message letter(SaveGame game, Club club, ClubCommittee c, String type, LocalDateTime today, String key,
            Object... args) {
        CommitteeMember president = c.member(CommitteeMember.PRESIDENT);
        Object[] all = new Object[args.length + 2];
        all[0] = club.getName();
        all[1] = president != null ? president.getFullName() : "";
        System.arraycopy(args, 0, all, 2, args.length);
        Message m = new Message();
        m.setCategory(MessageCategory.JOB);
        m.setMessageType(type);
        m.setPriority(MessageType.isWarning(type) ? MessagePriority.URGENT : MessagePriority.NORMAL);
        m.setTitle(LanguageModLoader.format(key + "_title", club.getName(), GameFormat.mediumDate(today.toLocalDate())));
        m.setPlainTextMessage(LanguageModLoader.format(key + "_text", all));
        m.setActionData(club.getId());
        m.setIsRead(false);
        m.setIsDeleted(false);
        return m;
    }

    private static final class MessageType {
        static boolean isWarning(String type) {
            return MSG_WARNING.equals(type) || MSG_FINAL_WARNING.equals(type) || MSG_DISMISSAL.equals(type);
        }
    }

    // ------------------------------------------------------------------------------------------ helpers

    /**
     * @return the League the club plays in: the main authority's League that lists it ({@code Club.currentLeague}
     *         is not set for the start season's clubs), or null
     */
    public static League leagueOf(SaveGame game, Club club) {
        League current = club.getCurrentLeague();
        if (current != null && lists(current, club)) {
            return current;
        }
        if (game == null || game.getMainAuthority() == null || game.getMainAuthority().getLeagues() == null) {
            return null;
        }
        for (League league : game.getMainAuthority().getLeagues()) {
            if (league != null && lists(league, club)) {
                return league;
            }
        }
        return null;
    }

    private static boolean lists(League league, Club club) {
        if (league.getLeagueClubs() == null) {
            return false;
        }
        for (Club member : league.getLeagueClubs()) {
            if (member != null && club.getId().equals(member.getId())) {
                return true;
            }
        }
        return false;
    }

    static String key(Club club) {
        return club.getDataId() != null ? club.getDataId() : String.valueOf(club.getId());
    }

    static BigDecimal balance(Club club) {
        return club.getClubBalance() != null ? club.getClubBalance() : BigDecimal.ZERO;
    }

    static int clamp(int confidence) {
        return Math.max(0, Math.min(100, confidence));
    }

    private static double round2(double v) {
        return Math.round(v * 100) / 100.0;
    }

    static Person person(SaveGame game, Long id) {
        if (id == null) {
            return null;
        }
        for (Person p : game.getAllPersons()) {
            if (p != null && id.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }

    static String fullName(Person p) {
        return ((p.getName() != null ? p.getName() : "") + " " + (p.getLastname() != null ? p.getLastname() : "")).trim();
    }
}
