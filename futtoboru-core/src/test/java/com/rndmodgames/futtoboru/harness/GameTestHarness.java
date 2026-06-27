package com.rndmodgames.futtoboru.harness;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.SeasonManager;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Headless API for new-game setup, day advancement, and mandatory-gate simulation.
 *
 * @see V1_TESTING_AND_SIMULATION_PLAN.md
 */
public final class GameTestHarness {

    public static final long PRESTON_NORTH_END_CLUB_ID = 1L;
    public static final long SEASON_1888_89_ID = 18L;
    public static final long ENGLAND_COUNTRY_ID = 1000L;

    private static final int DEFAULT_MAX_ADVANCE_DAYS = 400;
    private static final int SEASON_SIMULATION_MAX_DAYS = 700;

    private final Futtoboru application;
    private final FuttoboruGameEngine engine;

    public GameTestHarness() {
        HeadlessGdxTestSupport.ensureInitialized();
        this.application = HeadlessGdxTestSupport.application();
        this.engine = HeadlessGdxTestSupport.gameEngine();
    }

    public Futtoboru application() {
        return application;
    }

    public FuttoboruGameEngine engine() {
        return engine;
    }

    public SaveGame game() {
        return application.getCurrentGame();
    }

    /**
     * Starts a new employed save at {@code clubId} for {@code seasonId} (1888–89 = 18).
     */
    public void newGameEmployedAtClub(long clubId, long seasonId) {
        DatabaseLoader dbLoader = DatabaseLoader.getInstance();
        Season season = findSeason(dbLoader, seasonId);
        Country england = DatabaseLoader.getCountryById(ENGLAND_COUNTRY_ID);
        if (england == null) {
            throw new IllegalStateException("England country id " + ENGLAND_COUNTRY_ID + " not loaded");
        }

        SaveGame save = new SaveGame();
        LocalDateTime start = LocalDateTime.of(1888, 4, 1, 4, 0, 0);
        save.setGameStartDate(start);
        save.setGameDate(start);

        // DatabaseLoader shares one Authority and Season script instances across saves.
        // Reset leagues and copy scripts so each harness run is isolated (MandatoryGateIT).
        Authority authority = DatabaseLoader.getMainAuthority();
        authority.setLeagues(new ArrayList<>());
        save.setMainAuthority(authority);

        for (BasicScript script : season.getSeasonScripts()) {
            save.getGameScripts().add(copyScriptForNewGame(script));
        }
        save.setSelectedCountries(List.of(england));

        List<Club> clubs = dbLoader.getClubsByCountry(england);
        if (clubs != null) {
            for (Club club : clubs) {
                if (club != null && club.getScheduledMatches() != null) {
                    club.getScheduledMatches().clear();
                }
            }
            save.getAllClubs().addAll(clubs);
        }

        Person owner = new Person();
        owner.setId(1L);
        owner.setName("Harness");
        owner.setLastname("Manager");
        owner.setCurrentClubId(clubId);
        save.setOwner(owner);
        save.setStartSeasonId(seasonId);
        save.setSimulatedSeasonIndex(0);

        application.setCurrentGame(save);
        HeadlessGdxTestSupport.authorityManager().refreshCurrentGame();
        application.initializeJobSystem();
    }

    public int getNextGameAction() {
        return engine.getNextGameAction();
    }

    /**
     * Raw engine advance (ignores UI mandatory gates). Use for fast-forward after gates cleared.
     */
    public void continueOneDay() {
        LocalDateTime before = game().getGameDate();
        engine.continueGame();
        LocalDateTime after = game().getGameDate();
        if (!after.isAfter(before)) {
            throw new SimulationDeadlockException("Game date did not advance from " + before);
        }
    }

    public void continueDays(int days) {
        for (int i = 0; i < days; i++) {
            continueOneDay();
        }
    }

    /**
     * Advances one day only if no mandatory draw gate is active (mirrors Continue button guard).
     */
    public void continueOneDayRespectingGates() {
        int action = getNextGameAction();
        if (action == FuttoboruGameEngine.DRAW_ACTION) {
            throw new SimulationDeadlockException(
                    "Mandatory draw gate active — call acknowledgeMandatoryDraws() first");
        }
        if (action == FuttoboruGameEngine.MATCH_PREVIEW_ACTION) {
            throw new SimulationDeadlockException(
                    "Match preview gate active — not handled in Phase 0 harness (action=MATCH_PREVIEW)");
        }
        continueOneDay();
    }

    public LocalDateTime advanceUntil(Predicate<SaveGame> condition) {
        return advanceUntil(condition, DEFAULT_MAX_ADVANCE_DAYS);
    }

    public void continueOneDayAcknowledgingDraws() {
        deliverPendingMessages();
        for (int attempt = 0; attempt < 5; attempt++) {
            if (getNextGameAction() != FuttoboruGameEngine.DRAW_ACTION) {
                break;
            }
            int acknowledged = acknowledgeMandatoryDraws();
            deliverPendingMessages();
            if (acknowledged == 0) {
                break;
            }
        }
        continueOneDay();
    }

    public void simulateDays(int days) {
        for (int i = 0; i < days; i++) {
            continueOneDayAcknowledgingDraws();
        }
    }

