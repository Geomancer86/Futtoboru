package com.rndmodgames.futtoboru.screens;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.rndmodgames.futtoboru.ui.WizardStepLabel;
import com.kotcrab.vis.ui.widget.LinkLabel;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.components.MainMenuButton;
import com.rndmodgames.futtoboru.data.Person;
import com.rndmodgames.futtoboru.data.Season;
import com.rndmodgames.futtoboru.dialogs.ConfirmDialog;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.system.SaveLoadSystem;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * New Game Step 1 — Season & Manager Screen.
 *
 * <p>Rewritten 2026-07-19 per the pre-game menus UX audit
 * ({@code docs/planning/ANALYSIS_PREGAME_MENUS_UX_AUDIT_V1.md}). Fixes applied here:
 * <ul>
 *   <li>Start is disabled whenever season or manager is unselected (P0 — used to be reachable with a
 *       null manager, crashing later in {@code NewGameOverviewScreen}).</li>
 *   <li>Fixed the {@code toArray(new String[allSeasons.size()])} sizing bug on the saved-managers box.</li>
 *   <li>Dropdown selection alone now sets the active manager (removed the redundant "Select Manager"
 *       button — one interaction model instead of two).</li>
 *   <li>Added a one-line manager summary and a per-manager Delete control (behind {@link ConfirmDialog}).</li>
 *   <li>Season selection now survives a round trip through Create Manager and back.</li>
 *   <li>{@code hide()}/{@code dispose()} now actually dispose the stage (previously both were no-ops —
 *       a real, if slow, memory leak every time this screen was visited).</li>
 *   <li>Single-season case renders a label instead of a single-item dropdown.</li>
 *   <li>The season source caption now reflects the real URL host instead of a hardcoded "Wikipedia".</li>
 * </ul>
 *
 * @author Geomancer86
 */
public class NewGameSeasonScreen implements Screen {

    Game game;
    Stage stage;

    // Dynamic Components
    VisLabel selectedSeasonDescriptionLabel = new VisLabel("");
    VisLabel seasonCountriesNumber = new VisLabel("");
    VisLabel seasonStartDateValueLabel = new VisLabel("");
    LinkLabel seasonUrlSourceValueLabel = new LinkLabel("");
    VisLabel managerSummaryLabel = new VisLabel("");

    VisSelectBox<Season> availableSeasonsSelectBox = null;
    VisTextButton startGame = null;

    //
    private Person selectedManager;
    private Season selectedSeason;

    public NewGameSeasonScreen(Game parent) {
        this(parent, null, null);
    }

    /**
     * @param preselectedSeason season to restore on entry — used when routing back here from
     *                          {@code NewManagerScreen} so the player's earlier season choice survives
     *                          the round trip (pre-game menus audit 2026-07-19).
     */
    public NewGameSeasonScreen(Game parent, Season preselectedSeason) {
        this(parent, preselectedSeason, null);
    }

    /**
     * @param preselectedManager manager just created on {@code NewManagerScreen}, wired directly as the
     *                           active selection in memory — no save-to-file-then-reload-by-filename
     *                           round trip needed (pre-game menus audit 2026-07-19).
     */
    public NewGameSeasonScreen(Game parent, Season preselectedSeason, Person preselectedManager) {

        //
        this.game = parent;

        stage = new Stage(UiScale.newViewport());

        VisTable mainContainer = new VisTable();
        mainContainer.pad(50);

        //
        final VisTable table = new VisTable(true);

        // Step indicator
        table.row();
        table.add(WizardStepLabel.of(1, 3)).colspan(2).left();

        //
        VisLabel seasonLabel = new VisLabel(LanguageModLoader.getValue("starting_season"));
        VisLabel seasonDescriptionLabel = new VisLabel(LanguageModLoader.getValue("season_description"));
        VisLabel seasonStartDateLabel = new VisLabel(LanguageModLoader.getValue("season_start_date"));
        VisLabel seasonUrlSourceLabel = new VisLabel(LanguageModLoader.getValue("season_url_source"));
        VisLabel seasonCountriesLabel = new VisLabel(LanguageModLoader.getValue("season_countries"));

        // Get All Available Seasons
        List<Season> allSeasons = DatabaseLoader.getInstance().getSeasons();

        // Single-season case: a label reads better than a one-item dropdown (P1).
        boolean singleSeason = allSeasons.size() == 1;

        if (singleSeason) {
            selectedSeason = allSeasons.get(0);
        } else if (preselectedSeason != null && allSeasons.contains(preselectedSeason)) {
            selectedSeason = preselectedSeason;
        }

        Actor seasonSelector;
        if (singleSeason) {
            seasonSelector = new VisLabel(allSeasons.get(0).getName());
        } else {
            availableSeasonsSelectBox = new VisSelectBox<>();
            availableSeasonsSelectBox.setItems(allSeasons.toArray(new Season[allSeasons.size()]));

            if (selectedSeason != null) {
                availableSeasonsSelectBox.setSelected(selectedSeason);
            }

            availableSeasonsSelectBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {

                    if (availableSeasonsSelectBox.getSelected() != null) {
                        selectedSeason = availableSeasonsSelectBox.getSelected();
                        updateDynamicComponents(selectedSeason);
                        updateStartButtonState();
                    }
                }
            });

