package com.rndmodgames.futtoboru.system.save;

import java.io.File;
import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;

/**
 * U-06 / D18: one rotating autosave slot ({@link SaveLoadSystem#AUTOSAVE_FILE_NAME}), written when the game date
 * reaches the 1st of a month and on the day of a season rollover. The engine calls {@link #afterDay} once per
 * advanced day. At most one write per game and date; the game is encoded on the game thread (a consistent
 * snapshot) and the file is written on a background thread, so play never waits on the disk. A failure is logged
 * with {@link Log#error} and the game goes on.
 *
 * <p>Off by default when there is no window ({@code Gdx.gl == null}: headless tests), so tests never write to the
 * player's Documents; a test turns it on with {@link #setEnabled} after pointing {@link SaveLoadSystem#setGameFolder}
 * at a temporary folder.
 */
public class Autosave {

    private static final String TAG = "Autosave";

    /** One writer for every autosave: writes happen in order, never two at once. */
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "futtoboru-autosave");
        t.setDaemon(true);
        return t;
    });

    private boolean enabled = Gdx.gl != null;
    private boolean rolloverPending;
    private int seenRollovers;
    private WeakReference<SaveGame> lastGame = new WeakReference<>(null);
    private LocalDate lastDate;
    private int writes;
    private Future<?> lastWrite;

    /** @return true when an autosave is due on {@code date}: the 1st of a month, or a season rolled over that day */
    public static boolean isDue(LocalDate date, boolean seasonRolledOver) {
        return date != null && (seasonRolledOver || date.getDayOfMonth() == 1);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Called after each advanced day with the authority's season-rollover count so far
     * ({@code AuthorityManager#getSeasonRollovers}); writes the autosave when due.
     *
     * @return true when an autosave was started for this day
     */
    public boolean afterDay(SaveGame game, int seasonRollovers) {
        if (seasonRollovers > seenRollovers) {
            rolloverPending = true;
        }
        seenRollovers = seasonRollovers;
        if (!enabled || game == null || game.getGameDate() == null) {
            return false;
        }
        LocalDate date = game.getGameDate().toLocalDate();
        if (!isDue(date, rolloverPending)) {
            return false;
        }
        if (lastGame.get() == game && date.equals(lastDate)) {
            return false; // already written for this game and date (a rollover later the same day waits a day)
        }
        lastGame = new WeakReference<>(game);
        lastDate = date;
        rolloverPending = false;
        String text;
        try {
            text = SaveCodec.encode(game, SaveLoadSystem.AUTOSAVE_NAME, LocalDateTime.now());
        } catch (RuntimeException e) {
            Log.error(Log.SYSTEM_INIT, TAG, "autosave for " + date + " could not be encoded", e);
            return false;
        }
        File file = SaveLoadSystem.autosaveFile();
        writes++;
        lastWrite = WRITER.submit(() -> {
            try {
                SaveLoadSystem.writeSaveFile(file, text);
                Log.info(Log.SYSTEM_INIT, TAG, "autosaved " + date + " to " + file);
            } catch (Exception e) {
                Log.error(Log.SYSTEM_INIT, TAG, "autosave for " + date + " could not be written to " + file, e);
            }
        });
        return true;
    }

    /** @return the game date of the last autosave started, or null */
    public LocalDate getLastDate() {
        return lastDate;
    }

    /** @return how many autosaves were started */
    public int getWrites() {
        return writes;
    }

    /** Waits (tests, quitting) until the last autosave started is on disk. @return false on timeout */
    public boolean awaitWrite(long timeoutMillis) {
        Future<?> f = lastWrite;
        if (f == null) {
            return true;
        }
        try {
            f.get(timeoutMillis, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            return f.isDone();
        }
    }
}
