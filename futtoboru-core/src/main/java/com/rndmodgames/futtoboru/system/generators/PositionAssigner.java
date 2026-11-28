package com.rndmodgames.futtoboru.system.generators;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.system.Log;

/**
 * E-01b: positions for players the data gives none (generated people, a squad player without a {@code positions}
 * entry, an old save). No dice: a squad player without a position takes the 2-3-5 slot his squad is shortest of
 * (the rule the data's {@code placeholder} positions follow, docs/DATA_MODEL.md "Positions and dribbling"); a
 * generated player gets the line his seeded attributes suit best. Same seed, same positions.
 */
public final class PositionAssigner {

    /** Players per slot in the 2-3-5: 1 goalkeeper, 2 full backs, 3 half backs, one of each forward slot. */
    private static final Map<Position, Integer> TWO_THREE_FIVE = new EnumMap<>(Position.class);
    static {
        for (Position p : Position.values()) {
            TWO_THREE_FIVE.put(p, 1);
        }
        TWO_THREE_FIVE.put(Position.FULL_BACK, 2);
        TWO_THREE_FIVE.put(Position.HALF_BACK, 3);
    }

    private PositionAssigner() {
    }

    /**
     * Gives every player of the squad without a position, in squad order, the slot the squad is shortest of: the
     * line with the fewest players for what the 2-3-5 asks of it (1 / 2 / 3 / 5), then that line's slot with the
     * fewest; ties in {@link Position.Line} and {@link Position} order.
     *
     * @return how many players got a position
     */
    public static int fillMissing(List<Player> squad) {
        if (squad == null) {
            return 0;
        }
        Map<Position, Integer> count = counts(squad);
        int assigned = 0;
        for (Player player : squad) {
            if (player == null || player.getPosition() != null) {
                continue;
            }
            // first the line the squad is shortest of, then that line's shortest slot (count/target compared
            // cross-multiplied, whole numbers; ties to the earlier one)
            Position.Line line = null;
            for (Position.Line l : Position.Line.values()) {
                if (line == null || lineCount(count, l) * lineTarget(line) < lineCount(count, line) * lineTarget(l)) {
                    line = l;
                }
            }
            Position shortest = null;
            for (Position p : Position.values()) {
                if (p.getLine() == line && (shortest == null
                        || count.get(p) * TWO_THREE_FIVE.get(shortest) < count.get(shortest) * TWO_THREE_FIVE.get(p))) {
                    shortest = p;
                }
            }
            player.setPosition(shortest);
            count.merge(shortest, 1, Integer::sum);
            assigned++;
        }
        return assigned;
    }

    /** {@link #fillMissing(List)} for each club; logs the clubs that needed it. */
    public static int fillMissing(Iterable<Club> clubs, String why) {
        int total = 0;
        if (clubs == null) {
            return 0;
        }
        for (Club club : clubs) {
            if (club == null) {
                continue;
            }
            int n = fillMissing(club.getPlayers());
            if (n > 0) {
                Log.info(Log.DATA_GENERATION, "PositionAssigner", why + ": " + n + " players of " + club.getName()
                        + " got a 2-3-5 slot");
            }
            total += n;
        }
        return total;
    }

    /** @return true when the squad has a goalkeeper, two full backs, three half backs and five forwards */
    public static boolean canFieldTwoThreeFive(List<Player> squad) {
        int[] line = new int[Position.Line.values().length];
        if (squad != null) {
            for (Player p : squad) {
                if (p != null && p.getPosition() != null) {
                    line[p.getPosition().getLine().ordinal()]++;
                }
            }
        }
        return line[Position.Line.GOAL.ordinal()] >= 1 && line[Position.Line.BACK.ordinal()] >= 2
                && line[Position.Line.HALF.ordinal()] >= 3 && line[Position.Line.FORWARD.ordinal()] >= 5;
    }

    /**
     * The position a generated player's (seeded) attributes suit best: the line with the best average of its
     * skills (goal: shot stopping, area control, hand to hand, rushing out; back: tackling, marking, strength,
     * heading; half: passing, tackling, endurance, teamwork; forward: dribbling, kicking, speed, trick shots), ties
     * to the earlier line. A forward goes to the wing when speed is his best forward skill, inside when dribbling,
     * else centre; the side follows whether his dexterity is even. Whole-number maths, no dice.
     */
    public static Position forGenerated(Player p) {
        int goal = w(p.getShotStopping()) + w(p.getAreaControl()) + w(p.getHandToHand()) + w(p.getRushingOut());
        int back = w(p.getTackling()) + w(p.getMarking()) + w(p.getStrength()) + w(p.getHeading());
        int half = w(p.getPassing()) + w(p.getTackling()) + w(p.getEndurance()) + w(p.getTeamwork());
        int forward = w(p.getDribbling()) + w(p.getKicking()) + w(p.getSpeed()) + w(p.getTrickShots());
        int best = Math.max(Math.max(goal, back), Math.max(half, forward));
        if (goal == best) {
            return Position.GOALKEEPER;
        }
        if (back == best) {
            return Position.FULL_BACK;
        }
        if (half == best) {
            return Position.HALF_BACK;
        }
        boolean right = w(p.getDexterity()) % 2 == 0;
        int speed = w(p.getSpeed());
        int dribbling = w(p.getDribbling());
        int kicking = w(p.getKicking());
        if (kicking >= speed && kicking >= dribbling) {
            return Position.CENTRE_FORWARD;
        }
        if (speed >= dribbling) {
            return right ? Position.RIGHT_WING : Position.LEFT_WING;
        }
        return right ? Position.INSIDE_RIGHT : Position.INSIDE_LEFT;
    }

    private static Map<Position, Integer> counts(List<Player> squad) {
        Map<Position, Integer> count = new EnumMap<>(Position.class);
        for (Position p : Position.values()) {
            count.put(p, 0);
        }
        for (Player player : squad) {
            if (player != null && player.getPosition() != null) {
                count.merge(player.getPosition(), 1, Integer::sum);
            }
        }
        return count;
    }

    private static int lineCount(Map<Position, Integer> count, Position.Line line) {
        int n = 0;
        for (Position p : Position.values()) {
            n += p.getLine() == line ? count.get(p) : 0;
        }
        return n;
    }

    private static int lineTarget(Position.Line line) {
        int n = 0;
        for (Position p : Position.values()) {
            n += p.getLine() == line ? TWO_THREE_FIVE.get(p) : 0;
        }
        return n;
    }

    private static int w(Float v) {
        return v == null ? 10 : Math.round(v);
    }
}
