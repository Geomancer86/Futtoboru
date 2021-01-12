package com.rndmodgames.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.rndmodgames.futtoboru.game.Futtuboru;


public class DesktopLauncher {
    
	public static void main (String[] arg) {
	    
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		new LwjglApplication(new Futtuboru(), config);
	}
}
