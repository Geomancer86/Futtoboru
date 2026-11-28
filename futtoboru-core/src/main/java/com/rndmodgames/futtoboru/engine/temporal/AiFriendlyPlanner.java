package com.rndmodgames.futtoboru.engine.temporal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.FriendlyTerms;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * G-13 (D31): AI clubs arrange their own friendlies. Each day every club but the manager's looks at the day
 * {@code leadDays} ahead and, if that day is still free, again {@code retryDays} ahead ({@link FriendlyTerms} of its
 * standing, {@code seasons/<id>/friendlies.json}): on a weekday with a chance and inside the friendly season, its
 * secretary writes to a club without a fixed match that day (any club with a ground and eleven men, the manager's
 * club too, which gets an invitation letter). The invitation then follows the {@link FriendlyArranger} rules: the
 * answer comes the next day (accept, or decline with a reason), clashes with league and cup matches call it off, and
 * an accepted friendly is played through {@code MatchResultRecorder} without touching the table.
 * <p>
 * Deterministic: each look is its own stream, {@code fork("aifriendly:<club id>:<day>:<lead>")}.
 */
public class AiFriendlyPlanner {

    public static final String FORK = "aifriendly:";

    private final Futtoboru app;
    private final FriendlyArranger arranger;

    public AiFriendlyPlanner(Futtoboru app) {
        this(app, new FriendlyArranger(app));
    }

    public AiFriendlyPlanner(Futtoboru app, FriendlyArranger arranger) {
        this.app = app;
        this.arranger = arranger;
    }

    /**
     * The day's letters of every AI club.
     *
     * @return the invitations sent today
     */
    public List<Match> daily() {
        List<Match> sent = new ArrayList<>();
        SaveGame game = app.getCurrentGame();
        if (game == null || game.getGameDate() == null || game.getAllClubs() == null) {
            return sent;
        }
        LocalDate today = game.getGameDate().toLocalDate();
        Club manager = game.getCurrentClub();
        for (Club club : new ArrayList<>(game.getAllClubs())) {
            if (club == null || club.getId() == null || club.getPlayers() == null || club.getPlayers().size() < 11
                    || (manager != null && club.getId().equals(manager.getId()))) {
                continue;
            }
            Match m = write(game, club, today);
            if (m != null) {
                sent.add(m);
            }
        }
        if (!sent.isEmpty()) {
            Log.info(Log.ENGINE_SCHEDULING, "AiFriendlyPlanner", sent.size() + " friendly invitations sent on " + today);
        }
        return sent;
    }

    /** One club's look at its two days: at most one letter a day. */
    private Match write(SaveGame game, Club club, LocalDate today) {
        FriendlyTerms first = termsOf(game, club, today.plusDays(1));
        int[] leads = first.getRetryDays() == first.getLeadDays() ? new int[] { first.getLeadDays() }
                : new int[] { first.getLeadDays(), first.getRetryDays() };
        for (int lead : leads) {
            LocalDate day = today.plusDays(lead);
            FriendlyTerms terms = termsOf(game, club, day);
            double chance = terms.chanceOn(day);
            if (chance <= 0 || !terms.inSeason(day) || !isFree(club, day)) {
                continue;
            }
            RandomSource random = Futtoboru.randomOf(app).fork(FORK + club.getId() + ":" + day + ":" + lead);
            if (random.nextDouble() >= chance) {
                continue;
            }
            List<Club> candidates = new ArrayList<>();
            for (Club other : arranger.possibleOpponents(club)) {
                if (!FriendlyArranger.hasMatchOn(other, day, null)) {
                    candidates.add(other);
                }
            }
            if (candidates.isEmpty()) {
                continue;
            }
            Club opponent = candidates.get(random.nextInt(candidates.size()));
            boolean hasGround = club.getStadium() != null && club.getStadium().getCapacity() != null;
            boolean atHome = hasGround && random.nextBoolean();
            Match match = arranger.propose(club, opponent, day, atHome);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    /** No match fixed and no invitation of its own waiting for that day. */
    private static boolean isFree(Club club, LocalDate day) {
        if (FriendlyArranger.hasMatchOn(club, day, null)) {
            return false;
        }
        if (club.getProposedMatches() != null) {
            for (Match m : club.getProposedMatches()) {
                if (m != null && m.getMatchDateTime() != null && m.getMatchDateTime().toLocalDate().equals(day)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** The friendly terms of a club's standing in the season of {@code day}. */
    public static FriendlyTerms termsOf(SaveGame game, Club club, LocalDate day) {
        int season = SeasonCalendar.startYearOf(day);
        return FriendlyTerms.forStanding(DatabaseLoader.getFriendlyTerms(season),
                ContractManager.standingOf(game, club, season));
    }
}
