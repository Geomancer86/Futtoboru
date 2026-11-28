package com.rndmodgames.futtoboru;

import java.util.ArrayList;
import java.util.List;

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
import com.rndmodgames.futtoboru.system.NewGameClubs;
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
        DatabaseLoader.getInstance();
        return setUp(application);
    }

    /**
     * A new game started the way the player does after playing or loading another one in the same session (P2-10):
     * the main menu's New Game button calls {@link Futtoboru#prepareNewGame()}; the game data is not read again.
     */
    public static SaveGame newGameInSession(Futtoboru application) {
        application.prepareNewGame();
        return setUp(application);
    }

    private static SaveGame setUp(Futtoboru application) {
        DatabaseLoader.getInstance(); // the data, loaded
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

        // the screen's step 6 (P2-13: each club once)
        NewGameClubs.addTo(game, DatabaseLoader.getCompetitions(), selected);
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
                // a mandatory draw only scheduled is delivered at once by the INBOX -> DRAW button (P2-11: the
                // FA Cup draw message is scheduled for the day after the draw and also stops time)
                if (game.getScheduledMessages() != null) {
                    for (Message m : new ArrayList<>(game.getScheduledMessages())) {
                        if (m != null && Boolean.TRUE.equals(m.getIsMandatory())) {
                            application.getGameEngine().getMessageManager().deliverMessage(m);
                        }
                    }
                }
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
