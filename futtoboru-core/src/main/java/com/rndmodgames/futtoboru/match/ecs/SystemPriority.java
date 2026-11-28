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
    /** E-15 stand-in referee (line crossings, goals, restart placement) until the E-16 laws system replaces it. */
    public static final int RESTART_STAND_IN = 92;
    public static final int CROWD = 100;
    public static final int EVENT_DETECTION = 110;
    public static final int CLOCK = 120;

    // Draw passes (viewer, E-21; docs/design/MATCH_VIEWER_PLAN.md §2.2): run once per frame by MatchWorld.draw, in
    // this order. Bands leave gaps: a second pass in a band takes band + 1, + 2 ... and gets its own constant here.
    /** FRAME band: view state every later pass reads (interpolated transforms, camera, sky). */
    public static final int DRAW_FRAME = 1000;
    public static final int DRAW_INTERPOLATION = DRAW_FRAME;
    public static final int DRAW_CAMERA = 1002;
    public static final int DRAW_SKY = 1004;
    /** Match events turned into view events (particles, camera kicks, crowd reactions). */
    public static final int DRAW_EVENTS = 1010;
    /** Light sources gathered for the light map. */
    public static final int DRAW_LIGHTS = 1015;
    /** Skyline backdrop behind the far side of the ground (K1), with chimney smoke. */
    public static final int DRAW_SKYLINE = 1090;
    public static final int DRAW_PITCH = 1100;
    /** Mud, divots, footprints, wear overlay. */
    public static final int DRAW_MARKS = 1120;
    public static final int DRAW_SHADOWS = 1150;
    /** Touchline buildings behind the far bank (T1: pavilion, shed, tent, carriage, telegraph pole). */
    public static final int DRAW_TOUCHLINE_BACK = 1170;
    /** Crowd along the far touchline (behind the players in a three-quarter view). */
    public static final int DRAW_CROWD_BACK = 1180;
    /** Far touchline strip in front of the bank (T1: rope, press table, committee, trainer, pressman, constable). */
    public static final int DRAW_TOUCHLINE = 1190;
    /** Players, officials, ball and goal frames, depth-sorted together. */
    public static final int DRAW_ACTORS = 1200;
    /** The ball in the air above heads. */
    public static final int DRAW_OVERHEAD = 1250;
    public static final int DRAW_LIGHT = 1300;
    public static final int DRAW_PARTICLES = 1350;
    /** Crowd along the near touchline (in front of the players). */
    public static final int DRAW_CROWD_FRONT = 1400;
    /** Rain, snow, fog, cloud shadows. */
    public static final int DRAW_ATMOSPHERE = 1500;
    /** Shake, grade, flashes. */
    public static final int DRAW_CAMERA_FX = 1800;
    public static final int DRAW_DEBUG = 1850;
    public static final int DRAW_POST = 1900;

    private SystemPriority() {
    }
}
