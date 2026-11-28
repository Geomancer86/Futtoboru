package com.rndmodgames.futtoboru.engine.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.ClubStyle;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamShape;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * G-01: picking the eleven. The best man per place from his attributes and position ({@link TeamRatings#inSlot}),
 * with an optional seeded "form" of a few percent per man (the committee's view on the day). Greedy over every
 * (man, place) pair, best first, ties to the lower place and then the lower player id: deterministic, and a man is
 * only put out of position when that is still his best use.
 */
public final class TeamSelector {

    /** Spread of the seeded form factor (standard deviation, clamped at two). */
    static final double FORM_SPREAD = 0.03;

    private TeamSelector() {
    }

    /** The form factor per man from {@code rng} (one fork per player id, so squad order does not matter); 1 without rng. */
    public static Map<Long, Double> form(List<Player> men, RandomSource rng) {
        Map<Long, Double> form = new HashMap<>();
        for (Player p : men) {
            if (p != null && p.getId() != null) {
                double f = 1;
                if (rng != null) {
                    double g = Math.max(-2, Math.min(2, rng.fork("form:" + p.getId()).nextGaussian()));
                    f = 1 + FORM_SPREAD * g;
                }
                form.put(p.getId(), f);
            }
        }
        return form;
    }

    /**
     * Fills the empty places of {@code sheet} from the men of {@code available} not on it yet, best pairs first.
     * With fewer men than places, the places left stay empty (the club plays short). Names a captain when the
     * sheet has none (or he is not on it any more).
     *
     * @param form a factor per player id ({@link #form}); null = 1 for everybody
     */
    public static void fillEmpty(TeamSheet sheet, List<Player> available, Map<Long, Double> form) {
        List<Player> free = new ArrayList<>();
        for (Player p : available) {
            if (p != null && p.getId() != null && !sheet.isSelected(p.getId())) {
                free.add(p);
            }
        }
        List<Integer> open = new ArrayList<>();
        for (int s = 0; s < TeamShape.SLOTS; s++) {
            if (sheet.getPlayerId(s) == null) {
                open.add(s);
            }
        }
        if (!free.isEmpty() && !open.isEmpty()) {
            List<double[]> pairs = new ArrayList<>(); // {score, slot, player index}
            for (int i = 0; i < free.size(); i++) {
                Player p = free.get(i);
                double f = form != null ? form.getOrDefault(p.getId(), 1.0) : 1.0;
                for (int s : open) {
                    pairs.add(new double[] { TeamRatings.inSlot(p, sheet.position(s)) * f, s, i });
                }
            }
            pairs.sort((a, b) -> {
                int c = Double.compare(b[0], a[0]);
                if (c != 0) {
                    return c;
                }
                c = Double.compare(a[1], b[1]);
                if (c != 0) {
                    return c;
                }
                return Long.compare(free.get((int) a[2]).getId(), free.get((int) b[2]).getId());
            });
            boolean[] used = new boolean[free.size()];
            for (double[] pair : pairs) {
                int s = (int) pair[1];
                int i = (int) pair[2];
                if (!used[i] && sheet.getPlayerId(s) == null) {
                    sheet.setPlayerId(s, free.get(i).getId());
                    used[i] = true;
                }
            }
        }
        if (sheet.getCaptainId() == null || !sheet.isSelected(sheet.getCaptainId())) {
            sheet.setCaptainId(captain(sheet, available));
        }
    }

    /** The eleven for {@code shape} from scratch (style kept as given, captain named). */
    public static TeamSheet pick(Long clubId, TeamShape shape, ClubStyle style, List<Player> available,
            Map<Long, Double> form) {
        TeamSheet sheet = new TeamSheet(clubId, shape, style);
        fillEmpty(sheet, available, form);
        return sheet;
    }

    /** How much stronger another shape must be before a committee leaves the 2-3-5 of 1888 (placeholder). */
    static final double USUAL_SHAPE_MARGIN = 0.03;

    /**
     * What an AI committee picks (and the first sheet of the player's club): for each era shape the best eleven and
     * the style that suits them, then the 2-3-5 (the usual shape of 1888) unless another shape's side is at least
     * {@link #USUAL_SHAPE_MARGIN} stronger overall ({@link TeamStrength#overall()}; ties to the earlier shape).
     * <b>Placeholder</b> (D30 A3): no sourced club shapes or styles yet, so the committee plays to its men; club
     * data, once researched, would fix them instead.
     */
    public static TeamSheet bestSheet(Long clubId, List<Player> available, Map<Long, Double> form) {
        Map<Long, Player> squad = new HashMap<>();
        for (Player p : available) {
            if (p != null && p.getId() != null) {
                squad.put(p.getId(), p);
            }
        }
        TeamSheet best = null;
        double bestOverall = -1;
        for (TeamShape shape : TeamShape.values()) {
            TeamSheet sheet = pick(clubId, shape, ClubStyle.COMBINATION, available, form);
            sheet.setStyle(TeamStrength.bestStyle(sheet, squad));
            double overall = TeamStrength.of(sheet, squad).overall();
            if (shape == TeamShape.TWO_THREE_FIVE) {
                overall *= 1 + USUAL_SHAPE_MARGIN;
            }
            if (overall > bestOverall + 1e-9) {
                best = sheet;
                bestOverall = overall;
            }
        }
        return best;
    }

    /** The man of the sheet with the best captaincy ({@link TeamRatings#captaincy}); ties to the lower id. */
    public static Long captain(TeamSheet sheet, List<Player> squad) {
        Player best = null;
        for (Player p : squad) {
            if (p == null || p.getId() == null || !sheet.isSelected(p.getId())) {
                continue;
            }
            if (best == null || TeamRatings.captaincy(p) > TeamRatings.captaincy(best)
                    || (TeamRatings.captaincy(p) == TeamRatings.captaincy(best) && p.getId() < best.getId())) {
                best = p;
            }
        }
        return best != null ? best.getId() : null;
    }
}
