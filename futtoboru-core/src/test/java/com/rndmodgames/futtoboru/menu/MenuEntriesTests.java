package com.rndmodgames.futtoboru.menu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.menu.MenuEntries.MenuEntry;
import com.rndmodgames.futtoboru.ui.LanguageBundleKeysTest;

/** U-04b: one menu-entry list for both professions. */
class MenuEntriesTests {

    private static List<GameScreenId> screens(List<MenuEntry> entries) {
        return entries.stream().map(MenuEntry::screen).collect(Collectors.toList());
    }

    @Test
    void managerMenu() {
        assertEquals(List.of(GameScreenId.HOME, GameScreenId.INBOX, GameScreenId.MAIN_SQUAD,
                GameScreenId.SCHEDULE, GameScreenId.MATCH_HISTORY, GameScreenId.COMPETITIONS,
                GameScreenId.HISTORY, GameScreenId.CLUB_INFO, GameScreenId.FINANCES,
                GameScreenId.COMMITTEE, GameScreenId.CANDIDATES,
                GameScreenId.PERSON_DETAILS, GameScreenId.AUTHORITY),
                screens(MenuEntries.forProfession(true)));
    }

    @Test
    void jobSeekerMenuHasNoClubScreens() {
        assertEquals(List.of(GameScreenId.HOME, GameScreenId.INBOX, GameScreenId.HISTORY,
                GameScreenId.CLUB_BROWSER, GameScreenId.JOB_BOARD, GameScreenId.MY_APPLICATIONS,
                GameScreenId.PERSON_DETAILS, GameScreenId.AUTHORITY),
                screens(MenuEntries.forProfession(false)));
    }

    @Test
    void eachScreenAndLabelOnce() {
        Set<GameScreenId> screens = new HashSet<>();
        Set<String> labels = new HashSet<>();
        for (MenuEntry entry : MenuEntries.ALL) {
            assertTrue(screens.add(entry.screen()), "screen twice: " + entry);
            assertTrue(labels.add(entry.labelKey()), "label twice: " + entry);
        }
    }

    @Test
    void groupsAreContiguous() {
        // the separator logic draws one line per group change, so a group must not come back later
        Set<Integer> closed = new HashSet<>();
        int last = -1;
        for (MenuEntry entry : MenuEntries.ALL) {
            if (entry.group() != last) {
                assertFalse(closed.contains(entry.group()), "group split: " + entry);
                if (last >= 0) {
                    closed.add(last);
                }
                last = entry.group();
            }
        }
    }

    @Test
    void drillDownsHighlightAnEntry() {
        Set<GameScreenId> entryScreens = MenuEntries.ALL.stream().map(MenuEntry::screen).collect(Collectors.toSet());
        MenuEntries.DRILL_DOWNS.forEach((drillDown, entry) -> {
            assertFalse(entryScreens.contains(drillDown), "a drill-down is not a menu entry: " + drillDown);
            assertTrue(entryScreens.contains(entry), "drill-down " + drillDown + " maps to a non-entry " + entry);
        });
        assertEquals(GameScreenId.COMPETITIONS, MenuEntries.entryScreenFor(GameScreenId.CUP_PLAYOFFS));
        assertEquals(GameScreenId.INBOX, MenuEntries.entryScreenFor(GameScreenId.INBOX));
    }

    @Test
    void everyLabelIsInEveryLanguageBundle() throws Exception {
        for (MenuEntry entry : MenuEntries.ALL) {
            LanguageBundleKeysTest.assertKeyInAllBundles(entry.labelKey());
        }
    }
}