    public League primaryLeague() {
        if (game().getMainAuthority() == null || game().getMainAuthority().getLeagues() == null
                || game().getMainAuthority().getLeagues().isEmpty()) {
            return null;
        }
        return game().getMainAuthority().getLeagues().get(0);
    }

    public boolean isPrimaryLeagueComplete() {
        League league = primaryLeague();
        if (league == null) {
            return false;
        }
        return new LeagueStandingsManager().isLeagueComplete(league);
    }

    public int countEditionsWithChampions() {
        return new SeasonManager(application).countFinalizedLeagueEditions(game());
    }

    public int getSimulatedSeasonIndex() {
        return game().getSimulatedSeasonIndex();
    }

    /**
     * Advances through league creation, mandatory draws, and until the primary league season completes.
     */
    public void simulateUntilPrimaryLeagueComplete() {
        advanceUntil(save -> primaryLeague() != null, 30);
        for (int day = 0; day < SEASON_SIMULATION_MAX_DAYS; day++) {
            if (isPrimaryLeagueComplete()) {
                return;
            }
            continueOneDayAcknowledgingDraws();
        }
        throw new SimulationDeadlockException(
                "simulateUntilPrimaryLeagueComplete: league not complete within "
                        + SEASON_SIMULATION_MAX_DAYS + " days (date=" + game().getGameDate() + ")");
    }

    /**
     * Simulates until {@code targetChampionEditions} league editions have recorded champions.
     */
    public void simulateUntilChampionEditions(int targetChampionEditions) {
        simulateUntilChampionEditions(targetChampionEditions, SEASON_SIMULATION_MAX_DAYS * targetChampionEditions);
    }

    public void simulateUntilChampionEditions(int targetChampionEditions, int maxDays) {
        advanceUntil(save -> primaryLeague() != null, 30);
        simulateDays(5);
        while (getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION) {
            acknowledgeMandatoryDraws();
        }
        for (int day = 0; day < maxDays; day++) {
            if (countEditionsWithChampions() >= targetChampionEditions) {
                return;
            }
            continueOneDayAcknowledgingDraws();
        }
        throw new SimulationDeadlockException(
                "simulateUntilChampionEditions: wanted " + targetChampionEditions
                        + " champion editions but got " + countEditionsWithChampions()
                        + " within " + maxDays + " days (date=" + game().getGameDate() + ")");
    }

    public LocalDateTime advanceUntil(Predicate<SaveGame> condition, int maxDays) {
        for (int day = 0; day < maxDays; day++) {
            if (condition.test(game())) {
                return game().getGameDate();
            }
            continueOneDay();
        }
        throw new SimulationDeadlockException(
                "advanceUntil: condition not met within " + maxDays + " days (date="
                        + game().getGameDate() + ", nextAction=" + getNextGameAction() + ")");
    }

    /**
     * Marks mandatory league/cup draw messages as viewed (same effect as {@code LeagueDrawScreenTable}).
     */
    public int acknowledgeMandatoryDraws() {
        SaveGame save = game();
        deliverPendingMessages();
        if (save.getAllMessages() == null) {
            return 0;
        }

        int acknowledged = 0;
        for (Message message : save.getAllMessages()) {
            if (message == null || message.getMessageType() == null) {
                continue;
            }
            boolean mandatory = Boolean.TRUE.equals(message.getIsMandatory());
            if (!mandatory) {
                continue;
            }
            String type = message.getMessageType();
            if ("LEAGUE_DRAW".equals(type) || "CUP_DRAW".equals(type) || "FIXTURE_DRAW".equals(type)) {
                message.setIsRead(true);
                message.setIsMandatory(false);
                acknowledged++;
            }
        }
        if (save.getScheduledMessages() != null) {
            for (Message message : save.getScheduledMessages()) {
                if (message == null || message.getMessageType() == null) {
                    continue;
                }
                String type = message.getMessageType();
                if (("LEAGUE_DRAW".equals(type) || "CUP_DRAW".equals(type) || "FIXTURE_DRAW".equals(type))
                        && Boolean.TRUE.equals(message.getIsMandatory())) {
                    message.setIsRead(true);
                    message.setIsMandatory(false);
                    acknowledged++;
                }
            }
        }
        return acknowledged;
    }

    public boolean hasUnreadMandatoryDraw() {
        return getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION;
    }

    public void assertNotBlocked() {
        if (hasUnreadMandatoryDraw()) {
            throw new AssertionError("Expected no mandatory draw gate, but getNextGameAction=DRAW");
        }
    }

    public void deliverPendingMessages() {
        LocalDateTime current = game().getGameDate();
        engine.getMessageManager().deliverScheduledMessages(current);
    }

    private static Season findSeason(DatabaseLoader dbLoader, long seasonId) {
        for (Season season : dbLoader.getSeasons()) {
            if (season.getId() != null && season.getId().equals(seasonId)) {
                return season;
            }
        }
        throw new IllegalArgumentException("Season id " + seasonId + " not found");
    }

    private static BasicScript copyScriptForNewGame(BasicScript source) {
        BasicScript copy = new BasicScript();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setDescription(source.getDescription());
        copy.setScriptType(source.getScriptType());
        copy.setExecutionTime(source.getExecutionTime());
        copy.setIsExecuted(false);
        if (source.getScriptValues() != null) {
            copy.setScriptValues(new HashMap<>(source.getScriptValues()));
        }
        return copy;
    }
}
