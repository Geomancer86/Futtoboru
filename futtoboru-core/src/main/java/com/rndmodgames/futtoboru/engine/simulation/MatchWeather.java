package com.rndmodgames.futtoboru.engine.simulation;

import java.time.LocalDate;
import java.time.Month;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.Surface;

/**
 * Ground and weather of a match (E-17a): one surface and a constant wetness, drawn from
 * {@code root.fork("weather:" + date + ":" + ground)} so two matches on the same ground and day share it and a result
 * never depends on the order of the day's matches. The shares per month are placeholders (D30) until the weather
 * research gives sourced ones: winter months freeze or soak the ground more often.
 *
 * @param surface state of the turf
 * @param wetness {@link PitchConditions} scale, 0 dry .. 0.8 waterlogged
 */
public record MatchWeather(Surface surface, double wetness) {

    /** A dry firm pitch. */
    public static final MatchWeather DRY = new MatchWeather(Surface.DRY, PitchConditions.DRY);

    public MatchWeather {
        if (surface == null || !(wetness >= 0 && wetness <= 1)) {
            throw new IllegalArgumentException(surface + " " + wetness);
        }
    }

    /** Ground key: the home club's stadium data id, else the club's data id, else its runtime id. */
    public static String groundOf(Club home) {
        if (home.getStadium() != null && home.getStadium().getDataId() != null) {
            return home.getStadium().getDataId();
        }
        return home.getDataId() != null ? home.getDataId() : "club-" + home.getId();
    }

    /** Weather of {@code ground} on {@code date}; DRY when the date is unknown. */
    public static MatchWeather of(RandomSource root, LocalDate date, String ground) {
        if (date == null) {
            return DRY;
        }
        double r = root.fork("weather:" + date + ":" + ground).nextDouble();
        Month m = date.getMonth();
        boolean winter = m == Month.DECEMBER || m == Month.JANUARY || m == Month.FEBRUARY;
        if (winter) {
            if (r < 0.10) {
                return new MatchWeather(Surface.FROZEN, PitchConditions.DRY);
            }
            if (r < 0.20) {
                return new MatchWeather(Surface.MUDDY, PitchConditions.WATERLOGGED);
            }
            if (r < 0.45) {
                return new MatchWeather(Surface.WET, PitchConditions.RAIN);
            }
            if (r < 0.65) {
                return new MatchWeather(Surface.WET, PitchConditions.DRIZZLE);
            }
            return new MatchWeather(Surface.DRY, PitchConditions.DAMP);
        }
        if (r < 0.05) {
            return new MatchWeather(Surface.MUDDY, PitchConditions.WATERLOGGED);
        }
        if (r < 0.25) {
            return new MatchWeather(Surface.WET, PitchConditions.RAIN);
        }
        if (r < 0.40) {
            return new MatchWeather(Surface.DRY, PitchConditions.DRIZZLE);
        }
        return DRY;
    }
}
