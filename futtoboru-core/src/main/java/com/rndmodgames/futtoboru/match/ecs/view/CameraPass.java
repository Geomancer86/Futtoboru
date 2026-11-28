package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Matrix4;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.IdentityComponent;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;

/**
 * FRAME band, after interpolation: the side-on camera follows the interpolated ball ({@link ViewCamera#follow}, real
 * frame time, so the path is the same at any frame rate) and, when there is a batch, sets its projection to the art
 * pixel view (every later pass draws in art pixels, origin bottom left). The first frame snaps to the ball. Writes
 * only view state.
 */
public final class CameraPass extends EntitySystem implements DrawPass {

    private final ViewFrame view;
    private final InterpolationPass interpolation;
    private final ComponentMapper<IdentityComponent> ids = ComponentMapper.getFor(IdentityComponent.class);
    private final Matrix4 projection = new Matrix4();
    private ViewTransform ball;
    private boolean placed;

    public CameraPass(ViewFrame view, InterpolationPass interpolation) {
        super(SystemPriority.DRAW_CAMERA);
        this.view = view;
        this.interpolation = interpolation;
    }

    @Override
    public void draw(FrameContext frame) {
        ViewCamera camera = view.viewCamera();
        if (ball == null) {
            ball = findBall();
        }
        if (ball != null) {
            if (!placed) {
                camera.snapTo(ball.x);
                placed = true;
            } else {
                camera.follow(ball.x, frame.frameSeconds());
            }
        }
        if (view.draws()) {
            projection.setToOrtho2D(0, 0, (float) camera.orthoWidth(), (float) camera.orthoHeight());
            view.batch().setProjectionMatrix(projection);
        }
    }

    private ViewTransform findBall() {
        for (int i = 0; i < interpolation.size(); i++) {
            ViewTransform t = interpolation.get(i);
            IdentityComponent id = ids.get(t.entity);
            if (id != null && id.role == SpawnRole.BALL) {
                return t;
            }
        }
        return null;
    }

    /** The ball's view transform once found (null before the first capture). */
    public ViewTransform ball() {
        return ball;
    }
}
