package com.rndmodgames.futtoboru;

import java.util.ArrayList;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.AuthorityManager;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.ScriptsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * A new 1888-89 game set up the way NewGameOverviewScreen does it (England selected, the manager at Preston), and
 * days of play, for tests that need a game in progress (P2-4 runtime ids, P2-5 saves).
 */
public final class NewGameFixture {

    private NewGameFixture() {
    }

    /** Builds a fresh database and a new game, made the application's current game with fresh managers. */
    public static SaveGame newGame(Futtoboru application) {
        DatabaseLoader.resetDatabaseLoaderInstance();
        DatabaseLoader db = DatabaseLoader.getInstance();
        Season season = DatabaseLoader.getSeasonByDataId("1888-89");

        SaveGame game = new SaveGame();
        bind(application, game);

        game.setMainAuthority(DatabaseLoader.getMainAuthority());
        Country england = DatabaseLoader.getCountryByDataId("england");
        List<Country> selected = new ArrayList<>();
        selected.add(england);
        game.setSelectedCountries(selected);

        Person owner = new Person();
        owner.setName("Test");
        owner.setLastname("Manager");
        owner.setCountry(england);
        game.setOwner(owner);
        game.assignId(owner);
        game.getAllPersons().add(owner);
        owner.setPrimaryProfession(DatabaseLoader.getProfessionByDataId(Profession.MANAGER));
        owner.setCurrentCountry(england);
        game.setGameStartDate(season.getStartDate());
        game.setGameDate(season.getStartDate());

        for (Competition comp : DatabaseLoader.getCompetitions()) {
            if (Competition.COMPETITION_CUP.equals(comp.getCompetitionType())) {
                game.getAllCups().add(comp);
            } else {
                game.getAllLeagues().add(comp);
            }
            for (CompetitionEdition edition : comp.getEditions()) {
                for (Long clubId : edition.getParticipantClubsIds()) {
                    if (game.getClubById(clubId) == null) {
                        game.getAllClubs().add(DatabaseLoader.getClubById(clubId));
                    }
                }
            }
        }
        for (Club club : db.getClubsByCountry(england)) {
            if (game.getClubById(club.getId()) == null) {
                game.getAllClubs().add(club);
            }
        }
        owner.setCurrentClubId(DatabaseLoader.getClubByDataId("preston-north-end").getId());
        game.getGameScripts().addAll(season.getSeasonScripts());

        // a person generated during play
        Person generated = application.getPersonGenerator().generateUniquePerson(england, season, false);
        game.getAllPersons().add(generated);

        application.initializeJobSystem();
        application.getJobManager().initializeJobOpenings();
        return game;
    }

    /** Makes {@code game} the application's current game with fresh engine managers bound to it. */
    public static void bind(Futtoboru application, SaveGame game) {
        application.startLoadedGame(game);
        ScriptsManager scripts = new ScriptsManager(application);
        AuthorityManager authority = new AuthorityManager(application);
        application.setScriptsManager(scripts);
        application.setGameEngine(new FuttoboruGameEngine(application, scripts, authority));
    }

    /**
     * Plays {@code days} days. With {@code readDraws}, the mandatory draw messages are marked read first each day,
     * as the player does by opening the draw screen (time stops at an unread one).
     */
    public static void play(Futtoboru application, SaveGame game, int days, boolean readDraws) {
        for (int day = 0; day < days; day++) {
            if (readDraws) {
                for (Message m : game.getAllMessages()) {
                    if (Boolean.TRUE.equals(m.getIsMandatory())) {
                        m.setIsRead(true);
                    }
                }
            }
            application.getScriptsManager().checkGameScripts();
            application.getGameEngine().continueGame();
        }
    }
}
