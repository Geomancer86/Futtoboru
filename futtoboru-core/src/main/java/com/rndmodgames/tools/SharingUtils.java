package com.rndmodgames.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Sharing Utils v2
 * 
 * Backported from DarkBlade Sharing Utils v1
 * 
 * @author Geomancer86
 */
public class SharingUtils {

    // TODO: parametrize and save in personal preferences
    private static final String USER_HOME = "user.home";
    private static final String GAME_FOLDER = "/Documents/RndModGames/Futtuboru";
    private static final String SCREENSHOT_FOLDER = "screenshots";
 
    //
    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
    
    /**
     * Takes a Screenshot and Saves to Screenshot Folder
     */
    public static void screenshot() {
        
        byte [] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);

        /**
         * This loop makes sure the whole screenshot is opaque and looks exactly like what the user is seeing
         */
        for (int i = 4; i < pixels.length; i += 4) {
            pixels[i - 1] = (byte) 255;
        }

        String userHomePath = System.getProperty(USER_HOME);
        String screenshotFullPath = userHomePath + "\\" + GAME_FOLDER + "\\" + SCREENSHOT_FOLDER;
        
        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
        
        Date date = new Date();
        String filename = "\\screenshot_" + DATE_FORMAT.format(date) + ".png";
        
        PixmapIO.writePNG(Gdx.files.absolute(screenshotFullPath + filename), pixmap);
        pixmap.dispose();
    }
}