package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;

/**
 * Where {@link InterpolationPass} reads a drawn entity's simulation position. One source per kind of body (ball now;
 * player and official bodies from E-13 add theirs in {@code MatchSystems.installDrawPasses}). A source only reads.
 */
public interface PoseSource {

    /** The entities this source reads; a static family. */
    Family family();

    /** Writes the entity's simulation position (metres; x along the pitch, y across, z up) into {@code xyz[0..2]}. */
    void read(Entity entity, double[] xyz);
}
