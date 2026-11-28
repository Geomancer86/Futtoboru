package com.rndmodgames.futtoboru.match.ecs.events;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Every event of one match (E-17, D19-9), in tick order, kept as primitives: six ints per event in one growing array,
 * so appending from the simulation loop allocates nothing but an occasional array growth. {@link #get(int)} builds a
 * {@link MatchEvent} view on demand (the ticker, the report, tests).
 *
 * The log also keeps what turns ticks into minutes (tick rate, half length) and the ground and weather the match was
 * played on, so the report needs nothing else. {@link #encode()} gives the compact saved form (a short versioned
 * Base64 string of varints, about 10 bytes an event); {@link #decode(String)} reads it back.
 */
public final class MatchEventLog {

    /** Prefix of the saved form; a different prefix is a format this version cannot read. */
    static final String FORMAT = "E1:";

    private static final int STRIDE = 6;
    private static final int TICK = 0, HEAD = 1, ACTOR = 2, OTHER = 3, POS = 4, DATA = 5;

    private final int tickHz;
    private final int halfMinutes;
    /** {@code Surface.ordinal()} of the ground, -1 unknown. */
    private final int surface;
    /** Wetness of the ground 0..1000. */
    private final int wetnessPermille;

    private int[] rows;
    private int size;

    /**
     * @param surface         {@code Surface.ordinal()}, -1 unknown
     * @param wetnessPermille wetness of the ground, 0..1000
     */
    public MatchEventLog(int tickHz, int halfMinutes, int surface, int wetnessPermille) {
        this(tickHz, halfMinutes, surface, wetnessPermille, 256);
    }

    private MatchEventLog(int tickHz, int halfMinutes, int surface, int wetnessPermille, int capacity) {
        if (tickHz <= 0 || halfMinutes < 0) {
            throw new IllegalArgumentException("tickHz " + tickHz + ", half " + halfMinutes);
        }
        this.tickHz = tickHz;
        this.halfMinutes = halfMinutes;
        this.surface = surface;
        this.wetnessPermille = Math.max(0, Math.min(1000, wetnessPermille));
        this.rows = new int[Math.max(1, capacity) * STRIDE];
    }

    // ---- writing (simulation loop) ----------------------------------------------------------------------------------

    /**
     * Appends one event. Ticks must not go backwards.
     *
     * @param side  {@code Team.ordinal()}, -1 none
     * @param actor player ref ({@link MatchEvent#ref}), -1 none
     * @param other player ref, -1 none
     * @param x     metres (kept to the decimetre)
     */
    public void add(long tick, MatchEventType type, int side, int actor, int other, double x, double y, int data) {
        if (size > 0 && tick < rows[(size - 1) * STRIDE + TICK]) {
            throw new IllegalArgumentException("tick " + tick + " before " + rows[(size - 1) * STRIDE + TICK]);
        }
        if ((size + 1) * STRIDE > rows.length) {
            rows = Arrays.copyOf(rows, rows.length * 2);
        }
        int o = size * STRIDE;
        rows[o + TICK] = (int) tick;
        rows[o + HEAD] = type.code() | ((side + 1) << 8);
        rows[o + ACTOR] = actor;
        rows[o + OTHER] = other;
        rows[o + POS] = (dm(x) << 16) | (dm(y) & 0xFFFF);
        rows[o + DATA] = data;
        size++;
    }

    /** Metres to decimetres, kept in a short. */
    private static int dm(double metres) {
        long v = Math.round(metres * 10);
        return (int) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v));
    }

    // ---- reading ----------------------------------------------------------------------------------------------------

    public int size() {
        return size;
    }

    public int tickHz() {
        return tickHz;
    }

    public int halfMinutes() {
        return halfMinutes;
    }

    /** {@code Surface.ordinal()} of the ground, -1 unknown. */
    public int surface() {
        return surface;
    }

    /** Wetness of the ground, 0..1. */
    public double wetness() {
        return wetnessPermille / 1000.0;
    }

    /** The match minute of a tick: 1..half in the first half, half+1..2*half in the second. */
    public int minuteOf(long tick) {
        long perMinute = 60L * tickHz;
        long halfTicks = halfMinutes * perMinute;
        if (halfMinutes == 0) {
            return (int) (tick / perMinute) + 1;
        }
        if (tick < halfTicks) {
            return (int) Math.min(halfMinutes, tick / perMinute + 1);
        }
        return halfMinutes + (int) Math.min(halfMinutes, (tick - halfTicks) / perMinute + 1);
    }

    public MatchEventType type(int i) {
        return MatchEventType.ofCode(row(i, HEAD) & 0xFF);
    }

    public long tick(int i) {
        return row(i, TICK);
    }

    public MatchEvent get(int i) {
        int head = row(i, HEAD);
        int side = (head >> 8) - 1;
        int pos = row(i, POS);
        long tick = row(i, TICK);
        return new MatchEvent(tick, minuteOf(tick), MatchEventType.ofCode(head & 0xFF),
                side < 0 ? null : Team.values()[side], row(i, ACTOR), row(i, OTHER), (pos >> 16) / 10.0,
                ((short) pos) / 10.0, row(i, DATA));
    }

    /** Every event, in order. */
    public List<MatchEvent> events() {
        return filtered(MatchEventLevel.FULL);
    }

    /** The events shown under {@code filter} (D19-9), in order. */
    public List<MatchEvent> filtered(MatchEventLevel filter) {
        List<MatchEvent> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            MatchEvent e = get(i);
            if (e.type() != null && e.shownAt(filter)) {
                out.add(e);
            }
        }
        return out;
    }

    /** The events of one type, in order. */
    public List<MatchEvent> of(MatchEventType type) {
        List<MatchEvent> out = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (type(i) == type) {
                out.add(get(i));
            }
        }
        return out;
    }

    /** Events of one type. */
    public int count(MatchEventType type) {
        int n = 0;
        for (int i = 0; i < size; i++) {
            if (type(i) == type) {
                n++;
            }
        }
        return n;
    }

    private int row(int i, int field) {
        if (i < 0 || i >= size) {
            throw new IndexOutOfBoundsException(i);
        }
        return rows[i * STRIDE + field];
    }

    /** Same header and the same events. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MatchEventLog)) {
            return false;
        }
        MatchEventLog l = (MatchEventLog) o;
        return tickHz == l.tickHz && halfMinutes == l.halfMinutes && surface == l.surface
                && wetnessPermille == l.wetnessPermille && size == l.size
                && Arrays.equals(rows, 0, size * STRIDE, l.rows, 0, size * STRIDE);
    }

    @Override
    public int hashCode() {
        int h = 31 * size + tickHz;
        for (int i = 0; i < size * STRIDE; i++) {
            h = 31 * h + rows[i];
        }
        return h;
    }

    @Override
    public String toString() {
        return "MatchEventLog[" + size + " events]";
    }

    // ---- saved form -------------------------------------------------------------------------------------------------

    /** The compact saved form ({@link #FORMAT} + URL-safe Base64 of varints). */
    public String encode() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(16 + size * 12);
        varint(out, tickHz);
        varint(out, halfMinutes);
        varint(out, surface + 1);
        varint(out, wetnessPermille);
        varint(out, size);
        int prev = 0;
        for (int i = 0; i < size; i++) {
            int o = i * STRIDE;
            varint(out, rows[o + TICK] - prev);
            prev = rows[o + TICK];
            varint(out, rows[o + HEAD]);
            varint(out, rows[o + ACTOR] + 1);
            varint(out, rows[o + OTHER] + 1);
            varint(out, zigzag(rows[o + POS] >> 16));
            varint(out, zigzag((short) rows[o + POS]));
            varint(out, rows[o + DATA]);
        }
        return FORMAT + Base64.getUrlEncoder().withoutPadding().encodeToString(out.toByteArray());
    }

    /**
     * Reads a saved log.
     *
     * @return the log, or null when {@code text} is null, empty, of another format or damaged
     */
    public static MatchEventLog decode(String text) {
        if (text == null || !text.startsWith(FORMAT)) {
            return null;
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(text.substring(FORMAT.length()));
            int[] at = { 0 };
            int hz = readVarint(bytes, at);
            int half = readVarint(bytes, at);
            int surface = readVarint(bytes, at) - 1;
            int wet = readVarint(bytes, at);
            int n = readVarint(bytes, at);
            if (n < 0 || n > bytes.length) {
                return null;
            }
            MatchEventLog log = new MatchEventLog(hz, half, surface, wet, Math.max(1, n));
            int tick = 0;
            for (int i = 0; i < n; i++) {
                int o = i * STRIDE;
                tick += readVarint(bytes, at);
                log.rows[o + TICK] = tick;
                log.rows[o + HEAD] = readVarint(bytes, at);
                log.rows[o + ACTOR] = readVarint(bytes, at) - 1;
                log.rows[o + OTHER] = readVarint(bytes, at) - 1;
                int x = unzigzag(readVarint(bytes, at));
                int y = unzigzag(readVarint(bytes, at));
                log.rows[o + POS] = (x << 16) | (y & 0xFFFF);
                log.rows[o + DATA] = readVarint(bytes, at);
            }
            log.size = n;
            return at[0] == bytes.length ? log : null;
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static int zigzag(int v) {
        return (v << 1) ^ (v >> 31);
    }

    private static int unzigzag(int v) {
        return (v >>> 1) ^ -(v & 1);
    }

    private static void varint(ByteArrayOutputStream out, int v) {
        while ((v & ~0x7F) != 0) {
            out.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        out.write(v);
    }

    private static int readVarint(byte[] b, int[] at) {
        int v = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            int x = b[at[0]++];
            v |= (x & 0x7F) << shift;
            if ((x & 0x80) == 0) {
                return v;
            }
        }
        throw new IllegalArgumentException("varint too long");
    }
}
