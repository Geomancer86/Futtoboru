package com.rndmodgames.futtoboru.match.ecs.pitch;

/** The two sides of a match, as E-12 needs them (map to the ECS team component at integration). */
public enum Team {
    HOME, AWAY;

    public Team other() {
        return this == HOME ? AWAY : HOME;
    }
}
