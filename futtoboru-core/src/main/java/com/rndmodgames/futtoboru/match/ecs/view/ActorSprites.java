package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.ui.gfx.atlas.RuntimeAtlas;

/**
 * Where the draw passes get their pictures: players, keepers and officials ({@link #actor(Pose)}), the ball
 * ({@link #ball(Pose)}) and named pieces of the crowd, touchline and people sheets ({@link #sprite(String, String, int)}).
 * {@link IndexActorSprites} serves the baked sheets of D26 ({@code resources/art/match/index.json}); {@link #NONE}
 * answers null for everything and the passes draw placeholders.
 *
 * <p>Directions follow the index (README): 0 = S (faces the camera), 1 = SE, 2 = E (screen right), 3 = NE, 4 = N
 * (away), 5 = NW, 6 = W, 7 = SW.
 */
public interface ActorSprites {

    /** No sprites: the passes draw placeholders. */
    ActorSprites NONE = new ActorSprites() {
        @Override
        public void load(RuntimeAtlas atlas) {
        }

        @Override
        public Frame actor(Pose pose) {
            return null;
        }

        @Override
        public Frame ball(Pose pose) {
            return null;
        }

        @Override
        public Sprite sprite(String kind, String name, int scale) {
            return null;
        }
    };

    int DIRECTIONS = 8;

    /** Packs every frame into the viewer's atlas (render thread, once, before the first draw; never in a tick). */
    void load(RuntimeAtlas atlas);

    /** Once per frame before any lookup: view clock, frame time, pitch wetness. */
    default void begin(ViewFrame frame) {
    }

    /** The picture of a player, keeper or official this frame, or null for the placeholder. */
    Frame actor(Pose pose);

    /** The picture of the ball this frame, or null for the code-drawn ball. */
    Frame ball(Pose pose);

    /**
     * A named piece of a non-kit sheet, or null.
     *
     * @param kind  sheet kind ({@code crowd}, {@code touchline}, {@code people}, {@code officials}, {@code ball})
     * @param name  region name ({@code prop/rope/0}, {@code trainer/idle/S/1} ...)
     * @param scale sheet scale (1 or 2); a missing 2x falls back to 1x
     */
    Sprite sprite(String kind, String name, int scale);

    /** One region with its feet pivot: {@code pivotX} from the left, {@code footY} from the bottom, in texels. */
    final class Sprite {
        public final TextureRegion region;
        public final int width;
        public final int height;
        public final int pivotX;
        public final int footY;
        /** Sheet scale (1 or 2). */
        public final int scale;

        public Sprite(TextureRegion region, int width, int height, int pivotX, int footY, int scale) {
            this.region = region;
            this.width = width;
            this.height = height;
            this.pivotX = pivotX;
            this.footY = footY;
            this.scale = scale;
        }
    }

    /**
     * What a pass knows about one drawn actor this frame (filled by the pass, reused: no allocation). Sim values are
     * copies; a sprite source never sees a component.
     */
    final class Pose {
        /** Stable per-entity key (creation index) for view-side animation state. */
        public int key;
        public SpawnRole role;
        /** Slot (0 = keeper) for players; order among officials (0 = referee, 1 and 2 umpires). */
        public int index;
        /** Interpolated position, metres. */
        public double x, y, z;
        /** Velocity, m/s (ground plane; the ball's {@code vz} too). */
        public double vx, vy, vz;
        /** Facing, rad (0 = +x, counter-clockwise), when {@link #hasFacing}; otherwise from motion or the ball. */
        public double facing;
        public boolean hasFacing;
        /** On the ground after a knock-down. */
        public boolean down;
        /** Injury state (0 none, 1 knock, 2 passenger, 3 off: {@code InjuryComponent}). */
        public int injury;
        /** Putting his shoulder into a charge. */
        public boolean charge;
        /** Ball: spin about the vertical, rad/s; water share 0..1. */
        public double spin, saturation;
        /** Where the ball is (idle actors face it). */
        public double ballX, ballY;
        /** Perspective scale at the actor's depth, art px/m, and the screen magnification. */
        public double pixelsPerMetre;
        public int magnification = 1;
        /** The view's halfway scale ({@link ViewCamera#pixelsPerMetre()}): default 10 px/m or the wide 5 px/m. */
        public double viewPixelsPerMetre = ViewCamera.PIXELS_PER_METRE;

        public double speed() {
            return Math.sqrt(vx * vx + vy * vy);
        }

        public Pose clear() {
            key = 0;
            role = null;
            index = 0;
            x = y = z = vx = vy = vz = facing = spin = saturation = ballX = ballY = 0;
            hasFacing = down = charge = false;
            injury = 0;
            pixelsPerMetre = ViewCamera.PIXELS_PER_METRE;
            magnification = 1;
            viewPixelsPerMetre = ViewCamera.PIXELS_PER_METRE;
            return this;
        }
    }

    /** A chosen picture: the sprite, its texel size in screen pixels and an optional overlay (mud) with its alpha. */
    final class Frame {
        public Sprite sprite;
        public int texel = 1;
        public Sprite overlay;
        public float overlayAlpha;
        /** What was chosen (tests, debug): animation name, direction 0..7, frame. */
        public String animation;
        public int direction;
        public int frame;
    }
}
