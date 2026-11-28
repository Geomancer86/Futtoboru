// Adapted from DarkBlade (DepthSortTest), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** The depth order is exactly the stable sort by depth of the entries in the order of their ties. */
class DepthSortTest {

    /** The reference: the entries in tie order, then a stable sort by depth. */
    private static List<Integer> reference(float[] depths, int[] ties) {
        List<Integer> entries = new ArrayList<>();
        for (int i = 0; i < depths.length; i++) {
            entries.add(i);
        }
        entries.sort((a, b) -> Integer.compare(ties[a], ties[b]));
        entries.sort((a, b) -> Float.compare(depths[a], depths[b]));
        return entries;
    }

    private static List<Integer> sorted(DepthSort sort, float[] depths, int[] ties) {
        sort.clear();
        for (int i = 0; i < depths.length; i++) {
            assertEquals(i, sort.add(depths[i], ties[i]));
        }
        sort.sort();
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < sort.size(); i++) {
            order.add(sort.order(i));
        }
        return order;
    }

    @Test
    void ordersLikeAStableSort() {
        Random random = new Random(74);
        DepthSort sort = new DepthSort();
        for (int round = 0; round < 200; round++) {
            int n = random.nextInt(round < 100 ? 40 : 3000);
            float[] depths = new float[n];
            int[] ties = new int[n];
            List<Integer> seqs = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                seqs.add(i);
            }
            Collections.shuffle(seqs, random);
            for (int i = 0; i < n; i++) {
                // actors across a pitch: many on the same line (ties), some anywhere
                boolean onLine = random.nextBoolean();
                depths[i] = onLine ? -random.nextInt(64) : -random.nextFloat() * 64;
                ties[i] = seqs.get(i);
            }
            assertEquals(reference(depths, ties), sorted(sort, depths, ties), "round " + round);
        }
    }

    @Test
    void allTheSameDepthKeepsTheTies() {
        DepthSort sort = new DepthSort();
        float[] depths = new float[500];
        int[] ties = new int[500];
        for (int i = 0; i < 500; i++) {
            depths[i] = -3.5f;
            ties[i] = 1000 - i * 2;
        }
        assertEquals(reference(depths, ties), sorted(sort, depths, ties));
    }

    @Test
    void farDepthsDoNotBreakTheBuckets() {
        DepthSort sort = new DepthSort();
        float[] depths = { 1e30f, -1e30f, 0f, 3f, -2f, Float.MAX_VALUE, -Float.MAX_VALUE, 0f };
        int[] ties = { 0, 1, 2, 3, 4, 5, 6, 7 };
        assertEquals(reference(depths, ties), sorted(sort, depths, ties));
    }

    @Test
    void anEmptyFrame() {
        DepthSort sort = new DepthSort();
        sort.sort();
        assertEquals(0, sort.size());
    }
}
