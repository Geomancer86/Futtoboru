package com.rndmodgames.futtoboru.engine.temporal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.engine.messages.MessageManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Friendly matches (U-08). Friendlies filled most Saturdays of a club's year in 1888-89: the secretary wrote to
 * another club, which answered by letter.
 *
 * <ol>
 * <li>{@link #propose}: the manager picks an opponent, a day at least {@link #MIN_NOTICE_DAYS} ahead and the ground.
 * The match waits in the proposing club's proposed list.</li>
 * <li>{@link #answerProposals} (each day, from {@link MatchScheduler#checkClubProposedMatches}): the day after the
 * letter the other club accepts or declines with a reason, and the manager gets a letter in the inbox. An accepted
 * friendly goes to both clubs' scheduled lists and is played like any other match through
 * {@code MatchResultRecorder} (which leaves the league table alone). The home club sells the tickets
 * ({@link MatchScheduler#checkClubSheduledMatches}).</li>
 * <li>{@link #cancelClashes}: a friendly is called off when a league or cup match is fixed for one of its clubs on
 * the same day (cup ties and league fixtures come first).</li>
 * </ol>
 * The answer comes from {@code RandomSource.fork("friendly:<match id>")}, so the same seed gives the same answers.
 * <p>
 * G-13: AI clubs write to each other through the same rules ({@link AiFriendlyPlanner}). When an AI club invites the
 * manager's club, the manager gets an invitation letter ({@link #MESSAGE_INVITATION}) and answers it from the inbox
 * ({@link #answerInvitation}); an invitation still unanswered two days before the match lapses. The visitors of a
 * friendly are paid a guarantee or a share of the gate ({@code FriendlyGuarantee}).
 */
public class FriendlyArranger {

    /** Chance that a club with a free day accepts. */
    public static final double DEFAULT_ACCEPT_CHANCE = 0.8;
    /** The letter needs a day to arrive and a day to be answered. */
    public static final int MIN_NOTICE_DAYS = 2;
    /** Saturday afternoon kick-off. */
    public static final int KICK_OFF_HOUR = 15;

    public static final String MESSAGE_ACCEPTED = "FRIENDLY_ACCEPTED";
    public static final String MESSAGE_DECLINED = "FRIENDLY_DECLINED";
    public static final String MESSAGE_CANCELLED = "FRIENDLY_CANCELLED";
    /** G-13: an AI club invites the manager's club (actionData = match id); answered from the inbox. */
    public static final String MESSAGE_INVITATION = "FRIENDLY_INVITATION";
    public static final String MESSAGE_LAPSED = "FRIENDLY_LAPSED";

    /** Why a friendly cannot be proposed (shown on the arrange form) or was declined (sent by letter). */
    public enum Reason {
        NONE(null),
        NO_OPPONENT("friendly_error_opponent"),
        TOO_SOON("friendly_error_too_soon"),
        OWN_ENGAGED("friendly_reason_own_engaged"),
        PENDING("friendly_error_pending"),
        ENGAGED("friendly_reason_engaged"),
        TOO_LATE("friendly_reason_too_late"),
        NO_SIDE("friendly_reason_no_side"),
        TRAVEL("friendly_reason_travel"),
        GROUND_LET("friendly_reason_ground"),
        COMPETITION("friendly_reason_competition");

        /** Bundle key of the reason text. */
        public final String key;

        Reason(String key) {
            this.key = key;
        }
    }

    private final Futtoboru app;
    private final double acceptChance;

    public FriendlyArranger(Futtoboru app) {
        this(app, DEFAULT_ACCEPT_CHANCE);
    }

    /** {@code acceptChance} 1 or 0 lets tests fix the answer of a club with a free day. */
    public FriendlyArranger(Futtoboru app, double acceptChance) {
        this.app = app;
        this.acceptChance = acceptChance;
    }

    private SaveGame game() {
        return app.getCurrentGame();
    }

    /** Clubs that can play a friendly with {@code own}: a ground and eleven men, sorted by name. */
    public List<Club> possibleOpponents(Club own) {
        List<Club> clubs = new ArrayList<>();
        if (game() == null || own == null) {
            return clubs;
        }
        for (Club club : game().getAllClubs()) {
            if (club == null || club.getId() == null || club.getId().equals(own.getId())) {
                continue;
            }
            if (club.getStadium() == null || club.getStadium().getCapacity() == null
                    || club.getPlayers() == null || club.getPlayers().size() < 11) {
                continue;
            }
            clubs.add(club);
        }
        clubs.sort(Comparator.comparing(c -> c.getName() != null ? c.getName() : ""));
        return clubs;
    }

    /** {@link Reason#NONE} when {@code own} may invite {@code opponent} for {@code date}. */
    public Reason check(Club own, Club opponent, LocalDate date) {
        if (own == null || opponent == null || opponent.getId() == null || opponent.getId().equals(own.getId())) {
            return Reason.NO_OPPONENT;
        }
        LocalDate today = game().getGameDate().toLocalDate();
        if (date == null || date.isBefore(today.plusDays(MIN_NOTICE_DAYS))) {
            return Reason.TOO_SOON;
        }
        if (hasMatchOn(own, date, null)) {
            return Reason.OWN_ENGAGED;
        }
        for (Match proposed : own.getProposedMatches()) {
            if (proposed != null && sameDay(proposed, date)) {
                return Reason.PENDING;
            }
        }
        return Reason.NONE;
    }

    /**
     * Sends the invitation: a friendly in {@code own}'s proposed list, answered the next day.
     *
     * @return the proposed match, or null when {@link #check} refuses it
     */
    public Match propose(Club own, Club opponent, LocalDate date, boolean atHome) {
        Reason reason = check(own, opponent, date);
        if (reason != Reason.NONE) {
            Log.info(Log.ENGINE_SCHEDULING, "FriendlyArranger", "Friendly not proposed: " + reason);
            return null;
        }
        Match match = new Match();
        match.setId(game().getIdAllocator().nextId());
        match.setMatchType(Match.FRIENDLY_MATCH);
        match.setHomeClubId(atHome ? own.getId() : opponent.getId());
        match.setAwayClubId(atHome ? opponent.getId() : own.getId());
        match.setProposeDateTime(game().getGameDate());
        match.setMatchDateTime(date.atTime(KICK_OFF_HOUR, 0));
        match.setIsProposed(true);
        match.setIsAccepted(false);
        own.getProposedMatches().add(match);
        Log.info(Log.ENGINE_SCHEDULING, "FriendlyArranger", own.getName() + " invite " + opponent.getName()
                + " to a friendly on " + date + (atHome ? " at home" : " away"));
        if (awaitsManager(own, opponent)) {
            invitationLetter(match, own, opponent);
        }
        return match;
    }

    /** G-13: an invitation from an AI club to the manager's club waits for the manager's answer. */
    private boolean awaitsManager(Club proposer, Club opponent) {
        Club current = game().getCurrentClub();
        return current != null && current.getId() != null && opponent != null
                && current.getId().equals(opponent.getId()) && !current.getId().equals(proposer.getId());
    }

    /** G-13: true while the invitation {@code matchId} to the manager's club can still be answered. */
    public boolean isOpenInvitation(Long matchId) {
        return matchId != null && proposerOf(matchId) != null;
    }

    private Club proposerOf(Long matchId) {
        if (game() == null || matchId == null) {
            return null;
        }
        for (Club club : game().getAllClubs()) {
            if (club == null || club.getProposedMatches() == null) {
                continue;
            }
            for (Match m : club.getProposedMatches()) {
                if (m != null && matchId.equals(m.getId())) {
                    return club;
                }
            }
        }
        return null;
    }

    /**
     * G-13: the manager answers an AI club's invitation from the inbox. Accepting fixes the friendly unless one of
     * the clubs now has a match that day or the day has come; declining just drops it.
     *
     * @return {@link Reason#NONE} when the answer was taken (accepted and fixed, or declined); otherwise why the
     *         friendly could not be fixed ({@link Reason#TOO_LATE} also when the invitation is no longer open)
     */
    public Reason answerInvitation(Long matchId, boolean accept) {
        Club proposer = proposerOf(matchId);
        Club manager = game() != null ? game().getCurrentClub() : null;
        if (proposer == null || manager == null) {
            return Reason.TOO_LATE;
        }
        Match match = null;
        for (Match m : proposer.getProposedMatches()) {
            if (m != null && matchId.equals(m.getId())) {
                match = m;
            }
        }
        proposer.getProposedMatches().remove(match);
        match.setIsProposed(false);
        if (!accept) {
            Log.info(Log.ENGINE_SCHEDULING, "FriendlyArranger", manager.getName() + " decline the invitation of "
                    + proposer.getName() + " for " + match.getMatchDateTime());
            return Reason.NONE;
        }
        LocalDate today = game().getGameDate().toLocalDate();
        LocalDate day = match.getMatchDateTime() != null ? match.getMatchDateTime().toLocalDate() : null;
        Reason reason = Reason.NONE;
        if (day == null || !day.isAfter(today)) {
            reason = Reason.TOO_LATE;
        } else if (hasMatchOn(manager, day, match)) {
            reason = Reason.OWN_ENGAGED;
        } else if (hasMatchOn(proposer, day, match)) {
            reason = Reason.ENGAGED;
        }
        if (reason != Reason.NONE) {
            return reason;
        }
        match.setIsAccepted(true);
        proposer.getScheduledMatches().add(match);
        manager.getScheduledMatches().add(match);
        Log.info(Log.ENGINE_SCHEDULING, "FriendlyArranger", manager.getName() + " accept the invitation of "
                + proposer.getName() + " for " + day);
        return Reason.NONE;
    }

    /**
     * The other club answers each invitation of {@code club} posted before today.
     *
     * @return the answers by match id ({@link Reason#NONE} = accepted), in the order of the proposals
     */
    public Map<Long, Reason> answerProposals(Club club) {
        Map<Long, Reason> answers = new LinkedHashMap<>();
        if (club == null || club.getProposedMatches() == null || club.getProposedMatches().isEmpty()) {
            return answers;
        }
        LocalDate today = game().getGameDate().toLocalDate();
        for (Match match : new ArrayList<>(club.getProposedMatches())) {
            if (match == null) {
                club.getProposedMatches().remove(match);
                continue;
            }
            if (match.getProposeDateTime() != null && !match.getProposeDateTime().toLocalDate().isBefore(today)) {
                continue; // the letter is still in the post
            }
            Club opponent = game().getClubById(otherClubId(match, club));
            if (opponent != null && awaitsManager(club, opponent)) {
                // G-13: the manager answers from the inbox; still unanswered two days before the match, it lapses
                LocalDate day = match.getMatchDateTime() != null ? match.getMatchDateTime().toLocalDate() : null;
                if (day != null && !day.isBefore(today.plusDays(MIN_NOTICE_DAYS))) {
                    continue;
                }
                club.getProposedMatches().remove(match);
                match.setIsProposed(false);
                answers.put(match.getId(), Reason.TOO_LATE);
                letter(opponent, MESSAGE_LAPSED, "friendly_lapsed_title", "friendly_lapsed_body", match, club, null);
                continue;
            }
            club.getProposedMatches().remove(match);
            if (opponent == null) {
                Log.warn(Log.ENGINE_SCHEDULING, "FriendlyArranger: dropping friendly " + match.getId()
                        + ", opponent not found");
                continue;
            }
            Reason reason = decide(match, club, opponent, today);
            answers.put(match.getId(), reason);
            if (reason == Reason.NONE) {
                match.setIsProposed(false);
                match.setIsAccepted(true);
                club.getScheduledMatches().add(match);
                opponent.getScheduledMatches().add(match);
                Log.info(Log.ENGINE_SCHEDULING, "FriendlyArranger", opponent.getName() + " accept the friendly on "
                        + match.getMatchDateTime().toLocalDate());
                letter(club, MESSAGE_ACCEPTED, "friendly_accepted_title", "friendly_accepted_body", match, opponent,
                        null);
            } else {
                match.setIsProposed(false);
                Log.info(Log.ENGINE_SCHEDULING, "FriendlyArranger", opponent.getName() + " decline the friendly on "
                        + match.getMatchDateTime().toLocalDate() + ": " + reason);
                letter(club, MESSAGE_DECLINED, "friendly_declined_title", "friendly_declined_body", match, opponent,
                        reason);
            }
        }
        return answers;
    }

    /** Calls off {@code club}'s friendlies on a day it now has a league or cup match. */
    public void cancelClashes(Club club) {
        if (club == null || club.getScheduledMatches() == null) {
            return;
        }
        for (Match friendly : new ArrayList<>(club.getScheduledMatches())) {
            if (!isFriendly(friendly) || Boolean.TRUE.equals(friendly.getIsPlayed())
                    || friendly.getMatchDateTime() == null) {
                continue;
            }
            Club other = game().getClubById(otherClubId(friendly, club));
            LocalDate day = friendly.getMatchDateTime().toLocalDate();
            boolean clash = hasCompetitionMatchOn(club, day) || (other != null && hasCompetitionMatchOn(other, day));
            if (!clash) {
                continue;
            }
            club.getScheduledMatches().remove(friendly);
            if (other != null) {
                other.getScheduledMatches().remove(friendly);
            }
            Log.info(Log.ENGINE_SCHEDULING, "FriendlyArranger", "Friendly " + friendly.getId() + " on " + day
                    + " called off: a competition match that day");
            Club current = game().getCurrentClub();
            if (current != null && other != null) {
                if (current.getId().equals(club.getId())) {
                    letter(club, MESSAGE_CANCELLED, "friendly_cancelled_title", "friendly_cancelled_body", friendly,
                            other, Reason.COMPETITION);
                } else if (current.getId().equals(other.getId())) {
                    letter(other, MESSAGE_CANCELLED, "friendly_cancelled_title", "friendly_cancelled_body", friendly,
                            club, Reason.COMPETITION);
                }
            }
        }
    }

    private Reason decide(Match match, Club own, Club opponent, LocalDate today) {
        LocalDate day = match.getMatchDateTime() != null ? match.getMatchDateTime().toLocalDate() : null;
        if (day == null || !day.isAfter(today)) {
            return Reason.TOO_LATE;
        }
        if (hasMatchOn(own, day, match)) {
            return Reason.OWN_ENGAGED;
        }
        if (hasMatchOn(opponent, day, match)) {
            return Reason.ENGAGED;
        }
        RandomSource random = Futtoboru.randomOf(app).fork("friendly:" + match.getId());
        if (random.nextDouble() < acceptChance) {
            return Reason.NONE;
        }
        boolean opponentAtHome = opponent.getId().equals(match.getHomeClubId());
        // a club asked to play at home may have let its ground; a club asked to travel may not pay the fares
        return random.nextBoolean() ? Reason.NO_SIDE : (opponentAtHome ? Reason.GROUND_LET : Reason.TRAVEL);
    }

    /** True when {@code club} has an unplayed match other than {@code except} on {@code day}. */
    static boolean hasMatchOn(Club club, LocalDate day, Match except) {
        if (club.getScheduledMatches() == null) {
            return false;
        }
        for (Match m : club.getScheduledMatches()) {
            if (m == null || m == except || (except != null && m.getId() != null && m.getId().equals(except.getId()))
                    || Boolean.TRUE.equals(m.getIsPlayed())) {
                continue;
            }
            if (sameDay(m, day) && isClubFixture(club, m)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when {@code club} plays or may still play {@code m}: one of its two clubs, or a cup tie of a later round
     * whose clubs are not known yet (it sits in every entrant's list) and the club has not lost a tie of that cup.
     */
    public static boolean isClubFixture(Club club, Match m) {
        if (club == null || club.getId() == null || m == null) {
            return false;
        }
        if (club.getId().equals(m.getHomeClubId()) || club.getId().equals(m.getAwayClubId())) {
            return true;
        }
        if (m.getHomeClubId() != null && m.getAwayClubId() != null) {
            return false;
        }
        if (m.getCompetitionEditionId() == null || club.getPlayedMatches() == null) {
            return true;
        }
        for (Match played : club.getPlayedMatches()) {
            if (played != null && m.getCompetitionEditionId().equals(played.getCompetitionEditionId())
                    && Boolean.TRUE.equals(played.getIsPlayed()) && played.getHomeGoals() != null
                    && played.getAwayGoals() != null) {
                boolean home = club.getId().equals(played.getHomeClubId());
                int own = home ? played.getHomeGoals() : played.getAwayGoals();
                int other = home ? played.getAwayGoals() : played.getHomeGoals();
                if (own < other) {
                    return false; // knocked out
                }
            }
        }
        return true;
    }

    private static boolean hasCompetitionMatchOn(Club club, LocalDate day) {
        if (club.getScheduledMatches() == null) {
            return false;
        }
        for (Match m : club.getScheduledMatches()) {
            if (m == null || isFriendly(m) || Boolean.TRUE.equals(m.getIsPlayed()) || !sameDay(m, day)) {
                continue;
            }
            if (club.getId().equals(m.getHomeClubId()) || club.getId().equals(m.getAwayClubId())) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameDay(Match m, LocalDate day) {
        return m.getMatchDateTime() != null && m.getMatchDateTime().toLocalDate().equals(day);
    }

    public static boolean isFriendly(Match m) {
        return m != null && m.getMatchType() != null && m.getMatchType() == Match.FRIENDLY_MATCH;
    }

    private static Long otherClubId(Match match, Club club) {
        return club.getId().equals(match.getHomeClubId()) ? match.getAwayClubId() : match.getHomeClubId();
    }

    /** A letter to the manager of {@code to}, only when that is the manager's club. */
    private void letter(Club to, String type, String titleKey, String bodyKey, Match match, Club opponent,
            Reason reason) {
        Club current = game().getCurrentClub();
        if (current == null || !current.getId().equals(to.getId())) {
            return;
        }
        MessageManager messages = app.getGameEngine() != null ? app.getGameEngine().getMessageManager() : null;
        if (messages == null) {
            return;
        }
        String date = formatDate(match.getMatchDateTime());
        String ground = LanguageModLoader.getValue(to.getId().equals(match.getHomeClubId())
                ? "friendly_letter_home" : "friendly_letter_away");
        Message message = new Message();
        message.setCategory(MessageCategory.MATCH);
        message.setMessageType(type);
        message.setPriority(MessagePriority.NORMAL);
        message.setTitle(fill(LanguageModLoader.getValue(titleKey), opponent.getName(), date));
        message.setPlainTextMessage(fill(LanguageModLoader.getValue(bodyKey), opponent.getName(), date, ground,
                reason != null ? LanguageModLoader.getValue(reason.key) : ""));
        message.setIsRead(false);
        message.setIsDeleted(false);
        messages.deliverMessage(message);
    }

    /** G-13: the letter inviting the manager's club ({@code to}) to a friendly with {@code from}. */
    private void invitationLetter(Match match, Club from, Club to) {
        MessageManager messages = app.getGameEngine() != null ? app.getGameEngine().getMessageManager() : null;
        if (messages == null) {
            return;
        }
        String date = formatDate(match.getMatchDateTime());
        boolean away = !to.getId().equals(match.getHomeClubId());
        String ground = LanguageModLoader.getValue(away ? "friendly_letter_away" : "friendly_letter_home");
        String terms = LanguageModLoader.getValue(away ? "friendly_invitation_terms_away"
                : "friendly_invitation_terms_home");
        Message message = new Message();
        message.setCategory(MessageCategory.MATCH);
        message.setMessageType(MESSAGE_INVITATION);
        message.setPriority(MessagePriority.NORMAL);
        message.setActionData(match.getId());
        message.setTitle(fill(LanguageModLoader.getValue("friendly_invitation_title"), from.getName(), date));
        message.setPlainTextMessage(fill(LanguageModLoader.getValue("friendly_invitation_body"), from.getName(), date,
                ground, terms));
        message.setIsRead(false);
        message.setIsDeleted(false);
        messages.deliverMessage(message);
    }

    /** The date in the language's pattern ({@code schedule_date_pattern}, numeric). */
    public static String formatDate(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        // U-12: in the game language; a bad pattern in a language file falls back to dd/MM/yyyy
        return GameFormat.formatter("schedule_date_pattern", "dd/MM/yyyy").format(date);
    }

    /** The label with {0}, {1}... replaced by {@code args} (the bundles hold plain patterns). */
    public static String fill(String pattern, Object... args) {
        String result = pattern;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return result;
    }
}
