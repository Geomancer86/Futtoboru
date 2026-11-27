package com.rndmodgames.futtoboru.tables.main;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * What the Home screen shows (U-04c), read from the current game. No UI code, so it is tested headless; the
 * {@link HomeScreenTable} only turns it into labels.
 *
 * <p>Read only: nothing in the game is changed (the standings are sorted on a copy).
 */
public final class HomeDashboard {

    /** The club's next match not yet played, from today on. {@code opponent} is null while not drawn yet. */
    public record Fixture(LocalDateTime date, String opponent, boolean home, int matchType, String competition,
            long daysUntil) {
    }

    /** The club's last played match. */
    public record Result(LocalDateTime date, String opponent, boolean home, int goalsFor, int goalsAgainst,
            int matchType) {
    }

    /** The club's place in its league table (1 = top). */
    public record LeaguePosition(String league, int position, int clubs, int played, int points) {
    }

    private final String managerName;
    private final String clubName;
    private final LocalDateTime date;
    private final Fixture nextFixture;
    private final Result lastResult;
    private final int unreadMessages;
    private final int unreadNeedingAction;
    private final LeaguePosition leaguePosition;
    private final BigDecimal balance;
    private final int pendingApplications;
    private final int pendingOffers;

    private HomeDashboard(String managerName, String clubName, LocalDateTime date, Fixture nextFixture,
            Result lastResult, int unreadMessages, int unreadNeedingAction, LeaguePosition leaguePosition,
            BigDecimal balance, int pendingApplications, int pendingOffers) {
        this.managerName = managerName;
        this.clubName = clubName;
        this.date = date;
        this.nextFixture = nextFixture;
        this.lastResult = lastResult;
        this.unreadMessages = unreadMessages;
        this.unreadNeedingAction = unreadNeedingAction;
        this.leaguePosition = leaguePosition;
        this.balance = balance;
        this.pendingApplications = pendingApplications;
        this.pendingOffers = pendingOffers;
    }

    /** Reads the dashboard from {@code game} (an empty dashboard for null). */
    public static HomeDashboard of(SaveGame game) {
        if (game == null) {
            return new HomeDashboard(null, null, null, null, null, 0, 0, null, null, 0, 0);
        }
        Person owner = game.getOwner();
        Club club = game.getCurrentClub();
        LocalDateTime today = game.getGameDate();

        int unread = 0;
        int needingAction = 0;
        for (Message message : game.getAllMessages()) {
            if (message != null && !Boolean.TRUE.equals(message.getIsRead())) {
                unread++;
                if (Boolean.TRUE.equals(message.getIsMandatory())) {
                    needingAction++;
                }
            }
        }

        int applications = 0;
        int offers = 0;
        Long ownerId = owner != null ? owner.getId() : null;
        if (ownerId != null) {
            for (JobApplication application : game.getAllApplications()) {
                if (application != null && ownerId.equals(application.getApplicantId())
                        && application.getStatus() == ApplicationStatus.PENDING) {
                    applications++;
                }
            }
            for (JobOffer offer : game.getPendingOffers()) {
                if (offer != null && ownerId.equals(offer.getRecipientId())
                        && (offer.getStatus() == OfferStatus.PENDING || offer.getStatus() == OfferStatus.NEGOTIATING)) {
                    offers++;
                }
            }
        }

        return new HomeDashboard(managerName(owner), club != null ? club.getName() : null, today,
                club != null ? nextFixture(game, club, today) : null,
                club != null ? lastResult(game, club) : null,
                unread, needingAction,
                club != null ? leaguePosition(game, club) : null,
                club != null ? club.getClubBalance() : null,
                applications, offers);
    }

    private static String managerName(Person owner) {
        if (owner == null) {
            return null;
        }
        String name = ((owner.getName() != null ? owner.getName() : "") + " "
                + (owner.getLastname() != null ? owner.getLastname() : "")).trim();
        return name.isEmpty() ? null : name;
    }

    static Fixture nextFixture(SaveGame game, Club club, LocalDateTime today) {
        Match next = null;
        for (Match match : nonNull(club.getScheduledMatches())) {
            if (match == null || match.getMatchDateTime() == null || Boolean.TRUE.equals(match.getIsPlayed())) {
                continue;
            }
            if (today != null && match.getMatchDateTime().toLocalDate().isBefore(today.toLocalDate())) {
                continue;
            }
            if (next == null || match.getMatchDateTime().isBefore(next.getMatchDateTime())) {
                next = match;
            }
        }
        if (next == null) {
            return null;
        }
        boolean home = Objects.equals(club.getId(), next.getHomeClubId());
        Club opponent = game.getClubById(home ? next.getAwayClubId() : next.getHomeClubId());
        long days = today == null ? 0
                : ChronoUnit.DAYS.between(today.toLocalDate(), next.getMatchDateTime().toLocalDate());
        return new Fixture(next.getMatchDateTime(), opponent != null ? opponent.getName() : null, home,
                matchType(next), competitionName(game, next.getCompetitionId()), days);
    }

