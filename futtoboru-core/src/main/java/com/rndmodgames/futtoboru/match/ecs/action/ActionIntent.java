package com.rndmodgames.futtoboru.match.ecs.action;

/** What a player wants to do with the ball (E-14); set in an {@link ActionCommand} by the AI (E-15) or tests. */
public enum ActionIntent {
    NONE,
    /** Push the ball ahead toward the target (dribble), or cushion it (first touch) when it comes fast. */
    DRIBBLE_TOUCH,
    /** Pass to the target point: along the ground when the target height is 0, lofted otherwise. */
    PASS,
    /** Lofted cross: the ball arrives at the target point at the target height. */
    CROSS,
    /** Shot at the target point (on the goal) at the target height; power sets the pace. */
    SHOT,
    /** Long high clearance toward the target point (lands there). */
    CLEAR,
    /** Header toward the target point and height; only with the ball in head range. */
    HEADER,
    /** Keeper: catch and hold (own half in 1888-89); a ball too fast to hold is parried. */
    KEEPER_CATCH,
    /** Keeper: punch toward the target (own half in 1888-89). */
    KEEPER_PUNCH,
    /** Two-handed throw from above the head toward the target (throw-in; a holding keeper's throw). */
    THROW_IN;

    /** Kicks with the foot (the keeper may punt them from his hands). */
    public boolean isKick() {
        return this == PASS || this == CROSS || this == SHOT || this == CLEAR;
    }
}
