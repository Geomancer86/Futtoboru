package com.rndmodgames.futtoboru.engine.injuries;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerInjury;

/**
 * G-02: whether a man can be picked on a day, and why not. The one availability API: the instant result, the AI and
 * the player's team sheet (G-01 {@code TeamSheets.isAvailable}) all ask here, for every club alike. Today the only
 * reason is an injury; a later reason (suspension, unregistered, away at work...) is one more {@link Reason}.
 */
public final class Availability {

    /** Men on a side (no substitutes in 1888: a club that cannot raise eleven plays short). */
    public static final int SIDE = 11;

    public enum Reason {
        AVAILABLE("availability_available"),
        INJURED("availability_injured");

        private final String labelKey;

        Reason(String labelKey) {
            this.labelKey = labelKey;
        }

        public String labelKey() {
            return labelKey;
        }
    }

    /** A man's availability on one day. */
    public static final class Status {
        private final Reason reason;
        private final PlayerInjury injury;
        private final int daysLeft;

        Status(Reason reason, PlayerInjury injury, int daysLeft) {
            this.reason = reason;
            this.injury = injury;
            this.daysLeft = daysLeft;
        }

        public boolean isAvailable() {
            return reason == Reason.AVAILABLE;
        }

        public Reason getReason() {
            return reason;
        }

        /** The injury keeping him out, or null. */
        public PlayerInjury getInjury() {
            return injury;
        }

        /** Days until he is available again (0 when he is). */
        public int getDaysLeft() {
            return daysLeft;
        }

        /** The first day he is available again, or null when he is. */
        public LocalDateTime getUntil() {
            return injury != null ? injury.getUntil() : null;
        }
    }

    private static final Status AVAILABLE = new Status(Reason.AVAILABLE, null, 0);

    private Availability() {
    }

    /** @return the man's availability on that day (by date: the time of day does not matter) */
    public static Status of(Player player, LocalDateTime date) {
        PlayerInjury injury = player != null ? player.getInjury() : null;
        if (injury == null || injury.getUntil() == null || date == null) {
            return AVAILABLE;
        }
        long left = java.time.temporal.ChronoUnit.DAYS.between(date.toLocalDate(), injury.getUntil().toLocalDate());
        return left > 0 ? new Status(Reason.INJURED, injury, (int) left) : AVAILABLE;
    }

    public static boolean isAvailable(Player player, LocalDateTime date) {
        return of(player, date).isAvailable();
    }

    /** @return the club's men available on that day, in squad order */
    public static List<Player> available(Club club, LocalDateTime date) {
        if (club == null || club.getPlayers() == null) {
            return Collections.emptyList();
        }
        List<Player> out = new ArrayList<>();
        for (Player p : club.getPlayers()) {
            if (p != null && isAvailable(p, date)) {
                out.add(p);
            }
        }
        return out;
    }

    /**
     * The eleven who take the field on that day before G-01's team sheet: the first {@link #SIDE} available men in
     * squad order (fewer when the club cannot raise eleven: it plays short).
     */
    public static List<Player> matchSide(Club club, LocalDateTime date) {
        List<Player> all = available(club, date);
        return all.size() > SIDE ? new ArrayList<>(all.subList(0, SIDE)) : all;
    }
}
