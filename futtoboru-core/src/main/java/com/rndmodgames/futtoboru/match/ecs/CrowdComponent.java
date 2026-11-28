package com.rndmodgames.futtoboru.match.ecs;

import com.badlogic.ashley.core.Component;

/**
 * The crowd as sections, not people (D22, E-18): plain arrays on one {@link SpawnRole#CROWD_SECTION} entity, written
 * only by {@link CrowdSystem} and hashed. Section {@code i} sits at ({@link #x}, {@link #y}) around the pitch; its
 * {@link #density} (0..1: how full it is), {@link #homeShare} (0..1 of its people who support the home side),
 * {@link #mood} (-1 angry .. 0 calm .. 1 cheering) and {@link #encroach} (0..1: how close the crowd is to the line).
 */
public final class CrowdComponent implements Component, HashedComponent {

    public final int count;
    public final double[] x;
    public final double[] y;
    /** Weight of the section for encroachment: 1 on a touchline, less behind a goal. */
    public final double[] touch;
    public final double[] density;
    public final double[] homeShare;
    public final double[] mood;
    public final double[] encroach;

    public CrowdComponent(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("sections " + count);
        }
        this.count = count;
        x = new double[count];
        y = new double[count];
        touch = new double[count];
        density = new double[count];
        homeShare = new double[count];
        mood = new double[count];
        encroach = new double[count];
    }

    @Override
    public void hashInto(StateHash hash) {
        hash.mix("crowd.count", count);
        for (int i = 0; i < count; i++) {
            hash.mix("crowd.density", density[i]);
            hash.mix("crowd.homeShare", homeShare[i]);
            hash.mix("crowd.mood", mood[i]);
            hash.mix("crowd.encroach", encroach[i]);
        }
    }
}
