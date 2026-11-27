package com.rndmodgames.futtoboru.match.ecs;

import com.badlogic.ashley.core.Component;

/** Creation index and role of every match entity; the index breaks ties and orders the state hash. */
public final class IdentityComponent implements Component, HashedComponent {

    /** 0-based creation index, unique per world. */
    public final int index;
    public final SpawnRole role;

    IdentityComponent(int index, SpawnRole role) {
        this.index = index;
        this.role = role;
    }

    @Override
    public void hashInto(StateHash hash) {
        hash.mix("id.index", index);
        hash.mix("id.role", role.ordinal());
    }
}
