package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.ui.gfx.atlas.RuntimeAtlas;

/**
 * Where the actor pass gets the pictures of players, keepers and officials. The baked sprite sheets of D26 (doll
 * players A2 in 8 directions, keeper G2, officials O2) land in {@code resources/art/match/**} with a JSON index; their
 * loader implements this interface. Until then {@link #NONE} answers null for everything and the actor pass draws
 * placeholder figures (coloured blocks in the team's colours).
 *
 * <p>Directions: 0 faces +x (right on screen), counting counter-clockwise in 45 degree steps seen from above: 2 faces
 * away from the camera (+y), 4 faces left, 6 faces the camera.
 */
public interface ActorSprites {

    /** No sprites: the actor pass draws placeholders. */
    ActorSprites NONE = new ActorSprites() {
        @Override
        public void load(RuntimeAtlas atlas) {
        }

        @Override
        public Frame frame(SpawnRole role, int kit, int direction, int step, double pixelsPerMetre) {
            return null;
        }
    };

    int DIRECTIONS = 8;

    /** Packs every frame into the viewer's atlas (render thread, once, before the first draw; never in a tick). */
    void load(RuntimeAtlas atlas);

    /**
     * The picture of one actor this frame, or null to fall back to the placeholder.
     *
     * @param role           player side or official
     * @param kit            index of the actor within its role (squad number order), for per-player looks
     * @param direction      0..7 (see the class comment)
     * @param step           animation step (running frames advance with distance covered)
     * @param pixelsPerMetre art pixels per metre at the actor's depth: a sheet with several sizes picks the nearest
     */
    Frame frame(SpawnRole role, int kit, int direction, int step, double pixelsPerMetre);

    /** One picture and the point between its feet (pixels from its bottom-left corner). */
    final class Frame {
        public final TextureRegion region;
        public final int footX;
        public final int footY;

        public Frame(TextureRegion region, int footX, int footY) {
            this.region = region;
            this.footX = footX;
            this.footY = footY;
        }
    }
}
