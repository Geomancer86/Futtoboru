package com.rndmodgames.futtoboru.data.recruitment;

import java.io.Serializable;

/**
 * R-04 (docs/design/RECRUITMENT_AND_TRIALS.md §4.1): how much one observer of the player's club has seen of one man
 * (a candidate or another club's player), in minutes watched. Only the exposure is kept: the observer's error on each
 * attribute is drawn from a named fork of the game's root source ({@code engine.recruitment.Perception}), so it is
 * the same draw every time and a reload changes nothing. Saved in {@code SaveGame.knowledge} (optional, saveVersion 3).
 */
public class Knowledge implements Serializable {

    private static final long serialVersionUID = 1L;

    /** the observer's key ({@code engine.recruitment.Observers}: {@code p<personId>} or {@code c:<club>:<name>}) */
    private String observer;
    /** the man seen: his player id (a candidate's player too) */
    private Long player;
    /** minutes watched (a match he attended is 90) */
    private Float minutes;

    public Knowledge() {
    }

    public Knowledge(String observer, Long player, float minutes) {
        this.observer = observer;
        this.player = player;
        this.minutes = minutes;
    }

    public String getObserver() {
        return observer;
    }

    public Long getPlayer() {
        return player;
    }

    public float getMinutes() {
        return minutes != null ? minutes : 0f;
    }

    public void addMinutes(float more) {
        this.minutes = getMinutes() + more;
    }

    /** @return the index key of an (observer, man) pair */
    public static String key(String observer, Long player) {
        return observer + "|" + player;
    }
}
