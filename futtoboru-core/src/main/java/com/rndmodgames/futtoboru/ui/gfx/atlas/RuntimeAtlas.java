// Adapted from DarkBlade (RuntimeAtlas), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx.atlas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;

/**
 * One texture page (or a few) for everything the match viewer draws, so depth-sorted actors, shadows and code-made
 * pieces never switch textures between draw calls (docs/design/MATCH_VIEWER_PLAN.md §3 row 6).
 *
 * <ul>
 * <li>Pieces are packed by a {@link ShelfPacker}; a full page opens another.</li>
 * <li>A piece goes up on its own ({@code Texture.draw}, a sub-image), with a border of {@link #PADDING} pixels copied
 * from its own edge (nearest, never blended), so the filter never reads a neighbour.</li>
 * <li>{@link #adopt(TextureAtlas)} copies the regions of a loaded {@code TextureAtlas} into the pages and re-points
 * the <b>same</b> {@code AtlasRegion} objects: holders draw from the page without a code change. Adopt an atlas the
 * moment it loads, before anyone calls {@code findRegions} (which returns copies that would keep the old texture).</li>
 * <li>Every page starts with a {@link #WHITE} x {@link #WHITE} white piece ({@link #white(Texture)}): flat shapes
 * (placeholder actors, shadows, lines) tint it and stay on the page.</li>
 * <li>Render thread only. Every entry point calls {@link ArtGuard#check(String)}: simulation code never asks for
 * art.</li>
 * </ul>
 */
public final class RuntimeAtlas implements Disposable {

    public static final int PAGE = 2048;
    public static final int PADDING = 2;
    public static final int WHITE = 4;

    private final TextureFilter filter;
    private final int pageWidth;
    private final int pageHeight;
    private final List<Texture> pages = new ArrayList<>();
    private final Map<TextureAtlas, Boolean> adopted = new IdentityHashMap<>();
    private final Map<Texture, TextureRegion> whites = new IdentityHashMap<>();
    private ShelfPacker packer;

    private int pieces;
    private long pieceBytes;
    private int adoptedRegions;

    /** Pixel-art atlas: {@link #PAGE} square pages, nearest filter. */
    public RuntimeAtlas() {
        this(TextureFilter.Nearest, PAGE, PAGE);
    }

    public RuntimeAtlas(TextureFilter filter, int pageWidth, int pageHeight) {
        if (pageWidth <= WHITE + PADDING * 2 || pageHeight <= WHITE + PADDING * 2) {
            throw new IllegalArgumentException("page too small: " + pageWidth + "x" + pageHeight);
        }
        this.filter = filter;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
    }

    /**
     * The piece in a page, as a region of its own size; null when it is bigger than a page (the caller keeps its own
     * texture). The atlas does not dispose the pixmap.
     */
    public TextureRegion add(Pixmap piece) {
        return add(piece, 0, 0, piece.getWidth(), piece.getHeight());
    }

    /** The rectangle (x, y, w, h) of the source in a page; null when it is bigger than a page. */
    public TextureRegion add(Pixmap source, int x, int y, int w, int h) {
        ArtGuard.check("runtime atlas piece");
        int[] at = place(w + PADDING * 2, h + PADDING * 2);
        if (at == null) {
            return null;
        }
        Pixmap padded = padded(source, x, y, w, h, PADDING);
        try {
            current().draw(padded, at[0], at[1]);
        } finally {
            padded.dispose();
        }
        pieces++;
        pieceBytes += (long) w * h * 4;
        return new TextureRegion(current(), at[0] + PADDING, at[1] + PADDING, w, h);
    }

    /**
     * Copies every region of the atlas into the pages and points its {@code AtlasRegion} objects there, once per atlas;
     * size, name, index and offsets of each region do not change.
     *
     * @return the regions moved (0 when the atlas was adopted already)
     */
    public int adopt(TextureAtlas atlas) {
        ArtGuard.check("runtime atlas adopt");
        if (atlas == null || adopted.containsKey(atlas)) {
            return 0;
        }
        adopted.put(atlas, Boolean.TRUE);
        Map<Texture, Pixmap> sources = new IdentityHashMap<>();
        int moved = 0;
        try {
            for (AtlasRegion region : atlas.getRegions()) {
                if (region.rotate) {
                    continue; // a rotated region keeps its page
                }
                Pixmap source = sources.computeIfAbsent(region.getTexture(), RuntimeAtlas::pixels);
                if (source == null) {
                    continue;
                }
                boolean flipX = region.isFlipX();
                boolean flipY = region.isFlipY();
                TextureRegion copy = add(source, region.getRegionX(), region.getRegionY(), region.getRegionWidth(),
                        region.getRegionHeight());
                if (copy == null) {
                    continue;
                }
                region.setRegion(copy);
                region.flip(flipX, flipY);
                moved++;
            }
        } finally {
            for (Pixmap pixmap : sources.values()) {
                if (pixmap != null) {
                    pixmap.dispose();
                }
            }
        }
        adoptedRegions += moved;
        return moved;
    }

