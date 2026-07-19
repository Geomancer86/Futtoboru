package com.rndmodgames.futtoboru.screens;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.kotcrab.vis.ui.util.form.FormInputValidator;
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
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.dialogs.ConfirmDialog;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.futtoboru.system.validation.ManagerBirthDateValidator;
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
 * <p>Fixed 2026-07-19 per the pre-game menus UX audit
 * ({@code docs/planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md}):
 * <ul>
 *   <li>The Create button is now disabled until the birth-date field parses as a plausible calendar
 *       date (see {@link ManagerBirthDateValidator}) — previously only non-empty was checked, and a
 *       {@code ParseException} on garbage input was silently swallowed, saving a manager with a
 *       {@code null} birth date (P0 — silent data corruption).</li>
 *   <li>Creating a manager whose last name collides with an existing save now asks to overwrite via
 *       {@link ConfirmDialog} instead of overwriting silently (P0).</li>
 *   <li>Name/Last Name have a sane max length — they are used verbatim as a save filename.</li>
 *   <li>Added a Back button to return to the Season screen without losing the season selection, in
 *       addition to the pre-existing Main Menu exit (P1).</li>
 * </ul>
 *
 * <p>Nationality/Languages/Picture and the City half of {@code CountryAndCitySelectBox} are tracked as
 * separate, genuinely-new scope in {@code docs/specs/pregame_menus_polish/spec.md} unit 5 — not fixed
 * here.
 * 
 * @author Geomancer86
 */
public class NewManagerScreen implements Screen {

	Game game;
	Stage stage;

	// Season the player picked on the previous screen; carried through so it survives this round trip.
	private final Season returnSeason;

	public NewManagerScreen(Game parent) {
		this(parent, null);
	}

	public NewManagerScreen(Game parent, Season returnSeason) {

		this.game = parent;
		this.returnSeason = returnSeason;
		
		stage = new Stage(UiScale.newViewport());
		
		/**
		 * Person to be Created
		 */
		Person manager = new Person();
		
		VisTable mainContainer = new VisTable(true);
		mainContainer.pad(50);
		
		/**
		 * Name Label
		 */
		VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("name"));

		/**
		 * Name Validatable Text Field
		 */
		final VisValidatableTextField nameTextField = new VisValidatableTextField();
		nameTextField.setMaxLength(MAX_NAME_LENGTH);
      
        nameTextField.setTextFieldListener(new TextFieldListener() {
            public void keyTyped(VisTextField textField, char key) {
                // no-op: FormValidator drives the Create button's disabled state on every keystroke
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
        lastNameTextField.setMaxLength(MAX_NAME_LENGTH);
        
        lastNameTextField.setTextFieldListener(new TextFieldListener() {
            public void keyTyped(VisTextField textField, char key) {
                // no-op
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
                // no-op
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
        
        final int seasonStartYear = returnSeason != null && returnSeason.getStartDate() != null
                ? returnSeason.getStartDate().getYear()
                : DEFAULT_SEASON_START_YEAR;

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

                if (createManager.isDisabled()) {
                    return;
                }

                LocalDate birthDate = ManagerBirthDateValidator.tryParse(birthDateTextField.getText());
                if (birthDate == null) {
                    // Defense-in-depth only: the FormValidator below should make this unreachable.
                    Gdx.app.error("NewManagerScreen", "Create clicked with an unparseable birth date despite validator");
                    return;
                }

                String lastNameFileBaseName = lastNameTextField.getText().trim().toLowerCase();

                if (SaveLoadSystem.managerFileExists(lastNameFileBaseName)) {
                    new ConfirmDialog(
                            LanguageModLoader.getValue("create_manager"),
                            LanguageModLoader.getValue("manager_overwrite_confirm_message"),
                            () -> completeManagerCreation(manager, nameTextField, lastNameTextField, birthDate,
                                    placeOfBirthSelectBox, lastNameFileBaseName)
                    ).show(stage);
                } else {
                    completeManagerCreation(manager, nameTextField, lastNameTextField, birthDate,
                            placeOfBirthSelectBox, lastNameFileBaseName);
                }
            }
        });
        
		mainContainer.row();
		mainContainer.add(createManager);
		mainContainer.add(validationErrorLabel);
		
        /**
         * Form Validator
         */
        FormValidator validator = new FormValidator(createManager, validationErrorLabel);

        validator.notEmpty(nameTextField, LanguageModLoader.getValue("manager_name_validation_empty"));
        validator.notEmpty(lastNameTextField, LanguageModLoader.getValue("manager_last_name_validation_empty"));

        FormInputValidator birthDateValidator = new FormInputValidator(
                LanguageModLoader.getValue("manager_birthdate_invalid")) {
            @Override
            protected boolean validate(String input) {
                return ManagerBirthDateValidator.isValid(input, seasonStartYear);
            }
        };
        validator.custom(birthDateTextField, birthDateValidator);
        
        //
        mainContainer.row();
        
        /**
         * Back Button - returns to the Season screen without losing the season selection
         */
        final VisTextButton backButton = new VisTextButton(LanguageModLoader.getValue("back"));
        backButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SCREEN, returnSeason);
            }
        });

        /**
         * Main Menu Button
         */
        final MainMenuButton exitButton = new MainMenuButton(game);
        
        mainContainer.add(backButton).right();
        mainContainer.add(exitButton).right();

        // Apply DEBUG to tables as per Settings
//        stage.setDebugAll(DarkBlade.DEBUG_TABLES_ENABLED);
        
        // Add Settings Screen Main Container to Stage
        UiScale.addOverflowScroll(stage, mainContainer);
	}

	private static final int MAX_NAME_LENGTH = 40;
	private static final int DEFAULT_SEASON_START_YEAR = 1888;

	/**
	 * Finalizes and persists the manager, then routes back to the Season screen with the new manager
	 * wired directly onto {@code selectedManager} in memory — no reload-by-filename round trip needed
	 * (P1 fix, pre-game menus UX audit 2026-07-19).
	 */
	private void completeManagerCreation(Person manager, VisValidatableTextField nameTextField,
			VisValidatableTextField lastNameTextField, LocalDate birthDate,
			CountryAndCitySelectBox placeOfBirthSelectBox, String lastNameFileBaseName) {

		manager.setName(nameTextField.getText().trim());
		manager.setLastname(lastNameTextField.getText().trim());
		manager.setBirthDate(LocalDateTime.of(birthDate, LocalTime.MIDNIGHT));
		manager.setCountry(placeOfBirthSelectBox.getSelectedCountry());
		manager.setState(placeOfBirthSelectBox.getSelectedState());

		SaveLoadSystem.saveManagerDataFile(manager, lastNameFileBaseName);

		((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SCREEN, returnSeason, manager);
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
		UiScale.updateStageViewport(stage, width, height);
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

	    // Hide will be called after switching to a separate screen
	    this.dispose();
	}

	@Override
	public void dispose() {

		// Dispose on screen change
		stage.dispose();
	}
}
