package com.rndmodgames.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.rndmodgames.futtoboru.game.Futtoboru;

public class DesktopLauncher {
    
    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        
        return new Lwjgl3Application(new Futtoboru(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        
        // TODO: add release - build version - etc
        configuration.setTitle("Futtoboru v0.1.0-alpha");
        configuration.useVsync(true);
        
        // Limits FPS to the refresh rate of the currently active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        
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
}
