package com.rndmodgames.futtoboru.engine.recruitment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.data.PlayerProfession;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.language.LanguageSkill;
import com.rndmodgames.futtoboru.data.recruitment.Candidate;
import com.rndmodgames.futtoboru.data.recruitment.Origin;
import com.rndmodgames.futtoboru.data.recruitment.OriginClub;
import com.rndmodgames.futtoboru.data.recruitment.PoolRegion;
import com.rndmodgames.futtoboru.engine.contracts.ContractCalendar;
import com.rndmodgames.futtoboru.engine.contracts.ContractManager;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.data.LanguageData;
import com.rndmodgames.futtoboru.system.data.RecruitmentData;
import com.rndmodgames.futtoboru.system.generators.LanguageGenerator;
import com.rndmodgames.futtoboru.system.generators.NameGenerator;
import com.rndmodgames.futtoboru.system.generators.PlayerAttributeGenerator;
import com.rndmodgames.futtoboru.system.generators.PositionAssigner;

/**
 * R-03 (docs/design/RECRUITMENT_AND_TRIALS.md §3.2): seeded candidate pools per region ({@code recruitment/pools.json})
 * and season. Each pool is built at the close season (1 May, and the first day of a game) on
 * {@code fork("recruit:pool:" + season + ":" + region)}: the region's <b>real men first</b> (D28 Q2: a sourced
 * {@code people.json} man listed for the season, in no squad, 17 or older; real identity, generated ability), then
 * <b>generated men</b> up to the region's size. In the season (September to April) a few men arrive on the 1st of
 * each month and as many of the longest-waiting generated men leave, so the pool turns over slowly.
 *
 * <p>A generated man: a §3.1 source kind of the region with its age band (never under 17, D28 Q9); born abroad at the
 * region's {@code foreignBorn} rates (Q13), with the age he came over at; an origin backstory of that kind with its
 * small attribute nudges and occupation (Q12); the region's football upbringing, each tendency taken with its chance
 * (Q14); languages and literacy from his birth country, area and cohort (R-11a), English native when he came as a
 * child. All on his own fork {@code fork("recruit:cand:" + season + ":" + region + ":" + n)} (a real man:
 * {@code "recruit:cand:" + dataId}, so he is the same man every season), so a reload cannot reroll a man.
 *
 * <p>Placeholders (D30 A3): age bands per kind, the 70% of the foreign-born who came as children, the 35% chance of a
 * sourced origin club, the in-season months and turnover.
 */
public final class CandidatePools {

    /** The youngest man a pool holds (D28 Q9: the youngest sourced debut, R§5.3). */
    public static final int MIN_AGE = 17;
    /** A foreign-born man came over as a child (age 0..12) this often, else as a man (placeholder). */
    static final double ARRIVED_AS_CHILD = 0.7;
    static final int CHILD_MAX_AGE = 12;
    static final int ADULT_ARRIVAL_AGE = 16;
    /** The chance a man plays for one of the region's sourced origin clubs rather than a local one (placeholder). */
    static final double ORIGIN_CLUB_CHANCE = 0.35;

    private static final PlayerAttributeGenerator ATTRIBUTES = new PlayerAttributeGenerator();

    private CandidatePools() {
    }

    /** @return the age band {min, max} of a §3.1 source kind (design table; under 17 lifted to 17) */
    static int[] ageBand(String kind) {
        switch (kind == null ? "" : kind) {
        case "works-team":
            return new int[] { 18, 28 };
        case "scottish-import":
            return new int[] { 19, 26 };
        case "league-poach":
            return new int[] { 20, 30 };
        case "released":
            return new int[] { 22, 32 };
        case "amateur":
            return new int[] { 20, 30 };
        case "veteran":
            return new int[] { 30, 34 };
        default: // local-junior, and a schoolboy only from 17
            return new int[] { MIN_AGE, 24 };
        }
    }

    // --------------------------------------------------------------------------------------------- the day

