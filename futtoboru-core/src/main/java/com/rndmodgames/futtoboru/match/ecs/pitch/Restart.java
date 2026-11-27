package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * A restart of play: data only, no player logic.
 *
 * @param type              kick-off, throw-in, goal kick or corner
 * @param team              the side that takes it
 * @param spot              where the ball is placed (or thrown from, on the touchline), {@code z = 0}
 * @param offsideApplies    whether a team-mate can be offside from it (1888-89: yes from a throw-in and a kick-off,
 *                          never from a goal kick or a corner)
 * @param goalDirectAllowed whether the taker can score straight from it without anyone else touching the ball
 * @param twoHandsOverhead  throw-in only: must be thrown with both hands from above the head (since 1883)
 */
public record Restart(RestartType type, Team team, Vec3 spot, boolean offsideApplies, boolean goalDirectAllowed,
        boolean twoHandsOverhead) {
}
