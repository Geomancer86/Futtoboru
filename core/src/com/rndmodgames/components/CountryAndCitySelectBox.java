package com.rndmodgames.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.City;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.State;
import com.rndmodgames.localization.LanguageModLoader;

public class CountryAndCitySelectBox extends VisTable {

    private Country selectedCountry;
    private State selectedState;
    private City selectedCity;
    
    /**
     * Creates a Country and City Selector based on SelectBox and Automatic
     *  Loading of Countries and Cities
     */
    public CountryAndCitySelectBox(){

        // This enables the automatic component spacing
        super(true);
        
//        setDebug(true);
        
        VisLabel countryLabel = new VisLabel(LanguageModLoader.getValue("country"));
        VisLabel stateLabel = new VisLabel(LanguageModLoader.getValue("state"));
        
        VisSelectBox<Country> countrySelectBox = new VisSelectBox<>();
        VisSelectBox<State> stateSelectBox = new VisSelectBox<>();

        /**
         * Load default list of Countries from /mods/countries.txt file and Load the SelectBox
         * 
         *  - The City SelectBox will be automatically filled with the right Cities for each Country
         */
        FileHandle countriesTxt = Gdx.files.internal("mods/countries.txt");
        List<Country> availableCountries = new ArrayList<>();

        if (countriesTxt.exists()){
            BufferedReader reader = new BufferedReader(countriesTxt.reader());
            
            String line;
            
            try {
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                while (line != null) {
                    
                    if (!line.startsWith("#")) {
  
                        String [] splitted = line.split(",");
                        
                        /**
                         * Country Format
                         * 
                         *  # COLUMNS
                         *  # id, common name, formal name
                         */
                        Country country = new Country();
                        
                        country.setId(Long.valueOf(splitted[0]));
                        country.setCommonName(splitted[1]);
                        
                        availableCountries.add(country);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
            }
            
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        /**
         * Load Countries Select Box
         */
        countrySelectBox.setItems(availableCountries.toArray(new Country[availableCountries.size()]));
        
        /**
         * Countries Select Box Listener
         */
        countrySelectBox.addListener(new ChangeListener() {
            
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                // The selected Country
                selectedCountry = countrySelectBox.getSelected();

                /**
                 * Search for the Country States txt file
                 */
                FileHandle statesTxt = Gdx.files.internal("mods/states/" + selectedCountry.getCommonName().toLowerCase() + ".txt");
                List<State> availableStates = new ArrayList<>();

                if (statesTxt.exists()){
                    BufferedReader reader = new BufferedReader(statesTxt.reader());
                    
                    String line;
                    
                    try {
                        line = reader.readLine();

                        // Use # symbol as starting for comment (to enable or disable available resolutions)
                        while (line != null) {
                            
                            if (!line.startsWith("#")) {

                                String [] splitted = line.split(",");
                                
                                /**
                                 * Country Format
                                 * 
                                 *  # COLUMNS
                                 *  # id, common name
                                 */
                                State state = new State();
                                
                                state.setId(Long.valueOf(splitted[0]));
                                state.setName(splitted[1]);
                                
                                availableStates.add(state);
                            }
                            
                            line = reader.readLine();
                        }
                        
                    } catch (IOException e) {
                        // TODO: If error, restore default resolutions.txt file
                    }
                    
                } else {
                    // TODO: If file doesn't exist, restore from default
                }
                
                if (availableStates != null && !availableStates.isEmpty()) {
                    // State Select Box
                    stateSelectBox.setItems(availableStates.toArray(new State[availableStates.size()]));

                    // hacky
                    stateSelectBox.setSelectedIndex(1);
                    stateSelectBox.setSelectedIndex(0);
                } else {
                    
                    stateSelectBox.clearItems();
                }
            }
        });
        
        stateSelectBox.addListener(new ChangeListener() {
            
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                // The selected State
                selectedState = stateSelectBox.getSelected();

            }
        });
        
        // hacky
        countrySelectBox.setSelectedIndex(1);
        countrySelectBox.setSelectedIndex(0);
        
        super.add(countryLabel);
        super.add(countrySelectBox);
        super.add(stateLabel);
        super.add(stateSelectBox).grow();
    }

    public Country getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(Country selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

    public State getSelectedState() {
        return selectedState;
    }

    public void setSelectedState(State selectedState) {
        this.selectedState = selectedState;
    }

    public City getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(City selectedCity) {
        this.selectedCity = selectedCity;
    }
}