    /**
     * The pools' day: a new season's pools on the first day of the season's engagements (1 May) or of a game, else
     * on the 1st of a month in the season the month's arrivals. Idempotent per season and month.
     *
     * @return true when the pools changed
     */
    public static boolean afterDay(SaveGame game, LocalDateTime now, RandomSource root) {
        if (game == null || now == null || root == null || data() == null) {
            return false;
        }
        int season = poolSeason(game, now.toLocalDate());
        if (game.getCandidatePoolSeason() == null || game.getCandidatePoolSeason() != season) {
            build(game, season, now, root);
            return true;
        }
        String month = month(now.toLocalDate());
        if (now.getDayOfMonth() == 1 && inSeason(now.toLocalDate()) && !month.equals(game.getCandidateArrivalsMonth())) {
            arrivals(game, season, now, root);
            return true;
        }
        return false;
    }

    /**
     * The season the pools are for: the engagements' season ({@code ContractCalendar.seasonOf}: from 1 May), never
     * before the game's own first season (the 1888-89 game starts on 1 April 1888 with 1888 engagements, G-04).
     */
    static int poolSeason(SaveGame game, LocalDate day) {
        int season = ContractCalendar.seasonOf(day);
        if (game.getContractsSeason() != null) {
            season = Math.max(season, game.getContractsSeason());
        } else if (game.getGameStartDate() != null) {
            season = Math.max(season, game.getGameStartDate().getYear());
        }
        return season;
    }

    /** @return true from September to April, when a few men a month come and go */
    static boolean inSeason(LocalDate day) {
        int m = day.getMonthValue();
        return m >= 9 || m <= 4;
    }

    private static String month(LocalDate day) {
        return day.getYear() + "-" + String.format(java.util.Locale.ROOT, "%02d", day.getMonthValue());
    }

    private static RecruitmentData data() {
        return DatabaseLoader.getInstance().getRecruitmentData();
    }

    /**
     * Rebuilds every region's pool for {@code season}: last season's men leave, except those a club still has an open
     * entry for (R-05: a shortlisted man, an advert reply on its way), who stay on top of the new pool.
     */
    public static void build(SaveGame game, int season, LocalDateTime now, RandomSource root) {
        RecruitmentData data = data();
        Set<Long> held = Shortlists.held(game);
        List<Candidate> kept = new ArrayList<>();
        for (Candidate c : game.getCandidates()) {
            if (c != null && c.getPlayer() != null && held.contains(c.getPlayer().getId())) {
                kept.add(c);
            }
        }
        game.getCandidates().clear();
        game.getCandidates().addAll(kept);
        Set<String> inSquads = peopleInSquads(game);
        for (Candidate c : kept) {
            if (c.getPlayer().getPerson() != null && c.getPlayer().getPerson().getDataId() != null) {
                inSquads.add(c.getPlayer().getPerson().getDataId()); // a kept real man is not drawn twice
            }
        }
        for (PoolRegion region : data.getPools().values()) {
            game.getCandidates().addAll(buildRegion(game, region, season, now, root, inSquads));
        }
        game.setCandidatePoolSeason(season);
        game.setCandidateArrivalsMonth(month(now.toLocalDate()));
        Log.info(Log.ENGINE_GAME, "CandidatePools", game.getCandidates().size() + " candidates in "
                + data.getPools().size() + " regions for " + ContractCalendar.seasonName(season));
    }

    /** One region's pool: its real men first, then generated men up to its size. */
    static List<Candidate> buildRegion(SaveGame game, PoolRegion region, int season, LocalDateTime now,
            RandomSource root, Set<String> inSquads) {
        List<Candidate> out = new ArrayList<>();
        for (PoolRegion.PoolPerson pp : region.people()) {
            if (out.size() >= region.size() || !pp.listedIn(season) || inSquads.contains(pp.person())) {
                continue;
            }
            Person person = DatabaseLoader.getInstance().getPersonByDataId(pp.person());
            if (person == null || person.getBirthDate() == null || age(person, now) < MIN_AGE) {
                continue;
            }
            out.add(real(game, region, pp, person, season, now, root.fork("recruit:cand:" + pp.person())));
        }
        for (int n = 0; out.size() < region.size(); n++) {
            out.add(generated(game, region, season, now,
                    root.fork("recruit:cand:" + season + ":" + region.dataId() + ":" + n)));
        }
        return out;
    }

