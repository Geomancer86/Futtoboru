package com.rndmodgames.components;

import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisTable;

/**
 * Social Network Links Component v1
 * 
 *  - A Container including many Icons/Links to Our Social Sites
 * 
 * @author Geomancer86
 */
public class SocialNetworkLinksComponent extends VisTable {

    /**
     * NOTE: avoid clicking during development to mess with Analytics
     */
    public static final String PATREON_URL = "https://www.patreon.com/nalvargonzalez";
    public static final String TWITTER_URL = "https://twitter.com/rndmodgames";
    public static final String FACEBOOK_URL = "https://www.facebook.com/RndModGames";
    public static final String INSTAGRAM_URL = "https://www.instagram.com/rndmodgames";
    public static final String RNDMODGAMES_URL = "https://rndmodgames.com";
    public static final String FUTTOBORU_GITHUB_URL = "https://github.com/Geomancer86/Futtoboru";
    
    public SocialNetworkLinksComponent() {

        row();
        add(new LinkLabel("Nicolas Alvargonzalez @ Patreon", PATREON_URL)).right();
        
        row();
        add(new LinkLabel("RndModGames @ Twitter", TWITTER_URL)).right();
        
        row();
        add(new LinkLabel("RndModGames @ Facebook", FACEBOOK_URL)).right();
        
        row();
        add(new LinkLabel("RndModGames @ Instagram", INSTAGRAM_URL)).right();
        
        row();
        add(new LinkLabel("RndModGames.com", RNDMODGAMES_URL)).right();
        
        row();
        add(new LinkLabel("Futtoboru @ Github", FUTTOBORU_GITHUB_URL)).right();
    }
}