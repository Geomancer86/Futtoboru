package com.rndmodgames.futtoboru.match.ecs.pitch;

/** Ways play restarts. FREE_KICK is not decided here (fouls/offside are E-16) but matters for "goal direct from". */
public enum RestartType {
    KICK_OFF, THROW_IN, GOAL_KICK, CORNER_KICK, FREE_KICK
}
