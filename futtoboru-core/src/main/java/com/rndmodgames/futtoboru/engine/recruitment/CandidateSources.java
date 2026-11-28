package com.rndmodgames.futtoboru.engine.recruitment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Message;
import com.rndmodgames.futtoboru.data.MessageCategory;
import com.rndmodgames.futtoboru.data.MessagePriority;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.recruitment.Advert;
import com.rndmodgames.futtoboru.data.recruitment.Agent;
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.Newspaper;
import com.rndmodgames.futtoboru.data.recruitment.PoolRegion;
import com.rndmodgames.futtoboru.data.recruitment.ShortlistEntry;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.engine.finances.Money;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.engine.recruitment.Perception.Attr;
import com.rndmodgames.futtoboru.engine.recruitment.Perception.Range;
import com.rndmodgames.futtoboru.menu.GameScreenId;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.RecruitmentData;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * R-05 (docs/design/RECRUITMENT_AND_TRIALS.md §3.3, slice R-05): how a candidate reaches a club, every club (D31: the
 * AI's clubs by the same sources, straight onto their lists). Run each day after the pools ({@link Recruitment}).
 * <ol>
 * <li><b>Letters</b> from men asking for a trial: a man of the club's pool (sometimes a neighbouring or a Scottish
 * one), more in the close season. A letter shows little ({@link #LETTER_MINUTES} for the secretary).</li>
 * <li><b>Adverts</b> the secretary places ({@link #placeAdvert}): a paper of a region, its cost booked once, period
 * wording, replies by letter from its region's pool over the next days (a Scottish paper about a week later).</li>
 * <li><b>Tips</b> from the trainer, the captain or a committee man, once a week at most: a man of a region he knows,
 * the best of a few by his own (biased) eye; the club gets the tipper's view ({@link #TIP_MINUTES} of his
 * exposure).</li>
 * <li><b>Agents</b> (Scotland, D28 Q3): a letter offering a few men for a fee each; the names come once it is
 * paid, with the agent's own (talked-up) word on them.</li>
 * <li><b>"Not retained" lists</b> in May (G-04): the men the other clubs let go at the rollover and still free.</li>
 * </ol>
 * The player's club gets each as an inbox letter (category {@code RECRUITMENT}) with Add to shortlist / Ignore (an
 * agent's: Pay / Ignore); an AI club lists the men at once (an agent only when it can afford him). Randomness on
 * {@code fork("recruit:src:<kind>:<club>:<day>")}. All rates are placeholders (D30 A3).
 */
public final class CandidateSources {

    // message types
    public static final String MSG_LETTER = "RECRUIT_LETTER";
    public static final String MSG_ADVERT = "RECRUIT_ADVERT_REPLY";
    public static final String MSG_TIP = "RECRUIT_TIP";
    public static final String MSG_AGENT = "RECRUIT_AGENT";
    public static final String MSG_AGENT_NAMES = "RECRUIT_AGENT_NAMES";
    public static final String MSG_RELEASED = "RECRUIT_NOT_RETAINED";

    /** a man's letter a day, in the close season (May-August) and in the season (placeholders) */
    static final double LETTER_CLOSE_SEASON = 0.12;
    static final double LETTER_IN_SEASON = 0.06;
    /** a letter from a man of another region than the club's own, and of those from Scotland (placeholders) */
    static final double LETTER_ELSEWHERE = 0.3;
    static final double LETTER_SCOTTISH = 0.4;
    /** a tip in a week (Mondays), an agent's letter in a week (Wednesdays) (placeholders) */
    static final double TIP_A_WEEK = 0.35;
    static final double AGENT_A_WEEK = 0.05;
    /** an AI club places an advert on a close-season Monday (placeholder) */
    static final double AI_ADVERT_A_WEEK = 0.06;
    /** an AI club pays an agent when this many fees are in its balance, half the time (placeholders) */
    static final int AI_AGENT_FEES_IN_HAND = 10;
    static final double AI_AGENT_PAYS = 0.5;
    /** a tipper weighs this many men of his region before naming one */
    static final int TIP_LOOKS_AT = 5;
    /** exposure: a letter, a tip (the tipper has watched him), a man the League clubs have had (placeholders) */
    public static final float LETTER_MINUTES = 5f;
    public static final float TIP_MINUTES = 45f;
    public static final float RELEASED_MINUTES = 30f;
    /** an advert reply comes from a man of the wanted line this often, when the paper's pool has one */
    static final double WANTED_LINE = 0.7;
    /** the "not retained" lists come out on or after this day of May, once a season */
    static final int NOT_RETAINED_DAY = 8;
    /** the list names this many men at most; an AI club takes this many of them */
    static final int NOT_RETAINED_LISTED = 12;
    static final int NOT_RETAINED_AI_TAKES = 2;
    /** a club places one advert in a paper at a time: not again in the same paper within these days */
    public static final int ADVERT_GAP_DAYS = 14;
    static final String SCOTLAND = "scotland";
    /** bundle key prefixes: an observer role ({@code perceive_role_trainer}) */
    static final String ROLE_KEY = "perceive_role_";

    private CandidateSources() {
    }

    private static RecruitmentData data() {
        return DatabaseLoader.getInstance().getRecruitmentData();
    }

    // --------------------------------------------------------------------------------------------- the day

    /** The day's arrivals for every club (after {@link Recruitment#afterDay}). */
    public static void daily(SaveGame game, LocalDateTime now, RandomSource root) {
        if (game == null || now == null || root == null) {
            return;
        }
        Shortlists.prune(game); // first: the rollover and the pools may have taken men away today
        if (data() == null || game.getCandidates().isEmpty()) {
            return;
        }
        LocalDate day = now.toLocalDate();
        dueReplies(game, now, root);
        for (Club club : ContractManager.uniqueClubs(game)) {
            if (club.getId() == null) {
                continue;
            }
            String key = clubKey(club) + ":" + day;
            letter(game, club, now, root.fork("recruit:src:letter:" + key));
            if (day.getDayOfWeek() == DayOfWeek.MONDAY) {
                tip(game, club, now, root, root.fork("recruit:src:tip:" + key));
                if (ContractCalendar.isCloseSeason(day) && !Shortlists.isHuman(game, club)) {
                    aiAdvert(game, club, now, root, root.fork("recruit:src:ai-advert:" + key));
                }
            }
            if (day.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
                agent(game, club, now, root.fork("recruit:src:agent:" + key));
            }
        }
        notRetained(game, now, root);
    }

    static String clubKey(Club club) {
        return club.getDataId() != null ? club.getDataId() : "id" + club.getId();
    }

    // --------------------------------------------------------------------------------------------- 1. letters

    private static void letter(SaveGame game, Club club, LocalDateTime now, RandomSource rng) {
        double chance = ContractCalendar.isCloseSeason(now.toLocalDate()) ? LETTER_CLOSE_SEASON : LETTER_IN_SEASON;
        if (rng.nextDouble() >= chance) {
            return;
        }
        String region = letterRegion(game, club, rng);
        Candidate c = pick(game, club, region, null, rng);
        if (c == null) {
            return;
        }
        ShortlistEntry e = arrive(game, club, c.getPlayer(), ShortlistEntry.LETTER, now);
        if (e == null) {
            return;
        }
        secretaryWatches(game, club, c.getPlayer(), LETTER_MINUTES);
        if (Shortlists.isHuman(game, club)) {
            Player p = c.getPlayer();
            Person person = p.getPerson();
            String body = c.getPlaysFor() != null
                    ? LanguageModLoader.format("recruit_letter_body", position(p), c.getPlaysFor(), club.getName(),
                            age(person, now), name(person))
                    : LanguageModLoader.format("recruit_letter_body_free", position(p), club.getName(),
                            age(person, now), name(person));
            post(game, club, now, MSG_LETTER, LanguageModLoader.format("recruit_letter_title", name(person)), body,
                    List.of(e));
        }
    }

    /** the club's own pool, else (now and then) a region the secretary knows or a Scottish one */
    private static String letterRegion(SaveGame game, Club club, RandomSource rng) {
        PoolRegion own = CandidatePools.regionOf(club);
        double u = rng.nextDouble();
        double v = rng.nextDouble();
        int pick = rng.nextInt(1 << 16);
        if (own != null && u >= LETTER_ELSEWHERE) {
            return own.dataId();
        }
        List<String> regions = new ArrayList<>();
        boolean scottishClub = own != null && SCOTLAND.equals(own.country());
        for (PoolRegion r : data().getPools().values()) {
            boolean scottish = SCOTLAND.equals(r.country());
            if (r != own && (v < LETTER_SCOTTISH ? scottish && !scottishClub : !scottish)) {
                regions.add(r.dataId());
            }
        }
        if (regions.isEmpty()) {
            return own != null ? own.dataId() : null;
        }
        return regions.get(pick % regions.size());
    }

    // --------------------------------------------------------------------------------------------- 2. adverts

    /** @return the papers a club may advertise in on that day (founded by then), in file order */
    public static List<Newspaper> newspapers(LocalDateTime now) {
        return data() == null || now == null ? List.of() : data().newspapersIn(now.getYear());
    }

    /** @return the club's advert in that paper placed less than {@link #ADVERT_GAP_DAYS} ago, or null */
    public static Advert running(SaveGame game, Club club, String newspaper, LocalDateTime now) {
        for (Advert a : game.getAdverts()) {
            if (club.getId().equals(a.getClub()) && newspaper.equals(a.getNewspaper()) && a.getPlaced() != null
                    && a.getPlaced().plusDays(ADVERT_GAP_DAYS).isAfter(now)) {
                return a;
            }
        }
        return null;
    }

    /** @return the club's adverts, latest first */
    public static List<Advert> advertsOf(SaveGame game, Club club) {
        List<Advert> out = new ArrayList<>();
        for (Advert a : game.getAdverts()) {
            if (club != null && club.getId() != null && club.getId().equals(a.getClub())) {
                out.add(0, a);
            }
        }
        return out;
    }

    /** @return the replies an advert has brought so far (arrived, whatever was done with them) */
    public static int repliesIn(SaveGame game, Advert a) {
        int n = 0;
        for (ShortlistEntry e : game.getShortlist()) {
            if (a.getClub().equals(e.getClub()) && ShortlistEntry.ADVERT.equals(e.getSource())
                    && a.getNewspaper().equals(e.getDetail()) && e.getSince() != null && a.getPlaced() != null
                    && !e.getSince().isBefore(a.getPlaced()) && !ShortlistEntry.DUE.equals(e.getStatus())) {
                n++;
            }
        }
        return n;
    }

    /** "WANTED, a first-class half-back. Apply, stating terms, to the Secretary, Bolton Wanderers." */
    public static String advertWording(Club club, String wanted) {
        return LanguageModLoader.format("recruit_advert_wording", LanguageModLoader.getValue(wantedKey(wanted)),
                club.getName());
    }

    /** @return the bundle key of a wanted line ({@code recruit_wanted_any}, {@code recruit_wanted_goal}...) */
    public static String wantedKey(String wanted) {
        return "recruit_wanted_" + (wanted == null ? Advert.ANY : wanted.toLowerCase(java.util.Locale.ROOT));
    }

    /** the lines an advert may ask for, {@link Advert#ANY} first */
    public static List<String> wantedLines() {
        List<String> out = new ArrayList<>();
        out.add(Advert.ANY);
        for (Position.Line l : Position.Line.values()) {
            out.add(l.name());
        }
        return out;
    }

    /**
     * Places an advert: its cost is booked now, once; its replies (from the paper's region pool, a man of the wanted
     * line when there is one) are drawn now on {@code fork("recruit:advert:<club>:<paper>:<day>")} and arrive by
     * letter over the paper's reply days.
     *
     * @return the advert, or null when the paper is unknown, not yet founded, or the club's advert there still runs
     */
    public static Advert placeAdvert(SaveGame game, Club club, String newspaper, String wanted, LocalDateTime now,
            RandomSource root) {
        Newspaper paper = data() == null || newspaper == null ? null : data().getNewspapers().get(newspaper);
        if (game == null || club == null || club.getId() == null || paper == null || now == null
                || paper.founded() > now.getYear() || running(game, club, newspaper, now) != null) {
            return null;
        }
        String line = wanted == null ? Advert.ANY : wanted;
        RandomSource rng = root.fork("recruit:advert:" + clubKey(club) + ":" + newspaper + ":" + now.toLocalDate());
        int replies = Math.max(1, (int) Math.round(paper.replies() * (0.5 + rng.nextDouble())));
        Advert advert = new Advert(club.getId(), newspaper, line, now, paper.costPence(), replies);
        game.getAdverts().add(advert);
        Shortlists.book(club, paper.costPence(), now);
        Position.Line wantedLine = Advert.ANY.equals(line) ? null : Position.Line.valueOf(line);
        for (int i = 0; i < replies; i++) {
            RandomSource r = rng.fork("reply:" + i);
            Candidate c = pick(game, club, paper.region(), r.nextDouble() < WANTED_LINE ? wantedLine : null, r);
            if (c == null) {
                continue;
            }
            ShortlistEntry e = Shortlists.arrive(game, club, c.getPlayer(), ShortlistEntry.ADVERT, ShortlistEntry.DUE,
                    now);
            if (e != null) {
                e.setDetail(newspaper);
                e.setSuggestedBy(paper.name());
                e.setDue(now.plusDays(paper.replyMinDays() + r.nextInt(paper.replyMaxDays() - paper.replyMinDays() + 1)));
            }
        }
        return advert;
    }

    /** advert replies whose day has come: a letter each (the player's club), on the list at once (an AI club) */
    private static void dueReplies(SaveGame game, LocalDateTime now, RandomSource root) {
        List<ShortlistEntry> due = new ArrayList<>();
        for (ShortlistEntry e : game.getShortlist()) {
            if (ShortlistEntry.DUE.equals(e.getStatus()) && e.getDue() != null && !e.getDue().isAfter(now)) {
                due.add(e);
            }
        }
        for (ShortlistEntry e : due) {
            Club club = game.getClubById(e.getClub());
            Player p = Shortlists.man(game, e.getPlayer());
            if (club == null || p == null) {
                continue;
            }
            boolean human = Shortlists.isHuman(game, club);
            e.setStatus(human ? ShortlistEntry.OFFERED : ShortlistEntry.LISTED);
            e.setSince(now);
            secretaryWatches(game, club, p, LETTER_MINUTES);
            if (human) {
                Candidate c = Perception.candidateOf(game, p);
                Newspaper paper = data().getNewspapers().get(e.getDetail());
                String paperName = paper != null ? paper.name() : e.getDetail();
                String playsFor = c != null && c.getPlaysFor() != null ? c.getPlaysFor()
                        : LanguageModLoader.getValue("recruit_no_club");
                post(game, club, now, MSG_ADVERT,
                        LanguageModLoader.format("recruit_advert_title", paperName, name(p.getPerson())),
                        LanguageModLoader.format("recruit_advert_body", paperName, position(p), playsFor,
                                age(p.getPerson(), now), name(p.getPerson())),
                        List.of(e));
            }
        }
    }

    /** an AI club's advert: now and then in the close season, in a paper of its region (else any) */
    private static void aiAdvert(SaveGame game, Club club, LocalDateTime now, RandomSource root, RandomSource rng) {
        if (rng.nextDouble() >= AI_ADVERT_A_WEEK) {
            return;
        }
        PoolRegion own = CandidatePools.regionOf(club);
        List<Newspaper> papers = new ArrayList<>();
        for (Newspaper n : newspapers(now)) {
            if (own != null && own.dataId().equals(n.region())) {
                papers.add(n);
            }
        }
        if (papers.isEmpty()) {
            papers.addAll(newspapers(now));
        }
        if (papers.isEmpty()) {
            return;
        }
        Newspaper paper = papers.get(rng.nextInt(papers.size()));
        List<String> lines = wantedLines();
        placeAdvert(game, club, paper.dataId(), lines.get(rng.nextInt(lines.size())), now, root);
    }

    // --------------------------------------------------------------------------------------------- 3. tips

    private static void tip(SaveGame game, Club club, LocalDateTime now, RandomSource root, RandomSource rng) {
        if (rng.nextDouble() >= TIP_A_WEEK) {
            return;
        }
        List<Observers.Observer> tippers = new ArrayList<>();
        for (Observers.Observer o : Observers.of(game, club, root)) {
            if (!Observers.SECRETARY.equals(o.role())) {
                tippers.add(o);
            }
        }
        if (tippers.isEmpty()) {
            return;
        }
        Observers.Observer o = tippers.get(rng.nextInt(tippers.size()));
        List<String> network = o.judgement().getNetwork();
        PoolRegion own = CandidatePools.regionOf(club);
        String region = network.isEmpty() ? (own != null ? own.dataId() : null)
                : rng.nextDouble() < 0.7 ? network.get(0) : network.get(rng.nextInt(network.size()));
        // he weighs a few men he knows of and names the one he likes best, by his own (biased) eye
        Candidate best = null;
        double bestScore = -1;
        for (int i = 0; i < TIP_LOOKS_AT; i++) {
            Candidate c = pick(game, club, region, null, rng.fork("look:" + i));
            if (c == null || c == best) {
                continue;
            }
            double score = liking(game, root, o, c);
            if (score > bestScore) {
                best = c;
                bestScore = score;
            }
        }
        if (best == null) {
            return;
        }
        ShortlistEntry e = arrive(game, club, best.getPlayer(), ShortlistEntry.TIP, now);
        if (e == null) {
            return;
        }
        e.setSuggestedRole(o.role());
        e.setSuggestedBy(o.name());
        Perception.watch(game, o.key(), best.getPlayer(), TIP_MINUTES);
        if (Shortlists.isHuman(game, club)) {
            Player p = best.getPlayer();
            String role = LanguageModLoader.getValue(ROLE_KEY + o.role());
            String playsFor = best.getPlaysFor() != null ? best.getPlaysFor()
                    : LanguageModLoader.getValue("recruit_no_club");
            post(game, club, now, MSG_TIP, LanguageModLoader.format("recruit_tip_title", role, o.name(),
                    name(p.getPerson())),
                    LanguageModLoader.format("recruit_tip_body", role, o.name(), name(p.getPerson()), position(p),
                            playsFor, age(p.getPerson(), now), words(view(game, root, o, p, best))),
                    List.of(e));
        }
    }

    /** @return how much the observer likes the man: the mean of his estimates (his bias included) */
    static double liking(SaveGame game, RandomSource root, Observers.Observer o, Candidate c) {
        Map<Attr, Range> v = view(game, root, o, c.getPlayer(), c);
        double sum = 0;
        int n = 0;
        boolean keeper = Perception.isKeeper(c.getPlayer());
        for (Map.Entry<Attr, Range> e : v.entrySet()) {
            if (e.getValue().seen() && (keeper || e.getKey().group() != Perception.Group.KEEPER)) {
                sum += e.getValue().centre();
                n++;
            }
        }
        return n == 0 ? 0 : sum / n;
    }

    /** one observer's ranges of a man (his minutes, "heard of" in his network, his bias) */
    static Map<Attr, Range> view(SaveGame game, RandomSource root, Observers.Observer o, Player p, Candidate c) {
        float minutes = Math.max(Perception.minutes(game, o, p, c), Perception.HEARD_MINUTES);
        List<String> network = o.judgement().getNetwork();
        boolean local = c != null && c.getRegion() != null && !network.isEmpty() && c.getRegion().equals(network.get(0));
        boolean scot = p.getPerson() != null && p.getPerson().getCountry() != null
                && SCOTLAND.equals(p.getPerson().getCountry().getDataId());
        Map<Attr, Range> out = new LinkedHashMap<>();
        for (Attr a : Perception.ATTRIBUTES) {
            out.put(a, Perception.perceive(root, o, p, a, minutes, local, scot));
        }
        return out;
    }

    // --------------------------------------------------------------------------------------------- 4. agents

    private static void agent(SaveGame game, Club club, LocalDateTime now, RandomSource rng) {
        if (rng.nextDouble() >= AGENT_A_WEEK || data().getAgents().isEmpty()) {
            return;
        }
        PoolRegion own = CandidatePools.regionOf(club);
        if (own != null && SCOTLAND.equals(own.country())) {
            return; // the agents sell Scots to English clubs
        }
        List<Agent> agents = new ArrayList<>(data().getAgents().values());
        agentWrites(game, club, agents.get(rng.nextInt(agents.size())), now, rng);
    }

    /**
     * An agent writes to the club offering a few men of his region: the player's club gets his letter (names for his
     * fee), an AI club that can afford him pays at once now and then.
     *
     * @return the entries made (unpaid, or listed at an AI club)
     */
    static List<ShortlistEntry> agentWrites(SaveGame game, Club club, Agent agent, LocalDateTime now,
            RandomSource rng) {
        List<Candidate> men = new ArrayList<>();
        for (int i = 0; i < agent.names(); i++) {
            Candidate c = pick(game, club, agent.region(), null, rng.fork("man:" + i));
            if (c != null && !men.contains(c)) {
                men.add(c);
            }
        }
        List<ShortlistEntry> entries = new ArrayList<>();
        if (men.isEmpty()) {
            return entries;
        }
        boolean human = Shortlists.isHuman(game, club);
        int total = agent.feePence() * men.size();
        if (!human) {
            boolean affords = club.getClubBalance() != null && club.getClubBalance()
                    .compareTo(Shortlists.pounds(total * AI_AGENT_FEES_IN_HAND)) >= 0;
            if (!affords || rng.nextDouble() >= AI_AGENT_PAYS) {
                return entries;
            }
        }
        for (Candidate c : men) {
            ShortlistEntry e = Shortlists.arrive(game, club, c.getPlayer(), ShortlistEntry.AGENT,
                    human ? ShortlistEntry.UNPAID : ShortlistEntry.LISTED, now);
            if (e != null) {
                e.setDetail(agent.dataId());
                e.setSuggestedBy(agent.name());
                e.setFeePence(agent.feePence());
                entries.add(e);
            }
        }
        if (entries.isEmpty()) {
            return entries;
        }
        if (!human) {
            Shortlists.book(club, agent.feePence() * entries.size(), now);
            for (ShortlistEntry e : entries) {
                secretaryWatches(game, club, Shortlists.man(game, e.getPlayer()), LETTER_MINUTES);
            }
            return entries;
        }
        post(game, club, now, MSG_AGENT, LanguageModLoader.format("recruit_agent_title", agent.name(), agent.place()),
                LanguageModLoader.format("recruit_agent_body", entries.size(), regionName(agent.region()), Money.format(
                        Shortlists.pounds(agent.feePence())), agent.name(), agent.place()),
                entries);
        return entries;
    }

    /**
     * "Pay the agent" on his letter: his fee for each man not yet paid for is booked once, the men go on the list,
     * and his letter with their names and his (talked-up) word on each follows.
     *
     * @return the pence paid (0 when nothing was left to pay for)
     */
    public static int payAgent(SaveGame game, Club club, Long letter, LocalDateTime now, RandomSource root) {
        int paid = 0;
        List<ShortlistEntry> bought = new ArrayList<>();
        for (ShortlistEntry e : Shortlists.ofLetter(game, letter)) {
            if (club.getId().equals(e.getClub()) && ShortlistEntry.UNPAID.equals(e.getStatus())) {
                paid += e.getFeePence();
                e.setStatus(ShortlistEntry.LISTED);
                e.setSince(now);
                bought.add(e);
            }
        }
        if (paid == 0) {
            return 0;
        }
        Shortlists.book(club, paid, now);
        StringBuilder lines = new StringBuilder();
        Agent agent = null;
        for (ShortlistEntry e : bought) {
            Player p = Shortlists.man(game, e.getPlayer());
            if (p == null) {
                continue;
            }
            agent = data().getAgents().get(e.getDetail());
            secretaryWatches(game, club, p, LETTER_MINUTES);
            Candidate c = Perception.candidateOf(game, p);
            lines.append(LanguageModLoader.format("recruit_agent_line", name(p.getPerson()), position(p),
                    c != null && c.getPlaysFor() != null ? c.getPlaysFor() : LanguageModLoader.getValue("recruit_no_club"),
                    age(p.getPerson(), now), words(agentsWord(p, agent)))).append('\n');
        }
        String who = agent != null ? agent.name() : "";
        post(game, club, now, MSG_AGENT_NAMES, LanguageModLoader.format("recruit_agent_names_title", who,
                GameFormat.mediumDate(now.toLocalDate())),
                LanguageModLoader.format("recruit_agent_names_body", lines.toString().trim(), who), List.of());
        return paid;
    }

    /** the agent's word on his man: the truth talked up by his {@code inflate} points, no doubt in it */
    static Map<Attr, Range> agentsWord(Player p, Agent agent) {
        int up = agent != null ? agent.inflate() : 0;
        Map<Attr, Range> out = new LinkedHashMap<>();
        for (Attr a : Perception.ATTRIBUTES) {
            Float t = a.of(p);
            out.put(a, t == null ? Perception.UNSEEN : Perception.range(t, 0.5, 0, up));
        }
        return out;
    }

    // --------------------------------------------------------------------------------------------- 5. not retained

    /** May's "not retained" lists (G-04): once a season, the men other clubs let go at the rollover, still free. */
    private static void notRetained(SaveGame game, LocalDateTime now, RandomSource root) {
        LocalDate day = now.toLocalDate();
        int season = ContractCalendar.seasonOf(day);
        if (day.getMonthValue() != 5 || day.getDayOfMonth() < NOT_RETAINED_DAY
                || Integer.valueOf(season).equals(game.getNotRetainedListSeason())) {
            return;
        }
        game.setNotRetainedListSeason(season);
        List<Player> free = new ArrayList<>();
        for (Player p : game.getFreeAgents()) {
            if (p != null && p.getId() != null && p.getPerson() != null && p.getPerson().getPreviousClubId() != null
                    && game.getClubById(p.getPerson().getPreviousClubId()) != null) {
                free.add(p);
            }
        }
        if (free.isEmpty()) {
            return;
        }
        free.sort(Comparator.comparing((Player p) -> game.getClubById(p.getPerson().getPreviousClubId()).getName())
                .thenComparing(Player::getId));
        for (Club club : ContractManager.uniqueClubs(game)) {
            if (club.getId() == null) {
                continue;
            }
            List<Player> theirs = new ArrayList<>();
            for (Player p : free) {
                if (!club.getId().equals(p.getPerson().getPreviousClubId())) {
                    theirs.add(p);
                }
            }
            if (Shortlists.isHuman(game, club)) {
                List<ShortlistEntry> entries = new ArrayList<>();
                StringBuilder lines = new StringBuilder();
                for (Player p : theirs) {
                    if (entries.size() >= NOT_RETAINED_LISTED) {
                        break;
                    }
                    ShortlistEntry e = released(game, club, p, now);
                    if (e != null) {
                        entries.add(e);
                        lines.append(LanguageModLoader.format("recruit_released_line", name(p.getPerson()),
                                position(p), e.getDetail())).append('\n');
                    }
                }
                if (!entries.isEmpty()) {
                    post(game, club, now, MSG_RELEASED,
                            LanguageModLoader.format("recruit_released_title", ContractCalendar.seasonName(season)),
                            LanguageModLoader.format("recruit_released_body", ContractCalendar.seasonName(season),
                                    lines.toString().trim()),
                            entries);
                }
            } else {
                RandomSource rng = root.fork("recruit:src:released:" + clubKey(club) + ":" + season);
                for (int i = 0; i < NOT_RETAINED_AI_TAKES && !theirs.isEmpty(); i++) {
                    released(game, club, theirs.remove(rng.nextInt(theirs.size())), now);
                }
            }
        }
    }

    private static ShortlistEntry released(SaveGame game, Club club, Player p, LocalDateTime now) {
        ShortlistEntry e = arrive(game, club, p, ShortlistEntry.RELEASED, now);
        if (e == null) {
            return null;
        }
        Club late = game.getClubById(p.getPerson().getPreviousClubId());
        e.setDetail(late != null ? late.getName() : null);
        e.setSuggestedBy(LanguageModLoader.getValue("recruit_by_press"));
        secretaryWatches(game, club, p, RELEASED_MINUTES);
        Person trainer = StaffRoles.trainerOf(game, club);
        if (trainer != null && trainer.getId() != null) {
            Perception.watch(game, Observers.personKey(trainer), p, RELEASED_MINUTES);
        }
        return e;
    }

    // --------------------------------------------------------------------------------------------- helpers

    /** a new entry: offered in a letter (the player's club) or listed at once (an AI club) */
    private static ShortlistEntry arrive(SaveGame game, Club club, Player man, String source, LocalDateTime now) {
        return Shortlists.arrive(game, club, man, source,
                Shortlists.isHuman(game, club) ? ShortlistEntry.OFFERED : ShortlistEntry.LISTED, now);
    }

    /**
     * A man of the region the club has no entry for yet (of the line, when one is asked and the region has one), or
     * null.
     */
    static Candidate pick(SaveGame game, Club club, String region, Position.Line line, RandomSource rng) {
        int u = rng.nextInt(1 << 20);
        if (region == null) {
            return null;
        }
        List<Candidate> fresh = new ArrayList<>();
        List<Candidate> ofLine = new ArrayList<>();
        for (Candidate c : CandidatePools.inRegion(game, region)) {
            if (c.getPlayer() == null || c.getPlayer().getId() == null || c.getPlayer().getPerson() == null
                    || Shortlists.entry(game, club.getId(), c.getPlayer().getId()) != null) {
                continue;
            }
            fresh.add(c);
            if (line != null && c.getPlayer().getPosition() != null && c.getPlayer().getPosition().getLine() == line) {
                ofLine.add(c);
            }
        }
        List<Candidate> from = !ofLine.isEmpty() ? ofLine : fresh;
        return from.isEmpty() ? null : from.get(u % from.size());
    }

    private static void secretaryWatches(SaveGame game, Club club, Player p, float minutes) {
        Person secretary = StaffRoles.secretaryOf(game, club);
        if (secretary != null && secretary.getId() != null && p != null) {
            Perception.watch(game, Observers.personKey(secretary), p, minutes);
        }
    }

    /** an inbox letter for the player's club; its men's entries remember it */
    private static void post(SaveGame game, Club club, LocalDateTime now, String type, String title, String body,
            List<ShortlistEntry> entries) {
        Message m = new Message();
        m.setId(game.getIdAllocator().nextId());
        m.setCategory(MessageCategory.RECRUITMENT);
        m.setMessageType(type);
        m.setPriority(MessagePriority.NORMAL);
        m.setTitle(title);
        m.setPlainTextMessage(body);
        m.setMessageTime(now);
        m.setActionData(club.getId());
        m.setActionScreen(GameScreenId.SHORTLIST);
        m.setIsRead(false);
        m.setIsDeleted(false);
        game.getAllMessages().add(m);
        for (ShortlistEntry e : entries) {
            e.setLetter(m.getId());
        }
    }

    /** the period words for a view, joined ("a clever dribbler, wants pace") */
    static String words(Map<Attr, Range> ranges) {
        StringBuilder b = new StringBuilder();
        for (String key : Perception.phraseKeys(ranges, false)) {
            b.append(b.length() == 0 ? "" : LanguageModLoader.getValue("perceive_join"))
                    .append(LanguageModLoader.getValue(key));
        }
        return b.toString();
    }

    static String name(Person person) {
        return person == null ? "" : ((person.getName() == null ? "" : person.getName()) + " "
                + (person.getLastname() == null ? "" : person.getLastname())).trim();
    }

    static String position(Player p) {
        return LanguageModLoader.getValue(p.getPosition() != null ? p.getPosition().labelKey() : "position_unknown");
    }

    static int age(Person person, LocalDateTime now) {
        return person == null || person.getBirthDate() == null ? 0 : CandidatePools.age(person, now);
    }

    /** a pool region's shown name (its data id when unknown) */
    static String regionName(String id) {
        PoolRegion r = data().getPools().get(id);
        return r != null ? r.name() : id;
    }
}
