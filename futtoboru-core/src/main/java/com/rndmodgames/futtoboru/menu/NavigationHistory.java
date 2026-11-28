package com.rndmodgames.futtoboru.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The in-game screens visited since the last menu entry was picked (U-04b): what Back and Esc go back through.
 *
 * <p>Picking a menu entry starts a new history at that entry. Opening any other screen pushes it, unless the screen
 * is already in the history: then everything after it is dropped (so "Back to Competitions" from a league page and
 * the Back button agree, and no loop builds up). Screen ids are {@link MainMenuManager} constants. No UI code, so it
 * is tested headless.
 */
public final class NavigationHistory {

    private final List<GameScreenId> screens = new ArrayList<>();

    /** Starts a new history at {@code root} (a menu entry was picked). */
    public void reset(GameScreenId root) {
        screens.clear();
        screens.add(root);
    }

    /** Records that {@code screen} is now shown. */
    public void visit(GameScreenId screen) {
        int index = screens.lastIndexOf(screen);
        if (index >= 0) {
            screens.subList(index + 1, screens.size()).clear();
        } else {
            screens.add(screen);
        }
    }

    /** Forgets the current screen (it redirected elsewhere, or Back is leaving it). */
    public void dropCurrent() {
        if (!screens.isEmpty()) {
            screens.remove(screens.size() - 1);
        }
    }

    /** @return true if there is a screen to go back to */
    public boolean canGoBack() {
        return screens.size() > 1;
    }

    /**
     * Leaves the current screen.
     *
     * @return the screen to show now, or NONE if there is none to go back to (nothing changes then)
     */
    public GameScreenId back() {
        if (!canGoBack()) {
            return GameScreenId.NONE;
        }
        dropCurrent();
        return current();
    }

    /** @return the current screen, or NONE when empty */
    public GameScreenId current() {
        return screens.isEmpty() ? GameScreenId.NONE : screens.get(screens.size() - 1);
    }

    /** @return the history, oldest first (read-only) */
    public List<GameScreenId> screens() {
        return Collections.unmodifiableList(screens);
    }
}
