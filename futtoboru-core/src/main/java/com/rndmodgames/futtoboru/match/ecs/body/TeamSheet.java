package com.rndmodgames.futtoboru.match.ecs.body;

import java.util.ArrayList;
import java.util.List;

/**
 * One side's eleven for the body simulation: the shape and one {@link AthleteInputs} per slot, in slot order
 * (slot 0 = keeper). Stand-in for the team sheet of the V2 {@code MatchInput} (E-17).
 */
public record TeamSheet(Shape shape, List<AthleteInputs> players) {

    public TeamSheet {
        if (shape == null || players == null || players.size() != 11) {
            throw new IllegalArgumentException("a shape and 11 players are required");
        }
        players = List.copyOf(players);
    }

    /** Eleven average players ({@link AthleteInputs#stub}) in the positions of {@code shape}. */
    public static TeamSheet stub(Shape shape) {
        List<AthleteInputs> list = new ArrayList<>(11);
        for (Shape.Slot s : shape.slots()) {
            list.add(AthleteInputs.stub(s.position()));
        }
        return new TeamSheet(shape, list);
    }
}
