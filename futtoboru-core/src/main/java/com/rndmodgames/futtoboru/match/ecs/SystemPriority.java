package com.rndmodgames.futtoboru.match.ecs;

/**
 * The one priority table of the match engine (§2): Ashley runs systems in ascending priority, so this table, not
 * registration order, fixes the order of a tick. Each lane adds its constant here and one line in
 * {@link MatchSystems}. Two systems may not share a priority ({@link MatchWorld} refuses it).
 */
public final class SystemPriority {

    // Simulation systems: one engine.update() = one tick.
    public static final int INPUT_COMMAND = 10;
    public static final int PERCEPTION = 20;
    public static final int DECISION = 30;
    public static final int OFFICIALS = 40;
    public static final int LOCOMOTION = 50;
    public static final int ACTION = 60;
    public static final int BALL = 70;
    public static final int CONTACT = 80;
    public static final int LAWS = 90;
    public static final int CROWD = 100;
    public static final int EVENT_DETECTION = 110;
    public static final int CLOCK = 120;

    // Draw passes (viewer, E-21): run once per frame by MatchWorld.draw, in this order.
    public static final int DRAW_INTERPOLATION = 1000;
    public static final int DRAW_PITCH = 1010;
    public static final int DRAW_PLAYERS = 1020;
    public static final int DRAW_BALL = 1030;
    public static final int DRAW_OFFICIALS = 1040;
    public static final int DRAW_CROWD = 1050;
    public static final int DRAW_VFX = 1060;
    public static final int DRAW_DEBUG = 1070;

    private SystemPriority() {
    }
}
