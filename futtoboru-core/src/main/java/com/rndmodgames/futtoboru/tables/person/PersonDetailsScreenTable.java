package com.rndmodgames.futtoboru.tables.person;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Person Details Screen Table v1
 * 
 * @author Geomancer86
 */
public class PersonDetailsScreenTable extends VisTable {

    //
    Game game;
//    Stage stage;
    
    // Static Components
    VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("name"));
    VisLabel lastNameLabel = new VisLabel(LanguageModLoader.getValue("last_name"));
    VisLabel nationalityLabel = new VisLabel(LanguageModLoader.getValue("nationality"));
    VisLabel ageLabel = new VisLabel(LanguageModLoader.getValue("age"));
    VisLabel currentCountryLabel = new VisLabel(LanguageModLoader.getValue("current_country"));
    VisLabel currentJobLabel = new VisLabel(LanguageModLoader.getValue("current_job"));
    VisLabel currentClubLabel = new VisLabel(LanguageModLoader.getValue("current_club"));
    
    // Dynamic Components
    VisLabel nameValueLabel = new VisLabel();
    VisLabel lastNameValueLabel = new VisLabel();
    VisLabel nationalityValueLabel = new VisLabel();
    VisLabel ageValueLabel = new VisLabel();
    VisLabel currentCountryValueLabel = new VisLabel();
    VisLabel currentJobValueLabel = new VisLabel();
    VisLabel currentClubValueLabel = new VisLabel();
    
    //
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    
    public PersonDetailsScreenTable(Game parent) {
        
        // auto margins
        super (true);
        
        //
        this.game = parent;
     
        // Name
        this.add(nameLabel);
        this.add(nameValueLabel);
        this.row();

        // Last Name
        this.add(lastNameLabel);
        this.add(lastNameValueLabel);
        this.row();
        
        // Nationality
        this.add(nationalityLabel);
        this.add(nationalityValueLabel);
        this.row();
        
        // Age
        this.add(ageLabel);
        this.add(ageValueLabel);
        this.row();
        
        // Current Country
        this.add(currentCountryLabel);
        this.add(currentCountryValueLabel);
        this.row();
        
        // Current Job
        this.add(currentJobLabel);
        this.add(currentJobValueLabel);
        this.row();
        
        // Current Club
        this.add(currentClubLabel);
        this.add(currentClubValueLabel);
        this.row();
    }
    
    //
    public void updateDynamicPersonComponents(Person person) {
        
        // 
        if (person != null) {
            
            //
            nameValueLabel.setText(person.getName());
            lastNameValueLabel.setText(person.getLastname());
            nationalityValueLabel.setText(person.getCountry().getCommonName());
            
            //
            ageValueLabel.setText(dateFormatter.format(person.getBirthDate()));
            
            //
            currentCountryValueLabel.setText(person.getCurrentCountry().getCommonName());
            currentJobValueLabel.setText(person.getPrimaryProfession().getName());
            
            //
            if (person.getCurrentClub() == null) {
                
                currentClubValueLabel.setText("UNAVAILABLE");
                
            } else {
                
                currentClubValueLabel.setText(person.getCurrentClub().getName());
            }
        }
    }
}