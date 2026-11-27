package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * The ball touching a post or the crossbar within a substep.
 *
 * @param end        the goal hit
 * @param member     which part of the frame
 * @param t          fraction (0..1) of the substep at first contact
 * @param ballCentre the ball centre at contact
 * @param point      the contact point on the surface of the frame
 * @param normal     unit normal at the contact, pointing from the frame towards the ball centre; the ball system
 *                   reflects the velocity component along it ({@code v - (1 + e)(v.n)n}) and integrates the rest of the
 *                   substep from {@code ballCentre}
 */
public record FrameContact(PitchEnd end, FrameMember member, double t, Vec3 ballCentre, Vec3 point, Vec3 normal) {
}
