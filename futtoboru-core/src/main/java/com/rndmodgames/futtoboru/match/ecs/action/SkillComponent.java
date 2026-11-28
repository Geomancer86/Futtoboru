package com.rndmodgames.futtoboru.match.ecs.action;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteInputs;
import com.rndmodgames.futtoboru.match.ecs.body.BodyParams;

/**
 * A player's ball skills (E-14) as 0..1 shares of the rating scale, mapped once from {@link AthleteInputs}. Fixed for
 * the match. Kept apart from the body's {@code AthleteComponent} so the body physics stay untouched.
 */
public final class SkillComponent implements Component, HashedComponent {

    /** Average in every skill (players without a {@code SkillComponent} act with these). */
    public static final SkillComponent AVERAGE = new SkillComponent(0.5, 0.5, 0.5, 0.5, 0.5);

    /** Full-swing foot speed and accuracy of shots and clearances. */
    public final double kicking;
    /** Passing and crossing accuracy, first touch. */
    public final double technique;
    /** Header accuracy, drive and jump. */
    public final double heading;
    /** Keeper catching and punching. */
    public final double handling;
    /** Close control: dribble touches and first touch (E-01 {@code dribbling}). */
    public final double dribbling;

    public SkillComponent(double kicking, double technique, double heading, double handling, double dribbling) {
        this.kicking = kicking;
        this.technique = technique;
        this.heading = heading;
        this.handling = handling;
        this.dribbling = dribbling;
    }

    public static SkillComponent from(AthleteInputs in, BodyParams p) {
        return new SkillComponent(unit(in.kicking(), p), unit(in.technique(), p), unit(in.heading(), p),
                unit(in.handling(), p), unit(in.dribbling(), p));
    }

    private static double unit(double rating, BodyParams p) {
        double u = (rating - p.ratingMin) / (p.ratingMax - p.ratingMin);
        return u < 0 ? 0 : (u > 1 ? 1 : u);
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("skill.kicking", kicking);
        h.mix("skill.technique", technique);
        h.mix("skill.heading", heading);
        h.mix("skill.handling", handling);
        h.mix("skill.dribbling", dribbling);
    }
}
