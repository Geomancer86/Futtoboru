package com.rndmodgames.futtoboru.match.ecs;

/** One surface over the whole pitch and constant wetness for the match: the default until E-12 and the weather fork. */
public record UniformPitch(Surface surface, double wetness) implements PitchConditions {

    public UniformPitch {
        if (surface == null) {
            throw new IllegalArgumentException("surface");
        }
        if (!(wetness >= 0 && wetness <= 1)) {
            throw new IllegalArgumentException("wetness must be in 0..1: " + wetness);
        }
    }

    @Override
    public Surface surfaceAt(double x, double y) {
        return surface;
    }
}
