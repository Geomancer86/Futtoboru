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
 * @param kicking      foot speed of a full swing (shots, clearances, long kicks) and their accuracy (E-14)
 * @param technique    passing and crossing accuracy, first touch (E-14)
 * @param heading      header accuracy, drive and jump (E-14)
 * @param handling     keeper catching and punching (E-14)
 * @param dribbling    close control: dribble touches and first touch (E-01 people data, D19-7)
 */
public record AthleteInputs(Position position, double speed, double acceleration, double dexterity, double stamina,
        double endurance, double strength, double age, double condition, Boolean shinPads, double kicking,
        double technique, double heading, double handling, double dribbling) {

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

    /** Body ratings only; the ball skills (E-14) are {@link #AVERAGE}. */
    public AthleteInputs(Position position, double speed, double acceleration, double dexterity, double stamina,
            double endurance, double strength, double age, double condition, Boolean shinPads) {
        this(position, speed, acceleration, dexterity, stamina, endurance, strength, age, condition, shinPads,
                AVERAGE, AVERAGE, AVERAGE, AVERAGE, AVERAGE);
    }

    /** An average 25-year-old at full fitness: the stub until real ratings reach the match input (E-15/E-17). */
    public static AthleteInputs stub(Position position) {
        return new AthleteInputs(position, AVERAGE, AVERAGE, AVERAGE, AVERAGE, AVERAGE, AVERAGE, 25, 1.0, null);
    }

    public AthleteInputs withSpeed(double rating) {
        return new AthleteInputs(position, rating, acceleration, dexterity, stamina, endurance, strength, age,
                condition, shinPads, kicking, technique, heading, handling, dribbling);
    }

    public AthleteInputs withShinPads(Boolean pads) {
        return new AthleteInputs(position, speed, acceleration, dexterity, stamina, endurance, strength, age,
                condition, pads, kicking, technique, heading, handling, dribbling);
    }

    /** The same player with these ball skills (E-14), all on the 3..23 scale. */
    public AthleteInputs withSkills(double kicking, double technique, double heading, double handling,
            double dribbling) {
        return new AthleteInputs(position, speed, acceleration, dexterity, stamina, endurance, strength, age,
                condition, shinPads, kicking, technique, heading, handling, dribbling);
    }
}