    /** The month's arrivals: {@code monthly} new men per region, and as many of its longest-waiting generated men go. */
    public static void arrivals(SaveGame game, int season, LocalDateTime now, RandomSource root) {
        String month = month(now.toLocalDate());
        Set<Long> held = Shortlists.held(game); // R-05: a man some club is looking at does not leave
        for (PoolRegion region : data().getPools().values()) {
            for (int n = 0; n < region.monthly(); n++) {
                game.getCandidates().add(generated(game, region, season, now,
                        root.fork("recruit:cand:" + season + ":" + region.dataId() + ":" + month + ":" + n)));
            }
            int over = inRegion(game, region.dataId()).size() - region.size();
            for (Iterator<Candidate> it = game.getCandidates().iterator(); it.hasNext() && over > 0;) {
                Candidate c = it.next();
                if (region.dataId().equals(c.getRegion()) && !c.isReal()
                        && (c.getPlayer() == null || !held.contains(c.getPlayer().getId()))) {
                    it.remove();
                    over--;
                }
            }
        }
        game.setCandidateArrivalsMonth(month);
    }

    // --------------------------------------------------------------------------------------------- lookups

    /** @return the candidates of one region, in pool order */
    public static List<Candidate> inRegion(SaveGame game, String region) {
        List<Candidate> out = new ArrayList<>();
        for (Candidate c : game.getCandidates()) {
            if (c != null && region != null && region.equals(c.getRegion())) {
                out.add(c);
            }
        }
        return out;
    }

    /** @return the pool region of a club, or null */
    public static PoolRegion regionOf(Club club) {
        RecruitmentData data = data();
        return data == null || club == null ? null : data.poolOfClub(club.getDataId());
    }

    /** @return his age on {@code day} */
    public static int age(Person person, LocalDateTime day) {
        return (int) ChronoUnit.YEARS.between(person.getBirthDate().toLocalDate(), day.toLocalDate());
    }

    /** The data ids of the people in a squad or among the free agents (a real man there is not a candidate). */
    static Set<String> peopleInSquads(SaveGame game) {
        Set<String> out = new HashSet<>();
        for (Club club : ContractManager.uniqueClubs(game)) {
            for (Player p : club.getPlayers()) {
                if (p != null && p.getPerson() != null && p.getPerson().getDataId() != null) {
                    out.add(p.getPerson().getDataId());
                }
            }
        }
        for (Player p : game.getFreeAgents()) {
            if (p != null && p.getPerson() != null && p.getPerson().getDataId() != null) {
                out.add(p.getPerson().getDataId());
            }
        }
        return out;
    }

    // --------------------------------------------------------------------------------------------- one man

    /** A real man: his data identity, a generated ability (D28 Q2), his sourced club and kind. */
    static Candidate real(SaveGame game, PoolRegion region, PoolRegion.PoolPerson pp, Person person, int season,
            LocalDateTime now, RandomSource rng) {
        Player player = new Player();
        player.setId(game.getIdAllocator().nextId());
        player.setPerson(person);
        ATTRIBUTES.generatePlayerAttributes(player, person, now, rng.fork("attributes"));
        Position position = Position.fromDataId(pp.position());
        player.setPosition(position != null ? position : PositionAssigner.forGenerated(player));
        Candidate c = base(region, season, now, player, pp.kind());
        c.setReal(true);
        c.setPlaysFor(pp.playsFor());
        return c;
    }

