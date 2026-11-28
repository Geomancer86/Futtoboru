// Adapted from DarkBlade (ShelfPackerTest), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx.atlas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** The shelves of an atlas page place every piece without overlaps, inside the page. */
class ShelfPackerTest {

    @Test
    void fillsAShelfLeftToRightThenOpensAnother() {
        ShelfPacker packer = new ShelfPacker(100, 100);
        assertArrayEquals(new int[] { 0, 0 }, packer.place(40, 20));
        assertArrayEquals(new int[] { 40, 0 }, packer.place(40, 30));
        // 30 more do not fit beside them: a new shelf under the tallest of the first (30)
        assertArrayEquals(new int[] { 0, 30 }, packer.place(30, 10));
        assertEquals(3, packer.placed());
    }

    @Test
    void whatDoesNotFitIsNull() {
        ShelfPacker packer = new ShelfPacker(64, 64);
        assertNull(packer.place(65, 10), "wider than the page");
        assertNull(packer.place(10, 65), "taller than the page");
        assertNull(packer.place(0, 10));
        assertArrayEquals(new int[] { 0, 0 }, packer.place(64, 60));
        assertNull(packer.place(10, 10), "the page is full");
    }

    @Test
    void manyPiecesNeverOverlapAndStayInside() {
        ShelfPacker packer = new ShelfPacker(2048, 2048);
        List<int[]> placed = new ArrayList<>();
        Random random = new Random(73);
        for (int i = 0; i < 2000; i++) {
            int w = 4 + random.nextInt(70);
            int h = 4 + random.nextInt(70);
            int[] at = packer.place(w, h);
            if (at == null) {
                break;
            }
            assertTrue(at[0] >= 0 && at[1] >= 0 && at[0] + w <= 2048 && at[1] + h <= 2048);
            placed.add(new int[] { at[0], at[1], w, h });
        }
        for (int i = 0; i < placed.size(); i++) {
            for (int j = i + 1; j < placed.size(); j++) {
                int[] a = placed.get(i);
                int[] b = placed.get(j);
                boolean apart = a[0] + a[2] <= b[0] || b[0] + b[2] <= a[0] || a[1] + a[3] <= b[1]
                        || b[1] + b[3] <= a[1];
                assertTrue(apart, "pieces " + i + " and " + j + " overlap");
            }
        }
        assertTrue(packer.used() > 0.5, "the shelves use more than half the page: " + packer.used());
    }

    @Test
    void aPageNeedsASize() {
        assertThrows(IllegalArgumentException.class, () -> new ShelfPacker(0, 10));
    }
}
