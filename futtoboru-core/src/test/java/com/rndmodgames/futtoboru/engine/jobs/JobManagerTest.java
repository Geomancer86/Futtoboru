package com.rndmodgames.futtoboru.engine.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.rndmodgames.futtoboru.data.jobs.JobOpening;
import com.rndmodgames.futtoboru.data.jobs.JobStatus;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Unit tests for {@link JobManager#getAvailableJobs()}'s incremental open-openings cache.
 *
 * <p>Found via JFR profiling 2026-07-19: {@code getAvailableJobs()} used to re-filter
 * {@code SaveGame.getActiveJobOpenings()} in full on every call — a list that only ever grows,
 * because expired/filled openings are flagged via {@code setStatus} but never removed (other
 * screens need them for by-id history lookups). In a 60-season soak, {@code LocalTime.compareTo}
 * alone (called transitively via the date-comparison filter) accounted for ~85% of all sampled
 * CPU time. This class had zero prior test coverage despite being a daily hot path.
 */
class JobManagerTest {

    private static final LocalDateTime GAME_DATE = LocalDateTime.of(1889, 3, 1, 0, 0);

    private static boolean headlessInitialized;

    private Futtoboru application;
    private SaveGame save;
    private JobManager jobManager;

    @BeforeEach
    void setUp() {
        if (!headlessInitialized) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            config.updatesPerSecond = 30;
            new HeadlessApplication(new Futtoboru(), config);
            headlessInitialized = true;
        }

        application = new Futtoboru();
        save = new SaveGame();
        save.setActiveJobOpenings(new ArrayList<>());
        save.setGameDate(GAME_DATE);
        application.setCurrentGame(save);

        jobManager = new JobManager(application, null);
    }

    private JobOpening openOpening(long id, long clubId, LocalDateTime deadline) {
        JobOpening opening = new JobOpening();
        opening.setId(id);
        opening.setClubId(clubId);
        opening.setProfessionId(1L);
        opening.setPostedDate(GAME_DATE.minusDays(1));
        opening.setApplicationDeadline(deadline);
        save.getActiveJobOpenings().add(opening);
        return opening;
    }

    @Test
    void newlyCreatedOpeningIsImmediatelyAvailable() {
        openOpening(1L, 10L, GAME_DATE.plusDays(30));

        List<JobOpening> available = jobManager.getAvailableJobs();

        assertEquals(1, available.size());
        assertEquals(1L, available.get(0).getId());
    }

    @Test
    void expiredOpeningDropsOutOfAvailableJobsAfterUpdate() {
        JobOpening opening = openOpening(2L, 10L, GAME_DATE.minusDays(1));
        // Precondition: still flagged OPEN until updateJobOpenings() runs the daily expiry check.
        assertEquals(JobStatus.OPEN, opening.getStatus());

        jobManager.updateJobOpenings();

        assertEquals(JobStatus.EXPIRED, opening.getStatus());
        assertTrue(jobManager.getAvailableJobs().isEmpty());
        // The underlying all-time list must still contain the opening for by-id history lookups
        // (offer/application screens) — this fix must not delete history, only skip it when scanning.
        assertEquals(1, save.getActiveJobOpenings().size());
    }

    @Test
    void filledOpeningDropsOutOfAvailableJobsImmediately() {
        JobOpening opening = openOpening(3L, 10L, GAME_DATE.plusDays(30));
        assertEquals(1, jobManager.getAvailableJobs().size());

        opening.setStatus(JobStatus.FILLED);

        assertTrue(jobManager.getAvailableJobs().isEmpty());
        assertEquals(1, save.getActiveJobOpenings().size());
    }

    @Test
    void cachePicksUpOpeningsAddedAcrossMultipleCalls() {
        openOpening(4L, 10L, GAME_DATE.plusDays(30));
        assertEquals(1, jobManager.getAvailableJobs().size());

        openOpening(5L, 20L, GAME_DATE.plusDays(30));
        List<JobOpening> available = jobManager.getAvailableJobs();

        assertEquals(2, available.size());
    }

    @Test
    void availableJobsForClubFiltersCorrectlyAfterMixedStatusChanges() {
        openOpening(6L, 10L, GAME_DATE.plusDays(30));
        JobOpening otherClubOpening = openOpening(7L, 20L, GAME_DATE.plusDays(30));
        otherClubOpening.setStatus(JobStatus.FILLED);

        com.rndmodgames.futtoboru.data.Club club = new com.rndmodgames.futtoboru.data.Club();
        club.setId(10L);

        List<JobOpening> forClub = jobManager.getAvailableJobs(club);

        assertEquals(1, forClub.size());
        assertEquals(6L, forClub.get(0).getId());
    }

    @Test
    void reloadingIntoANewBackingListRebuildsCacheRatherThanLeakingStaleEntries() {
        openOpening(8L, 10L, GAME_DATE.plusDays(30));
        assertEquals(1, jobManager.getAvailableJobs().size());

        // Simulate a save load: a brand new backing list replaces the old one.
        List<JobOpening> reloaded = new ArrayList<>();
        JobOpening afterLoad = new JobOpening();
        afterLoad.setId(9L);
        afterLoad.setClubId(30L);
        afterLoad.setApplicationDeadline(GAME_DATE.plusDays(10));
        reloaded.add(afterLoad);
        save.setActiveJobOpenings(reloaded);

        List<JobOpening> available = jobManager.getAvailableJobs();

        assertEquals(1, available.size());
        assertEquals(9L, available.get(0).getId());
        assertFalse(available.stream().anyMatch(o -> o.getId() == 8L));
    }
}
