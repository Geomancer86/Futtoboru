package com.rndmodgames.futtoboru.match.ecs.body;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;

/**
 * A player's physical limits and stamina (E-13, §3.3), mapped once from {@link AthleteInputs} by
 * {@link #from(AthleteInputs, BodyParams)}. The limits are fixed for the match; stamina and reserve change every
 * tick ({@link LocomotionSystem}). Effective limits also depend on kit, surface and injury.
 */
public final class AthleteComponent implements Component, HashedComponent {

    public final Position position;
    /** Fresh top speed on firm turf in reference boots, m/s. */
    public final double topSpeed;
    /** Acceleration from standstill, m/s^2; braking is {@link #decel}. */
    public final double accel, decel;
    /** Turn rate standing and at top speed, rad/s; in between it falls with (v/top)^2. */
    public final double turnStanding, turnAtTop;
    /** 0..1 from the ratings: charges given (strength) and taken (balance = strength + dexterity). */
    public final double strength01, balance01;
    /** Short-term capacity (stamina rating) and recovery (endurance rating), ~0.7..1.3. */
    public final double capacity, recovery;
    /** Injury multiplier from age and condition, 1 = average. */
    public final double fragility;

    /** Short-term stamina 0..1: sprints use it, walking refills it. */
    public double stamina = 1;
    /** Match reserve 0..1: everything above standing uses it; half-time returns a little. */
    public double reserve = 1;
    /** In goal: may handle ({@link #isKeeper()}). */
    private boolean keeper;

    private AthleteComponent(Position position, double topSpeed, double accel, double decel, double turnStanding,
            double turnAtTop, double strength01, double balance01, double capacity, double recovery,
            double fragility) {
        this.position = position;
        this.topSpeed = topSpeed;
        this.accel = accel;
        this.decel = decel;
        this.turnStanding = turnStanding;
        this.turnAtTop = turnAtTop;
        this.strength01 = strength01;
        this.balance01 = balance01;
        this.capacity = capacity;
        this.recovery = recovery;
        this.fragility = fragility;
        this.keeper = position == Position.GOALKEEPER;
    }

    /** Maps ratings to physics (all D, BodyParams). */
    public static AthleteComponent from(AthleteInputs in, BodyParams p) {
        double speed = unit(in.speed(), p);
        double acc = unit(in.acceleration(), p);
        double agility = unit(in.dexterity(), p);
        double strength = unit(in.strength(), p);
        double accel = lerp(p.accelMin, p.accelMax, acc);
        double fragility = (1 + Math.max(0, in.age() - p.fragilityAgeFrom) * p.fragilityPerYear)
                * (1 + (p.fragilityAtZeroCondition - 1) * (1 - in.condition()));
        return new AthleteComponent(in.position(), lerp(p.topSpeedMin, p.topSpeedMax, speed), accel,
                accel * p.decelOverAccel, lerp(p.turnStandingMin, p.turnStandingMax, agility),
                lerp(p.turnAtTopMin, p.turnAtTopMax, agility), strength, 0.5 * strength + 0.5 * agility,
                0.7 + 0.6 * unit(in.stamina(), p), 0.7 + 0.6 * unit(in.endurance(), p), fragility);
    }

    /**
     * True for the man in goal, who may handle where the season's law allows ({@code RulesProfile#keeperHandling}).
     * The Laws name no keeper by position, only "the goalkeeper": starts as the GOALKEEPER position, and the player
     * AI hands it to whoever holds the keeper slot (E-16b: an outfielder replacing a keeper who went off).
     */
    public boolean isKeeper() {
        return keeper;
    }

    /** Puts this player in goal (or takes him out of it). */
    public void setKeeper(boolean inGoal) {
        this.keeper = inGoal;
    }

    /** Turn rate at speed v (fresh, unlimited grip): from standing to top-speed rate with (v/top)^2. */
    public double turnRate(double v) {
        double s = Math.min(1, v / topSpeed);
        return turnStanding + (turnAtTop - turnStanding) * s * s;
    }

    /** Half-time: short-term stamina full, part of the used reserve back. */
    public void halfTime(BodyParams p) {
        stamina = 1;
        reserve += (1 - reserve) * p.halfTimeReserveRecovery;
    }

    private static double unit(double rating, BodyParams p) {
        double u = (rating - p.ratingMin) / (p.ratingMax - p.ratingMin);
        return u < 0 ? 0 : (u > 1 ? 1 : u);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("ath.position", position.ordinal());
        h.mix("ath.top", topSpeed);
        h.mix("ath.accel", accel);
        h.mix("ath.decel", decel);
        h.mix("ath.turn0", turnStanding);
        h.mix("ath.turnTop", turnAtTop);
        h.mix("ath.strength", strength01);
        h.mix("ath.balance", balance01);
        h.mix("ath.capacity", capacity);
        h.mix("ath.recovery", recovery);
        h.mix("ath.fragility", fragility);
        h.mix("ath.stamina", stamina);
        h.mix("ath.reserve", reserve);
        h.mix("ath.keeper", keeper ? 1L : 0L);
    }
}
