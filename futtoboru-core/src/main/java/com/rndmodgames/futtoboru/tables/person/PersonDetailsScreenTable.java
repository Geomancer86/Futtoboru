package com.rndmodgames.futtoboru.tables.person;

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
    VisLabel currentJobLabel = new VisLabel(LanguageModLoader.getValue("current_job"));
    
    // Dynamic Components
    VisLabel nameValueLabel = new VisLabel();
    VisLabel lastNameValueLabel = new VisLabel();
    VisLabel nationalityValueLabel = new VisLabel();
    VisLabel ageValueLabel = new VisLabel();
    VisLabel currentJobValueLabel = new VisLabel();
    
    public PersonDetailsScreenTable(Game parent) {
        
        // auto margins
        super (true);
        
        //
        this.game = parent;

        // without a size we cannot align
//        this.setWidth(400);
//        this.setHeight(600);
                
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
        
        // Current Job
        this.add(currentJobLabel);
        this.add(currentJobValueLabel);
        this.row();
    }
    
    /**
     * TODO: only update once if dirty?
     */
    public void updateDynamicPersonComponents(Person person) {
        
        // 
        if (person != null) {
            
            //
            nameValueLabel.setText(person.getName());
            lastNameValueLabel.setText(person.getLastname());
            nationalityValueLabel.setText(person.getCountry().getCommonName());
            ageValueLabel.setText(person.getBirthYear());
            currentJobValueLabel.setText(person.getPrimaryProfession().getName());
        }
    }
}