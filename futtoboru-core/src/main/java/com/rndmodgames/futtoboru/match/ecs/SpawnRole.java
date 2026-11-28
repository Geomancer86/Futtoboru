package com.rndmodgames.futtoboru.match.ecs;

/**
 * What an entity is, in the order entities must be created (§2.1 rule 3): home 1..11, away 1..11, ball, officials,
 * crowd sections, then the two sides' team entities (E-15 tactics: home, away). {@link MatchWorld#spawn(SpawnRole)} refuses a role that comes before one already spawned, so the
 * creation order, and with it Ashley's iteration order and the state hash, is the same in every run.
 */
public enum SpawnRole {
    HOME_PLAYER,
    AWAY_PLAYER,
    BALL,
    OFFICIAL,
    CROWD_SECTION,
    /** One per side, home first: team-level state such as the AI's tactics (E-15). */
    TEAM
}
