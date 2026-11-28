// Adapted from DarkBlade (DepthSort), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

import java.util.Arrays;

/**
 * The draw order of a frame's actors, back to front, without a comparator, boxing or allocation.
 *
 * <ul>
 * <li>Each entry has a depth (the smaller is drawn first) and a tie (the smaller first): the order is exactly the
 * stable sort by depth of the entries in the order of their ties. The match viewer uses the ground depth of an actor
 * (for the side-on camera: the far side of the pitch first) and the entity's creation index as the tie.</li>
 * <li>Bucketed by depth (counting sort, at most one bucket per entry and no more than {@link #MAX_BUCKETS}), then
 * insertion sort inside each bucket: O(n) when the depths spread.</li>
 * <li>The arrays grow to the most entries a frame ever had and are reused.</li>
 * </ul>
 */
public final class DepthSort {

    static final int MAX_BUCKETS = 4096;

    private float[] depths = new float[64];
    private int[] ties = new int[64];
    private int[] order = new int[64];
    private int[] sorted = new int[64];
    private int[] buckets = new int[65];
    private int[] bucketOf = new int[64];
    private int size;

    public void clear() {
        size = 0;
    }

    /** Adds an entry; returns its index, the one {@link #order(int)} gives back. */
    public int add(float depth, int tie) {
        if (size == depths.length) {
            grow(size * 2);
        }
        depths[size] = depth;
        ties[size] = tie;
        return size++;
    }

    public int size() {
        return size;
    }

    /** The index of the entry drawn in place {@code i}, after {@link #sort()}. */
    public int order(int i) {
        return order[i];
    }

    public void sort() {
        if (size == 0) {
            return;
        }
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < size; i++) {
            min = Math.min(min, depths[i]);
            max = Math.max(max, depths[i]);
        }
        int count = Math.max(1, Math.min(size, MAX_BUCKETS));
        float range = max - min;
        float scale = range > 0f && !Float.isInfinite(range) ? (count - 1) / range : 0f;
        if (buckets.length < count + 1) {
            buckets = new int[count + 1];
        }
        for (int b = 0; b <= count; b++) {
            buckets[b] = 0;
        }
        for (int i = 0; i < size; i++) {
            int b = scale > 0f ? Math.min(count - 1, Math.max(0, (int) ((depths[i] - min) * scale))) : 0;
            bucketOf[i] = b;
            buckets[b + 1]++;
        }
        for (int b = 0; b < count; b++) {
            buckets[b + 1] += buckets[b];
        }
        // stable placement by bucket, in the order the entries came
        for (int i = 0; i < size; i++) {
            sorted[buckets[bucketOf[i]]++] = i;
        }
        // inside each run of the same bucket, by depth and tie (the runs are short)
        int start = 0;
        while (start < size) {
            int b = bucketOf[sorted[start]];
            int end = start + 1;
            while (end < size && bucketOf[sorted[end]] == b) {
                end++;
            }
            insertion(start, end);
            start = end;
        }
        System.arraycopy(sorted, 0, order, 0, size);
    }

    private void insertion(int from, int to) {
        for (int i = from + 1; i < to; i++) {
            int entry = sorted[i];
            int j = i - 1;
            while (j >= from && after(sorted[j], entry)) {
                sorted[j + 1] = sorted[j];
                j--;
            }
            sorted[j + 1] = entry;
        }
    }

    /** Whether a goes after b. */
    private boolean after(int a, int b) {
        float da = depths[a];
        float db = depths[b];
        if (da > db) {
            return true;
        }
        return !(da < db) && ties[a] > ties[b];
    }

    private void grow(int length) {
        depths = Arrays.copyOf(depths, length);
        ties = Arrays.copyOf(ties, length);
        order = new int[length];
        sorted = new int[length];
        bucketOf = new int[length];
    }
}
