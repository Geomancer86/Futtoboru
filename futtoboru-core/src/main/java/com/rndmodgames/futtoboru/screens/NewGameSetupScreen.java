package com.rndmodgames.futtoboru.screens;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.rndmodgames.futtoboru.ui.WizardStepLabel;
import com.kotcrab.vis.ui.widget.Tooltip;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Continent;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Step 2 — Choose Countries Screen.
 *
 * <p>Fixed 2026-07-19 per the pre-game menus UX audit
 * ({@code docs/planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md}):
 * <ul>
 *   <li>Next is now disabled (with a tooltip) whenever zero countries are selected (P0 — previously
 *       reachable, sending an empty countries list one screen further where it would eventually throw).</li>
 *   <li>Removed the hardcoded {@code countryCheckBox.setChecked(true)} override that fought the
 *       checkbox's own listener on every checkbox. The deliberate default is now: pre-check every
 *       active country only on first entry (empty incoming selection); once the player has made any
 *       selection at all (including via Back from Overview), their exact choices are respected.</li>
 *   <li>Added a live "N countries / M clubs selected" counter and Select All / Select None actions.</li>
 * </ul>
 *
 * <p>Country flags/icons per row are tracked as separate, genuinely-new scope — not added here.
 * 
 * @author Geomancer86
 *
 */
public class NewGameSetupScreen implements Screen {

    Game game;
    Stage stage;
    SpriteBatch batch;
    Texture img;
    
    //
    List<Country> selectedCountries = new ArrayList<>();
    Season startingSeason = null;

    VisLabel selectionCounterLabel = new VisLabel("");
    VisTextButton nextButton;
    Tooltip nextButtonTooltip;
    
