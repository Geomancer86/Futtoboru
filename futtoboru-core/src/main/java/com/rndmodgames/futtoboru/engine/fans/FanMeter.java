package com.rndmodgames.futtoboru.engine.fans;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.FanStanding;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.history.HistoryFacts;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * G-10: the fan-love meter. Each club-player pair has a points meter ({@link FanStanding} on the player); the tier
 * (0 unknown .. 5 legend) is read from it with hysteresis: up only at {@code (t+1)*band + hysteresis}, down only below
 * {@code t*band - hysteresis}, so a tier does not flip every week. A new pair starts in the middle of its tier's band,
 * the tier from the season's {@code fanTiers} (or 1 for a real, sourced man, 0 for a placeholder or generated one).
 *
 * <p>After every recorded match, for both clubs alike (D31): a man on the frozen team sheet gains the appearance
 * points, each goal its goal points (more against a listed rival), a cup tie won and a final won add to every man on
 * the sheet; a squad man left out loses the absence points. A legend loses at the damped rate. The side's points are
 * scaled by one seeded draw per match and club ({@code fork("fans:<match>:<club>")}). Weights: {@code fans.json}
 * (estimates). The tier never changes match physics (H-01 §4).
 */
public final class FanMeter {

    private FanMeter() {
    }

    /** The tier a meter at {@code points} shows, coming from {@code tier} (hysteresis). */
    public static int nextTier(int tier, double points, FanRules r) {
        double band = r.band();
        double h = r.hysteresis();
        int t = Math.max(0, Math.min(HistoryFacts.MAX_TIER, tier));
        while (t < HistoryFacts.MAX_TIER && points >= (t + 1) * band + h) {
            t++;
        }
        while (t > 0 && points < t * band - h) {
            t--;
        }
        return t;
    }

    /** The meter value a pair starts from: the middle of the tier's band. */
    public static double startPoints(int tier, FanRules r) {
        return tier * r.band() + r.band() / 2;
    }

    /** Adds {@code delta} meter points (damped for a legend losing them) and moves the tier. */
    public static void add(FanStanding s, double delta, FanRules r) {
        double d = delta < 0 && s.getTier() >= HistoryFacts.MAX_TIER ? delta * r.get(FanRules.LEGEND_DAMPING) : delta;
        double max = (HistoryFacts.MAX_TIER + 1) * r.band();
        double points = Math.max(0, Math.min(max, s.getPoints() + d));
        s.setPoints(points);
        s.setTier(nextTier(s.getTier(), points, r));
    }

    /** The tier a club's fans give a player now: his meter, else his start tier from the data. */
    public static int tierOf(Player p, Club club) {
        if (p != null && p.getFans() != null) {
            return p.getFans().getTier();
        }
        return startTier(DatabaseLoader.getGameData(), p, club);
    }

    /**
     * How far his meter is towards the next tier's threshold, 0..100 (from the bottom of his tier's band), or -1 at
     * the top tier.
     */
    public static int percentToNext(Player p, Club club) {
        FanRules r = FanRules.current();
        int tier = tierOf(p, club);
        if (tier >= HistoryFacts.MAX_TIER) {
            return -1;
        }
        double points = p != null && p.getFans() != null ? p.getFans().getPoints() : startPoints(tier, r);
        double from = tier * r.band() - (tier == 0 ? 0 : r.hysteresis());
        double to = (tier + 1) * r.band() + r.hysteresis();
        return (int) Math.round(Math.max(0, Math.min(100, 100 * (points - from) / (to - from))));
    }

    /**
     * The start tier of a pair: the season's {@code fanTiers} entry; without one, 1 for a real man of the data whose
     * record is not a placeholder, 0 otherwise (H-01 §4 default).
     */
    public static int startTier(GameData data, Player p, Club club) {
        Person person = p == null ? null : p.getPerson();
        if (data == null || person == null || person.getDataId() == null) {
            return 0;
        }
        int tier = new HistoryFacts(data, "en").fanTier(person.getDataId(),
                club == null ? null : club.getDataId());
        if (tier > 0) {
            return tier;
        }
        DataRecord rec = data.get(GameData.PERSON, person.getDataId());
        return rec != null && !"placeholder".equals(rec.str("confidence")) ? 1 : 0;
    }