    /** The pixels of a texture loaded from a file, read again from its data; null when they cannot be read. */
    private static Pixmap pixels(Texture texture) {
        TextureData data = texture.getTextureData();
        if (data.getType() != TextureData.TextureDataType.Pixmap) {
            return null;
        }
        if (!data.isPrepared()) {
            data.prepare();
        }
        Pixmap pixmap = data.consumePixmap();
        // a pixmap the data keeps (disposePixmap false) is copied, so disposing ours never breaks the texture
        if (!data.disposePixmap()) {
            Pixmap copy = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
            copy.setBlending(Pixmap.Blending.None);
            copy.drawPixmap(pixmap, 0, 0);
            return copy;
        }
        return pixmap;
    }

    /** The rectangle of the source with a border of {@code pad} pixels, each copied from the nearest edge pixel. */
    public static Pixmap padded(Pixmap source, int x, int y, int w, int h, int pad) {
        Pixmap padded = new Pixmap(w + pad * 2, h + pad * 2, Format.RGBA8888);
        padded.setBlending(Pixmap.Blending.None);
        // a stretched copy would blend neighbours (bilinear by default): the border repeats the edge pixel exactly
        padded.setFilter(Pixmap.Filter.NearestNeighbour);
        padded.drawPixmap(source, x, y, w, h, pad, pad, w, h);
        if (pad > 0) {
            padded.drawPixmap(source, x, y, w, 1, pad, 0, w, pad);
            padded.drawPixmap(source, x, y + h - 1, w, 1, pad, pad + h, w, pad);
            padded.drawPixmap(source, x, y, 1, h, 0, pad, pad, h);
            padded.drawPixmap(source, x + w - 1, y, 1, h, pad + w, pad, pad, h);
            fill(padded, 0, 0, pad, source.getPixel(x, y));
            fill(padded, pad + w, 0, pad, source.getPixel(x + w - 1, y));
            fill(padded, 0, pad + h, pad, source.getPixel(x, y + h - 1));
            fill(padded, pad + w, pad + h, pad, source.getPixel(x + w - 1, y + h - 1));
        }
        return padded;
    }

    private static void fill(Pixmap pixmap, int x, int y, int size, int rgba) {
        for (int py = y; py < y + size; py++) {
            for (int px = x; px < x + size; px++) {
                pixmap.drawPixel(px, py, rgba);
            }
        }
    }

    private int[] place(int w, int h) {
        if (w > pageWidth || h > pageHeight) {
            return null;
        }
        if (packer != null) {
            int[] at = packer.place(w, h);
            if (at != null) {
                return at;
            }
        }
        openPage();
        return packer.place(w, h);
    }

    private void openPage() {
        Pixmap blank = new Pixmap(pageWidth, pageHeight, Format.RGBA8888);
        blank.setBlending(Pixmap.Blending.None);
        blank.setColor(0f, 0f, 0f, 0f);
        blank.fill();
        // every page starts with a white piece (WHITE plus its border, all white) that flat shapes stretch and tint
        ShelfPacker fresh = new ShelfPacker(pageWidth, pageHeight);
        int[] white = fresh.place(WHITE + PADDING * 2, WHITE + PADDING * 2);
        blank.setColor(1f, 1f, 1f, 1f);
        blank.fillRectangle(white[0], white[1], WHITE + PADDING * 2, WHITE + PADDING * 2);
        Texture page = new Texture(blank);
        page.setFilter(filter, filter);
        blank.dispose();
        pages.add(page);
        packer = fresh;
        whites.put(page, new TextureRegion(page, white[0] + PADDING, white[1] + PADDING, WHITE, WHITE));
    }

    /** The white piece of that page, or null when it is not one of this atlas' pages. */
    public TextureRegion white(Texture page) {
        return whites.get(page);
    }

    /** The white piece of the current page (opens the first page when none exists yet). */
    public TextureRegion white() {
        ArtGuard.check("runtime atlas white");
        if (pages.isEmpty()) {
            openPage();
        }
        return whites.get(current());
    }

    private Texture current() {
        return pages.get(pages.size() - 1);
    }

    public List<Texture> pages() {
        return Collections.unmodifiableList(pages);
    }

    public int pieces() {
        return pieces;
    }

    /** What the pieces would weigh as textures of their own, in bytes (RGBA). */
    public long pieceBytes() {
        return pieceBytes;
    }

    /** What the pages weigh, in bytes (RGBA). */
    public long pageBytes() {
        return (long) pages.size() * pageWidth * pageHeight * 4;
    }

    public int adoptedRegions() {
        return adoptedRegions;
    }

    /** Share of the last page in use, 0 to 1. */
    public double lastPageUsed() {
        return packer == null ? 0 : packer.used();
    }

    @Override
    public void dispose() {
        for (Texture page : pages) {
            page.dispose();
        }
        pages.clear();
        whites.clear();
        packer = null;
    }
}
