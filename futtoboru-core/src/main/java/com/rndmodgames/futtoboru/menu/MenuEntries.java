package com.rndmodgames.futtoboru.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * The in-game left menu, for every profession, in one list (U-04b).
 *
 * <p><b>To add a screen to the menu, add one {@link MenuEntry} to {@link #ALL}</b> (label key in the language bundles,
 * screen id, who sees it, group) and, if it has drill-down pages, map them to it in {@link #DRILL_DOWNS} so the entry
 * stays highlighted there. Nothing else needs to change for the button to appear. No UI code, so it is tested
 * headless.
 */
public final class MenuEntries {

    /** Who sees an entry. */
    public enum Audience {
        /** every profession */
        ALL,
        /** a manager with a club */
        MANAGER,
        /** no club: unemployed or retired player looking for a job */
        UNEMPLOYED;

        boolean includes(boolean manager) {
            return this == ALL || (this == MANAGER) == manager;
        }
    }

    /**
     * One menu button.
     *
     * @param labelKey key in {@code mods/language/BaseBundle_*.properties}
     * @param screen   {@link MainMenuManager} screen id
     * @param audience who sees it
     * @param group    entries of different groups are separated by a line
     */
    public record MenuEntry(String labelKey, int screen, Audience audience, int group) {
    }

    private static final int GROUP_MAIN = 0;
    private static final int GROUP_MATCHES = 1;
    private static final int GROUP_CLUB = 2;
    private static final int GROUP_JOBS = 3;
    private static final int GROUP_PERSON = 4;

    /** Every entry, in menu order. */
    public static final List<MenuEntry> ALL = List.of(
            new MenuEntry("home", MainMenuManager.HOME_SCREEN, Audience.ALL, GROUP_MAIN),
            new MenuEntry("inbox", MainMenuManager.INBOX_SCREEN, Audience.ALL, GROUP_MAIN),
            new MenuEntry("squad", MainMenuManager.MAIN_SQUAD_SCREEN, Audience.MANAGER, GROUP_MAIN),

            new MenuEntry("schedule", MainMenuManager.SCHEDULE_SCREEN, Audience.MANAGER, GROUP_MATCHES),
            new MenuEntry("match_history", MainMenuManager.MATCH_HISTORY_SCREEN, Audience.MANAGER, GROUP_MATCHES),
            new MenuEntry("competitions", MainMenuManager.COMPETITIONS_SCREEN, Audience.MANAGER, GROUP_MATCHES),
            new MenuEntry("history", MainMenuManager.HISTORY_SCREEN, Audience.ALL, GROUP_MATCHES),

            new MenuEntry("club_info", MainMenuManager.CLUB_INFO_SCREEN, Audience.MANAGER, GROUP_CLUB),
            new MenuEntry("finances", MainMenuManager.FINANCES_SCREEN, Audience.MANAGER, GROUP_CLUB),

            new MenuEntry("club_browser", MainMenuManager.CLUB_BROWSER_SCREEN, Audience.UNEMPLOYED, GROUP_JOBS),
            new MenuEntry("job_board", MainMenuManager.JOB_BOARD_SCREEN, Audience.UNEMPLOYED, GROUP_JOBS),
            new MenuEntry("my_applications", MainMenuManager.MY_APPLICATIONS_SCREEN, Audience.UNEMPLOYED, GROUP_JOBS),

            new MenuEntry("personal_details", MainMenuManager.PERSON_DETAILS_SCREEN, Audience.ALL, GROUP_PERSON),
            new MenuEntry("authority", MainMenuManager.AUTHORITY_SCREEN, Audience.ALL, GROUP_PERSON));

    /** Screens reached from inside another screen, mapped to the menu entry that stays highlighted. */
    public static final Map<Integer, Integer> DRILL_DOWNS = Map.ofEntries(
            Map.entry(MainMenuManager.LEAGUE_DETAIL_SCREEN, MainMenuManager.COMPETITIONS_SCREEN),
            Map.entry(MainMenuManager.CUP_DETAIL_SCREEN, MainMenuManager.COMPETITIONS_SCREEN),
            Map.entry(MainMenuManager.CUP_PLAYOFFS_SCREEN, MainMenuManager.COMPETITIONS_SCREEN),
            Map.entry(MainMenuManager.PLAYER_DETAIL_SCREEN, MainMenuManager.MAIN_SQUAD_SCREEN),
            Map.entry(MainMenuManager.CLUB_DETAIL_SCREEN, MainMenuManager.CLUB_BROWSER_SCREEN),
            Map.entry(MainMenuManager.JOB_OFFER_SCREEN, MainMenuManager.MY_APPLICATIONS_SCREEN),
            Map.entry(MainMenuManager.NEGOTIATION_SCREEN, MainMenuManager.MY_APPLICATIONS_SCREEN),
            Map.entry(MainMenuManager.LEAGUE_DRAW_SCREEN, MainMenuManager.INBOX_SCREEN),
            Map.entry(MainMenuManager.CUP_DRAW_SCREEN, MainMenuManager.INBOX_SCREEN),
            Map.entry(MainMenuManager.MATCH_PREVIEW_SCREEN, MainMenuManager.SCHEDULE_SCREEN),
            Map.entry(MainMenuManager.MATCH_RESULT_SCREEN, MainMenuManager.SCHEDULE_SCREEN));

    private MenuEntries() {
    }

    /** @return the entries a manager ({@code true}) or a person without a club ({@code false}) sees, in order */
    public static List<MenuEntry> forProfession(boolean manager) {
        List<MenuEntry> entries = new ArrayList<>();
        for (MenuEntry entry : ALL) {
            if (entry.audience().includes(manager)) {
                entries.add(entry);
            }
        }
        return Collections.unmodifiableList(entries);
    }

    /**
     * Which menu the owner sees (J-01): the club menu ({@link Audience#MANAGER}) when he works at a club, whatever
     * his post (a director or scout hired from the job board too), else the job-seeker menu. It went by the
     * profession alone: a hired director kept the job-seeker menu, a manager without a club got the club menu.
     *
     * @return true for {@code forProfession(true)}
     */
    public static boolean hasClub(SaveGame game) {
        return game != null && game.getCurrentClub() != null;
    }

    /** @return the menu entry screen to highlight while {@code screen} is shown (itself for an entry screen) */
    public static int entryScreenFor(int screen) {
        return DRILL_DOWNS.getOrDefault(screen, screen);
    }
}