    /** A generated man of the region (see the class comment). */
    static Candidate generated(SaveGame game, PoolRegion region, int season, LocalDateTime now, RandomSource rng) {
        RecruitmentData data = data();
        List<String> kinds = region.sourceKinds();
        String kind = kinds.isEmpty() ? "local-junior" : kinds.get(rng.nextInt(kinds.size()));
        int[] band = ageBand(kind);
        int age = Math.max(MIN_AGE, band[0] + rng.nextInt(band[1] - band[0] + 1));

        // born abroad at the region's rates (D28 Q13)
        String born = region.country();
        double u = rng.nextDouble();
        double acc = 0;
        for (Map.Entry<String, Double> e : region.foreignBorn().entrySet()) {
            acc += e.getValue();
            if (u < acc) {
                born = e.getKey();
                break;
            }
        }
        boolean foreign = !born.equals(region.country());
        Integer arrived = null;
        if (foreign) {
            arrived = rng.nextDouble() < ARRIVED_AS_CHILD ? rng.nextInt(CHILD_MAX_AGE + 1)
                    : ADULT_ARRIVAL_AGE + rng.nextInt(age - ADULT_ARRIVAL_AGE + 1);
        }

        // origin backstory of his kind (D28 Q12)
        List<Origin> origins = new ArrayList<>();
        for (Origin o : data.getOrigins().values()) {
            if (o.getSourceKinds().contains(kind)) {
                origins.add(o);
            }
        }
        Origin origin = origins.isEmpty() ? null : origins.get(rng.nextInt(origins.size()));
        PlayerProfession occupation = null;
        if (origin != null && !origin.getOccupations().isEmpty()) {
            occupation = DatabaseLoader.getInstance().getPlayerProfessionByDataId(
                    origin.getOccupations().get(rng.nextInt(origin.getOccupations().size())));
        }

        // the man
        Country nameCountry = DatabaseLoader.getInstance().getCountryByDataId(born);
        if (nameCountry == null || DatabaseLoader.getInstance().getFirstNames(nameCountry).size() < 2
                || DatabaseLoader.getInstance().getLastNames(nameCountry).size() < 2) {
            nameCountry = DatabaseLoader.getInstance().getCountryByDataId("england");
        }
        String[] name = NameGenerator.generateName(nameCountry, rng);
        Person person = new Person();
        person.setName(name[0]);
        person.setLastname(name[1]);
        Country birthCountry = DatabaseLoader.getInstance().getCountryByDataId(born);
        person.setCountry(birthCountry);
        person.setCurrentCountry(DatabaseLoader.getInstance().getCountryByDataId(region.country()));
        person.setBirthDate(now.minusYears(age).minusDays(rng.nextInt(365)).toLocalDate().atStartOfDay());
        person.setPrimaryProfession(DatabaseLoader.getInstance().getProfessionByDataId(Profession.PLAYER));
        game.assignId(person);

        Player player = new Player();
        player.setId(game.getIdAllocator().nextId());
        player.setPerson(person);
        player.setPlayerProfession(occupation);
        ATTRIBUTES.generatePlayerAttributes(player, person, now, rng.fork("attributes"));
        if (origin != null) {
            for (Map.Entry<String, Integer> n : origin.getNudges().entrySet()) {
                ATTRIBUTES.applyNudge(player, n.getKey(), n.getValue());
            }
        }
        // his football upbringing (D28 Q14): a man who came over grown learned the game elsewhere
        List<String> taken = new ArrayList<>();
        boolean raisedHere = !foreign || arrived <= CHILD_MAX_AGE;
        PoolRegion.Upbringing up = region.upbringing();
        for (Map.Entry<String, Integer> t : up.tendencies().entrySet()) {
            double uTaken = rng.nextDouble();
            if (raisedHere && uTaken < up.chance()) {
                ATTRIBUTES.applyNudge(player, t.getKey(), t.getValue());
                taken.add(t.getKey());
            }
        }
        player.setDribbling(PlayerAttributeGenerator.deriveDribbling(player.getDexterity(), player.getAcceleration(),
                player.getTrickShots(), player.getSpeed()));
        player.setPosition(PositionAssigner.forGenerated(player));

        languages(person, region, born, arrived, origin, rng.fork("lang"));

        Candidate c = base(region, season, now, player, kind);
        c.setOrigin(origin != null ? origin.getDataId() : null);
        c.setArrivedAge(arrived);
        c.setUpbringing(raisedHere ? up.style() : null);
        c.setTendencies(taken);
        playsFor(c, region, origin, kind, rng);
        return c;
    }

