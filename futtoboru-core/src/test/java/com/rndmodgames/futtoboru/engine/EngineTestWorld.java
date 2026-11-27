package com.rndmodgames.futtoboru.engine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.CompetitionRules;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.engine.cup.CupBracketGenerator;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * A small hand-built game for engine tests: one league of N clubs (2-1-0 points) and helpers for
 * league fixtures and a cup bracket. Installs a fresh SaveGame, AuthorityManager and engine on the application.
 */
public class EngineTestWorld {

    public final Futtoboru app;
    public final SaveGame game = new SaveGame();
    public final League league = new League();
    public final List<Club> clubs = new ArrayList<>();
    public final FuttoboruGameEngine engine;
    public final AuthorityManager authorityManager;

    public EngineTestWorld(Futtoboru app, int numClubs) {
        this.app = app;
        game.setGameDate(LocalDateTime.of(1888, 9, 1, 12, 0));
        game.setGameStartDate(game.getGameDate());

        for (int i = 0; i < numClubs; i++) {
            Club club = new Club("Club " + (char) ('A' + i));
            club.setId(game.getIdAllocator().nextId());
            clubs.add(club);
            game.getAllClubs().add(club);
        }

        league.setId(game.getIdAllocator().nextId());
        league.setName("Test League");
        league.setLeagueClubs(new ArrayList<>(clubs));
        league.setRules(CompetitionRules.createHistoricalRules());

        Authority authority = new Authority();
        authority.getLeagues().add(league);
        game.setMainAuthority(authority);

        app.setCurrentGame(game);
        authorityManager = new AuthorityManager(app);
        engine = new FuttoboruGameEngine(app, new ScriptsManager(app), authorityManager);
        app.setGameEngine(engine);
    }

    public Club club(int i) {
        return clubs.get(i);
    }

    /** A scheduled league match on the current date, in both clubs' lists */
    public Match leagueMatch(Club home, Club away) {
        Match match = new Match();
        match.setId(game.getIdAllocator().nextId());
        match.setMatchType(Match.LEAGUE_MATCH);
        match.setHomeClubId(home.getId());
        match.setAwayClubId(away.getId());
        match.setMatchDateTime(game.getGameDate());
        match.setIsAccepted(true);
        home.getScheduledMatches().add(match);
        away.getScheduledMatches().add(match);
        return match;
    }

    /** Give a match a result without the random simulator */
    public static void score(Match match, int home, int away) {
        match.setHomeGoals(home);
        match.setAwayGoals(away);
        match.setIsPlayed(true);
    }

    /**
     * A cup over the given clubs with its complete bracket placed in the clubs' lists the way
     * AuthorityManager.generateCupDraw does (round 1 to its two clubs, later rounds to every participant).
     */
    public Cup cup(List<Club> participants) {
        Cup cup = new Cup();
        cup.competition.setId(game.getIdAllocator().nextId());
        cup.competition.setName("Test Cup");
        cup.competition.setCompetitionType(Competition.COMPETITION_CUP);
        cup.edition.setId(game.getIdAllocator().nextId());
        cup.edition.setName("Test Cup 1888-89");
        cup.edition.setStartDate(game.getGameDate());
        cup.edition.setEndDate(game.getGameDate().plusMonths(6));
        for (Club c : participants) {
            cup.edition.getParticipantClubsIds().add(c.getId());
        }
        cup.competition.getEditions().add(cup.edition);
        game.getAllCups().add(cup.competition);

        cup.matches = new CupBracketGenerator(game.getIdAllocator()).generateCompleteBracket(
            cup.edition.getParticipantClubsIds(), cup.competition.getId(), cup.edition.getId(), game.getGameDate(), 1);
        for (Match m : cup.matches) {
            if (m.getRound() == 1) {
                game.getClubById(m.getHomeClubId()).getScheduledMatches().add(m);
                game.getClubById(m.getAwayClubId()).getScheduledMatches().add(m);
            } else {
                for (Club c : participants) {
                    c.getScheduledMatches().add(m);
                }
            }
        }
        return cup;
    }

    public static class Cup {
        public final Competition competition = new Competition();
        public final CompetitionEdition edition = new CompetitionEdition();
        public List<Match> matches;

        public Match byPath(String path) {
            for (Match m : matches) {
                if (path.equals(m.getBracketPath())) {
                    return m;
                }
            }
            return null;
        }
    }
}
