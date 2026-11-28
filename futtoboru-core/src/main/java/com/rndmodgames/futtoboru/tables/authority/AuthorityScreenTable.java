package com.rndmodgames.futtoboru.tables.authority;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.temporal.SeasonCalendar;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.history.HistoryProvider;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Authority Screen Table v1
 *
 * TODO / WIP:
 *
 *  - Data structure
 *
 *  ----------------------------------
 *  FIFA: Sport Top Governing Body
 *  IFAB: Rules of the Game Management
 *
 *
 *  ----------------------------------
 *  Federation internationale de football association [FIFA] https://en.wikipedia.org/wiki/FIFA
 *
 *  - Foundation: 21 May 1904
 *  - HQ       : Zurich, Switzerland
 *
 *  ----------------------------------
 *
 *  International Football Association Board [IFAB] https://en.wikipedia.org/wiki/International_Football_Association_Board
 *
 *  - Formation: 2 June 1886
 *  - Purpose  : Manage the [Laws of the Game]
 *  - HQ       : Zurich, Switzerland
 *  - Members  : FIFA,
 *               The Football Association [ENGLAND]
 *               Scotish Football Association
 *               Football Association of Wales
 *               Irish Football Association
 *
 * @author Geomancer86
 */
public class AuthorityScreenTable extends VisTable {

    //
    Game game;
    SaveGame currentGame;

    //
    private static final String IFAB_WIKIPEDIA_URL = "https://en.wikipedia.org/wiki/International_Football_Association_Board";

    // Dynamic Components
    VisTable leaguesListTable = new VisTable(true);
    VisTable teamsListTable = new VisTable(true);
    VisLabel seasonValueLabel = new VisLabel("");

    public AuthorityScreenTable(Game parent) {

        // auto margins
        super (true);

        //
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();

        /**
         * Main Authority
         */
        VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("name"));
        VisLabel nameValueLabel = new VisLabel(currentGame.getMainAuthority().getName());

        // Name
        this.add(nameLabel);
        this.add(nameValueLabel);
        this.row();

        // Season (U-12): the one the league is in; 1889-90 from the rollover on
        this.add(new VisLabel(LanguageModLoader.getValue("authority_season")));
        this.add(seasonValueLabel);
        this.row();

        /**
         * Authority Leagues
         */
        VisLabel leaguesLabel = new VisLabel(LanguageModLoader.getValue("leagues"));

        // Leagues
        this.row();
        this.addSeparator().colspan(2);
        this.add(leaguesLabel).colspan(2);
        this.row();
        this.add(leaguesListTable).colspan(2);

        VisLabel teamsLabel = new VisLabel(LanguageModLoader.getValue("teams"));

        this.row();
        this.addSeparator().colspan(2);
        this.add(teamsLabel).colspan(2);

        this.row();
        this.add(teamsListTable).colspan(2);

        //
        updateDynamicComponents();
    }

    public void updateDynamicComponents() {

        //
        this.currentGame = ((Futtoboru) game).getCurrentGame();
        seasonValueLabel.setText(seasonLabel(currentGame));
        listActiveLeagues();
        listActiveTeams();
    }

    /**
     * TODO WIP:
     *
     *
     */
    public void listActiveLeagues() {

        Authority main = currentGame.getMainAuthority();

        leaguesListTable.clear();

        leaguesListTable.row();

        // No existing Leagues
        if (main.getLeagues().isEmpty()) {

            leaguesListTable.add(new VisLabel(LanguageModLoader.getValue("authority_no_leagues")));

        } else {

            // Add Leagues Component
            leaguesListTable.add(new VisLabel(LanguageModLoader.getValue("top_level_league")));
            leaguesListTable.add(new VisLabel(main.getLeagues().get(0).getName()));
        }
    }

    /** The clubs of the current season ({@link #seasonClubs}), each with its year and source link. */
    public void listActiveTeams() {

        List<Club> clubs = seasonClubs(currentGame);

        Log.info(Log.UI_COMPETITIONS, "LISTING SEASON " + clubs.size() + " ACTIVE TEAMS");

        //
        teamsListTable.clear();

        for (Club club : clubs) {

            teamsListTable.add(new VisLabel(club.getFullName()));
            teamsListTable.add(new VisLabel(club.getYear() != null ? club.getYear().toString() : "-"));
            teamsListTable.add(new LinkLabel(LanguageModLoader.getValue("authority_wikipedia"), club.getUrlSource()));
            teamsListTable.row();
        }
    }

    /** @return the authority's top league, or null before it is founded (April 1888) */
    static League topLeague(SaveGame game) {
        Authority main = game != null ? game.getMainAuthority() : null;
        return main != null && main.getLeagues() != null && !main.getLeagues().isEmpty() ? main.getLeagues().get(0)
                : null;
    }

    /**
     * The top league's current season (U-12): the first edition without a final table, so the season being played
     * (or about to start) until its rollover, then the next one (1889-90 is generated early, while 1888-89 is still
     * played). The last edition when every one is finished; null without a league or editions.
     */
    static CompetitionEdition currentEdition(League league) {
        if (league == null || league.getEditions() == null || league.getEditions().isEmpty()) {
            return null;
        }
        for (CompetitionEdition edition : league.getEditions()) {
            if (edition != null && edition.getFinalStandings().isEmpty()) {
                return edition;
            }
        }
        return league.getEditions().get(league.getEditions().size() - 1);
    }

    /** "1888-89": the current league season, or the season of the game date before there is a league. */
    static String seasonLabel(SaveGame game) {
        CompetitionEdition edition = currentEdition(topLeague(game));
        if (edition != null) {
            return HistoryProvider.season(edition);
        }
        if (game == null || game.getGameDate() == null) {
            return "-";
        }
        int year = SeasonCalendar.forDate(game.getGameDate()).getStartYear();
        return year + "-" + String.format(Locale.ROOT, "%02d", (year + 1) % 100);
    }

    /**
     * The clubs of the current season, as the game holds them: the current edition's entrants, else the league's
     * clubs, else (before the league is founded) the starting season's clubs.
     */
    static List<Club> seasonClubs(SaveGame game) {
        List<Club> clubs = new ArrayList<>();
        League league = topLeague(game);
        CompetitionEdition edition = currentEdition(league);
        if (edition != null && edition.getParticipantClubsIds() != null) {
            for (Long id : edition.getParticipantClubsIds()) {
                Club club = game.getClubById(id);
                if (club != null && !clubs.contains(club)) {
                    clubs.add(club);
                }
            }
        }
        if (clubs.isEmpty() && league != null && league.getLeagueClubs() != null) {
            for (Club club : league.getLeagueClubs()) {
                if (club != null && !clubs.contains(club)) {
                    clubs.add(club);
                }
            }
        }
        Season starting = clubs.isEmpty() ? startingSeason(game) : null;
        if (starting != null && starting.getClubs() != null) {
            for (Club club : starting.getClubs()) {
                Club inGame = game != null ? game.getClubById(club.getId()) : null;
                clubs.add(inGame != null ? inGame : club);
            }
        }
        return clubs;
    }

    /** The season the game started in (by its start date), else the first one in the data. */
    private static Season startingSeason(SaveGame game) {
        List<Season> seasons = DatabaseLoader.getInstance().getSeasons();
        if (seasons == null || seasons.isEmpty()) {
            return null;
        }
        for (Season season : seasons) {
            if (game != null && season.getStartDate() != null && season.getStartDate().equals(game.getGameStartDate())) {
                return season;
            }
        }
        return seasons.get(0);
    }
}
