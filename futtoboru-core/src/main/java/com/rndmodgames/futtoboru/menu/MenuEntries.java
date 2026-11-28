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
     * @param screen   the screen it opens
     * @param audience who sees it
     * @param group    entries of different groups are separated by a line
     */
    public record MenuEntry(String labelKey, GameScreenId screen, Audience audience, int group) {
    }

    private static final int GROUP_MAIN = 0;
    private static final int GROUP_MATCHES = 1;
    private static final int GROUP_CLUB = 2;
    private static final int GROUP_JOBS = 3;
    private static final int GROUP_PERSON = 4;

    /** Every entry, in menu order. */
    public static final List<MenuEntry> ALL = List.of(
            new MenuEntry("home", GameScreenId.HOME, Audience.ALL, GROUP_MAIN),
            new MenuEntry("inbox", GameScreenId.INBOX, Audience.ALL, GROUP_MAIN),
            new MenuEntry("squad", GameScreenId.MAIN_SQUAD, Audience.MANAGER, GROUP_MAIN),

            new MenuEntry("schedule", GameScreenId.SCHEDULE, Audience.MANAGER, GROUP_MATCHES),
            new MenuEntry("match_history", GameScreenId.MATCH_HISTORY, Audience.MANAGER, GROUP_MATCHES),
            new MenuEntry("competitions", GameScreenId.COMPETITIONS, Audience.MANAGER, GROUP_MATCHES),
            new MenuEntry("history", GameScreenId.HISTORY, Audience.ALL, GROUP_MATCHES),

            new MenuEntry("club_info", GameScreenId.CLUB_INFO, Audience.MANAGER, GROUP_CLUB),
            new MenuEntry("finances", GameScreenId.FINANCES, Audience.MANAGER, GROUP_CLUB),
            new MenuEntry("committee", GameScreenId.COMMITTEE, Audience.MANAGER, GROUP_CLUB),
            new MenuEntry("candidates", GameScreenId.CANDIDATES, Audience.MANAGER, GROUP_CLUB),

            new MenuEntry("club_browser", GameScreenId.CLUB_BROWSER, Audience.UNEMPLOYED, GROUP_JOBS),
            new MenuEntry("job_board", GameScreenId.JOB_BOARD, Audience.UNEMPLOYED, GROUP_JOBS),
            new MenuEntry("my_applications", GameScreenId.MY_APPLICATIONS, Audience.UNEMPLOYED, GROUP_JOBS),

            new MenuEntry("personal_details", GameScreenId.PERSON_DETAILS, Audience.ALL, GROUP_PERSON),
            new MenuEntry("authority", GameScreenId.AUTHORITY, Audience.ALL, GROUP_PERSON));

    /** Screens reached from inside another screen, mapped to the menu entry that stays highlighted. */
    public static final Map<GameScreenId, GameScreenId> DRILL_DOWNS = Map.ofEntries(
            Map.entry(GameScreenId.LEAGUE_DETAIL, GameScreenId.COMPETITIONS),
            Map.entry(GameScreenId.CUP_DETAIL, GameScreenId.COMPETITIONS),
            Map.entry(GameScreenId.CUP_PLAYOFFS, GameScreenId.COMPETITIONS),
            Map.entry(GameScreenId.PLAYER_DETAIL, GameScreenId.MAIN_SQUAD),
            Map.entry(GameScreenId.RETAINED_LIST, GameScreenId.MAIN_SQUAD),
            Map.entry(GameScreenId.CLUB_DETAIL, GameScreenId.CLUB_BROWSER),
            Map.entry(GameScreenId.JOB_OFFER, GameScreenId.MY_APPLICATIONS),
            Map.entry(GameScreenId.NEGOTIATION, GameScreenId.MY_APPLICATIONS),
            Map.entry(GameScreenId.LEAGUE_DRAW, GameScreenId.INBOX),
            Map.entry(GameScreenId.CUP_DRAW, GameScreenId.INBOX),
            Map.entry(GameScreenId.MATCH_PREVIEW, GameScreenId.SCHEDULE),
            Map.entry(GameScreenId.MATCH_RESULT, GameScreenId.SCHEDULE));

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
    public static GameScreenId entryScreenFor(GameScreenId screen) {
        return DRILL_DOWNS.getOrDefault(screen, screen);
    }
}
