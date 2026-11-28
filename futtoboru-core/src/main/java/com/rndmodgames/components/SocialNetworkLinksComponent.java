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

    /** Link texts: account and site names (proper names, the same in every language, so not in the bundles). */
    static final String PATREON_LINK = "Nicolas Alvargonzalez @ Patreon";
    static final String TWITTER_LINK = "RndModGames @ Twitter";
    static final String FACEBOOK_LINK = "RndModGames @ Facebook";
    static final String INSTAGRAM_LINK = "RndModGames @ Instagram";
    static final String RNDMODGAMES_LINK = "RndModGames.com";
    static final String FUTTOBORU_GITHUB_LINK = "Futtoboru @ Github";
    
    public SocialNetworkLinksComponent() {

        row();
        add(new LinkLabel(PATREON_LINK, PATREON_URL)).right();
        
        row();
        add(new LinkLabel(TWITTER_LINK, TWITTER_URL)).right();
        
        row();
        add(new LinkLabel(FACEBOOK_LINK, FACEBOOK_URL)).right();
        
        row();
        add(new LinkLabel(INSTAGRAM_LINK, INSTAGRAM_URL)).right();
        
        row();
        add(new LinkLabel(RNDMODGAMES_LINK, RNDMODGAMES_URL)).right();
        
        row();
        add(new LinkLabel(FUTTOBORU_GITHUB_LINK, FUTTOBORU_GITHUB_URL)).right();
    }
}