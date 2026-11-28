package com.rndmodgames.futtoboru.system.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rndmodgames.futtoboru.NewGameFixture;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;

/**
 * T-01 (D27): the seeded 1888-89 season, played once per test JVM and shared by the slow tests that used to replay
 * it each (AutosaveTests' rollover test, SaveGameTests' played-game cases). A new game ({@link NewGameFixture}, seed
 * {@link #SEED}) is played day by day to the season rollover with the autosave on, recording the days it fires; on
 * day {@link #SNAPSHOT_DAY} the game is encoded once. Each test gets an independent copy decoded from that text
 * ({@link #copyAtSnapshot}), so no test sees another test's changes. The autosave only counts days here (cheap
 * encoder; a real autosave of a played season is 24 MB): the real autosave file is covered by AutosaveTests'
 * first-of-month test.
 */
public final class SeasonMemo {

    public static final long SEED = 1888L;
    /** 1 Apr + 170 days = mid-September 1888: league and cup created and drawn, first league days played */
    public static final int SNAPSHOT_DAY = 170;
    /** about 385 days to the rollover; more than twice that means the game is stuck */
    static final int DAY_BUDGET = 800;

    private static SeasonMemo played;

    private final List<LocalDate> autosaveDates = new ArrayList<>();
    private LocalDate rolloverDate;
    private int rollovers;
    private String snapshot;
    private String snapshotFingerprint;
    private long millis;

    private SeasonMemo() {
    }

    /**
     * The memo, played on the first call with {@code application} (constructed with {@link #SEED}). The application
     * is re-seeded with its forced seed first, as a New Game does: an earlier test of the same class may have loaded a
     * save, which replaces the seed with the save's (the memo then depended on the order the test classes ran in, red
     * on Linux CI only, P2-13 sweep).
     */
    public static synchronized SeasonMemo get(Futtoboru application) {
        if (played == null) {
            long seed = application.isSeedForced() ? application.reseedForNewGame() : application.getSeed();
            if (!application.isSeedForced() || seed != SEED) {
                throw new IllegalArgumentException("the season memo is played with seed " + SEED + ", not "
                        + seed + ": construct the application with new Futtoboru(SEED)");
            }
            played = play(application);
        }
        return played;
    }

    private static SeasonMemo play(Futtoboru application) {
        SeasonMemo memo = new SeasonMemo();
        long start = System.currentTimeMillis();
        File previousFolder = SaveLoadSystem.getGameFolder();
        File folder;
        try {
            folder = Files.createTempDirectory("futtoboru-season-memo").toFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        SaveLoadSystem.setGameFolder(folder); // the rotating autosave slot never lands in a test's folder
        Autosave autosave = null;
        try {
            SaveGame game = NewGameFixture.newGame(application);
            autosave = application.getGameEngine().getAutosave();
            autosave.setEnabled(true);
            autosave.setEncoderForTests(g -> "{}");
            for (int day = 1; day <= DAY_BUDGET
                    && application.getGameEngine().getAuthorityManager().getSeasonRollovers() == 0; day++) {
                int before = autosave.getWrites();
                NewGameFixture.play(application, game, 1, true);
                if (autosave.getWrites() > before) {
                    assertEquals(before + 1, autosave.getWrites(), "one autosave per day at most");
                    assertEquals(game.getGameDate().toLocalDate(), autosave.getLastDate());
                    memo.autosaveDates.add(autosave.getLastDate());
                }
                if (day == SNAPSHOT_DAY) {
                    memo.snapshot = SaveCodec.encode(game, "Season memo", LocalDateTime.of(2000, 1, 1, 0, 0));
                    memo.snapshotFingerprint = GameFingerprint.of(game).toString();
                }
            }
            assertTrue(autosave.awaitWrite(10_000), "the autosave file was written");
            memo.rollovers = application.getGameEngine().getAuthorityManager().getSeasonRollovers();
            memo.rolloverDate = game.getGameDate().toLocalDate();
        } finally {
            if (autosave != null) {
                autosave.setEnabled(false);
            }
            SaveLoadSystem.setGameFolder(previousFolder);
        }
        assertTrue(memo.snapshot != null, "the season reached day " + SNAPSHOT_DAY);
        // the copies are only worth sharing if they are the game: decoding the text gives the same fingerprint
        LoadResult copy = SaveCodec.decode(memo.snapshot);
        assertTrue(copy.isLoaded(), copy.toString());
        assertEquals(memo.snapshotFingerprint, GameFingerprint.of(copy.getGame()).toString(), "memo round trip");
        memo.millis = System.currentTimeMillis() - start;
        System.out.println("SeasonMemo: seed " + SEED + " season played once in " + memo.millis + " ms, "
                + memo.autosaveDates.size() + " autosave days, rollover " + memo.rolloverDate);
        return memo;
    }

    /** @return the days the autosave fired, in order, from the new game to the rollover day */
    public List<LocalDate> autosaveDates() {
        return Collections.unmodifiableList(autosaveDates);
    }

    public LocalDate rolloverDate() {
        return rolloverDate;
    }

    /** @return the season rollovers counted by the authority when the play stopped (1 when it rolled over) */
    public int rollovers() {
        return rollovers;
    }

    /**
     * @return an independent copy of the game on day {@link #SNAPSHOT_DAY}, decoded from the memo's save text (the
     *         database is rebuilt from it, as a load does) and bound to {@code application} with fresh managers
     */
    public SaveGame copyAtSnapshot(Futtoboru application) {
        LoadResult result = SaveCodec.decode(snapshot);
        assertTrue(result.isLoaded(), result.toString());
        assertEquals(new ArrayList<String>(), result.getProblems());
        NewGameFixture.bind(application, result.getGame());
        return result.getGame();
    }
}
