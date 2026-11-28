package com.rndmodgames.futtoboru.match.ecs.body;

import com.rndmodgames.futtoboru.data.Position;

/**
 * What the body simulation needs from one player, copied out of the game data by the caller (the match input,
 * E-17), so {@code match.ecs} never reads {@code Player} itself. Ratings on the people data's 3..23 scale (E-01).
 *
 * @param position     this season's position (E-01); sets the physique default (D22) and marks the keeper
 * @param speed        top speed
 * @param acceleration acceleration from standstill
 * @param dexterity    agility: turn rate
 * @param stamina      short-term capacity (sprints)
 * @param endurance    recovery and match reserve
 * @param strength     shoulder charges given and taken
 * @param age          years, for fragility
 * @param condition    fitness 0..1 at kick-off (E-01 condition), for fragility
 * @param shinPads     wears shin pads; null = decided by the season's {@link KitParams#shinPadShare}
 */
public record AthleteInputs(Position position, double speed, double acceleration, double dexterity, double stamina,
        double endurance, double strength, double age, double condition, Boolean shinPads) {

    /** Middle of the 3..23 scale. */
    public static final double AVERAGE = 13;

    public AthleteInputs {
        if (position == null) {
            throw new IllegalArgumentException("position");
        }
        if (!(condition >= 0 && condition <= 1) || !(age > 0)) {
            throw new IllegalArgumentException("condition " + condition + ", age " + age);
        }
    }

    /** An average 25-year-old at full fitness: the stub until real ratings reach the match input (E-15/E-17). */
    public static AthleteInputs stub(Position position) {
        return new AthleteInputs(position, AVERAGE, AVERAGE, AVERAGE, AVERAGE, AVERAGE, AVERAGE, 25, 1.0, null);
    }

    public AthleteInputs withSpeed(double rating) {
        return new AthleteInputs(position, rating, acceleration, dexterity, stamina, endurance, strength, age,
                condition, shinPads);
    }

    public AthleteInputs withShinPads(Boolean pads) {
        return new AthleteInputs(position, speed, acceleration, dexterity, stamina, endurance, strength, age,
                condition, pads);
    }
}
