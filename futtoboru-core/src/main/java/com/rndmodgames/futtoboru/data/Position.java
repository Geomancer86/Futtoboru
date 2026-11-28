package com.rndmodgames.futtoboru.data;

/**
 * A player's position in the 2-3-5 of 1888 (E-01): one goalkeeper, two full backs, three half backs and five
 * forwards. Half backs and full backs have no side in the data (the sources rarely give one); the five forward
 * slots do. The data value is {@link #getDataId()} (squads.json {@code positions}); saves store the enum name.
 *
 * It is a fact of a season (a squad entry), not of a person: players moved between lines over a career.
 * See docs/DATA_MODEL.md "Positions and dribbling".
 */
public enum Position {

    GOALKEEPER("goalkeeper", Line.GOAL),
    FULL_BACK("full-back", Line.BACK),
    HALF_BACK("half-back", Line.HALF),
    RIGHT_WING("right-wing", Line.FORWARD),
    INSIDE_RIGHT("inside-right", Line.FORWARD),
    CENTRE_FORWARD("centre-forward", Line.FORWARD),
    INSIDE_LEFT("inside-left", Line.FORWARD),
    LEFT_WING("left-wing", Line.FORWARD);

    /** The four lines of the era's shapes (2-3-5, 2-2-6, 1-2-7...). */
    public enum Line {
        GOAL, BACK, HALF, FORWARD
    }

    private final String dataId;
    private final Line line;

    Position(String dataId, Line line) {
        this.dataId = dataId;
        this.line = line;
    }

    /** @return the id used in the data ({@code centre-forward}) */
    public String getDataId() {
        return dataId;
    }

    public Line getLine() {
        return line;
    }

    /** @return the language bundle key of the position's name ({@code position_centre_forward}) */
    public String labelKey() {
        return "position_" + name().toLowerCase(java.util.Locale.ROOT);
    }

    /** @return the position with this data id, or null when unknown */
    public static Position fromDataId(String id) {
        if (id != null) {
            for (Position p : values()) {
                if (p.dataId.equals(id)) {
                    return p;
                }
            }
        }
        return null;
    }
}
