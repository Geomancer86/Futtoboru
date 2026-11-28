package com.rndmodgames.futtoboru.match.ecs.body;

import java.util.List;

import com.rndmodgames.futtoboru.data.Position;

/**
 * The era's team shapes (D19-6; MATCH_ENGINE_V2.md §1): 2-3-5 (the 1880s standard, Preston's), 2-2-6 and 1-2-7
 * (older, forward-heavy). Each slot is a position and a kick-off spot in the team's own frame: {@code depth} from
 * the own goal line toward halfway as a share of half the length (1 = front line, placed by the kick-off law), and
 * {@code lateral} as a share of half the width, positive to the left when attacking. Slot 0 is the keeper.
 * Spots are D (period line-up diagrams show the lines, not distances); E-15 shifts them in play.
 */
public enum Shape {

    TWO_THREE_FIVE("2-3-5", 8, List.of(
            new Slot(Position.GOALKEEPER, 0.02, 0),
            new Slot(Position.FULL_BACK, 0.30, -0.25), new Slot(Position.FULL_BACK, 0.30, 0.25),
            new Slot(Position.HALF_BACK, 0.55, -0.55), new Slot(Position.HALF_BACK, 0.55, 0), new Slot(Position.HALF_BACK, 0.55, 0.55),
            new Slot(Position.RIGHT_WING, 1, -0.80), new Slot(Position.INSIDE_RIGHT, 1, -0.30), new Slot(Position.CENTRE_FORWARD, 1, 0),
            new Slot(Position.INSIDE_LEFT, 1, 0.30), new Slot(Position.LEFT_WING, 1, 0.80))),

    TWO_TWO_SIX("2-2-6", 7, List.of(
            new Slot(Position.GOALKEEPER, 0.02, 0),
            new Slot(Position.FULL_BACK, 0.30, -0.25), new Slot(Position.FULL_BACK, 0.30, 0.25),
            new Slot(Position.HALF_BACK, 0.55, -0.35), new Slot(Position.HALF_BACK, 0.55, 0.35),
            new Slot(Position.RIGHT_WING, 1, -0.80), new Slot(Position.INSIDE_RIGHT, 1, -0.40), new Slot(Position.CENTRE_FORWARD, 1, -0.12),
            new Slot(Position.CENTRE_FORWARD, 1, 0.12), new Slot(Position.INSIDE_LEFT, 1, 0.40), new Slot(Position.LEFT_WING, 1, 0.80))),

    ONE_TWO_SEVEN("1-2-7", 7, List.of(
            new Slot(Position.GOALKEEPER, 0.02, 0),
            new Slot(Position.FULL_BACK, 0.35, 0),
            new Slot(Position.HALF_BACK, 0.60, -0.35), new Slot(Position.HALF_BACK, 0.60, 0.35),
            new Slot(Position.RIGHT_WING, 1, -0.85), new Slot(Position.INSIDE_RIGHT, 1, -0.50), new Slot(Position.CENTRE_FORWARD, 1, -0.20),
            new Slot(Position.CENTRE_FORWARD, 1, 0), new Slot(Position.CENTRE_FORWARD, 1, 0.20), new Slot(Position.INSIDE_LEFT, 1, 0.50),
            new Slot(Position.LEFT_WING, 1, 0.85)));

    /** One place in a shape. */
    public record Slot(Position position, double depth, double lateral) {
    }

    private final String label;
    private final int kicker;
    private final List<Slot> slots;

    Shape(String label, int kicker, List<Slot> slots) {
        if (slots.size() != 11 || slots.get(0).position() != Position.GOALKEEPER) {
            throw new IllegalArgumentException(label + ": 11 slots, keeper first");
        }
        this.label = label;
        this.kicker = kicker;
        this.slots = slots;
    }

    /** "2-3-5". */
    public String label() {
        return label;
    }

    /** Slot of the forward who takes the kick-off (stands at the ball). */
    public int kickerSlot() {
        return kicker;
    }

    public List<Slot> slots() {
        return slots;
    }

    public Slot slot(int i) {
        return slots.get(i);
    }
}
