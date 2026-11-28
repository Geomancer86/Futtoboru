package com.rndmodgames.futtoboru.system.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.language.LanguageRate;
import com.rndmodgames.futtoboru.data.language.LanguageSkill;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.system.data.DataRecord;
import com.rndmodgames.futtoboru.system.data.GameData;
import com.rndmodgames.futtoboru.system.data.LanguageData;

/**
 * R-11a (design §10.2): a person's languages and literacy, generated from his upbringing only: birth area (sourced,
 * else drawn from the area weights of his country), birth-year band, parents' origin (drawn from the share of people
 * born in another country) and schooling (the cohort's signature rate and read/write split, moved by his origin's
 * literacy shift). Never from his football attributes, and never on the player's random stream: every draw comes from
 * the person's own fork {@code root.fork("lang:" + key)} ({@link #streamName(Person)}), so a person's languages depend
 * only on the game seed and who he is. The same code fills a loaded old save (v2 -> v3), so it equals a new game.
 *
 * <p>Level rules beyond the data are estimates (marked here, tunable later): a bilingual man's English is basic to
 * native; a man who speaks only his home language knows a few words of English; a parent's language is passed on with
 * the chance that a person of the parent's country spoke it, at a few words to basic; the schooled language (English,
 * unless he speaks under basic English) carries his read/write level, his home language one level less, a heritage
 * language none. All men in the game are men, so the men's signature rate is used.
 */
public final class LanguageGenerator {

    /** Birth year used when a person has no birth date. */
    static final int DEFAULT_BIRTH_YEAR = 1865;
    /** The fallback split of read/write levels 1..4 for a man who signs, when no row gives one (estimate). */
    static final double[] DEFAULT_READ_WRITE = { 0.3, 0.35, 0.25, 0.1 };
    static final double DEFAULT_SIGNS = 0.7;

    /** What the generator decided for one person. */
    public record Result(String birthArea, String parentsOrigin, String homeLanguage, List<LanguageSkill> languages) {
    }

    private LanguageGenerator() {
    }

    /** @return the random stream name of a person's languages: data id, else runtime id, else name and birth date */
    public static String streamName(Person p) {
        if (p.getDataId() != null) {
            return "lang:" + p.getDataId();
        }
        if (p.getId() != null) {
            return "lang:id:" + p.getId();
        }
        return "lang:gen:" + p.getName() + "|" + p.getLastname() + "|" + p.getBirthDate();
    }

    /**
     * @param country      country data id of his birth (null = England)
     * @param sourcedArea  his birth area when a source gives it ({@code people.json birthArea}), else null
     * @param literacyShift his origin's literacy shift (-1..1; 0 without an origin)
     * @param rng          the person's own fork
     */
    public static Result generate(LanguageData data, String country, String sourcedArea, int birthYear,
            int literacyShift, RandomSource rng) {
        String c = country == null ? "england" : country;
        // 1. birth area
        String area = sourcedArea;
        double u = rng.nextDouble();
        if (area == null) {
            double acc = 0;
            for (LanguageData.Area a : data.areasOf(c)) {
                acc += a.weight();
                area = a.id();
                if (u < acc) {
                    break;
                }
            }
        }
        boolean britishIsles = !data.areasOf(c).isEmpty();
        // 2. parents' origin
        String parents = pick(data.rate(LanguageRate::getParentsBorn, area, c, birthYear), rng.nextDouble());
        // 3. home language and English
        Map<String, Double> speaks = data.rate(LanguageRate::getSpeaks, area, c, birthYear);
        String home = pick(speaks, rng.nextDouble());
        if (home == null) {
            home = data.nationalLanguage(c);
        }
        List<LanguageSkill> out = new ArrayList<>();
        out.add(new LanguageSkill(home, LanguageSkill.MAX, LanguageSkill.MAX, 0));
        int englishSpeak = LanguageSkill.MAX;
        int englishUnderstand = LanguageSkill.MAX;
        double uOnly = rng.nextDouble();
        double uLevel = rng.nextDouble();
        double uBump = rng.nextDouble();
        if (!LanguageData.ENGLISH.equals(home)) {
            Map<String, Double> only = data.rate(LanguageRate::getOnly, area, c, birthYear);
            double share = speaks == null ? 0 : speaks.getOrDefault(home, 0.0);
            double onlyShare = only == null || share <= 0 ? 0 : only.getOrDefault(home, 0.0) / share;
            boolean monolingual = !britishIsles || uOnly < onlyShare;
            if (monolingual) {
                englishSpeak = uLevel < 0.5 ? 0 : 1;
                englishUnderstand = 1;
            } else {
                englishSpeak = uLevel < 0.25 ? 2 : uLevel < 0.75 ? 3 : 4;
                englishUnderstand = Math.min(LanguageSkill.MAX, englishSpeak + (uBump < 0.5 ? 1 : 0));
            }
        }
        // 4. a parent's language
        double uHeritage = rng.nextDouble();
        double uHeritageLevel = rng.nextDouble();
        String heritage = null;
        if (parents != null && !parents.equals(c)) {
            String best = null;
            double bestShare = 0;
            for (String l : heritageCandidates(data, parents, birthYear)) {
                double s = data.speakersIn(parents, l, birthYear);
                if (s > bestShare) {
                    best = l;
                    bestShare = s;
                }
            }
            if (best != null && !best.equals(home) && uHeritage < bestShare) {
                heritage = best;
            }
        }
        // 5. literacy (schooling): signs his name, then the read/write split, moved by the origin
        Double signs = data.rate(LanguageRate::getSignsMen, area, c, birthYear);
        double[] split = data.rate(LanguageRate::getReadWriteIfSigns, area, c, birthYear);
        double uSigns = rng.nextDouble();
        double uSplit = rng.nextDouble();
        int level = 0;
        if (uSigns < (signs == null ? DEFAULT_SIGNS : signs)) {
            double[] s = split == null ? DEFAULT_READ_WRITE : split;
            double acc = 0;
            level = s.length;
            for (int i = 0; i < s.length; i++) {
                acc += s[i];
                if (uSplit < acc) {
                    level = i + 1;
                    break;
                }
            }
        }
        level = Math.max(0, Math.min(LanguageSkill.MAX, level + literacyShift));
        boolean schooledInEnglish = LanguageData.ENGLISH.equals(home) || englishSpeak >= 2;
        if (LanguageData.ENGLISH.equals(home)) {
            out.set(0, new LanguageSkill(home, LanguageSkill.MAX, LanguageSkill.MAX, level));
        } else {
            out.set(0, new LanguageSkill(home, LanguageSkill.MAX, LanguageSkill.MAX,
                    schooledInEnglish ? Math.max(0, level - 1) : level));
            out.add(new LanguageSkill(LanguageData.ENGLISH, englishSpeak, englishUnderstand,
                    schooledInEnglish ? level : Math.min(level, 1)));
        }
        if (heritage != null) {
            int speak = uHeritageLevel < 0.5 ? 1 : 2;
            out.add(new LanguageSkill(heritage, speak, Math.min(3, speak + 1), 0));
        }
        return new Result(area, parents, home, out);
    }

