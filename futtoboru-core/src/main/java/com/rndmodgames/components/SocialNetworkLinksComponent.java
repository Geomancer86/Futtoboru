package com.rndmodgames.components;

import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
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
    
    public SocialNetworkLinksComponent() {
        
//        add(new VisLabel("Follow us on:"));
        
        row();
        add(new LinkLabel("Patreon", PATREON_URL));
        
        row();
        add(new LinkLabel("Twitter", TWITTER_URL));
        
        row();
        add(new LinkLabel("Facebook", FACEBOOK_URL));
        
        row();
        add(new LinkLabel("Instagram", INSTAGRAM_URL));
        
        row();
        add(new LinkLabel("RndModGames", RNDMODGAMES_URL));
    }
}