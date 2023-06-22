package com.rndmodgames.futtoboru.screens;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.form.FormValidator;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.VisTextField.TextFieldListener;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.rndmodgames.components.CountryAndCitySelectBox;
import com.rndmodgames.components.MainMenuButton;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Manager Screen v1
 *  
 *  - Name
 *  - Lastname
 *  - Picture/Icon
 *  
 *  - Place of Birth
 *  - Date of Birth
 *  
 *  - Nationality
 *  - Second Nationality
 *  - Languages Spoken
 *  
 *  - Social Networks
 *  
 * @author Geomancer86
 */
public class NewManagerScreen implements Screen {

	Game game;
	Stage stage;
	SpriteBatch batch;
	Texture img;

	public NewManagerScreen(Game parent) {

		this.game = parent;
		
		stage = new Stage(new ScreenViewport());
		
		/**
		 * Person to be Created
		 */
		Person manager = new Person();
		
		VisTable mainContainer = new VisTable(true);
		mainContainer.pad(50).setFillParent(true);
		
		/**
		 * Name Label
		 */
		VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("name"));

		/**
		 * Name Validatable Text Field
		 */
		final VisValidatableTextField nameTextField = new VisValidatableTextField();
      
        nameTextField.setTextFieldListener(new TextFieldListener() {

            public void keyTyped(VisTextField textField, char key) {

//                  worldSeedTextField.setSelection(0, 0);
                // When you press 'enter', do something

//                  if (key == 13) {
                // System.out.println("You have typed " + worldSeedTextField.getText());
//                  }
            }
        });
		
        /**
         * Last Name
         */
        VisLabel lastNameLabel = new VisLabel(LanguageModLoader.getValue("last_name"));

        /**
         * Last Name Validatable Text Field
         */
        final VisValidatableTextField lastNameTextField = new VisValidatableTextField();
        
        lastNameTextField.setTextFieldListener(new TextFieldListener() {

            public void keyTyped(VisTextField textField, char key) {

//                  worldSeedTextField.setSelection(0, 0);
                // When you press 'enter', do something

//                  if (key == 13) {
                // System.out.println("You have typed " + worldSeedTextField.getText());
//                  }
            }
        });
        
        /**
         * Birthdate Label
         */
        VisLabel birthDateLabel = new VisLabel(LanguageModLoader.getValue("birth_date") + " dd/MM/yyyy");
        
        /**
         * Birth Date Validatable Text Field
         */
        final VisValidatableTextField birthDateTextField = new VisValidatableTextField();
        
        birthDateTextField.setTextFieldListener(new TextFieldListener() {

            public void keyTyped(VisTextField textField, char key) {

//                  worldSeedTextField.setSelection(0, 0);
                // When you press 'enter', do something

//                  if (key == 13) {
                // System.out.println("You have typed " + worldSeedTextField.getText());
//                  }
            }
        });
        
        /**
         * Create Manager Button
         */
		final VisTextButton createManager = new VisTextButton(LanguageModLoader.getValue("create_manager"));
          
        /**
         * Validation Error Label
         */
        VisLabel validationErrorLabel = new VisLabel();
        
        /**
         * Add Manager Name Row
         */
		mainContainer.row();
		mainContainer.add(nameLabel);
		mainContainer.add(nameTextField);
		
		/**
		 * Add Manager Last Name Row
		 */
		mainContainer.row();
        mainContainer.add(lastNameLabel);
        mainContainer.add(lastNameTextField);
		
        /**
         * Add Manager Birthdate Row
         */
        mainContainer.row();
        mainContainer.add(birthDateLabel);
        mainContainer.add(birthDateTextField);
        
        /**
         * Add Manager Place of Birth 
         * 
         * Birth City/Country/Nationalities
         * 
         * TODO: move to separate component after done with the logic
         */
        VisLabel placeOfBirthLabel = new VisLabel(LanguageModLoader.getValue("place_of_birth"));

        CountryAndCitySelectBox placeOfBirthSelectBox = new CountryAndCitySelectBox();
        
        mainContainer.row();
        mainContainer.add(placeOfBirthLabel);
        mainContainer.add(placeOfBirthSelectBox);
        
        /**
         * Add Manager Other Nationalities Row
         */
        
        /**
         * Add Manager Other Languages Row
         */
        
		/**
		 * Add Create Manager Button Row
		 */
        createManager.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
 
                /**
                 * Complete Manager Profile
                 */
                manager.setName(nameTextField.getText());
                manager.setLastname(lastNameTextField.getText());
                
                /**
                 * TODO: make sure date format is right
                 */
                DateFormat birthDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
                
                try {
                    
                    Date birthDate = birthDateFormatter.parse(birthDateTextField.getText());
                    
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(birthDate);
                    
                    manager.setBirthDay(calendar.get(Calendar.DAY_OF_MONTH));
                    manager.setBirthMonth(calendar.get(Calendar.MONTH));
                    manager.setBirthYear(calendar.get(Calendar.YEAR));
                    
                } catch (ParseException e) {
                    
                    e.printStackTrace();
                }
                
//                manager.setBirthdate(new Date());
                manager.setCountry(placeOfBirthSelectBox.getSelectedCountry());
                manager.setState(placeOfBirthSelectBox.getSelectedState());

                System.out.println(manager.toString());
                
                /**
                 * After creating the New Manager, we save it to file and switch to the New Game Screen
                 * 
                 * TODO: we need to check if there is another manager file with the same name and ask to rewrite or
                 *          save to separate file
                 */
                SaveLoadSystem.saveManagerDataFile(manager, manager.getLastname().toLowerCase());
                
                System.out.println(manager.toString());
                
                /**
                 * Redirect/Go Back to New Game Screen
                 */
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SCREEN);
            }
        });
        
		mainContainer.row();
		mainContainer.add(createManager);
		mainContainer.add(validationErrorLabel);
		
        /**
         * Form Validator
         * 
         * TODO: Date format validation
         */
        FormValidator validator = new FormValidator(createManager, validationErrorLabel);

        validator.notEmpty(nameTextField, LanguageModLoader.getValue("manager_name_validation_empty"));
        validator.notEmpty(lastNameTextField, LanguageModLoader.getValue("manager_last_name_validation_empty"));
        validator.notEmpty(birthDateTextField, LanguageModLoader.getValue("manager_birthdate_cannot_be_empty"));
        
        //
        mainContainer.row();
        
        /**
         * Main Menu Button
         */
        final MainMenuButton exitButton = new MainMenuButton(game);
        
        mainContainer.add(exitButton).right().colspan(2);

        // Apply DEBUG to tables as per Settings
//        stage.setDebugAll(DarkBlade.DEBUG_TABLES_ENABLED);
        
        // Add Settings Screen Main Container to Stage
        stage.addActor(mainContainer);
	}
	
	@Override
	public void show() {
		
		// Add input capabilities
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void render(float delta) {
		
		// Clear blit
		Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Draw
        stage.act();
        stage.draw(); 
	}

	/**
	 * Utility method used to return all the available World Size on 
	 */
	
	@Override
	public void resize(int width, int height) {
		
		// Update viewport on resize to keep event coordinate for buttons/widgets
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {

	    System.out.println("HIDE WAS CALLED ON NEW MANAGER SCREEN - DISPOSE");
	    
	 // Hide will be called after switching to a separate screen
	    this.dispose();
	}

	@Override
	public void dispose() {

		// Dispose on screen change
		stage.dispose();
	}
}