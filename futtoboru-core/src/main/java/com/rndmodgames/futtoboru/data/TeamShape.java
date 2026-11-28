package com.rndmodgames.futtoboru.data;

import java.util.List;
import java.util.Locale;

/**
 * The era's team shapes (D19-6, G-01): eleven places, keeper first, then backs, half backs and forwards. 2-3-5 was
 * the usual shape of 1888 (Preston's); 2-2-6 and 1-2-7 are the older, forward-heavy ones.
 *
 * <p>Slot {@code i} here is slot {@code i} of the ECS engine's {@code match.ecs.body.Shape} with the same
 * {@link #label()} (same {@link Position} in the same order; {@code TeamShapeTests} checks it), so a
 * {@link TeamSheet} maps onto the engine's line-up one to one.
 */
public enum TeamShape {

    TWO_THREE_FIVE("2-3-5", Position.GOALKEEPER, Position.FULL_BACK, Position.FULL_BACK, Position.HALF_BACK,
            Position.HALF_BACK, Position.HALF_BACK, Position.RIGHT_WING, Position.INSIDE_RIGHT,
            Position.CENTRE_FORWARD, Position.INSIDE_LEFT, Position.LEFT_WING),

    TWO_TWO_SIX("2-2-6", Position.GOALKEEPER, Position.FULL_BACK, Position.FULL_BACK, Position.HALF_BACK,
            Position.HALF_BACK, Position.RIGHT_WING, Position.INSIDE_RIGHT, Position.CENTRE_FORWARD,
            Position.CENTRE_FORWARD, Position.INSIDE_LEFT, Position.LEFT_WING),

    ONE_TWO_SEVEN("1-2-7", Position.GOALKEEPER, Position.FULL_BACK, Position.HALF_BACK, Position.HALF_BACK,
            Position.RIGHT_WING, Position.INSIDE_RIGHT, Position.CENTRE_FORWARD, Position.CENTRE_FORWARD,
            Position.CENTRE_FORWARD, Position.INSIDE_LEFT, Position.LEFT_WING);

    /** Places on a team sheet. */
    public static final int SLOTS = 11;

    private final String label;
    private final List<Position> slots;

    TeamShape(String label, Position... slots) {
        if (slots.length != SLOTS || slots[0] != Position.GOALKEEPER) {
            throw new IllegalArgumentException(label + ": 11 slots, keeper first");
        }
        this.label = label;
        this.slots = List.of(slots);
    }

    /** "2-3-5" (also the label of the ECS engine's shape). */
    public String label() {
        return label;
    }

    /** The position of each place, slot 0 = keeper. */
    public List<Position> slots() {
        return slots;
    }

    public Position slot(int i) {
        return slots.get(i);
    }

    /** @return places of this shape in {@code line} (2-3-5: 1 / 2 / 3 / 5) */
    public int count(Position.Line line) {
        int n = 0;
        for (Position p : slots) {
            if (p.getLine() == line) {
                n++;
            }
        }
        return n;
    }

    /** @return the language bundle key of the tooltip that describes the shape ({@code tip_shape_two_three_five}) */
    public String tipKey() {
        return "tip_shape_" + name().toLowerCase(Locale.ROOT);
    }

    /** @return the shape with this label ("2-3-5"), or null */
    public static TeamShape fromLabel(String label) {
        for (TeamShape s : values()) {
            if (s.label.equals(label)) {
                return s;
            }
        }
        return null;
    }
}
