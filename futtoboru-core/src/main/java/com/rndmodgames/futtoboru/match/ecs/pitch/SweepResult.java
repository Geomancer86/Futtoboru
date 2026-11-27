package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * What happened to the ball along one substep. At most one of the two is set: the earliest event wins. After a
 * {@link #contact()} the rest of the segment is void (the ball bounces off the frame); after a {@link #crossing()}
 * the ball is out of play.
 */
public record SweepResult(FrameContact contact, LineCrossing crossing) {

    public static final SweepResult NOTHING = new SweepResult(null, null);

    public boolean hasContact() {
        return contact != null;
    }

    public boolean hasCrossing() {
        return crossing != null;
    }
}
