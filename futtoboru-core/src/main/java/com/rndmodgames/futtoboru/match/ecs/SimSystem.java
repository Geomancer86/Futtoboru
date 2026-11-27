package com.rndmodgames.futtoboru.match.ecs;

/**
 * Marker for a simulation system (docs/design/MATCH_ENGINE_ECS.md §1.1, §2): an Ashley {@code EntitySystem} that runs
 * once per fixed tick inside {@link MatchWorld#tick()}, may write simulation components and is covered by the
 * {@link StateHash}. It never reads the frame delta: one {@code update(float)} call is one tick of
 * {@link MatchWorld#tickSeconds()}.
 *
 * A system is either a {@code SimSystem} or a {@link DrawPass}, never both ({@link MatchWorld} refuses it).
 */
public interface SimSystem {
}
