package com.rndmodgames.futtoboru.system.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.badlogic.gdx.utils.JsonValue;
import com.rndmodgames.futtoboru.data.language.LanguageRate;

/**
 * R-11a: the language data of the packs ({@code languages.json}, {@code areas.json}, {@code language_rates.json}) and
 * the rate lookup of design §10.2. A rate is looked up per field: the row of the man's birth area covering his birth
 * year, else the row of his country, else the default row (validate.py guarantees one row per target, field and year).
 * A birth year outside every band counts as the nearest band year.
 */
public final class LanguageData {

    /** The language of the schools in Britain and Ireland, and the default home language. */
    public static final String ENGLISH = "english";

    /** A language of {@code languages.json}; {@code countries} = where it is the national (school) language. */
    public record Language(String id, long runtimeId, String name, List<String> countries) {
    }

    /** A rate area of {@code areas.json}; {@code weight} = its share of the country's people (1881). */
    public record Area(String id, long runtimeId, String name, String country, double weight) {
    }

    private final Map<String, Language> languages = new LinkedHashMap<>();
    private final Map<String, Area> areas = new LinkedHashMap<>();
    private final List<LanguageRate> rates = new ArrayList<>();
    private int minYear = Integer.MAX_VALUE;
    private int maxYear = Integer.MIN_VALUE;

    private LanguageData() {
    }

    public static LanguageData from(GameData data) {
        LanguageData out = new LanguageData();
        for (DataRecord r : data.all(GameData.LANGUAGE)) {
            out.languages.put(r.id(), new Language(r.id(), r.runtimeId(), r.str("name"), r.refs("countries")));
        }
        for (DataRecord r : data.all(GameData.AREA)) {
            out.areas.put(r.id(), new Area(r.id(), r.runtimeId(), r.str("name"), r.ref("country"),
                    r.decimal("weight").doubleValue()));
        }
        for (DataRecord r : data.records("language_rate", null)) {
            JsonValue rw = r.json.get("readWriteIfSigns");
            double[] split = null;
            if (rw != null && rw.isArray()) {
                split = new double[rw.size];
                int i = 0;
                for (JsonValue x = rw.child; x != null; x = x.next) {
                    split[i++] = x.asDouble();
                }
            }
            LanguageRate rate = new LanguageRate(r.ref("area"), r.has("countries") ? r.refs("countries") : null,
                    r.integer("bornFrom"), r.integer("bornUntil"), table(data, r, "speaks", GameData.LANGUAGE),
                    table(data, r, "only", GameData.LANGUAGE), number(r, "signsMen"), number(r, "signsWomen"),
                    table(data, r, "parentsBorn", GameData.COUNTRY), split, r.str("confidence"));
            out.rates.add(rate);
            out.minYear = Math.min(out.minYear, rate.getBornFrom());
            out.maxYear = Math.max(out.maxYear, rate.getBornUntil());
        }
        return out;
    }

    private static Map<String, Double> table(GameData data, DataRecord r, String field, String kind) {
        return r.has(field) ? Collections.unmodifiableMap(data.shares(r, field, kind)) : null;
    }

    private static Double number(DataRecord r, String field) {
        return r.has(field) ? r.decimal(field).doubleValue() : null;
    }

    public Map<String, Language> getLanguages() {
        return Collections.unmodifiableMap(languages);
    }

    public Language language(String id) {
        return id == null ? null : languages.get(id);
    }

    public Map<String, Area> getAreas() {
        return Collections.unmodifiableMap(areas);
    }

    public Area area(String id) {
        return id == null ? null : areas.get(id);
    }

    /** @return the areas of a country in file order (empty when it has none: a man born abroad) */
    public List<Area> areasOf(String country) {
        List<Area> out = new ArrayList<>();
        for (Area a : areas.values()) {
            if (a.country().equals(country)) {
                out.add(a);
            }
        }
        return out;
    }

    public List<LanguageRate> getRates() {
        return Collections.unmodifiableList(rates);
    }

    /** @return the national (school) language of a country: the first language listing it, else English */
    public String nationalLanguage(String country) {
        for (Language l : languages.values()) {
            if (l.countries().contains(country)) {
                return l.id();
            }
        }
        return ENGLISH;
    }

    /**
     * @param field the rate wanted, e.g. {@code LanguageRate::getSignsMen}
     * @return the value of the most specific row (area, then country, then default) covering the year, or null
     */
    public <T> T rate(Function<LanguageRate, T> field, String area, String country, int birthYear) {
        if (rates.isEmpty()) {
            return null;
        }
        int year = Math.max(minYear, Math.min(maxYear, birthYear));
        T byCountry = null;
        T byDefault = null;
        for (LanguageRate r : rates) {
            T v = r.covers(year) ? field.apply(r) : null;
            if (v == null) {
                continue;
            }
            if (area != null && area.equals(r.getArea())) {
                return v;
            }
            if (byCountry == null && r.getCountries() != null && r.getCountries().contains(country)) {
                byCountry = v;
            }
            if (byDefault == null && r.isDefault()) {
                byDefault = v;
            }
        }
        return byCountry != null ? byCountry : byDefault;
    }

    /**
     * @return the share of the people of {@code country} (weighted over its areas) speaking {@code language}, for
     *         a man born in {@code birthYear}: the chance a parent born there passes some of it on. A country without
     *         areas: 1 for its national language, else 0.
     */
    public double speakersIn(String country, String language, int birthYear) {
        List<Area> list = areasOf(country);
        if (list.isEmpty()) {
            return language.equals(nationalLanguage(country)) ? 1 : 0;
        }
        double total = 0;
        for (Area a : list) {
            Map<String, Double> speaks = rate(LanguageRate::getSpeaks, a.id(), country, birthYear);
            total += a.weight() * (speaks == null ? 0 : speaks.getOrDefault(language, 0.0));
        }
        return total;
    }
}
