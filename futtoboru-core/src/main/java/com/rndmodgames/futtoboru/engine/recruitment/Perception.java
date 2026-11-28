package com.rndmodgames.futtoboru.engine.recruitment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Match;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.StaffJudgement;
import com.rndmodgames.futtoboru.data.TeamSheet;
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.Knowledge;
import com.rndmodgames.futtoboru.data.recruitment.PoolRegion;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * R-04 hidden ratings and perception (docs/design/RECRUITMENT_AND_TRIALS.md §4, D28 Q5/Q6). The truth stays on the
 * {@link Player}; the player's club sees a candidate or another club's man only through its {@link Observers}.
 *
 * <p>Per observer and attribute: {@code est = true + bias + z * sigma}, where {@code z} in [-1, 1] is drawn once from
 * {@code fork("perceive:" + observer + ":" + man + ":" + attribute)} (the same draw every time, so a reload or a new
 * look changes nothing) and {@code sigma = SIGMA_MATCH * (1 - skill / 30) * sqrt(tau / minutes)}: one match seen
 * ({@code tau} = 90 minutes) gives {@code SIGMA_MATCH * (1 - skill / 30)}, more minutes narrow it, a better judge
 * starts narrower. The shown range is {@code est +- (HALF_WIDTH * sigma + |bias|)}, clamped to 3..23: wide enough
 * that it always holds the truth, shifted by the observer's bias. A man never seen has no range.
 *
 * <p>Exposure: minutes per (observer, man) in {@code SaveGame.knowledge}, from the matches the observer attended (the
 * secretary and the trainer go to every match of the club, the committee to home matches, the captain plays) and
 * {@link #watch} (R-05: going to see a man play); a candidate in a region the observer knows men in is "heard of"
 * ({@link #HEARD_MINUTES}). Club view = the observers' ranges weighted by skill x minutes (Q6).
 *
 * <p>All constants are placeholders (D30 A3).
 */
public final class Perception {

    /** sigma after one match by a judge of skill 0 (placeholder) */
    public static final double SIGMA_MATCH = 5.0;
    public static final float MATCH_MINUTES = 90f;
    /** about 80% of a normal error; the error is bounded, so the range always holds the truth */
    public static final double HALF_WIDTH = 1.28;
    /** the shift of a bias (design §4.1: "+1 to pace for men he likes the look of") */
    public static final double BIAS = 1.0;
    /** what a man known in the observer's network regions is worth before anyone has seen him */
    public static final float HEARD_MINUTES = 15f;
    /** character is slower to read than legs; an outfield man is rarely seen in goal */
    static final double MENTAL_TAU = 2.0;
    static final double KEEPER_TAU_OUTFIELD = 10.0;
    static final double SIGMA_MAX = 20.0;
    public static final float MIN = StaffJudgement.MIN;
    public static final float MAX = StaffJudgement.MAX;
    static final String SCOTLAND = "scotland";

    /** The four judging groups of {@link StaffJudgement}. */
    public enum Group {
        PHYSICAL, TECHNICAL, MENTAL, KEEPER
    }

    /** One perceived attribute: id ({@code long_shots}), UI name ({@code Long Shots}), group, getter. */
    public static final class Attr {
        private final String id;
        private final String name;
        private final Group group;
        private final Function<Player, Float> getter;

        Attr(String id, String name, Group group, Function<Player, Float> getter) {
            this.id = id;
            this.name = name;
            this.group = group;
            this.getter = getter;
        }

        public String id() {
            return id;
        }

        /** the attribute name the Player detail screen uses ({@code attr_} key, change tracking) */
        public String name() {
            return name;
        }

        public Group group() {
            return group;
        }

        public Float of(Player p) {
            return getter.apply(p);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    public static final List<Attr> ATTRIBUTES = List.of(
            new Attr("acceleration", "Acceleration", Group.PHYSICAL, Player::getAcceleration),
            new Attr("speed", "Speed", Group.PHYSICAL, Player::getSpeed),
            new Attr("stamina", "Stamina", Group.PHYSICAL, Player::getStamina),
            new Attr("strength", "Strength", Group.PHYSICAL, Player::getStrength),
            new Attr("endurance", "Endurance", Group.PHYSICAL, Player::getEndurance),
            new Attr("jumping", "Jumping", Group.PHYSICAL, Player::getJumping),
            new Attr("dexterity", "Dexterity", Group.PHYSICAL, Player::getDexterity),
            new Attr("concentration", "Concentration", Group.MENTAL, Player::getConcentration),
            new Attr("courage", "Courage", Group.MENTAL, Player::getCourage),
            new Attr("determination", "Determination", Group.MENTAL, Player::getDetermination),
            new Attr("leadership", "Leadership", Group.MENTAL, Player::getLeadership),
            new Attr("perception", "Perception", Group.MENTAL, Player::getPerception),
            new Attr("positioning", "Positioning", Group.MENTAL, Player::getPositioning),
            new Attr("teamwork", "Teamwork", Group.MENTAL, Player::getTeamwork),
            new Attr("dribbling", "Dribbling", Group.TECHNICAL, Player::getDribbling),
            new Attr("passing", "Passing", Group.TECHNICAL, Player::getPassing),
            new Attr("kicking", "Kicking", Group.TECHNICAL, Player::getKicking),
            new Attr("long_shots", "Long Shots", Group.TECHNICAL, Player::getLongShots),
            new Attr("trick_shots", "Trick Shots", Group.TECHNICAL, Player::getTrickShots),
            new Attr("heading", "Heading", Group.TECHNICAL, Player::getHeading),
            new Attr("one_twos", "One-Twos", Group.TECHNICAL, Player::getOneTwos),
            new Attr("free_kicks", "Free Kicks", Group.TECHNICAL, Player::getFreeKicks),
            new Attr("corner_kicks", "Corner Kicks", Group.TECHNICAL, Player::getCornerKicks),
            new Attr("penalty_kicks", "Penalty Kicks", Group.TECHNICAL, Player::getPenaltyKicks),
            new Attr("throw_ins", "Throw-Ins", Group.TECHNICAL, Player::getThrowIns),
            new Attr("marking", "Marking", Group.TECHNICAL, Player::getMarking),
            new Attr("tackling", "Tackling", Group.TECHNICAL, Player::getTackling),
            new Attr("shot_stopping", "Shot Stopping", Group.KEEPER, Player::getShotStopping),
            new Attr("area_control", "Area Control", Group.KEEPER, Player::getAreaControl),
            new Attr("punching", "Punching", Group.KEEPER, Player::getPunching),
            new Attr("hand_to_hand", "Hand to Hand", Group.KEEPER, Player::getHandToHand),
            new Attr("rushing_out", "Rushing Out", Group.KEEPER, Player::getRushingOut),
            new Attr("area_positioning", "Area Positioning", Group.KEEPER, Player::getAreaPositioning));

    /** @return the attribute with this UI name ({@code "Long Shots"}), or null */
    public static Attr byName(String name) {
        for (Attr a : ATTRIBUTES) {
            if (a.name.equals(name)) {
                return a;
            }
        }
        return null;
    }

    /** A perceived range on the 3..23 scale; {@code seen} false = no opinion (the whole scale). */
    public static final class Range {
        private final float lo;
        private final float hi;
        private final float centre;
        private final boolean seen;

        Range(float lo, float hi, float centre, boolean seen) {
            this.lo = lo;
            this.hi = hi;
            this.centre = centre;
            this.seen = seen;
        }

        public float lo() {
            return lo;
        }

        public float hi() {
            return hi;
        }

        /** the estimate (clamped to the range) */
        public float centre() {
            return centre;
        }

        public boolean seen() {
            return seen;
        }

        public float width() {
            return hi - lo;
        }

        public boolean contains(float truth) {
            return lo <= truth && truth <= hi;
        }

        /** @return the shown numbers: whole points that hold the range ({@code floor(lo)}, {@code ceil(hi)}) */
        public int shownLo() {
            return (int) Math.floor(lo);
        }

        public int shownHi() {
            return (int) Math.ceil(hi);
        }

        @Override
        public String toString() {
            return seen ? lo + ".." + hi + " (" + centre + ")" : "unseen";
        }
    }

    public static final Range UNSEEN = new Range(MIN, MAX, (MIN + MAX) / 2f, false);

    /** @return the truth as a range of no width (an own player) */
    public static Range exact(float truth) {
        return new Range(truth, truth, truth, true);
    }

    private Perception() {
    }

    // ------------------------------------------------------------------------------------------- the maths

    /** @return the observer's judging skill for a group */
    public static int skill(StaffJudgement j, Group g) {
        switch (g) {
        case PHYSICAL:
            return j.getPhysical();
        case TECHNICAL:
            return j.getTechnical();
        case MENTAL:
            return j.getMental();
        default:
            return j.getKeeper();
        }
    }

    /** @return sigma for a judge of this skill after these minutes; infinite when never seen */
    public static double sigma(int skill, Group group, boolean keeper, double minutes) {
        if (minutes <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        double tau = MATCH_MINUTES;
        if (group == Group.MENTAL) {
            tau *= MENTAL_TAU;
        } else if (group == Group.KEEPER && !keeper) {
            tau *= KEEPER_TAU_OUTFIELD;
        }
        double s = SIGMA_MATCH * (1.0 - skill / 30.0) * Math.sqrt(tau / minutes);
        return Math.min(SIGMA_MAX, s);
    }

    /**
     * @return the observer's fixed error on this attribute of this man, in [-1, 1] (a bell from three uniforms),
     *         from its own fork
     */
    public static double error(RandomSource root, String observerKey, Long playerId, Attr a) {
        RandomSource r = root.fork("perceive:" + observerKey + ":" + playerId + ":" + a.id);
        double sum = r.nextDouble() + r.nextDouble() + r.nextDouble();
        return (sum - 1.5) / 1.5;
    }

    /**
     * The shift of a bias (§4.1): the trainer's pace (speed, acceleration), size (strength, jumping, heading), flair
     * (dribbling, trick shots, one-twos), a committee man's local lad and the Scots (everything, +1).
     */
    public static double bias(String bias, Attr a, boolean local, boolean scot) {
        if (bias == null) {
            return 0;
        }
        switch (bias) {
        case StaffJudgement.BIAS_PACE:
            return a.id.equals("speed") || a.id.equals("acceleration") ? BIAS : 0;
        case StaffJudgement.BIAS_SIZE:
            return a.id.equals("strength") || a.id.equals("jumping") || a.id.equals("heading") ? BIAS : 0;
        case StaffJudgement.BIAS_FLAIR:
            return a.id.equals("dribbling") || a.id.equals("trick_shots") || a.id.equals("one_twos") ? BIAS : 0;
        case StaffJudgement.BIAS_LOCAL:
            return local ? BIAS : 0;
        case StaffJudgement.BIAS_SCOTS:
            return scot ? BIAS : 0;
        default:
            return 0;
        }
    }

    /** @return the range of an estimate: {@code truth + bias + z*sigma +- (HALF_WIDTH*sigma + |bias|)}, clamped */
    public static Range range(float truth, double sigma, double z, double bias) {
        if (Double.isInfinite(sigma)) {
            return UNSEEN;
        }
        double est = truth + bias + z * sigma;
        double half = HALF_WIDTH * sigma + Math.abs(bias);
        float lo = clamp(est - half);
        float hi = clamp(est + half);
        return new Range(lo, hi, Math.max(lo, Math.min(hi, (float) est)), true);
    }

    /** One observer's range on one attribute of one man. */
    public static Range perceive(RandomSource root, Observers.Observer o, Player p, Attr a, double minutes,
            boolean local, boolean scot) {
        Float truth = a.of(p);
        if (truth == null) {
            return UNSEEN;
        }
        double sigma = sigma(skill(o.judgement(), a.group), a.group, isKeeper(p), minutes);
        if (Double.isInfinite(sigma)) {
            return UNSEEN;
        }
        return range(truth, sigma, error(root, o.key(), p.getId(), a), bias(o.judgement().getBias(), a, local, scot));
    }

    /**
     * The club view (Q6): the ranges of those who have seen him, weighted (skill x minutes). Each bound is a weighted
     * mean of bounds that hold the truth, so the consensus holds it too.
     */
    public static Range consensus(List<Range> ranges, List<Double> weights) {
        double w = 0;
        double lo = 0;
        double hi = 0;
        double centre = 0;
        float minLo = MAX;
        float maxLo = MIN;
        float minHi = MAX;
        float maxHi = MIN;
        for (int i = 0; i < ranges.size(); i++) {
            Range r = ranges.get(i);
            double wi = weights.get(i);
            if (r == null || !r.seen || wi <= 0) {
                continue;
            }
            w += wi;
            lo += wi * r.lo;
            hi += wi * r.hi;
            centre += wi * r.centre;
            minLo = Math.min(minLo, r.lo);
            maxLo = Math.max(maxLo, r.lo);
            minHi = Math.min(minHi, r.hi);
            maxHi = Math.max(maxHi, r.hi);
        }
        if (w <= 0) {
            return UNSEEN;
        }
        float l = Math.max(minLo, Math.min(maxLo, (float) (lo / w)));
        float h = Math.max(minHi, Math.min(maxHi, (float) (hi / w)));
        return new Range(l, h, Math.max(l, Math.min(h, (float) (centre / w))), true);
    }

    static boolean isKeeper(Player p) {
        return p.getPosition() == Position.GOALKEEPER;
    }

    private static float clamp(double x) {
        return (float) Math.max(MIN, Math.min(MAX, x));
    }

    // ------------------------------------------------------------------------------------------- the game

    /** What the club knows of one man: each observer's view and the club consensus. */
    public static final class Report {
        private final boolean known;
        private final List<ObserverView> observers;
        private final Map<Attr, Range> consensus;

        Report(boolean known, List<ObserverView> observers, Map<Attr, Range> consensus) {
            this.known = known;
            this.observers = observers;
            this.consensus = consensus;
        }

        /** true for one of the club's own men: the truth, no observers */
        public boolean known() {
            return known;
        }

        public List<ObserverView> observers() {
            return observers;
        }

        /** per attribute (in {@link #ATTRIBUTES} order): the club view */
        public Map<Attr, Range> consensus() {
            return consensus;
        }

        public boolean seen() {
            for (Range r : consensus.values()) {
                if (r.seen) {
                    return true;
                }
            }
            return false;
        }
    }

    /** One observer's view of a man. */
    public static final class ObserverView {
        private final Observers.Observer observer;
        private final float minutes;
        private final Map<Attr, Range> ranges;

        ObserverView(Observers.Observer observer, float minutes, Map<Attr, Range> ranges) {
            this.observer = observer;
            this.minutes = minutes;
            this.ranges = ranges;
        }

        public Observers.Observer observer() {
            return observer;
        }

        public float minutes() {
            return minutes;
        }

        public Map<Attr, Range> ranges() {
            return ranges;
        }
    }

    /** @return true when the man is in the player's club's squad (fully known) */
    public static boolean isOwn(SaveGame game, Player p) {
        Club mine = game != null ? game.getCurrentClub() : null;
        if (mine == null || p == null) {
            return false;
        }
        for (Player q : mine.getPlayers()) {
            if (q == p || (q != null && q.getId() != null && q.getId().equals(p.getId()))) {
                return true;
            }
        }
        return false;
    }

    /** @return the candidate whose man this is, or null */
    public static Candidate candidateOf(SaveGame game, Player p) {
        for (Candidate c : game.getCandidates()) {
            if (c != null && (c.getPlayer() == p
                    || (c.getPlayer() != null && p.getId() != null && p.getId().equals(c.getPlayer().getId())))) {
                return c;
            }
        }
        return null;
    }

    /** What the player's club knows of a man. */
    public static Report report(SaveGame game, Player p, RandomSource root) {
        return report(game, p, candidateOf(game, p), root);
    }

    /**
     * @param candidate his candidate record when he is one (his region: "heard of", local), else null
     */
    public static Report report(SaveGame game, Player p, Candidate candidate, RandomSource root) {
        Map<Attr, Range> consensus = new LinkedHashMap<>();
        if (isOwn(game, p)) {
            for (Attr a : ATTRIBUTES) {
                Float t = a.of(p);
                consensus.put(a, t == null ? UNSEEN : exact(t));
            }
            return new Report(true, Collections.emptyList(), consensus);
        }
        Club mine = game.getCurrentClub();
        List<Observers.Observer> observers = Observers.of(game, mine, root);
        String region = candidate != null ? candidate.getRegion() : regionOfPlayer(game, p);
        Person person = p.getPerson();
        boolean scot = person != null && person.getCountry() != null
                && SCOTLAND.equals(person.getCountry().getDataId());
        List<ObserverView> views = new ArrayList<>();
        for (Observers.Observer o : observers) {
            float minutes = minutes(game, o, p, candidate);
            List<String> network = o.judgement().getNetwork();
            boolean local = region != null && !network.isEmpty() && region.equals(network.get(0));
            Map<Attr, Range> ranges = new LinkedHashMap<>();
            for (Attr a : ATTRIBUTES) {
                ranges.put(a, perceive(root, o, p, a, minutes, local, scot));
            }
            views.add(new ObserverView(o, minutes, ranges));
        }
        for (Attr a : ATTRIBUTES) {
            List<Range> rs = new ArrayList<>();
            List<Double> ws = new ArrayList<>();
            for (ObserverView v : views) {
                rs.add(v.ranges.get(a));
                ws.add((double) skill(v.observer.judgement(), a.group) * v.minutes);
            }
            consensus.put(a, consensus(rs, ws));
        }
        return new Report(false, views, consensus);
    }

    /** @return the minutes this observer has seen of the man, "heard of" for a candidate in his network */
    public static float minutes(SaveGame game, Observers.Observer o, Player p, Candidate candidate) {
        Knowledge k = game.knowledgeOf(o.key(), p.getId(), false);
        float m = k != null ? k.getMinutes() : 0f;
        if (candidate != null && candidate.getRegion() != null
                && o.judgement().getNetwork().contains(candidate.getRegion())) {
            m += HEARD_MINUTES;
        }
        return m;
    }

    private static String regionOfPlayer(SaveGame game, Player p) {
        if (p.getPerson() == null || p.getPerson().getCurrentClubId() == null) {
            return null;
        }
        PoolRegion r = CandidatePools.regionOf(game.getClubById(p.getPerson().getCurrentClubId()));
        return r != null ? r.dataId() : null;
    }

    // ------------------------------------------------------------------------------------------- exposure

    /** R-05 and later: an observer watched a man for some minutes (a match, a trial). */
    public static void watch(SaveGame game, String observerKey, Player p, float minutes) {
        if (game == null || observerKey == null || p == null || p.getId() == null || minutes <= 0) {
            return;
        }
        game.knowledgeOf(observerKey, p.getId(), true).addMinutes(minutes);
    }

    /**
     * After a match was recorded (MatchResultRecorder, after the match joined the clubs' played lists): when the
     * player's club played it, its observers who were there saw the other side's eleven for 90 minutes.
     */
    public static void afterMatch(SaveGame game, Match match) {
        Club mine = game != null ? game.getCurrentClub() : null;
        if (mine == null || match == null || mine.getId() == null) {
            return;
        }
        if (!game.isKnowledgeCounted()) {
            ensure(game); // counts every played match, this one too
            return;
        }
        count(game, mine, match);
    }

    /**
     * A game from before R-04 (or a new one): count the club's matches already played, once. Uses today's
     * observers, the best record there is.
     */
    public static void ensure(SaveGame game) {
        if (game == null || game.isKnowledgeCounted()) {
            return;
        }
        game.setKnowledgeCounted(true);
        Club mine = game.getCurrentClub();
        if (mine == null || mine.getPlayedMatches() == null) {
            return;
        }
        for (Match m : mine.getPlayedMatches()) {
            count(game, mine, m);
        }
    }

    private static void count(SaveGame game, Club mine, Match match) {
        boolean home = mine.getId().equals(match.getHomeClubId());
        boolean away = mine.getId().equals(match.getAwayClubId());
        if ((!home && !away) || !Boolean.TRUE.equals(match.getIsPlayed())) {
            return;
        }
        TeamSheet own = home ? match.getHomeSheet() : match.getAwaySheet();
        TeamSheet other = home ? match.getAwaySheet() : match.getHomeSheet();
        if (other == null) {
            return;
        }
        List<String> there = new ArrayList<>();
        Person secretary = StaffRoles.secretaryOf(game, mine);
        if (secretary != null && secretary.getId() != null) {
            there.add(Observers.personKey(secretary));
        }
        Person trainer = StaffRoles.trainerOf(game, mine);
        if (trainer != null && trainer.getId() != null) {
            there.add(Observers.personKey(trainer));
        }
        if (home) {
            for (Observers.Observer o : Observers.committee(mine)) {
                there.add(o.key());
            }
        }
        Long captainId = own != null ? own.getCaptainId() : null;
        if (captainId != null) {
            for (Player p : mine.getPlayers()) {
                if (p != null && captainId.equals(p.getId()) && p.getPerson() != null
                        && p.getPerson().getId() != null) {
                    String key = Observers.personKey(p.getPerson());
                    if (!there.contains(key)) {
                        there.add(key);
                    }
                }
            }
        }
        for (Long id : other.selectedIds()) {
            if (id == null) {
                continue;
            }
            for (String key : there) {
                game.knowledgeOf(key, id, true).addMinutes(MATCH_MINUTES);
            }
        }
    }

    // ------------------------------------------------------------------------------------------- words (Q5)

    /** Bands of the period words per attribute (centre below 7, 11, 15, 19, else the top band). */
    static final float[] BANDS = { 7f, 11f, 15f, 19f };
    /** an attribute "worth a word" in the summary */
    static final float HIGH = 15f;
    static final float LOW = 8f;
    static final float SURE_WIDTH = 4f;
    static final float FAIR_WIDTH = 8f;

    /** @return the bundle key of the band word of a range ({@code perceive_band_1..5}, unknown when unseen) */
    public static String bandKey(Range r) {
        if (r == null || !r.seen) {
            return "perceive_band_unknown";
        }
        int band = 1;
        for (float b : BANDS) {
            if (r.centre >= b) {
                band++;
            }
        }
        return "perceive_band_" + band;
    }

    /**
     * @return the period phrases (bundle keys) for a view: his two best attributes worth a word ("a rare turn of
     *         speed") and his worst ("wants pace"), keeper attributes only for a keeper; "not seen" or "a fair player,
     *         no more" when there is nothing to say
     */
    public static List<String> phraseKeys(Map<Attr, Range> ranges, boolean keeper) {
        List<Map.Entry<Attr, Range>> seen = new ArrayList<>();
        for (Map.Entry<Attr, Range> e : ranges.entrySet()) {
            if (e.getValue() != null && e.getValue().seen && (keeper || e.getKey().group != Group.KEEPER)) {
                seen.add(e);
            }
        }
        List<String> out = new ArrayList<>();
        if (seen.isEmpty()) {
            out.add("perceive_not_seen");
            return out;
        }
        List<Map.Entry<Attr, Range>> high = new ArrayList<>(seen);
        high.removeIf(e -> e.getValue().centre < HIGH);
        high.sort(Comparator.comparingDouble((Map.Entry<Attr, Range> e) -> -e.getValue().centre));
        for (int i = 0; i < Math.min(2, high.size()); i++) {
            out.add("perceive_" + high.get(i).getKey().id + "_high");
        }
        List<Map.Entry<Attr, Range>> low = new ArrayList<>(seen);
        low.removeIf(e -> e.getValue().centre > LOW);
        low.sort(Comparator.comparingDouble((Map.Entry<Attr, Range> e) -> e.getValue().centre));
        if (!low.isEmpty()) {
            out.add("perceive_" + low.get(0).getKey().id + "_low");
        }
        if (out.isEmpty()) {
            out.add("perceive_ordinary");
        }
        return out;
    }

    /** @return how sure the view is ({@code perceive_conf_sure/fair/hearsay}), or null when nothing was seen */
    public static String confidenceKey(Map<Attr, Range> ranges, boolean keeper) {
        double width = 0;
        int n = 0;
        for (Map.Entry<Attr, Range> e : ranges.entrySet()) {
            if (e.getValue() != null && e.getValue().seen && (keeper || e.getKey().group != Group.KEEPER)) {
                width += e.getValue().width();
                n++;
            }
        }
        if (n == 0) {
            return null;
        }
        double mean = width / n;
        return mean <= SURE_WIDTH ? "perceive_conf_sure" : mean <= FAIR_WIDTH ? "perceive_conf_fair"
                : "perceive_conf_hearsay";
    }

    /** @return every bundle key the words can use (for the bundle test) */
    public static List<String> allWordKeys() {
        List<String> keys = new ArrayList<>();
        for (Attr a : ATTRIBUTES) {
            keys.add("perceive_" + a.id + "_high");
            keys.add("perceive_" + a.id + "_low");
        }
        for (int i = 1; i <= BANDS.length + 1; i++) {
            keys.add("perceive_band_" + i);
        }
        keys.add("perceive_band_unknown");
        keys.add("perceive_not_seen");
        keys.add("perceive_ordinary");
        keys.add("perceive_conf_sure");
        keys.add("perceive_conf_fair");
        keys.add("perceive_conf_hearsay");
        for (String role : List.of(Observers.SECRETARY, Observers.TRAINER, Observers.COMMITTEE, Observers.CAPTAIN)) {
            keys.add("perceive_role_" + role);
        }
        return keys;
    }
}
