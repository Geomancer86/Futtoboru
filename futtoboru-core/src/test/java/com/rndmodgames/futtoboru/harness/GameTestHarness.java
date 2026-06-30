package com.rndmodgames.futtoboru.harness;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.rndmodgames.futtoboru.data.Authority;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Competition;
import com.rndmodgames.futtoboru.data.CompetitionEdition;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.League;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.data.scripts.BasicScript;
import com.rndmodgames.futtoboru.data.tactics.ClubTacticsSetup;
import com.rndmodgames.futtoboru.data.tactics.FormationTemplate;
import com.rndmodgames.futtoboru.data.tactics.MatchLineup;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.engine.SeasonManager;
import com.rndmodgames.futtoboru.engine.economy.EconomyHealthMonitor;
import com.rndmodgames.futtoboru.engine.simulation.LeagueStandingsManager;
import com.rndmodgames.futtoboru.engine.simulation.MatchSimulator;
import com.rndmodgames.futtoboru.engine.tactics.BestElevenSelector;
import com.rndmodgames.futtoboru.engine.tactics.FormationRegistry;
import com.rndmodgames.futtoboru.engine.tactics.MatchLineupBuilder;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;

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
    private static final int LEAGUE_AND_CUP_SIMULATION_MAX_DAYS = 1200;

    private final Futtoboru application;
    private final FuttoboruGameEngine engine;
    private final SeasonManager seasonManager;

    public GameTestHarness() {
        HeadlessGdxTestSupport.ensureInitialized();
        this.application = HeadlessGdxTestSupport.application();
        this.engine = HeadlessGdxTestSupport.gameEngine();
        this.seasonManager = new SeasonManager(application);
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
        save.setSelectedCountries(new ArrayList<>(List.of(england)));

        List<Club> clubs = dbLoader.getClubsByCountry(england);
        if (clubs != null) {
            for (Club club : clubs) {
                if (club != null && club.getScheduledMatches() != null) {
                    club.getScheduledMatches().clear();
                }
            }
            save.getAllClubs().addAll(clubs);
        }

        attachCompetitionsFromDatabase(save);

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

    /**
     * Replaces the active save (mirrors load-game) and refreshes engine caches.
     */
    public void replaceCurrentGame(SaveGame loaded) {
        application.activateSaveGame(loaded);
        HeadlessGdxTestSupport.authorityManager().refreshCurrentGame();
    }

    /**
     * JSON roundtrip through {@link SaveLoadSystem#copySaveGame(SaveGame)}.
     */
    public SaveGame roundTripViaJson() {
        SaveGame copy = SaveLoadSystem.copySaveGame(game());
        replaceCurrentGame(copy);
        return copy;
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

    public Competition primaryCup() {
        if (game().getAllCups() == null || game().getAllCups().isEmpty()) {
            return null;
        }
        return game().getAllCups().get(0);
    }

    public boolean isPrimaryLeagueComplete() {
        League league = primaryLeague();
        if (league == null) {
            return false;
        }
        return new LeagueStandingsManager().isLeagueComplete(league);
    }

    public boolean isPrimaryCupComplete() {
        return seasonManager.isAnyCupEditionComplete(game());
    }

    public int countEditionsWithChampions() {
        return seasonManager.countFinalizedLeagueEditions(game());
    }

    public int countCupEditionsWithChampions() {
        return seasonManager.countFinalizedCupEditions(game());
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
     * Simulates until the primary league and FA Cup editions both record champions.
     */
    public void simulateUntilLeagueAndCupComplete() {
        simulateUntilLeagueAndCupComplete(LEAGUE_AND_CUP_SIMULATION_MAX_DAYS);
    }

    public void simulateUntilLeagueAndCupComplete(int maxDays) {
        advanceUntil(save -> primaryLeague() != null, 30);
        simulateDays(5);
        while (getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION) {
            acknowledgeMandatoryDraws();
        }
        for (int day = 0; day < maxDays; day++) {
            if (isPrimaryLeagueComplete() && isPrimaryCupComplete()) {
                return;
            }
            continueOneDayAcknowledgingDraws();
        }
        throw new SimulationDeadlockException(
                "simulateUntilLeagueAndCupComplete: league complete=" + isPrimaryLeagueComplete()
                        + ", cup complete=" + isPrimaryCupComplete()
                        + " within " + maxDays + " days (date=" + game().getGameDate() + ")");
    }

    /**
     * Simulates until {@code targetChampionEditions} league editions have recorded champions.
     */
    public void simulateUntilChampionEditions(int targetChampionEditions) {
        simulateUntilChampionEditions(targetChampionEditions, SEASON_SIMULATION_MAX_DAYS * targetChampionEditions);
    }

    public void simulateUntilChampionEditions(int targetChampionEditions, int maxDays) {
        simulateWithReport(targetChampionEditions, maxDays, true, false);
    }

    /**
     * Simulates league seasons and returns a structured report (champions, finances, timing).
     *
     * @param suppressStdout when true, discards {@code System.out} during simulation for realistic timing
     */
    public MultiSeasonSimulationReport simulateWithReport(
            int targetChampionEditions,
            int maxDays,
            boolean leagueOnly,
            boolean suppressStdout) {
        if (leagueOnly) {
            game().setAllCups(new ArrayList<>());
        }

        Club playerClub = game().getCurrentClub();
        String playerName = playerClub != null ? playerClub.getName() : "?";
        long playerId = playerClub != null && playerClub.getId() != null ? playerClub.getId() : -1L;
        MultiSeasonSimulationReport report =
                new MultiSeasonSimulationReport(playerId, playerName, game().getGameDate());

        PrintStream originalOut = System.out;
        if (suppressStdout) {
            System.setOut(new PrintStream(java.io.OutputStream.nullOutputStream()));
        }
        long startNs = System.nanoTime();
        int daysSimulated = 0;
        int lastChampionCount = 0;
        try {
            advanceUntil(save -> primaryLeague() != null, 30);
            simulateDays(5);
            daysSimulated += 5;
            while (getNextGameAction() == FuttoboruGameEngine.DRAW_ACTION) {
                acknowledgeMandatoryDraws();
            }
            for (int day = 0; day < maxDays; day++) {
                if (countEditionsWithChampions() >= targetChampionEditions) {
                    break;
                }
                continueOneDayAcknowledgingDraws();
                daysSimulated++;
                int championCount = countEditionsWithChampions();
                if (championCount > lastChampionCount) {
                    report.addSeason(snapshotLatestChampionEdition(championCount));
                    report.recordEconomyScan(game());
                    lastChampionCount = championCount;
                }
            }
        } finally {
            if (suppressStdout) {
                System.setOut(originalOut);
            }
        }

        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
        int finalChampions = countEditionsWithChampions();
        if (finalChampions < targetChampionEditions) {
            throw new SimulationDeadlockException(
                    "simulateWithReport: wanted " + targetChampionEditions
                            + " champion editions but got " + finalChampions
                            + " within " + maxDays + " days (date=" + game().getGameDate() + ")");
        }

        SaveGame save = game();
        int inboxSize = save.getAllMessages() != null ? save.getAllMessages().size() : 0;
        int simIndex = save.getSimulatedSeasonIndex() != null ? save.getSimulatedSeasonIndex() : 0;
        report.finish(
                save.getGameDate(),
                elapsedMs,
                daysSimulated,
                targetChampionEditions,
                finalChampions,
                inboxSize,
                simIndex,
                leagueOnly);
        report.recordEconomyScan(save);
        return report;
    }

    public double averageHomeAttendance(Club club) {
        if (club == null) {
            return 0;
        }
        int total = 0;
        int count = 0;
        if (club.getScheduledMatches() != null) {
            for (Match match : club.getScheduledMatches()) {
                if (isPlayedHomeMatch(club, match)) {
                    total += match.getAttendance() != null ? match.getAttendance() : 0;
                    count++;
                }
            }
        }
        if (club.getPlayedMatches() != null) {
            for (Match match : club.getPlayedMatches()) {
                if (isPlayedHomeMatch(club, match)) {
                    total += match.getAttendance() != null ? match.getAttendance() : 0;
                    count++;
                }
            }
        }
        return count > 0 ? (double) total / count : 0;
    }

    public void assertNoNegativePrimaryLeagueBalances() {
        League league = primaryLeague();
        var alerts = EconomyHealthMonitor.findNegativeBalancesInLeague(league);
        if (!alerts.isEmpty()) {
            throw new AssertionError("Negative Football League club balances: " + alerts);
        }
    }

    public void assertPlayerClubSolvent() {
        Club player = game().getCurrentClub();
        if (player != null && player.getClubBalance() != null
                && player.getClubBalance().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new AssertionError("Player club balance negative: " + player.getClubBalance());
        }
    }

    private static boolean isPlayedHomeMatch(Club club, Match match) {
        return match != null
                && Boolean.TRUE.equals(match.getIsPlayed())
                && club.getId() != null
                && club.getId().equals(match.getHomeClubId())
                && match.getMatchType() != null
                && match.getMatchType() == Match.LEAGUE_MATCH;
    }

    private SeasonEditionSummary snapshotLatestChampionEdition(int editionNumber) {
        SaveGame save = game();
        League league = primaryLeague();
        CompetitionEdition edition = null;
        if (league != null && league.getEditions() != null) {
            for (int i = league.getEditions().size() - 1; i >= 0; i--) {
                CompetitionEdition candidate = league.getEditions().get(i);
                if (candidate != null && candidate.getChampionsId() != null) {
                    edition = candidate;
                    break;
                }
            }
        }

        Long championId = edition != null ? edition.getChampionsId() : null;
        String championName = resolveClubName(championId);
        Club player = save.getCurrentClub();
        BigDecimal balance = player != null ? player.getClubBalance() : null;
        BigDecimal income = player != null ? player.getSeasonIncome() : null;
        BigDecimal spend = player != null ? player.getSeasonExpenditure() : null;
        int simIndex = save.getSimulatedSeasonIndex() != null ? save.getSimulatedSeasonIndex() : 0;
        int inbox = save.getAllMessages() != null ? save.getAllMessages().size() : 0;

        return new SeasonEditionSummary(
                editionNumber,
                edition != null ? edition.getName() : null,
                championId,
                championName,
                save.getGameDate(),
                balance,
                income,
                spend,
                simIndex,
                inbox);
    }

    private static String resolveClubName(Long clubId) {
        if (clubId == null) {
            return "?";
        }
        Club club = DatabaseLoader.getClubById(clubId);
        if (club != null && club.getName() != null) {
            return club.getName();
        }
        return "Club#" + clubId;
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

    /**
     * Mirrors {@link com.rndmodgames.futtoboru.screens.NewGameOverviewScreen} competition setup.
     */
    private static void attachCompetitionsFromDatabase(SaveGame save) {
        List<Competition> allCompetitions = DatabaseLoader.getCompetitions();
        if (allCompetitions == null) {
            return;
        }
        if (save.getAllCups() == null) {
            save.setAllCups(new ArrayList<>());
        }
        if (save.getAllLeagues() == null) {
            save.setAllLeagues(new ArrayList<>());
        }
        for (Competition comp : allCompetitions) {
            if (comp == null) {
                continue;
            }
            Competition attached = SaveLoadSystem.copyViaJson(Competition.class, comp);
            if (Competition.COMPETITION_CUP.equals(attached.getCompetitionType())) {
                if (!containsCompetitionByName(save.getAllCups(), attached.getName())) {
                    save.getAllCups().add(attached);
                }
            } else if (Competition.COMPETITION_LEAGUE.equals(attached.getCompetitionType())) {
                if (!containsCompetitionByName(save.getAllLeagues(), attached.getName())) {
                    save.getAllLeagues().add(attached);
                }
            }
            if (attached.getEditions() != null) {
                for (CompetitionEdition edition : attached.getEditions()) {
                    if (edition == null || edition.getParticipantClubsIds() == null) {
                        continue;
                    }
                    for (Long clubId : edition.getParticipantClubsIds()) {
                        if (clubId != null && save.getClubById(clubId) == null) {
                            Club club = DatabaseLoader.getClubById(clubId);
                            if (club != null) {
                                save.getAllClubs().add(club);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setClubLineup(long clubId, List<Long> playerIdsInSlotOrder) {
        Club club = game().getClubById(clubId);
        if (club == null) {
            throw new IllegalArgumentException("Club not found: " + clubId);
        }
        ClubTacticsSetup setup = club.getTacticsSetupOrDefault();
        setup.clearSlotAssignments();
        FormationTemplate template = FormationRegistry.getRequiredTemplate(setup.getFormationTemplateId());
        for (int i = 0; i < playerIdsInSlotOrder.size() && i < template.getSlots().size(); i++) {
            setup.assignPlayer(template.getSlots().get(i).getSlotId(), playerIdsInSlotOrder.get(i));
        }
        club.setTacticsSetup(setup);
    }

    public List<Long> bestElevenPlayerIds(Club club) {
        ClubTacticsSetup setup = BestElevenSelector.selectBestEleven(club);
        return setup.orderedPlayerIds(FormationRegistry.getRequiredTemplate(setup.getFormationTemplateId()));
    }

    public List<Long> worstElevenPlayerIds(Club club) {
        ClubTacticsSetup setup = BestElevenSelector.selectWorstEleven(club, FormationRegistry.DEFAULT_FORMATION_ID);
        return setup.orderedPlayerIds(FormationRegistry.getRequiredTemplate(setup.getFormationTemplateId()));
    }

    public int countHomeWinsWithLineup(long homeClubId, long awayClubId, List<Long> homeLineupIds,
            int iterations, long seed) {
        Club home = game().getClubById(homeClubId);
        Club away = game().getClubById(awayClubId);
        MatchLineup homeLineup = MatchLineupBuilder.fromPlayerIds(home, homeLineupIds,
                FormationRegistry.DEFAULT_FORMATION_ID);
        MatchLineup awayLineup = MatchLineupBuilder.buildForClub(away, null);
        int wins = 0;
        for (int i = 0; i < iterations; i++) {
            int[] score = MatchSimulator.simulateScore(homeLineup, home, awayLineup, away, new Random(seed + i));
            if (score[0] > score[1]) {
                wins++;
            }
        }
        return wins;
    }

    public Match findNextLeagueMatchForClub(long clubId) {
        Club club = game().getClubById(clubId);
        if (club == null || club.getScheduledMatches() == null) {
            return null;
        }
        for (Match match : club.getScheduledMatches()) {
            if (match != null && !Boolean.TRUE.equals(match.getIsPlayed())
                    && match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH) {
                return match;
            }
        }
        return null;
    }

    public Match findLastPlayedLeagueMatch(long clubId) {
        Club club = game().getClubById(clubId);
        if (club == null) {
            return null;
        }
        Match latest = null;
        if (club.getScheduledMatches() != null) {
            for (Match match : club.getScheduledMatches()) {
                if (match != null && Boolean.TRUE.equals(match.getIsPlayed())
                        && match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH) {
                    latest = match;
                }
            }
        }
        if (club.getPlayedMatches() != null) {
            for (Match match : club.getPlayedMatches()) {
                if (match != null && Boolean.TRUE.equals(match.getIsPlayed())
                        && match.getMatchType() != null && match.getMatchType() == Match.LEAGUE_MATCH) {
                    latest = match;
                }
            }
        }
        return latest;
    }

    private static boolean containsCompetitionByName(List<Competition> competitions, String name) {
        if (competitions == null || name == null) {
            return false;
        }
        for (Competition existing : competitions) {
            if (existing != null && name.equals(existing.getName())) {
                return true;
            }
        }
        return false;
    }
}