    /** The player's meter, created at his start tier the first time. */
    public static FanStanding standingOf(Player p, Club club, FanRules r) {
        if (p.getFans() == null) {
            int tier = startTier(DatabaseLoader.getGameData(), p, club);
            p.setFans(new FanStanding(startPoints(tier, r), tier));
        }
        return p.getFans();
    }

    /**
     * Moves both clubs' meters after a recorded match (called once per match by the recorder).
     *
     * @param homeCupTie / awayCupTie true when that side won a cup tie in this match
     * @param homeFinal / awayFinal true when that side won the cup (the final) in this match
     */
    public static void applyAfterMatch(SaveGame game, Match match, RandomSource random, boolean homeCupTie,
            boolean awayCupTie, boolean homeFinal, boolean awayFinal) {
        if (game == null || match == null || random == null) {
            return;
        }
        FanRules r = FanRules.current();
        Club home = game.getClubById(match.getHomeClubId());
        Club away = game.getClubById(match.getAwayClubId());
        side(r, random, match, home, away, match.getHomeSheet(), match.getHomeScorerIds(), homeCupTie, homeFinal);
        side(r, random, match, away, home, match.getAwaySheet(), match.getAwayScorerIds(), awayCupTie, awayFinal);
    }

    private static void side(FanRules r, RandomSource random, Match match, Club club, Club opponent, TeamSheet sheet,
            List<Long> scorerPersonIds, boolean cupTie, boolean cupFinal) {
        if (club == null || club.getPlayers() == null || sheet == null) {
            return;
        }
        RandomSource mood = random.fork("fans:" + match.getId() + ":" + club.getId());
        double scale = 1 + r.get(FanRules.JITTER) * (2 * mood.nextDouble() - 1);
        boolean derby = isRival(club, opponent);
        Set<Long> onSheet = new HashSet<>(sheet.selectedIds());
        for (Player p : club.getPlayers()) {
            if (p == null || p.getId() == null || p.getPerson() == null) {
                continue;
            }
            double delta;
            if (onSheet.contains(p.getId())) {
                delta = r.get(FanRules.APPEARANCE);
                int goals = 0;
                for (Long id : scorerPersonIds == null ? List.<Long>of() : scorerPersonIds) {
                    if (id != null && id.equals(p.getPerson().getId())) {
                        goals++;
                    }
                }
                delta += goals * (r.get(FanRules.GOAL) + (derby ? r.get(FanRules.DERBY_GOAL) : 0));
                if (cupTie) {
                    delta += r.get(FanRules.CUP_TIE_WON);
                }
                if (cupFinal) {
                    delta += r.get(FanRules.CUP_FINAL_WON);
                }
            } else {
                delta = r.get(FanRules.ABSENCE);
            }
            add(standingOf(p, club, r), delta * scale, r);
        }
    }

    /** True when the opponent is among the club's {@code rivals} in the data (either way round). */
    static boolean isRival(Club club, Club opponent) {
        GameData data = DatabaseLoader.getGameData();
        if (data == null || club == null || opponent == null || club.getDataId() == null
                || opponent.getDataId() == null) {
            return false;
        }
        return lists(data, club.getDataId(), opponent.getDataId()) || lists(data, opponent.getDataId(), club.getDataId());
    }

    private static boolean lists(GameData data, String clubId, String otherId) {
        DataRecord rec = data.get(GameData.CLUB, clubId);
        String other = data.resolve(GameData.CLUB, otherId);
        if (rec == null || other == null) {
            return false;
        }
        for (String rival : rec.refs("rivals")) {
            if (other.equals(data.resolve(GameData.CLUB, rival))) {
                return true;
            }
        }
        return false;
    }
}
