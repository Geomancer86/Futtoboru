package com.rndmodgames.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.BuildInfo;

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
        } catch (Exception e) {
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
        
        return new Lwjgl3Application(new Futtoboru(), getDefaultConfiguration());
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

        // Auto UI scale follows the monitor's OS scale (Windows 200% -> 2.0); GLFW is initialized by getDisplayMode()
        Futtoboru.SYSTEM_UI_SCALE = readMonitorContentScale();
        System.out.println("UI: monitor content scale " + Futtoboru.SYSTEM_UI_SCALE);
        
        // If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        // useful for testing performance, but can also be very stressful to some hardware.
        // You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        configuration.setWindowedMode(1920, 1080);
//        configuration.setWindowedMode(320, 240);

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