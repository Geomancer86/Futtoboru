// Adapted from DarkBlade (NoGl), ported under D23/D24.
package com.rndmodgames.futtoboru.ui.gfx;

import java.lang.reflect.Proxy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGraphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;

/**
 * An OpenGL that does nothing, for tests: {@code Texture}, its uploads and its sub-images run their Java code without
 * a GPU (every GL call answers 0, false, "" or nothing); a test checks the bookkeeping, not pixels on a card. Loads
 * the libGDX natives (for {@code Pixmap}) and installs headless mock graphics when none exist.
 *
 * Not enough for {@code SpriteBatch} or {@code ShaderProgram}: a shader never "compiles" here. Passes create batches
 * lazily or get one from the {@code ViewFrame} (null in headless tests).
 *
 * <pre>
 * NoGl.install();
 * try { ... } finally { NoGl.uninstall(); }
 * </pre>
 */
public final class NoGl {

    private static GL20 previous;
    private static GL20 previous20;
    private static Graphics previousGraphics;

    private NoGl() {
    }

    public static void install() {
        GdxNativesLoader.load();
        previous = Gdx.gl;
        previous20 = Gdx.gl20;
        previousGraphics = Gdx.graphics;
        // Texture asks the graphics about extensions while loading: the headless mock answers no to everything.
        if (Gdx.graphics == null) {
            Gdx.graphics = new MockGraphics();
        }
        GL20 none = (GL20) Proxy.newProxyInstance(GL20.class.getClassLoader(), new Class<?>[] { GL20.class },
                (proxy, method, args) -> {
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) {
                        return false;
                    }
                    if (type == int.class) {
                        return 0;
                    }
                    if (type == float.class) {
                        return 0f;
                    }
                    if (type == String.class) {
                        return "";
                    }
                    return null;
                });
        Gdx.gl = none;
        Gdx.gl20 = none;
    }

    public static void uninstall() {
        Gdx.gl = previous;
        Gdx.gl20 = previous20;
        Gdx.graphics = previousGraphics;
    }
}
