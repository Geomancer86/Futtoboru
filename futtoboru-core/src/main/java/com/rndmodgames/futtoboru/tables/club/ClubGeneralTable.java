package com.rndmodgames.futtoboru.tables.club;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Club General Table v1
 * 
 * 
 * 
 * 
 * 
 * @author Geomancer86
 */
public class ClubGeneralTable extends VisTable {

    public ClubGeneralTable() {
        
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