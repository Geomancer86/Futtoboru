package com.rndmodgames.futtoboru.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/** U-04b: what Back and Esc go back through in the game screen. */
class NavigationHistoryTests {

    private static final int HOME = MainMenuManager.HOME_SCREEN;
    private static final int COMPETITIONS = MainMenuManager.COMPETITIONS_SCREEN;
    private static final int CUP = MainMenuManager.CUP_DETAIL_SCREEN;
    private static final int PLAYOFFS = MainMenuManager.CUP_PLAYOFFS_SCREEN;
    private static final int SQUAD = MainMenuManager.MAIN_SQUAD_SCREEN;
    private static final int PLAYER = MainMenuManager.PLAYER_DETAIL_SCREEN;

    @Test
    void menuEntryHasNoBack() {
        NavigationHistory history = new NavigationHistory();
        history.reset(HOME);
        history.visit(HOME); // the entry's own screen being shown
        assertFalse(history.canGoBack());
        assertEquals(-1, history.back());
        assertEquals(HOME, history.current());
    }

    @Test
    void drillDownsGoBackOneAtATime() {
        NavigationHistory history = new NavigationHistory();
        history.reset(COMPETITIONS);
        history.visit(CUP);
        history.visit(PLAYOFFS);
        assertTrue(history.canGoBack());
        assertEquals(CUP, history.back());
        assertEquals(COMPETITIONS, history.back());
        assertFalse(history.canGoBack());
    }

    @Test
    void pickingAMenuEntryStartsANewHistory() {
        NavigationHistory history = new NavigationHistory();
        history.reset(SQUAD);
        history.visit(PLAYER);
        history.reset(COMPETITIONS);
        assertEquals(List.of(COMPETITIONS), history.screens());
        assertFalse(history.canGoBack());
    }

    @Test
    void revisitingAScreenDropsWhatCameAfterIt() {
        NavigationHistory history = new NavigationHistory();
        history.reset(COMPETITIONS);
        history.visit(CUP);
        history.visit(PLAYOFFS);
        history.visit(COMPETITIONS); // e.g. a link back to the list
        assertEquals(List.of(COMPETITIONS), history.screens());
    }

    @Test
    void refreshingTheSameScreenAddsNothing() {
        NavigationHistory history = new NavigationHistory();
        history.reset(SQUAD);
        history.visit(PLAYER);
        history.visit(PLAYER);
        assertEquals(List.of(SQUAD, PLAYER), history.screens());
    }

    @Test
    void redirectReplacesTheScreenThatCouldNotBeShown() {
        // MainMenuManager.redirectTo: the asked-for screen is dropped, the fallback is shown instead
        NavigationHistory history = new NavigationHistory();
        history.reset(COMPETITIONS);
        history.visit(CUP);       // asked for, but no cup selected
        history.dropCurrent();
        history.visit(COMPETITIONS);
        assertEquals(List.of(COMPETITIONS), history.screens());
        assertFalse(history.canGoBack());
    }

    @Test
    void redirectFromAMenuEntryLeavesOnlyTheFallback() {
        NavigationHistory history = new NavigationHistory();
        history.reset(SQUAD);
        history.visit(SQUAD);     // no club: redirected Home
        history.dropCurrent();
        history.visit(HOME);
        assertEquals(List.of(HOME), history.screens());
    }
}