    /** The non-English languages a parent from that country may pass on (its rate rows' languages, its national one). */
    private static Set<String> heritageCandidates(LanguageData data, String country, int birthYear) {
        Set<String> out = new LinkedHashSet<>();
        for (LanguageData.Area a : data.areasOf(country)) {
            Map<String, Double> s = data.rate(LanguageRate::getSpeaks, a.id(), country, birthYear);
            if (s != null) {
                out.addAll(s.keySet());
            }
        }
        String national = data.nationalLanguage(country);
        if (!LanguageData.ENGLISH.equals(national)) {
            out.add(national);
        }
        return out;
    }

    /** @return the key whose cumulative share first exceeds {@code u} (file order), or null */
    private static String pick(Map<String, Double> shares, double u) {
        if (shares == null) {
            return null;
        }
        double acc = 0;
        for (Map.Entry<String, Double> e : shares.entrySet()) {
            acc += e.getValue();
            if (u < acc) {
                return e.getKey();
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------ persons

    /**
     * Gives a person his languages (birth area from the data when sourced, literacy shift 0: origins come with R-03).
     *
     * @param root the database random source of the game ({@code seed -> fork("database")})
     */
    public static void assign(Person p, LanguageData data, GameData gameData, RandomSource root) {
        if (p == null || data == null || data.getLanguages().isEmpty()) {
            return;
        }
        String sourcedArea = null;
        if (gameData != null && p.getDataId() != null) {
            DataRecord r = gameData.get(GameData.PERSON, p.getDataId());
            sourcedArea = r == null ? null : r.ref("birthArea");
        }
        int year = p.getBirthDate() == null ? DEFAULT_BIRTH_YEAR : p.getBirthDate().getYear();
        String country = p.getCountry() == null ? null : p.getCountry().getDataId();
        Result r = generate(data, country, sourcedArea, year, 0, root.fork(streamName(p)));
        p.setBirthArea(r.birthArea());
        p.setParentsOrigin(r.parentsOrigin());
        p.setHomeLanguage(r.homeLanguage());
        p.setLanguages(new ArrayList<>(r.languages()));
    }

    /** Fills every person who has no languages yet (an old save, a person made before the data had languages). */
    public static int fillMissing(Collection<Person> people, LanguageData data, GameData gameData, RandomSource root) {
        int n = 0;
        for (Person p : people) {
            if (p != null && p.getLanguages().isEmpty()) {
                assign(p, data, gameData, root);
                n++;
            }
        }
        return n;
    }
}
