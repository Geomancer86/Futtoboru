package com.rndmodgames.futtoboru.match.ecs.pitch;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Parameters of one pitch: ground size, goal frame, era equipment (nets) and era markings. Metres throughout.
 *
 * <p>1888-89 (V2 §1, D7): length 100-200 yd, breadth 50-100 yd (default 110 x 70 yd until lane H-02 supplies each
 * ground); goal 8 yd between the posts, crossbar compulsory (since 1882/83) with its underside 8 ft above the
 * ground; <b>no nets</b> (league-wide from 1891-92), so close calls at the posts and bar become disputed goals (D19-5);
 * no penalty line, goal area, centre circle or penalty area (1891, 1902). Post and bar thickness was not fixed by
 * the Laws in 1888 (placeholder: 5 in, the later maximum). {@code disputedMarginM} is the tunable band around the
 * frame in which a crossing is "too close to call" (E-20 calibrates it).
 */
public record PitchParams(double lengthM, double widthM, double goalWidthM, double crossbarHeightM,
        double postRadiusM, double crossbarRadiusM, boolean nets, Set<EraMarking> markings, double disputedMarginM) {

    public static final double YARD = 0.9144;
    public static final double FOOT = 0.3048;
    public static final double INCH = 0.0254;

    /** Laws of 1888-89: pitch length limits in yards. */
    public static final double MIN_LENGTH_YD = 100, MAX_LENGTH_YD = 200;
    /** Laws of 1888-89: pitch breadth limits in yards. */
    public static final double MIN_WIDTH_YD = 50, MAX_WIDTH_YD = 100;
    /** Default ground until per-ground data exists (MATCH_ENGINE_ECS.md §3.4). */
    public static final double DEFAULT_LENGTH_YD = 110, DEFAULT_WIDTH_YD = 70;
    /** Default disputed-goal band around the frame (placeholder until E-20). */
    public static final double DEFAULT_DISPUTED_MARGIN_M = 0.10;

    public PitchParams {
        if (!(lengthM > 0) || !(widthM > 0) || !(goalWidthM > 0) || !(crossbarHeightM > 0)
                || !(postRadiusM > 0) || !(crossbarRadiusM > 0) || !(disputedMarginM >= 0)) {
            throw new IllegalArgumentException("pitch dimensions must be positive");
        }
        if (goalWidthM + 4 * postRadiusM >= widthM) {
            throw new IllegalArgumentException("goal wider than the pitch");
        }
        markings = markings == null || markings.isEmpty() ? Collections.unmodifiableSet(EnumSet.noneOf(EraMarking.class))
                : Collections.unmodifiableSet(EnumSet.copyOf(markings));
    }

    /** The 1888-89 default ground: 110 x 70 yd, no nets, no markings. */
    public static PitchParams defaults1888() {
        return laws1888(DEFAULT_LENGTH_YD, DEFAULT_WIDTH_YD);
    }

    /** A ground of the given size (yards) under the 1888-89 Laws; throws if outside the Laws' limits. */
    public static PitchParams laws1888(double lengthYd, double widthYd) {
        if (lengthYd < MIN_LENGTH_YD || lengthYd > MAX_LENGTH_YD || widthYd < MIN_WIDTH_YD || widthYd > MAX_WIDTH_YD) {
            throw new IllegalArgumentException("1888 Laws allow 100-200 x 50-100 yd, got " + lengthYd + " x " + widthYd);
        }
        double frame = 2.5 * INCH;
        return new PitchParams(lengthYd * YARD, widthYd * YARD, 8 * YARD, 8 * FOOT, frame, frame, false,
                EnumSet.noneOf(EraMarking.class), DEFAULT_DISPUTED_MARGIN_M);
    }

    /**
     * Equipment and markings of the season starting in {@code seasonStartYear} (ERA_TIMELINE.md): nets from 1891-92;
     * penalty line, goal-area arcs and centre circle from 1891-92; the 1902 penalty area replaces line and arcs from
     * 1902-03. Pitch size limits are those of 1888 (later changes are not modelled yet).
     */
    public static PitchParams forSeason(int seasonStartYear, double lengthYd, double widthYd) {
        PitchParams base = laws1888(lengthYd, widthYd);
        EnumSet<EraMarking> marks = EnumSet.noneOf(EraMarking.class);
        if (seasonStartYear >= 1891) {
            marks.add(EraMarking.CENTRE_CIRCLE_1891);
            if (seasonStartYear >= 1902) {
                marks.add(EraMarking.PENALTY_AREA_1902);
            } else {
                marks.add(EraMarking.PENALTY_LINE_1891);
                marks.add(EraMarking.GOAL_AREA_ARCS_1891);
            }
        }
        return base.withNets(seasonStartYear >= 1891).withMarkings(marks);
    }

    public PitchParams withNets(boolean withNets) {
        return new PitchParams(lengthM, widthM, goalWidthM, crossbarHeightM, postRadiusM, crossbarRadiusM, withNets,
                markings, disputedMarginM);
    }

    public PitchParams withMarkings(Set<EraMarking> newMarkings) {
        return new PitchParams(lengthM, widthM, goalWidthM, crossbarHeightM, postRadiusM, crossbarRadiusM, nets,
                newMarkings, disputedMarginM);
    }

    public PitchParams withDisputedMargin(double marginM) {
        return new PitchParams(lengthM, widthM, goalWidthM, crossbarHeightM, postRadiusM, crossbarRadiusM, nets,
                markings, marginM);
    }

    public boolean has(EraMarking marking) {
        return markings.contains(marking);
    }
}
