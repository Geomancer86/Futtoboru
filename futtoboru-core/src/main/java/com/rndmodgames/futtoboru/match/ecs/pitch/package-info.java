/**
 * E-12 (docs/design/MATCH_ENGINE_ECS.md §3.4): pitch and goal geometry, swept line crossings and post/bar contacts
 * along the ball path, and the 1888-89 restart decisions (throw-in, goal kick, corner, goal, kick-off).
 *
 * <p>Pure Java, headless, no libGDX, no static state, plain {@code double} arithmetic and {@code Math.sqrt} only
 * (bit-identical on every JVM, D22). Units are metres. Coordinates: origin at the centre of the pitch, {@code x}
 * along the length (goal lines at {@code x = ±length/2}, {@link com.rndmodgames.futtoboru.match.ecs.pitch.PitchEnd}),
 * {@code y} across (touchlines at {@code y = ±width/2}, {@link com.rndmodgames.futtoboru.match.ecs.pitch.Flank}),
 * {@code z} up from the ground.
 *
 * <p>Entry points: {@link com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams} (era and ground parameters),
 * {@link com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry#sweep(BallSegment)} (once per ball substep) and
 * {@link com.rndmodgames.futtoboru.match.ecs.pitch.RestartDecider} (crossing + last touch: restart).
 */
package com.rndmodgames.futtoboru.match.ecs.pitch;
