package com.rndmodgames.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.rndmodgames.PreferencesManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.BuildInfo;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.ui.UiScale;

/**
 * RndModGames Desktop Launcher v1
 * 
 * @author Geomancer86
 */
public class DesktopLauncher {
    
    public static void main(String[] args) {
        
        // Set up global exception handler to prevent window from closing on uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println("========================================");
                System.err.println("UNCAUGHT EXCEPTION IN THREAD: " + t.getName());
                System.err.println("========================================");
                System.err.println("Exception: " + e.getClass().getName());
                System.err.println("Message: " + e.getMessage());
                System.err.println("========================================");
                e.printStackTrace();
                System.err.println("========================================");
                System.err.println("Window will remain open for debugging.");
                System.err.println("Check the console output above for details.");
                System.err.println("========================================");
            }
        });
        
        try {
            createApplication();
            if (SmokeRun.enabled()) {
                // T-01: the window closed; end the JVM (engine threads included) with the smoke result
                System.exit(SmokeRun.passed() ? 0 : SmokeRun.EXIT_FAILED);
            }
        } catch (Exception e) {
            if (SmokeRun.enabled()) {
                e.printStackTrace();
                System.exit(SmokeRun.exitCodeFor(e));
            }
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR DURING APPLICATION STARTUP");
            System.err.println("========================================");
            System.err.println("Exception: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("========================================");
            e.printStackTrace();
            System.err.println("========================================");
            System.err.println("Press Enter to exit...");
            try {
                System.in.read();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    private static Lwjgl3Application createApplication() {
        
        // getDefaultConfiguration() starts GLFW: no display fails here, before the game exists
        Lwjgl3ApplicationConfiguration configuration = getDefaultConfiguration();
        return new Lwjgl3Application(SmokeRun.enabled() ? SmokeRun.game() : new Futtoboru(), configuration);
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        
        // Initialize build info
        BuildInfo.initialize();
        
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        
        // Set window title with build information
        configuration.setTitle(BuildInfo.getBuildInfoString());
        configuration.useVsync(true);
        
        // Limits FPS to the refresh rate of the currently active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        
        // If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        // useful for testing performance, but can also be very stressful to some hardware.
        // You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        // Auto UI scale follows the monitor's OS scale (Windows 200% -> 2x); GLFW is initialized by getDisplayMode()
        UiScale.setSystemScale(readMonitorContentScale());
        Log.info(Log.SYSTEM_INIT, "DesktopLauncher", "monitor content scale " + UiScale.getSystemScale());

        // U-03a / D18: one source for the window size, the saved preference (default and minimum 1280x720), so
        // Futtoboru.create() does not resize the window a second time
        Lwjgl3Preferences saved = new Lwjgl3Preferences(PreferencesManager.PREFERENCES_NAME, ".prefs/");
        int[] size = UiScale.windowSize(saved.getString(PreferencesManager.SETTINGS_RESOLUTION_PREF, null));
        configuration.setWindowedMode(size[0], size[1]);
        configuration.setWindowSizeLimits(UiScale.MIN_WIDTH, UiScale.MIN_HEIGHT, -1, -1);
        if (PreferencesManager.ENABLED.equals(saved.getString(PreferencesManager.SETTINGS_FULLSCREEN_PREF, null))) {
            configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        }

        // TODO: set Futtoboru icon
//        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        
        //
        return configuration;
    }

    /** the primary monitor's content scale (1 = 100%), 1 when it cannot be read */
    private static float readMonitorContentScale() {
        try {
            long monitor = org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor();
            if (monitor == 0L) {
                return 1f;
            }
            float[] x = new float[1];
            float[] y = new float[1];
            org.lwjgl.glfw.GLFW.glfwGetMonitorContentScale(monitor, x, y);
            float scale = Math.max(x[0], y[0]);
            return scale > 0f ? scale : 1f;
        } catch (Throwable unavailable) {
            return 1f;
        }
    }
}