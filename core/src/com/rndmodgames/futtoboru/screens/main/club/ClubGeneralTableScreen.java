package com.rndmodgames.futtoboru.screens.main.club;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Club General Table Screen
 * 
 * 
 * 
 * 
 * 
 * @author Geomancer86
 */
public class ClubGeneralTableScreen extends VisTable {

    public ClubGeneralTableScreen() {
        
        super(true);
    }
    
    public void setClub(Club club) {
        
        clear();
        
        row();
        add(new VisLabel(LanguageModLoader.getValue("club_name")));
        add(new VisLabel(club.getName()));
        
        row();
        add(new VisLabel(LanguageModLoader.getValue("league")));
        add(new VisLabel(club.getCurrentLeague().getName()));
    }
}