    //
    public NewGameSetupScreen(Game parent, Season startingSeason, List<Country> selectedCountries) {
        
        this.game = parent;
        
        // Keep track of selected Countries
        this.selectedCountries = selectedCountries;

        // Deliberate default (replaces the old forced-checked override): on first entry the incoming
        // selection is empty, so we default to "every active country selected" — the common case for a
        // new game. Any subsequent visit (Back from Overview) already has a non-empty list and that
        // exact selection is respected instead.
        boolean isFirstVisit = this.selectedCountries.isEmpty();
        
        // Keep track of starting season
        this.startingSeason = startingSeason;

        stage = new Stage(UiScale.newViewport());
        
        VisTable mainContainer = new VisTable();
        mainContainer.pad(50);
    
        // 
        final VisTable table = new VisTable(true);
        
        /**
         * Choose Countries Label
         * 
         *  - Load the Available Leagues from /mods/leagues Folder
         *      - Sort Countries by Continent Alphabetically
         *      - Show Countries sorted by Continent
         *          - Selected Country Checkmark
         *          - Country Flag
         *          - Country Name
         */
        VisLabel chooseCountriesLabel = new VisLabel(LanguageModLoader.getValue("choose_countries"));
        
        /**
         * Active Countries Label
         */
        final VisTable activeCountriesTable = new VisTable(true);

        final List<VisCheckBox> allCountryCheckBoxes = new ArrayList<>();
        
        /**
         * Show only the Continents with Active Countries
         */
        for (Continent continent : DatabaseLoader.getInstance().getContinents()) {

            // An active Continent will have Countries with active Clubs
            boolean activeContinent = false;

            /**
             * Show only the Countries with Active Clubs
             */
            for (final Country country : DatabaseLoader.getInstance().getCountriesByContinent(continent)) {
                
                if (!DatabaseLoader.getInstance().getClubsByCountry(country).isEmpty()) {
                    // Set Continent as active
                    activeContinent = true;
                }
            }
            
            // Add a new Row if this Continent is active
            if (activeContinent) {
                
                activeCountriesTable.row();
                activeCountriesTable.add(new VisLabel(continent.getName()));
            }
            
            /**
             * Add the Active Countries to Pick
             */
            for (final Country country : DatabaseLoader.getInstance().getCountriesByContinent(continent)) {
                
                if (DatabaseLoader.getInstance().getClubsByCountry(country).isEmpty()) {
                    
                    // Country doesn't have active Clubs — not selectable
                    
                } else {
                    
                    //
                    VisLabel countryLabel = new VisLabel(country.getCommonName());
                    final VisCheckBox countryCheckBox = new VisCheckBox("");
                    
                    // Deliberate default checked state (see isFirstVisit comment above) — set once,
                    // not fought over on every subsequent listener callback.
                    boolean shouldBeChecked = isFirstVisit || this.selectedCountries.contains(country);
                    countryCheckBox.setChecked(shouldBeChecked);
                    if (isFirstVisit && !this.selectedCountries.contains(country)) {
                        this.selectedCountries.add(country);
                    }
                    
                    countryCheckBox.addListener(new ChangeListener() {

                        @Override
                        public void changed(ChangeEvent event, Actor actor) {

                            /**
                             * Add to selected countries list on check - remove on uncheck
                             */
                            if (countryCheckBox.isChecked()) {

                                if (!NewGameSetupScreen.this.selectedCountries.contains(country)) {

                                    NewGameSetupScreen.this.selectedCountries.add(country);
                                }

                            } else {

                                NewGameSetupScreen.this.selectedCountries.remove(country);
                            }

                            updateSelectionCounter();
                            updateNextButtonState();
                        }
                    });
                    
                    allCountryCheckBoxes.add(countryCheckBox);
                    
                    activeCountriesTable.row();
                    activeCountriesTable.add(countryCheckBox);
                    activeCountriesTable.add(countryLabel).left();
                }
            }
            
            /**
             * Add Horizontal Separator
             * 
             * TODO: only if active continents > 1
             */
            if (activeContinent) {
                activeCountriesTable.row();
                activeCountriesTable.addSeparator();
            }
        }

        /**
         * Select All / Select None quick actions
         */
        final VisTextButton selectAllButton = new VisTextButton(LanguageModLoader.getValue("select_all"));
        selectAllButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                for (VisCheckBox checkBox : allCountryCheckBoxes) {
                    checkBox.setChecked(true);
                }
            }
        });

        final VisTextButton selectNoneButton = new VisTextButton(LanguageModLoader.getValue("select_none"));
        selectNoneButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                for (VisCheckBox checkBox : allCountryCheckBoxes) {
                    checkBox.setChecked(false);
                }
            }
        });

        VisTable quickActionsTable = new VisTable(true);
        quickActionsTable.add(selectAllButton);
        quickActionsTable.add(selectNoneButton);
        
        /**
         * Next Screen
         */
        nextButton = new VisTextButton(LanguageModLoader.getValue("next"));

        // NOTE: Tooltip(String) is NOT a text constructor -- it looks up a named TooltipStyle in the
        // skin and throws GdxRuntimeException if no style is registered under that exact string. The
        // Builder is the correct way to get a plain-text tooltip (found 2026-07-19 via an actual click
        // through the real game after two earlier bugs in this same wizard were already fixed the same
        // day -- see A-12 in docs/registries/blockers-resolved.yaml). Builder.target(...) already
        // attaches during build() -- a follow-up .attach() call here throws IllegalStateException
        // ("More than one tooltip cannot be added to the same target!"), found the same way one rebuild
        // later. updateNextButtonState() below re-targets it later via setTarget(), not attach(), which
        // is the correct way to change target on an already-attached Tooltip.
        nextButtonTooltip = new Tooltip.Builder(LanguageModLoader.getValue("choose_countries_none_selected_tooltip"))
                .target(nextButton)
                .build();
        
        nextButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                if (nextButton.isDisabled()) {
                    return;
                }
 
                /**
                 * Move to New Game Overview Screen
                 */
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_OVERVIEW_SCREEN, startingSeason, NewGameSetupScreen.this.selectedCountries);
            }
        });

        // Step indicator
        mainContainer.row();
        mainContainer.add(WizardStepLabel.of(2, 3)).left();

        // active nations label
        mainContainer.row();
        mainContainer.add(chooseCountriesLabel);

        // quick actions
        mainContainer.row();
        mainContainer.add(quickActionsTable);

        // live selection counter
        mainContainer.row();
        mainContainer.add(selectionCounterLabel);
        
        // active nations by continent table
        mainContainer.row();
        mainContainer.add(activeCountriesTable);
        
        mainContainer.row();
        
        // Button container for Next and Back buttons
        VisTable buttonContainer = new VisTable();
        
        // Back to Season Selection Button
        final VisTextButton backButton = new VisTextButton(LanguageModLoader.getValue("back"));
        backButton.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // Go back to New Game Season Screen, preserving the season selection.
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SCREEN, startingSeason);
            }
        });
        
        buttonContainer.add(backButton).padRight(10);
        buttonContainer.add(nextButton);
        
        mainContainer.add(buttonContainer).right();

        // 
        mainContainer.add(table);

        updateSelectionCounter();
        updateNextButtonState();
        
        // Add Settings Screen Main Container to Stage
        UiScale.addOverflowScroll(stage, mainContainer);
    }

    /**
     * Test-only entry point for headless/scripted E2E smoke checks (see
     * {@code Futtoboru.debug.startScreen=NEW_GAME_FULL_FLOW}): invokes the exact same Next-button logic
     * as a real click, without needing one. Mirrors {@code NewGameOverviewScreen.triggerStartGameForTesting()}.
     */
    public void triggerNextForTesting() {
        if (nextButton.isDisabled()) {
            throw new IllegalStateException("DEBUG: Next button is disabled -- no countries selected");
        }
        ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_OVERVIEW_SCREEN, startingSeason, selectedCountries);
    }

    private void updateNextButtonState() {
        boolean disabled = selectedCountries.isEmpty();
        nextButton.setDisabled(disabled);
        if (nextButtonTooltip != null) {
            nextButtonTooltip.setTarget(disabled ? nextButton : null);
        }
    }

    private void updateSelectionCounter() {
        int clubCount = 0;
        for (Country country : selectedCountries) {
            List<Club> clubs = DatabaseLoader.getInstance().getClubsByCountry(country);
            if (clubs != null) {
                clubCount += clubs.size();
            }
        }
        selectionCounterLabel.setText(LanguageModLoader.getValue("countries_clubs_selected_summary")
                .replace("{countries}", String.valueOf(selectedCountries.size()))
                .replace("{clubs}", String.valueOf(clubCount)));
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
        dispose();
    }

    @Override
    public void dispose() {

        // Dispose on screen change
        stage.dispose();
    }
}