    private static Candidate base(PoolRegion region, int season, LocalDateTime now, Player player, String kind) {
        Candidate c = new Candidate();
        c.setPlayer(player);
        c.setRegion(region.dataId());
        c.setSourceKind(kind);
        c.setSeason(season);
        c.setSince(now);
        return c;
    }

    /** Where he plays: a sourced origin club of the region (sometimes), else a local club; a released man has none. */
    private static void playsFor(Candidate c, PoolRegion region, Origin origin, String kind, RandomSource rng) {
        double u = rng.nextDouble();
        int pick = rng.nextInt(1 << 16);
        if ("released".equals(kind)) {
            return;
        }
        RecruitmentData data = data();
        List<OriginClub> sourced = new ArrayList<>();
        for (String id : region.originClubs()) {
            OriginClub oc = data.getOriginClubs().get(id);
            if (oc != null && (origin == null || oc.getOrigins().isEmpty() || oc.getOrigins().contains(origin.getDataId())
                    || "scottish".equals(oc.getKind()))) {
                sourced.add(oc);
            }
        }
        if (!sourced.isEmpty() && u < ORIGIN_CLUB_CHANCE) {
            OriginClub oc = sourced.get(pick % sourced.size());
            c.setOriginClub(oc.getDataId());
            c.setPlaysFor(oc.getName());
            return;
        }
        if (!region.localClubs().isEmpty() && !region.places().isEmpty()) {
            String pattern = region.localClubs().get(pick % region.localClubs().size());
            String place = region.places().get((pick / region.localClubs().size()) % region.places().size());
            c.setPlaysFor(pattern.replace("{place}", place));
        }
    }

    /**
     * His languages (R-11a, design §10.2) from his birth country and cohort, with his origin's literacy shift; a man
     * who came over as a child went to school here: native English and the region's schooling.
     */
    static void languages(Person person, PoolRegion region, String born, Integer arrived, Origin origin,
            RandomSource rng) {
        LanguageData lang = DatabaseLoader.getInstance().getLanguageData();
        if (lang == null || lang.getLanguages().isEmpty()) {
            return;
        }
        int year = person.getBirthDate().getYear();
        int shift = origin != null ? origin.getLiteracyShift() : 0;
        boolean foreign = arrived != null;
        LanguageGenerator.Result r = LanguageGenerator.generate(lang, born, foreign ? null : region.area(), year, shift,
                rng.fork("birth"));
        List<LanguageSkill> skills = new ArrayList<>(r.languages());
        if (foreign && arrived <= CHILD_MAX_AGE) {
            // schooled here: English native at the region's literacy; his home language (first) stays spoken, read
            // and written a level less than English at most
            LanguageGenerator.Result school = LanguageGenerator.generate(lang, region.country(), region.area(), year,
                    shift, rng.fork("school"));
            int readWrite = 0;
            for (LanguageSkill s : school.languages()) {
                if (LanguageData.ENGLISH.equals(s.getLanguage())) {
                    readWrite = s.getReadWrite();
                }
            }
            List<LanguageSkill> out = new ArrayList<>();
            LanguageSkill english = new LanguageSkill(LanguageData.ENGLISH, LanguageSkill.MAX, LanguageSkill.MAX,
                    readWrite);
            if (LanguageData.ENGLISH.equals(r.homeLanguage())) {
                out.add(english);
            }
            for (LanguageSkill s : skills) {
                if (LanguageData.ENGLISH.equals(s.getLanguage())) {
                    continue;
                }
                out.add(s.getLanguage().equals(r.homeLanguage())
                        ? new LanguageSkill(s.getLanguage(), s.getSpeak(), s.getUnderstand(),
                                Math.min(s.getReadWrite(), Math.max(0, readWrite - 1)))
                        : s);
                if (s.getLanguage().equals(r.homeLanguage())) {
                    out.add(english);
                }
            }
            skills = out;
        }
        person.setBirthArea(r.birthArea());
        person.setParentsOrigin(r.parentsOrigin());
        person.setHomeLanguage(r.homeLanguage());
        person.setLanguages(skills);
    }
}
