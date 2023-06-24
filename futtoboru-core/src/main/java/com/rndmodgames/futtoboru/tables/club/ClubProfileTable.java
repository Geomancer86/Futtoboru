package com.rndmodgames.futtoboru.tables.club;

import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Club Profile Table v1
 * 
 * 
 * @author Geomancer86
 */
public class ClubProfileTable extends VisTable {

    public ClubProfileTable() {
        
        super(true);

    }
    
    public void setClub(Club club) {
        
        clear();
        
        row();
        add(new VisLabel(LanguageModLoader.getValue("country")));
        add(new VisLabel(club.getCountry().getCommonName()));
        
        row();
        add(new VisLabel(LanguageModLoader.getValue("continent")));
        add(new VisLabel(club.getCountry().getContinent().getName()));

    }
}