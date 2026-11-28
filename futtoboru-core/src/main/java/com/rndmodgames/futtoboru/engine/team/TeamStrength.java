package com.rndmodgames.futtoboru.engine.team;

import java.util.Map;

import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * G-01: the strength of the eleven who play, for the instant result ({@code MatchSimulator}). Only the men on the
 * sheet count, each by his rating in his place ({@link TeamRatings#inSlot}: out of position costs), weighted by line:
 * <ul>
 *   <li><b>defence</b> = keeper ×2, each back ×1.5, each half back ×1, over the shape's places;</li>
 *   <li><b>attack</b> = each forward ×1, each half back ×0.5;</li>
 * </ul>
 * An empty place (playing short) counts 0 in its line, and the side loses 3% more per missing man. Then small
 * modifiers: the shape (2-2-6 and 1-2-7 trade defence for attack), the style (up to ±4% on attack as the men suit it)
 * and the captain (up to ±2%). All weights and modifiers are <b>placeholders</b> (D30 A3) for the review list.
 *
 * @param attack        attack rating (attribute scale)
 * @param defence       defence rating (attribute scale)
 * @param men           men on the sheet (11 unless short)
 * @param outOfPosition men in a place that is not their own position
 */
public record TeamStrength(double attack, double defence, int men, int outOfPosition) {

    /** Defence weight per line (GOAL, BACK, HALF, FORWARD). */
    private static final double[] DEFENCE = { 2.0, 1.5, 1.0, 0.0 };
    /** Attack weight per line. */
    private static final double[] ATTACK = { 0.0, 0.0, 0.5, 1.0 };
    /** Lost per missing man, on top of his place counting 0. */
    static final double SHORT_PENALTY = 0.03;

    /** (attack, defence) multipliers per shape, placeholder: the forward-heavy shapes score more and let in more. */
    static double shapeAttack(TeamShape shape) {
        switch (shape) {
        case TWO_TWO_SIX:
            return 1.05;
        case ONE_TWO_SEVEN:
            return 1.09;
        default:
            return 1.0;
        }
    }

    static double shapeDefence(TeamShape shape) {
        switch (shape) {
        case TWO_TWO_SIX:
            return 0.94;
        case ONE_TWO_SEVEN:
            return 0.87;
        default:
            return 1.0;
        }
    }

    /** Scoring weight per place position, placeholder: forwards most, the centre most of all, keeper almost never. */
    static double positionScoringWeight(Position slot) {
        switch (slot) {
        case CENTRE_FORWARD:
            return 10;
        case INSIDE_RIGHT:
        case INSIDE_LEFT:
            return 8;
        case RIGHT_WING:
        case LEFT_WING:
            return 6;
        case HALF_BACK:
            return 1.5;
        case FULL_BACK:
            return 0.4;
        default:
            return 0.05;
        }
    }

    public double overall() {
        return (attack + defence) / 2;
    }

    /**
     * @param squad the club's squad by player id (men on the sheet who are not in it count as empty places)
     */
    public static TeamStrength of(TeamSheet sheet, Map<Long, Player> squad) {
        if (sheet == null) {
            return new TeamStrength(0, 0, 0, 0);
        }
        TeamShape shape = sheet.getShape();
        double att = 0, attW = 0, def = 0, defW = 0, styleSum = 0;
        int men = 0, out = 0, outfield = 0;
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            Position slot = shape.slot(s);
            int line = slot.getLine().ordinal();
            attW += ATTACK[line];
            defW += DEFENCE[line];
            Player p = player(sheet.getPlayerId(s), squad);
            if (p == null) {
                continue;
            }
            men++;
            if (TeamRatings.outOfPosition(p, slot)) {
                out++;
            }
            double r = TeamRatings.inSlot(p, slot);
            att += ATTACK[line] * r;
            def += DEFENCE[line] * r;
            if (slot.getLine() != Position.Line.GOAL) {
                styleSum += TeamRatings.style(p, sheet.getStyle());
                outfield++;
            }
        }
        if (men == 0) {
            return new TeamStrength(0, 0, 0, 0);
        }
        double attack = att / attW;
        double defence = def / defW;
        double cohesion = 1 - SHORT_PENALTY * (TeamShape.SLOTS - men);
        double style = outfield > 0 ? 1 + 0.04 * clamp((styleSum / outfield - TeamRatings.PIVOT) / 5) : 1;
        Player captain = player(sheet.getCaptainId(), squad);
        double lead = captain != null && sheet.isSelected(captain.getId())
                ? 1 + 0.02 * clamp((TeamRatings.v(captain.getLeadership()) - TeamRatings.PIVOT) / 6) : 1;
        attack *= shapeAttack(shape) * style * cohesion * lead;
        defence *= shapeDefence(shape) * cohesion * lead;
        return new TeamStrength(attack, defence, men, out);
    }

    /** The style that suits these eleven best (higher average of what it asks); ties to combination. */
    public static ClubStyle bestStyle(TeamSheet sheet, Map<Long, Player> squad) {
        double dribbling = 0, combination = 0;
        for (int s = 1; s < TeamShape.SLOTS; s++) {
            Player p = player(sheet.getPlayerId(s), squad);
            if (p != null) {
                dribbling += TeamRatings.style(p, ClubStyle.DRIBBLING);
                combination += TeamRatings.style(p, ClubStyle.COMBINATION);
            }
        }
        return dribbling > combination ? ClubStyle.DRIBBLING : ClubStyle.COMBINATION;
    }

    /**
     * Who scored a goal: a man of the sheet, weighted by the position of his place ({@link #positionScoringWeight})
     * times his finishing (relative to the pivot, at least 0.3). One draw of {@code random} per goal.
     *
     * @return the scorer, or null when nobody is on the sheet
     */
    public static Player pickScorer(TeamSheet sheet, Map<Long, Player> squad, RandomSource random) {
        if (sheet == null) {
            return null;
        }
        double[] w = new double[TeamShape.SLOTS];
        double total = 0;
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            Player p = player(sheet.getPlayerId(s), squad);
            if (p != null && p.getPerson() != null) {
                w[s] = positionScoringWeight(sheet.position(s))
                        * Math.max(0.3, TeamRatings.finishing(p) / TeamRatings.PIVOT);
                total += w[s];
            }
        }
        if (total <= 0) {
            return null;
        }
        double r = random.nextDouble() * total;
        Player last = null;
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            if (w[s] <= 0) {
                continue;
            }
            last = player(sheet.getPlayerId(s), squad);
            r -= w[s];
            if (r < 0) {
                return last;
            }
        }
        return last;
    }

    private static Player player(Long id, Map<Long, Player> squad) {
        return id == null || squad == null ? null : squad.get(id);
    }

    private static double clamp(double x) {
        return Math.max(-1, Math.min(1, x));
    }
}