            seasonSelector = availableSeasonsSelectBox;
        }

        // set the default values as the Season will be selected automatically
        if (selectedSeason != null) {
            updateDynamicComponents(selectedSeason);
        }

        // Season
        table.row();
        table.add(seasonLabel);
        table.add(seasonSelector);

        // Season Description
        table.row();
        table.add(seasonDescriptionLabel);
        table.add(selectedSeasonDescriptionLabel);

        // Season Start Date
        table.row();
        table.add(seasonStartDateLabel);
        table.add(seasonStartDateValueLabel);

        // Season Source URL
        table.row();
        table.add(seasonUrlSourceLabel);
        table.add(seasonUrlSourceValueLabel);

        // Season Countries
        table.row();
        table.add(seasonCountriesLabel);
        table.add(seasonCountriesNumber);

        // Season Leagues
        // TODO: show

        // Season Teams
        // TODO: show

        // Season Competitions
        // TODO: show

        // Season Rules
        // TODO: show

        /**
         * Load Existing Manager - Create New Manager
         */
        VisLabel managerLabel = new VisLabel(LanguageModLoader.getValue("manager"));

        List<String> savedManagers = SaveLoadSystem.listSavedManagers();

        table.row();
        table.add(managerLabel);

        if (savedManagers.isEmpty()) {

            table.add(new VisLabel(LanguageModLoader.getValue("no_saved_managers")));

            if (preselectedManager != null) {
                selectedManager = preselectedManager;
                updateManagerSummary(selectedManager);
            }

        } else {

            // EXISTING MANAGERS LIST
            final VisSelectBox<String> savedManagersSelectBox = new VisSelectBox<>();
            savedManagersSelectBox.setItems(savedManagers.toArray(new String[savedManagers.size()]));

            // Loads the Person immediately on selection — one interaction model instead of a
            // dropdown-plus-button pair that could disagree with each other (P1 fix).
            savedManagersSelectBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    selectedManager = SaveLoadSystem.loadPersonFromFile(savedManagersSelectBox.getSelected());
                    updateManagerSummary(selectedManager);
                    updateStartButtonState();
                }
            });

            if (preselectedManager != null) {
                // Just created on NewManagerScreen — wire it directly, no file reload needed, and
                // best-effort sync the dropdown's visible selection to match by lastname.
                selectedManager = preselectedManager;
                for (String entry : savedManagers) {
                    if (preselectedManager.getLastname() != null
                            && entry.toLowerCase().contains(preselectedManager.getLastname().toLowerCase())) {
                        savedManagersSelectBox.setSelected(entry);
                        break;
                    }
                }
            } else {
                // automatically select first manager on the list
                selectedManager = SaveLoadSystem.loadPersonFromFile(savedManagersSelectBox.getSelected());
            }
            updateManagerSummary(selectedManager);

            final VisTextButton deleteManagerButton = new VisTextButton(LanguageModLoader.getValue("delete"));
            deleteManagerButton.addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    String toDelete = savedManagersSelectBox.getSelected();
                    if (toDelete == null) {
                        return;
                    }
                    new ConfirmDialog(
                            LanguageModLoader.getValue("delete"),
                            LanguageModLoader.getValue("delete_manager_confirm_message"),
                            () -> {
                                SaveLoadSystem.deleteSavedManager(toDelete);
                                // Simplest correct refresh: rebuild this screen from scratch.
                                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SCREEN, selectedSeason);
                            }
                    ).show(stage);
                }
            });

            table.add(savedManagersSelectBox);
            table.add(deleteManagerButton);

            table.row();
            table.add(new VisLabel(""));
            table.add(managerSummaryLabel).colspan(2).left();
        }

        // CREATE MANAGER BUTTON
        final VisTextButton createManager = new VisTextButton(LanguageModLoader.getValue("create_manager"));
        createManager.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                // Season selection is preserved across the round trip (see NewManagerScreen ctor).
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_MANAGER_SCREEN, selectedSeason);
            }
        });

        table.row();
        table.add(createManager);

        /**
         * NEXT/NEW GAME GAME BUTTON
         *
         * Disabled whenever season or manager is unselected (P0 fix — this used to be always
         * clickable, and clicking it with a null manager crashed several steps later in
         * NewGameOverviewScreen with a confusing "Game owner is null" exception).
         */
        startGame = new VisTextButton(LanguageModLoader.getValue("start"));
        updateStartButtonState();

        startGame.addCaptureListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                if (startGame.isDisabled() || selectedSeason == null || selectedManager == null) {
                    return;
                }

                /**
                 * Set the Selected Person as Game Owner
                 */
                SaveGame currentGame = new SaveGame();

                //
                ((Futtoboru) game).setCurrentGame(currentGame);

                // set game owner
                ((Futtoboru) game).getCurrentGame().setOwner(selectedManager);

                // Redirect to New Game Setup Screen
                ((Futtoboru) game).changeScreen(Futtoboru.NEW_GAME_SETUP_SCREEN, selectedSeason, new ArrayList<>());
            }
        });

        //
        table.row();
        table.add(startGame).pad(5);

        // Main Menu Button - Add right next to Start button in same row
        final MainMenuButton backButton = new MainMenuButton(game);
        table.add(backButton).pad(5);

        // Add table to main container
        mainContainer.add(table);

        // Wrapped in an overflow scroll pane (not a bare setFillParent(true) actor) so this screen
        // doesn't clip its bottom controls at higher UI scales or with many saved managers -- unlike
        // its sibling wizard steps (NewGameSetupScreen, NewGameOverviewScreen, NewManagerScreen,
        // SettingsScreen), this was the one pre-game screen still missing it (UI scroll audit, 2026-07-19).
        UiScale.addOverflowScroll(stage, mainContainer);
    }

    private void updateStartButtonState() {
        if (startGame != null) {
            startGame.setDisabled(selectedSeason == null || selectedManager == null);
        }
    }

    private void updateManagerSummary(Person manager) {
        if (manager == null) {
            managerSummaryLabel.setText("");
            return;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(manager.getName() != null ? manager.getName() : "").append(' ')
                .append(manager.getLastname() != null ? manager.getLastname() : "");

        if (manager.getBirthDate() != null) {
            summary.append(" (b. ").append(manager.getBirthDate().getYear()).append(')');
        }
        if (manager.getCountry() != null && manager.getCountry().getCommonName() != null) {
            summary.append(" — ").append(manager.getCountry().getCommonName());
        }

        managerSummaryLabel.setText(summary.toString().trim());
    }

    /**
     *
     */
    public void updateDynamicComponents(Season season) {

        if (season == null) {
            return;
        }

        // update season description label
        selectedSeasonDescriptionLabel.setText(season.getDescription());

        // update season countries label
        seasonCountriesNumber.setText(season.getCountries().size());

        // update season start date label
        seasonStartDateValueLabel.setText(DatabaseLoader.formatter.format(season.getStartDate()));

        // update season source url label — real URL host instead of a hardcoded "Wikipedia" (P1 fix)
        seasonUrlSourceValueLabel.setText(sourceHostOrFallback(season.getUrlSource()));
        seasonUrlSourceValueLabel.setUrl(season.getUrlSource());
    }

    private static String sourceHostOrFallback(String url) {
        if (url == null || url.isBlank()) {
            return LanguageModLoader.getValue("season_source_generic");
        }
        try {
            String host = new URI(url).getHost();
            return host != null ? host : LanguageModLoader.getValue("season_source_generic");
        } catch (Exception e) {
            return LanguageModLoader.getValue("season_source_generic");
        }
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
        stage.dispose();
    }
}
