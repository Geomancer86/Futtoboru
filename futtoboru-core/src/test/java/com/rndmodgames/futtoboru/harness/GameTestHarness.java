package com.rndmodgames.futtoboru.harness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
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
        save.setMainAuthority(DatabaseLoader.getMainAuthority());
        save.getGameScripts().addAll(season.getSeasonScripts());
        save.setSelectedCountries(List.of(england));

        List<Club> clubs = dbLoader.getClubsByCountry(england);
        if (clubs != null) {
            save.getAllClubs().addAll(clubs);
        }

        Person owner = new Person();
        owner.setId(1L);
        owner.setName("Harness");
        owner.setLastname("Manager");
        owner.setCurrentClubId(clubId);
        save.setOwner(owner);

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
}