    static Result lastResult(SaveGame game, Club club) {
        List<Match> matches = new ArrayList<>(nonNull(club.getPlayedMatches()));
        matches.addAll(nonNull(club.getScheduledMatches()));
        Match last = null;
        for (Match match : matches) {
            if (match == null || !Boolean.TRUE.equals(match.getIsPlayed()) || match.getMatchDateTime() == null
                    || match.getHomeGoals() == null || match.getAwayGoals() == null) {
                continue;
            }
            if (last == null || match.getMatchDateTime().isAfter(last.getMatchDateTime())) {
                last = match;
            }
        }
        if (last == null) {
            return null;
        }
        boolean home = Objects.equals(club.getId(), last.getHomeClubId());
        Club opponent = game.getClubById(home ? last.getAwayClubId() : last.getHomeClubId());
        return new Result(last.getMatchDateTime(), opponent != null ? opponent.getName() : null, home,
                home ? last.getHomeGoals() : last.getAwayGoals(), home ? last.getAwayGoals() : last.getHomeGoals(),
                matchType(last));
    }

    static LeaguePosition leaguePosition(SaveGame game, Club club) {
        League league = club.getCurrentLeague();
        if (league == null || !contains(league, club)) {
            league = null;
            if (game.getMainAuthority() != null) {
                for (League candidate : nonNull(game.getMainAuthority().getLeagues())) {
                    if (candidate != null && contains(candidate, club)) {
                        league = candidate;
                        break;
                    }
                }
            }
        }
        if (league == null) {
            return null;
        }
        List<Club> table = new LeagueStandingsManager().calculateStandings(league);
        int position = 0;
        for (int i = 0; i < table.size(); i++) {
            if (Objects.equals(table.get(i).getId(), club.getId())) {
                position = i + 1;
                break;
            }
        }
        if (position == 0) {
            return null;
        }
        return new LeaguePosition(league.getName(), position, table.size(),
                club.getMatchesPlayed() != null ? club.getMatchesPlayed() : 0,
                club.getPoints() != null ? club.getPoints() : 0);
    }

    private static boolean contains(League league, Club club) {
        for (Club member : nonNull(league.getLeagueClubs())) {
            if (member != null && member.getId() != null && member.getId().equals(club.getId())) {
                return true;
            }
        }
        return false;
    }

    /** @return the name of the league or cup with that id, or null */
    static String competitionName(SaveGame game, Long competitionId) {
        if (competitionId == null) {
            return null;
        }
        List<Competition> competitions = new ArrayList<>(nonNull(game.getAllLeagues()));
        competitions.addAll(nonNull(game.getAllCups()));
        for (Competition competition : competitions) {
            if (competition != null && competitionId.equals(competition.getId())) {
                return competition.getName();
            }
        }
        if (game.getMainAuthority() != null) {
            for (League league : nonNull(game.getMainAuthority().getLeagues())) {
                if (league != null && competitionId.equals(league.getId())) {
                    return league.getName();
                }
            }
        }
        return null;
    }

    private static int matchType(Match match) {
        return match.getMatchType() != null ? match.getMatchType() : Match.FRIENDLY_MATCH;
    }

    private static <T> List<T> nonNull(List<T> list) {
        return list != null ? list : List.of();
    }

    /** @return "Name Lastname" of the owner, or null */
    public String getManagerName() {
        return managerName;
    }

    /** @return the owner's club, or null when unemployed */
    public String getClubName() {
        return clubName;
    }

    public boolean hasClub() {
        return clubName != null;
    }

    public LocalDateTime getDate() {
        return date;
    }

    /** @return the next match, or null (no club, or nothing scheduled) */
    public Fixture getNextFixture() {
        return nextFixture;
    }

    /** @return the last played match, or null */
    public Result getLastResult() {
        return lastResult;
    }

    public int getUnreadMessages() {
        return unreadMessages;
    }

    /** @return unread mandatory messages (a draw to make): they block Continue */
    public int getUnreadNeedingAction() {
        return unreadNeedingAction;
    }

    /** @return the club's league place, or null (no club or not in a league) */
    public LeaguePosition getLeaguePosition() {
        return leaguePosition;
    }

    /** @return the club's balance, or null when unemployed */
    public BigDecimal getBalance() {
        return balance;
    }

    /** @return the owner's job applications still waiting for an answer */
    public int getPendingApplications() {
        return pendingApplications;
    }

    /** @return job offers to the owner still waiting for a reply */
    public int getPendingOffers() {
        return pendingOffers;
    }
}
