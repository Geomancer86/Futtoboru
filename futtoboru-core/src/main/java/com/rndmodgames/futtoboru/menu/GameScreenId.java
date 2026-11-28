package com.rndmodgames.futtoboru.menu;

/**
 * RF-01: the in-game screens of {@link MainMenuManager}. The numbers are the old int screen ids and must never
 * change: inbox messages store them in saves ({@code Message.actionScreen}).
 */
public enum GameScreenId {

    /** no screen (the old -1): nothing to go back to, or a stored number no screen has */
    NONE(-1),

    HOME(100),
    INBOX(200),
    PERSON_DETAILS(300),
    AUTHORITY(400),

    LEAGUE(500), // not used
    LEAGUE_DETAIL(501),
    CUP_DETAIL(502),
    CUP_PLAYOFFS(503),
    WORLD(600), // not used

    MAIN_SQUAD(1000),
    SCHEDULE(2000),
    CLUB_INFO(3000),
    COMMITTEE(3001),   // G-05
    CANDIDATES(3002),  // R-03
    FINANCES(4000),
    COMPETITIONS(5000),

    MATCH_PREVIEW(10000),
    MATCH_RESULT(20000),
    MATCH_HISTORY(30000),
    HISTORY(40000),    // U-07 champions, cup winners, final tables

    // Job System Screens (v1.0)
    CLUB_BROWSER(10001),
    CLUB_DETAIL(10002),
    JOB_BOARD(10003),
    MY_APPLICATIONS(10004),
    JOB_OFFER(10005),
    NEGOTIATION(10006),

    PLAYER_DETAIL(10007),

    LEAGUE_DRAW(10008),
    CUP_DRAW(10009),

    // G-04: the retained list (from the Squad screen or the club's April letter)
    RETAINED_LIST(10010);

    private final int id;

    GameScreenId(int id) {
        this.id = id;
    }

    /** @return the stored number of this screen (saves keep it) */
    public int id() {
        return id;
    }

    /** @return the screen with this stored number, or {@link #NONE} for null or a number no screen has */
    public static GameScreenId fromId(Integer id) {
        if (id != null) {
            for (GameScreenId screen : values()) {
                if (screen.id == id) {
                    return screen;
                }
            }
        }
        return NONE;
    }
}
