package com.rndmodgames.futtoboru.match.ecs;

/**
 * A simulation component that feeds the {@link StateHash}. It mixes every field that can change the future of the
 * match, in a fixed order, as exact bits. Its class must be listed in {@link MatchSystems#HASHED_COMPONENTS}.
 */
public interface HashedComponent {

    void hashInto(StateHash hash);
}
