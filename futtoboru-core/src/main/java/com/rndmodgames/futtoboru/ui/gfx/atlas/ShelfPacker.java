// Adapted from DarkBlade (ShelfPacker), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx.atlas;

/**
 * Where a rectangle goes in an atlas page: shelves (rows) filled left to right, a new shelf under the last one when a
 * rectangle does not fit beside the others. Pure arithmetic, no libGDX.
 *
 * <ul>
 * <li>Good for what the viewer atlas holds: pieces of a few heights that arrive one by one and never leave, so there
 * is nothing to free or repack.</li>
 * <li>A rectangle bigger than the page, or one that no longer fits, gets null: the atlas opens another page.</li>
 * </ul>
 */
public final class ShelfPacker {

    private final int width;
    private final int height;

    // the shelf being filled: its top, its height so far and where the next rectangle goes
    private int shelfY;
    private int shelfHeight;
    private int cursorX;

    private long usedArea;
    private int placed;

    public ShelfPacker(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("a page needs a size, got " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
    }

    /** The top left corner of a rectangle of that size, or null when it does not fit in this page any more. */
    public int[] place(int w, int h) {
        if (w <= 0 || h <= 0 || w > width || h > height) {
            return null;
        }
        // beside the others on the current shelf; the shelf grows to the tallest it holds while it fits the page
        if (cursorX + w <= width && shelfY + Math.max(shelfHeight, h) <= height) {
            return take(cursorX, shelfY, w, h);
        }
        // a new shelf under the current one
        int nextY = shelfY + shelfHeight;
        if (nextY + h > height) {
            return null;
        }
        shelfY = nextY;
        shelfHeight = 0;
        cursorX = 0;
        return take(0, shelfY, w, h);
    }

    private int[] take(int x, int y, int w, int h) {
        cursorX = x + w;
        shelfHeight = Math.max(shelfHeight, h);
        usedArea += (long) w * h;
        placed++;
        return new int[] { x, y };
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int placed() {
        return placed;
    }

    /** Share of the page covered by rectangles, 0 to 1. */
    public double used() {
        return usedArea / (double) ((long) width * height);
    }
}
