package com.rndmodgames.futtoboru.system.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * The era events (H-06, ERA_EVENTS_PROPOSAL.md, D30): the 1888-89 baseline of every rules / ball / kit / league
 * parameter plus the dated events that change them. {@link #at(LocalDate)} resolves all parameters for a date: start
 * from the baseline and apply, in date order, every event whose {@code effective} date has passed. The match engine
 * and the league read only the {@link Resolved} profile, never the events. Pure: no game state, no libGDX
 * application needed. The data is checked strictly by {@code tools/data/validate.py}; this class only refuses what it
 * cannot resolve (unknown parameter, wrong type).
 * <p>
 * Parameters are named {@code group.name} ({@code rules.offsideOpponents}, {@code ball.massMinG}, {@code
 * kit.numbers}, {@code league.maxWagePerWeek}). Numbers are held as doubles, flags as booleans, enums as text.
 * <p>
 * Events dated on or before the baseline date are already part of the baseline and are skipped (they stay in
 * {@link #events()} for history screens). An {@code adoption} (equipment that spreads over several seasons) moves the
 * value linearly from its {@code from} (default: the value before the event) to the result of the effect over N
 * seasons of 365.25 days after the effective date.
 */
public final class EraProfile {

    /** The default location of the files on the classpath. */
    public static final String RESOURCE_ROOT = "gamedata/core/era/";

    public enum Op {
        SET, SCALE, ADD
    }

    /** One change of one parameter. {@code adoptionSeasons == 0}: instant. */
    public record Effect(String param, Op op, Object value, int adoptionSeasons, Double adoptionFrom) {
    }

    /**
     * A dated era event. {@code countries} / {@code competitions} empty: everywhere.
     */
    public record Event(String id, String kind, String authority, LocalDate announced, LocalDate effective,
            List<String> countries, List<String> competitions, List<Effect> effects, String messageKey,
            List<String> requires) {
    }

    /** The parameters on one date, and the ids of the events that produced them. */
    public static final class Resolved {

        private final LocalDate date;
        private final Map<String, Object> values;
        private final List<String> applied;

        Resolved(LocalDate date, Map<String, Object> values, List<String> applied) {
            this.date = date;
            this.values = values;
            this.applied = applied;
        }

        public LocalDate date() {
            return date;
        }

        public boolean has(String param) {
            return values.containsKey(param);
        }

        public double getDouble(String param) {
            Object v = get(param);
            if (v instanceof Double) {
                return (Double) v;
            }
            throw new IllegalArgumentException("`" + param + "` is not a number: " + v);
        }

        public int getInt(String param) {
            return (int) Math.round(getDouble(param));
        }

        public boolean getBoolean(String param) {
            Object v = get(param);
            if (v instanceof Boolean) {
                return (Boolean) v;
            }
            throw new IllegalArgumentException("`" + param + "` is not true/false: " + v);
        }

        public String getString(String param) {
            Object v = get(param);
            if (v instanceof String) {
                return (String) v;
            }
            throw new IllegalArgumentException("`" + param + "` is not text: " + v);
        }

        /** Every parameter, in baseline order (an unmodifiable copy). */
        public Map<String, Object> asMap() {
            return Collections.unmodifiableMap(values);
        }

        /** Ids of the events applied (effective by the date), in the order applied. */
        public List<String> appliedEvents() {
            return Collections.unmodifiableList(applied);
        }

        private Object get(String param) {
            Object v = values.get(param);
            if (v == null) {
                throw new IllegalArgumentException("unknown era parameter `" + param + "`");
            }
            return v;
        }
    }

    private final LocalDate baselineDate;
    private final Map<String, Object> baseline;
    private final List<Event> events;

    private EraProfile(LocalDate baselineDate, Map<String, Object> baseline, List<Event> events) {
        this.baselineDate = baselineDate;
        this.baseline = baseline;
        this.events = events;
    }

    // ------------------------------------------------------------------------------------------------ loading

    /** Reads the shipped core files from the classpath. */
    public static EraProfile loadDefault() {
        String base = readResource(RESOURCE_ROOT + "baseline.json");
        String events = readResource(RESOURCE_ROOT + "events.json");
        if (base == null || events == null) {
            throw new IllegalStateException("era files missing under " + RESOURCE_ROOT);
        }
        return parse(base, List.of(events));
    }

    /**
     * @param baselineJson the text of {@code era/baseline.json}
     * @param eventsJson   the text of every {@code era/events.json} (core first, then mods), each a list of events
     */
    public static EraProfile parse(String baselineJson, List<String> eventsJson) {
        JsonValue b = new JsonReader().parse(baselineJson);
        LocalDate date = LocalDate.parse(b.getString("date"));
        Map<String, Object> values = new LinkedHashMap<>();
        for (JsonValue group = b.get("parameters").child; group != null; group = group.next) {
            for (JsonValue p = group.child; p != null; p = p.next) {
                values.put(group.name + "." + p.name, scalar(p));
            }
        }
        List<Event> events = new ArrayList<>();
        for (String text : eventsJson) {
            for (JsonValue e = new JsonReader().parse(text).child; e != null; e = e.next) {
                events.add(readEvent(e, values));
            }
        }
        events.sort(Comparator.comparing(Event::effective)); // stable: file order within a day
        return new EraProfile(date, values, Collections.unmodifiableList(events));
    }

    private static Event readEvent(JsonValue e, Map<String, Object> baseline) {
        String id = e.getString("id");
        List<Effect> effects = new ArrayList<>();
        JsonValue list = e.get("effects");
        for (JsonValue f = list == null ? null : list.child; f != null; f = f.next) {
            String param = f.getString("param");
            Object base = baseline.get(param);
            if (base == null) {
                throw new IllegalArgumentException("event `" + id + "`: unknown parameter `" + param + "`");
            }
            Op op = Op.valueOf(f.getString("op").toUpperCase());
            Object value = scalar(f.get("value"));
            boolean ok = op == Op.SET ? base.getClass() == value.getClass()
                    : base instanceof Double && value instanceof Double;
            if (!ok) {
                throw new IllegalArgumentException("event `" + id + "`: value of `" + param + "` has the wrong type");
            }
            JsonValue ad = f.get("adoption");
            int seasons = ad == null ? 0 : ad.getInt("seasons");
            Double from = ad == null || !ad.has("from") ? null : ad.getDouble("from");
            if (ad != null && !(base instanceof Double)) {
                throw new IllegalArgumentException("event `" + id + "`: adoption needs a number (`" + param + "`)");
            }
            effects.add(new Effect(param, op, value, seasons, from));
        }
        JsonValue scope = e.get("scope");
        return new Event(id, e.getString("kind"), e.getString("authority"), LocalDate.parse(e.getString("announced")),
                LocalDate.parse(e.getString("effective")), strings(scope == null ? null : scope.get("countries")),
                strings(scope == null ? null : scope.get("competitions")), Collections.unmodifiableList(effects),
                e.get("message").getString("key"), strings(e.get("requires")));
    }

    private static List<String> strings(JsonValue list) {
        List<String> out = new ArrayList<>();
        for (JsonValue s = list == null ? null : list.child; s != null; s = s.next) {
            out.add(s.asString());
        }
        return Collections.unmodifiableList(out);
    }

    private static Object scalar(JsonValue v) {
        if (v.isBoolean()) {
            return v.asBoolean();
        }
        if (v.isNumber()) {
            return v.asDouble();
        }
        if (v.isString()) {
            return v.asString();
        }
        throw new IllegalArgumentException("era values are numbers, flags or text: " + v);
    }

    private static String readResource(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = EraProfile.class.getClassLoader();
        }
        try (InputStream in = cl.getResourceAsStream(path)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    // ------------------------------------------------------------------------------------------------ queries

    /** The date the baseline values hold on (8 September 1888). */
    public LocalDate baselineDate() {
        return baselineDate;
    }

    /** Every event, sorted by effective date (including those before the baseline date). */
    public List<Event> events() {
        return events;
    }

    /** The baseline itself, as the profile on its date (no event applied). */
    public Resolved baseline() {
        return new Resolved(baselineDate, new LinkedHashMap<>(baseline), new ArrayList<>());
    }

    /** The parameters on {@code date}, every event applying everywhere (scopes ignored). */
    public Resolved at(LocalDate date) {
        return at(date, null, null);
    }

    /**
     * The parameters on {@code date} for a country and competition. An event with a scope applies only when its
     * lists contain the given id; a null id means "not filtered by that dimension" (every scoped event applies).
     */
    public Resolved at(LocalDate date, String country, String competition) {
        Map<String, Object> values = new LinkedHashMap<>(baseline);
        List<String> applied = new ArrayList<>();
        for (Event e : events) {
            if (e.effective().isAfter(date)) {
                break; // sorted
            }
            if (!e.effective().isAfter(baselineDate) || !inScope(e, country, competition)) {
                continue; // folded into the baseline, or not for this country / competition
            }
            for (Effect f : e.effects()) {
                apply(values, f, e.effective(), date);
            }
            applied.add(e.id());
        }
        return new Resolved(date, values, applied);
    }

    /** Events announced after {@code after} (exclusive) and up to {@code upTo} (inclusive), in announcement order. */
    public List<Event> announcedBetween(LocalDate after, LocalDate upTo) {
        List<Event> out = new ArrayList<>();
        for (Event e : events) {
            if (e.announced().isAfter(after) && !e.announced().isAfter(upTo)) {
                out.add(e);
            }
        }
        out.sort(Comparator.comparing(Event::announced));
        return out;
    }

    private static boolean inScope(Event e, String country, String competition) {
        return (country == null || e.countries().isEmpty() || e.countries().contains(country))
                && (competition == null || e.competitions().isEmpty() || e.competitions().contains(competition));
    }

    private static void apply(Map<String, Object> values, Effect f, LocalDate effective, LocalDate date) {
        Object before = values.get(f.param());
        Object target;
        switch (f.op()) {
        case SET:
            target = f.value();
            break;
        case SCALE:
            target = (Double) before * (Double) f.value();
            break;
        default:
            target = (Double) before + (Double) f.value();
            break;
        }
        if (f.adoptionSeasons() > 0) {
            double from = f.adoptionFrom() != null ? f.adoptionFrom() : (Double) before;
            double seasons = ChronoUnit.DAYS.between(effective, date) / 365.25 / f.adoptionSeasons();
            double t = Math.max(0, Math.min(1, seasons));
            target = from + ((Double) target - from) * t;
        }
        values.put(f.param(), target);
    }
}
