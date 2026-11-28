// Adapted from DarkBlade (RuntimeAtlasTest), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx.atlas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;
import com.rndmodgames.futtoboru.ui.gfx.NoGl;

/** The border copied around a piece, pages and their white piece, and the art guard (GL-free, {@link NoGl}). */
class RuntimeAtlasTest {

    @BeforeEach
    void gl() {
        NoGl.install();
    }

    @AfterEach
    void noGl() {
        ArtGuard.forbid(false);
        NoGl.uninstall();
    }

    @Test
    void theBorderRepeatsTheNearestEdgePixel() {
        Pixmap source = new Pixmap(8, 8, Format.RGBA8888);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                source.drawPixel(x, y, (x * 16 + y) << 8 | 0xFF);
            }
        }
        Pixmap padded = RuntimeAtlas.padded(source, 2, 3, 3, 2, 2);
        try {
            assertEquals(7, padded.getWidth());
            assertEquals(6, padded.getHeight());
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 3; x++) {
                    assertEquals(source.getPixel(2 + x, 3 + y), padded.getPixel(2 + x, 2 + y));
                }
            }
            // corners and edges copy the nearest pixel of the rectangle, never its neighbours in the source
            assertEquals(source.getPixel(2, 3), padded.getPixel(0, 0));
            assertEquals(source.getPixel(4, 4), padded.getPixel(6, 5));
            assertEquals(source.getPixel(3, 3), padded.getPixel(3, 0));
            assertEquals(source.getPixel(2, 4), padded.getPixel(0, 3));
        } finally {
            padded.dispose();
            source.dispose();
        }
    }

    @Test
    void piecesShareAPageWithItsWhitePieceAndAFullPageOpensAnother() {
        RuntimeAtlas atlas = new RuntimeAtlas(TextureFilter.Nearest, 64, 64);
        Pixmap piece = new Pixmap(20, 20, Format.RGBA8888);
        try {
            TextureRegion white = atlas.white();
            TextureRegion a = atlas.add(piece);
            assertSame(white.getTexture(), a.getTexture(), "the white piece lives on the piece's page");
            assertEquals(RuntimeAtlas.WHITE, white.getRegionWidth());
            assertEquals(20, a.getRegionWidth());
            for (int i = 0; i < 6; i++) {
                assertNotNull(atlas.add(piece));
            }
            assertEquals(2, atlas.pages().size(), "a full page opened another");
            assertNotNull(atlas.white(atlas.pages().get(1)));
            assertNull(atlas.add(new Pixmap(80, 10, Format.RGBA8888)), "bigger than a page: the caller keeps it");
            assertEquals(7, atlas.pieces());
        } finally {
            piece.dispose();
            atlas.dispose();
        }
    }

    @Test
    void artIsRefusedInsideASimulationTickAndWhenForbidden() {
        RuntimeAtlas atlas = new RuntimeAtlas(TextureFilter.Nearest, 64, 64);
        Pixmap piece = new Pixmap(4, 4, Format.RGBA8888);
        try {
            ArtGuard.enterSimulation();
            try {
                assertThrows(IllegalStateException.class, () -> atlas.add(piece));
            } finally {
                ArtGuard.exitSimulation();
            }
            ArtGuard.forbid(true);
            assertThrows(IllegalStateException.class, atlas::white);
        } finally {
            piece.dispose();
            atlas.dispose();
        }
    }
}
