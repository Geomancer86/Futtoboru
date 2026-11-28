package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * Ways play restarts. FREE_KICK and PENALTY_KICK are given by the laws system (E-16: fouls, offside, handball);
 * PENALTY_KICK only from 1891 ({@code RulesProfile.penalties}), never in 1888-89.
 */
public enum RestartType {
    KICK_OFF, THROW_IN, GOAL_KICK, CORNER_KICK, FREE_KICK, PENALTY_KICK
}
