package com.rndmodgames.futtoboru.engine.team;

import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;

/**
 * G-01: what a man is worth in a place of the team sheet, on the attribute scale (3..23). The weights below are
 * <b>placeholders</b> (D30 A3, listed in docs/HANDOFF.md for review): no source gives 1888 line ratings; they only
 * have to rank men sensibly until the ECS engine (D20) takes over the match.
 */
public final class TeamRatings {

    /** A missing attribute counts as the 3d6 mean, as in {@code PlayerAttributeGenerator}. */
    static final double MISSING = 10.5;

    /** The middle of the generated attributes (3d6 + modifiers): the pivot of the style and captain modifiers. */
    static final double PIVOT = 11.0;

    private TeamRatings() {
    }

    /**
     * A man's skill for a line: keeper (shot stopping, area control, hand to hand, rushing out, punching, area
     * positioning), back (tackling, marking, heading, strength, positioning, kicking: the long clearance), half back
     * (passing, tackling, teamwork, endurance, stamina, positioning), forward (dribbling, kicking, speed,
     * acceleration, trick shots, one-twos). Plain averages.
     */
    public static double line(Player p, Position.Line line) {
        if (p == null || line == null) {
            return 0;
        }
        switch (line) {
        case GOAL:
            return avg(p.getShotStopping(), p.getAreaControl(), p.getHandToHand(), p.getRushingOut(), p.getPunching(),
                    p.getAreaPositioning());
        case BACK:
            return avg(p.getTackling(), p.getMarking(), p.getHeading(), p.getStrength(), p.getPositioning(),
                    p.getKicking());
        case HALF:
            return avg(p.getPassing(), p.getTackling(), p.getTeamwork(), p.getEndurance(), p.getStamina(),
                    p.getPositioning());
        default:
            return avg(p.getDribbling(), p.getKicking(), p.getSpeed(), p.getAcceleration(), p.getTrickShots(),
                    p.getOneTwos());
        }
    }

    /**
     * How well a man of position {@code natural} plays in a place of position {@code slot}: 1 in his own position,
     * 0.93 elsewhere in his line (an inside right on the left), 0.8 in the next line, 0.7 two lines away, 0.5 a
     * keeper outfield or an outfield man in goal; 0.85 when his position is unknown.
     */
    public static double fit(Position natural, Position slot) {
        if (slot == null) {
            return 0;
        }
        if (natural == null) {
            return 0.85;
        }
        if (natural == slot) {
            return 1.0;
        }
        Position.Line a = natural.getLine();
        Position.Line b = slot.getLine();
        if (a == b) {
            return 0.93;
        }
        if (a == Position.Line.GOAL || b == Position.Line.GOAL) {
            return 0.5;
        }
        return Math.abs(a.ordinal() - b.ordinal()) == 1 ? 0.8 : 0.7;
    }

    /** @return true when the man's own position is known and is not the place's */
    public static boolean outOfPosition(Player p, Position slot) {
        return p != null && p.getPosition() != null && slot != null && p.getPosition() != slot;
    }

    /** A man's rating in a place: his line skill for the place's line times {@link #fit}. */
    public static double inSlot(Player p, Position slot) {
        if (p == null || slot == null) {
            return 0;
        }
        return line(p, slot.getLine()) * fit(p.getPosition(), slot);
    }

    /** Finishing: kicking, long shots, heading, trick shots. */
    public static double finishing(Player p) {
        return avg(p.getKicking(), p.getLongShots(), p.getHeading(), p.getTrickShots());
    }

    /** What a style asks of a man: combination (passing, teamwork, one-twos), dribbling (dribbling, dexterity, trick shots). */
    public static double style(Player p, ClubStyle style) {
        if (style == ClubStyle.DRIBBLING) {
            return avg(p.getDribbling(), p.getDexterity(), p.getTrickShots());
        }
        return avg(p.getPassing(), p.getTeamwork(), p.getOneTwos());
    }

    /** Captaincy: leadership first, determination second (the AI's choice of captain). */
    public static double captaincy(Player p) {
        return v(p.getLeadership()) + 0.25 * v(p.getDetermination());
    }

    static double v(Float f) {
        return f != null ? f : MISSING;
    }

    private static double avg(Float... values) {
        double sum = 0;
        for (Float f : values) {
            sum += v(f);
        }
        return sum / values.length;
    }
}
