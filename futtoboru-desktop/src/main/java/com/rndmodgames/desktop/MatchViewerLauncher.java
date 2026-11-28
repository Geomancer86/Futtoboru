package com.rndmodgames.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.rndmodgames.futtoboru.match.ecs.view.ViewerDebugApp;

/**
 * Debug launcher for the ECS match viewer (E-21b): the live match world in the side-on camera, without the game's
 * menus. Run from the desktop fat jar:
 *
 * <pre>
 * java -cp futtoboru-desktop/target/futtoboru-desktop-*-jar-with-dependencies.jar com.rndmodgames.desktop.MatchViewerLauncher
 * </pre>
 *
 * Optional: {@code -Dfuttoboru.viewer.shot=shot.png -Dfuttoboru.viewer.shotAfter=6 -Dfuttoboru.viewer.seconds=25}
 * (see {@link ViewerDebugApp}).
 */
public final class MatchViewerLauncher {

    private MatchViewerLauncher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Futtoboru match viewer (debug)");
        config.setWindowedMode(Integer.getInteger("futtoboru.viewer.width", 1280),
                Integer.getInteger("futtoboru.viewer.height", 720));
        config.useVsync(true);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new ViewerDebugApp(), config);
    }
}
