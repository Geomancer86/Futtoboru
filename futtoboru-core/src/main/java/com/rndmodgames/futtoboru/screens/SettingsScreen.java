package com.rndmodgames.futtoboru.screens;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.components.MainMenuButton;
import com.rndmodgames.darkblade.screens.tables.ModsSettingsTable;
import com.rndmodgames.darkblade.screens.tables.SoundSettingsTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.tables.settings.DebugLoggingSettingsTable;
import com.rndmodgames.localization.LanguageModLoader;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Settings Screen v1
 * 
 * - Window & Resolution
 * - Language & Regional Settings
 * - Autosave Options
 * 
 * @author Geomancer86
 */
public class SettingsScreen implements Screen {

	Game game;
	Stage stage;
	SpriteBatch batch;
	Texture img;
	
	// Debug Logging Settings Table (stored for refresh)
	private DebugLoggingSettingsTable debugLoggingSettings;

	// UI scale select box (its Auto entry shows the scale Auto picks, refreshed on resize)
	private VisSelectBox<String> uiScaleSelectBox;

	/**
	 * Settings Screen
	 * 
	 * @param parent
	 */
	public SettingsScreen(Game game) {
		this(game, Futtoboru.MENU_SCREEN);
	}

	/**
	 * Screen that Back (and Esc) returns to: the main menu, or {@link Futtoboru#GAME_SCREEN} when opened from the
	 * in-game Options (U-04d).
	 */
	private final int returnScreen;

	/** Set once Back was taken, so a second click or Esc in the same frame does not open two screens. */
	private boolean leaving = false;

	/**
	 * Settings Screen opened from somewhere else than the main menu
	 *
	 * @param returnScreen a {@link Futtoboru} screen id
	 */
	public SettingsScreen(Game game, int returnScreen) {

        this.game = game;
        this.returnScreen = returnScreen;
        
        stage = new Stage(UiScale.newViewport());

        VisTable mainContainer = new VisTable();

        // TODO: padding relative to resolution so stays relatively centered
        mainContainer.pad(UiTheme.space("xl")).setFillParent(true);

        VisTable graphicsSettings = new VisTable();
        VisTable soundSettings = new SoundSettingsTable(this.game);
        VisTable gameplaySettings = new VisTable();
        VisTable modSettings = new ModsSettingsTable(this.game);
        this.debugLoggingSettings = new DebugLoggingSettingsTable(this.game);
        
        VisLabel graphicsSettingsLabel = new VisLabel(LanguageModLoader.getValue("graphic_settings"));
        VisLabel resolutionLabel = new VisLabel(LanguageModLoader.getValue("resolution"));
        VisLabel fullScreenLabel = new VisLabel(LanguageModLoader.getValue("fullscreen"));
        VisLabel dayNightTintLabel = new VisLabel(LanguageModLoader.getValue("day_night_tint"));
        VisLabel damageCountersLabel = new VisLabel(LanguageModLoader.getValue("damage_counters"));
        VisLabel debugPhysicsLabel = new VisLabel(LanguageModLoader.getValue("debug_physics"));
        VisLabel debugPathFindingLabel = new VisLabel(LanguageModLoader.getValue("debug_pathfinding"));
        VisLabel debugSightLabel = new VisLabel(LanguageModLoader.getValue("debug_sight"));
        VisLabel shadowsLabel = new VisLabel(LanguageModLoader.getValue("shadows"));
        
        VisLabel uiScaleLabel = new VisLabel(LanguageModLoader.getValue("ui_scale"));

        VisLabel gameplaySettingsLabel = new VisLabel(LanguageModLoader.getValue("gameplay_settings"));

        /**
         * Resolution Select Box:
         * 
         * - Available resolutions loaded from filesystem resolutions.txt
         * 
         * TODO: move to mod loader
         */
        FileHandle resolutionsTxt = Gdx.files.internal("mods/resolutions.txt");
        List<String> availableResolutions = new ArrayList<>();

        if (resolutionsTxt.exists()) {
            
            BufferedReader reader = new BufferedReader(resolutionsTxt.reader());
            
            String line;
            
            try {
                line = reader.readLine();

                // Use # symbol as starting for comment (to enable or disable available resolutions)
                // Use x as separator, ie: 640x480 (to add resolutions)
                while (line != null) {
                    
                    // entries below the 1280x720 minimum (D18) are not offered
                    String entry = line.trim();
                    if (!entry.isEmpty() && !entry.startsWith("#") && UiScale.sanitizeResolution(entry).equals(entry)) {
                        availableResolutions.add(entry);
                    }
                    
                    line = reader.readLine();
                }
                
            } catch (IOException e) {
                // TODO: If error, restore default resolutions.txt file
            }
        } else {
            // TODO: If file doesn't exist, restore from default
        }
        
        // Resolutions Select Box
        final VisSelectBox<String> availableResolutionsSelectBox = new VisSelectBox<String>();
        availableResolutionsSelectBox.setItems(availableResolutions.toArray(new String[availableResolutions.size()]));
        
        // Set resolution from saved settings (or default)
        availableResolutionsSelectBox.setSelected(Futtoboru.RESOLUTION);
        
        /**
         * FullScreen Mode Checkbox
         */
        final VisCheckBox fullScreenCheckBox = new VisCheckBox("");
        
        availableResolutionsSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                
                // The selected resolution
                String res = availableResolutionsSelectBox.getSelected();
                
                // Update Game Resolution
                Futtoboru.RESOLUTION = res;
                
                // Update User Preferences
                ((Futtoboru) game).updateScreenResolution(res);
                
                // Set to Windowed
                fullScreenCheckBox.setChecked(false);
                
                if (Futtoboru.FULLSCREEN) {
                    ((Futtoboru) game).toggleFullscreen();
                } else {
                    ((Futtoboru) game).setScreen();
                }
            }
        });
        
        /**
         * UI scale (U-03a): Auto (shows the scale it picks now), 100%, 200%. Integer steps only: the atlas is Nearest.
         */
        uiScaleSelectBox = new VisSelectBox<>();
        final String[] uiScaleItems = new String[UiScale.PRESET_SCALES.length + 1];
        uiScaleItems[0] = autoScaleItem();
        for (int i = 0; i < UiScale.PRESET_SCALES.length; i++) {
            uiScaleItems[i + 1] = UiScale.formatPercent(UiScale.PRESET_SCALES[i]);
        }
        uiScaleSelectBox.setItems(uiScaleItems);
        uiScaleSelectBox.setSelectedIndex(uiScaleIndex(UiScale.getSetting()));
        uiScaleSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                int index = uiScaleSelectBox.getSelectedIndex();
                float scale = index <= 0 ? UiScale.AUTO : UiScale.PRESET_SCALES[index - 1];
                if (scale == UiScale.getSetting()) {
                    return;
                }
                ((Futtoboru) game).updateUiScale(scale);
            }
        });

        // Set FullScreen to Saved Value
//        fullScreenCheckBox.setChecked(DarkBlade.FULLSCREEN);
        fullScreenCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
                ((Futtoboru) game).toggleFullscreen();
            }
        });
        
        /**
         * Day-Night Rendering Checkbox
         */
        final VisCheckBox dayNightTintCheckBox = new VisCheckBox("");
//        dayNightTintCheckBox.setChecked(DarkBlade.DAY_NIGHT_RENDERING);
        
        dayNightTintCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
//                ((DarkBlade) game).toggleDayNightCycleRendering();
            }
        });
        
        /**
         *  Shadow Rendering Checkbox
         */
        final VisCheckBox shadowsCheckBox = new VisCheckBox("");
//        shadowsCheckBox.setChecked(DarkBlade.SHADOWS_RENDERING);
        
        shadowsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
//                ((DarkBlade) game).toggleShadowRendering();
            }
        });
        
        /**
         *  Damage Counters Rendering
         */
        final VisCheckBox damageCountersCheckBox = new VisCheckBox("");
//        damageCountersCheckBox.setChecked(DarkBlade.DAMAGE_COUNTERS_RENDERING);
        
        damageCountersCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
//                ((DarkBlade) game).toggleDamageCountersRendering();
            }
        });
        
        /**
         * Debug Physics Checkbox
         */
        final VisCheckBox debugPhysicsCheckBox = new VisCheckBox("");
//        debugPhysicsCheckBox.setChecked(DarkBlade.DEBUG_PHYSICS_RENDERING);
        
        debugPhysicsCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
//                ((DarkBlade) game).togglePhysicsDebugRendering();
            }
        });
        
        /**
         * Debug Path Finding Checkbox
         */
        final VisCheckBox debugPathFindingCheckBox = new VisCheckBox("");
//        debugPathFindingCheckBox.setChecked(DarkBlade.DEBUG_PATH_FINDING);
        
        debugPathFindingCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
//                ((DarkBlade) game).togglePathFindingDebug();
            }
        });
        
        /**
         * Debug Sight Checkbox
         */
        final VisCheckBox debugSightCheckBox = new VisCheckBox("");
//        debugSightCheckBox.setChecked(DarkBlade.DEBUG_SIGHT);
        
        debugSightCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
//                ((DarkBlade) game).toggleDebugSight();
            }
        });
        
        /**
         * Table Debug Checkbox
         */
        final VisCheckBox tableDebugCheckBox = new VisCheckBox("");
//        tableDebugCheckBox.setChecked(DarkBlade.DEBUG_TABLES_ENABLED);
        
        final VisLabel tableDebugLabel = new VisLabel(LanguageModLoader.getValue("debug_tables"));
        
        tableDebugCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
                // Toggle Debug User Preference
//                ((DarkBlade) game).toggleSettingsTableDebug();

                // Update Debug State
//                stage.setDebugAll(DarkBlade.DEBUG_TABLES_ENABLED);
            }
        });
        
        /**
         * Add Components to Graphic Settings Panel
         */
        graphicsSettings.add(graphicsSettingsLabel).pad(UiTheme.space("xs")).colspan(10).expandX().align(Align.center);
        
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Resolution
        graphicsSettings.add(resolutionLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(availableResolutionsSelectBox).pad(UiTheme.space("xs")).fill();
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Full Screen
        graphicsSettings.add(fullScreenLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(fullScreenCheckBox).pad(UiTheme.space("none")).align(Align.right);
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);

        // UI scale
        graphicsSettings.add(uiScaleLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(uiScaleSelectBox).pad(UiTheme.space("xs")).fill();
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);

        // Day-Night Cycle Rendering
        graphicsSettings.add(dayNightTintLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(dayNightTintCheckBox).pad(UiTheme.space("none")).align(Align.right);
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Shadows Rendering
        graphicsSettings.add(shadowsLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(shadowsCheckBox).pad(UiTheme.space("none")).align(Align.right);
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Damage Counters Rendering
        graphicsSettings.add(damageCountersLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(damageCountersCheckBox).pad(UiTheme.space("none")).align(Align.right);
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Debug Physics Label
        graphicsSettings.add(debugPhysicsLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(debugPhysicsCheckBox).pad(UiTheme.space("none")).align(Align.right);
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Debug Path Finding
        graphicsSettings.add(debugPathFindingLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(debugPathFindingCheckBox).pad(UiTheme.space("none")).align(Align.right);
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Debug Sight
        graphicsSettings.add(debugSightLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(debugSightCheckBox).pad(UiTheme.space("none")).align(Align.right);
        graphicsSettings.row().pad(UiTheme.space("xs")).expandX().align(Align.center);
        
        // Table Debug
        graphicsSettings.add(tableDebugLabel).pad(UiTheme.space("xs")).fill().align(Align.center);
        graphicsSettings.add(tableDebugCheckBox).pad(UiTheme.space("none")).align(Align.right);
        
        graphicsSettings.row().fill();

        // Gameplay Settings Panel
        gameplaySettings.add(gameplaySettingsLabel).pad(UiTheme.space("xs"));
        
        // 5 Panel Layout (added Debug Logging)
        // the panels scroll (the logging list is taller than 720 px; at 200% they are also wider than the window)
        // so the Back button below stays on screen (D18)
        VisTable panels = new VisTable();
        panels.add(graphicsSettings).pad(UiTheme.space("sm")).align(Align.top);
        panels.add(soundSettings).pad(UiTheme.space("sm")).align(Align.top);
        panels.add(gameplaySettings).pad(UiTheme.space("sm")).align(Align.top);
        panels.add(modSettings).pad(UiTheme.space("sm")).align(Align.top);
        panels.add(debugLoggingSettings).pad(UiTheme.space("sm")).align(Align.top);
        VisScrollPane panelsScroll = new com.rndmodgames.components.AutoFocusScrollPane(panels);
        panelsScroll.setFadeScrollBars(false);
        panelsScroll.setScrollingDisabled(false, false);
        panelsScroll.setOverscroll(false, false);
        mainContainer.add(panelsScroll).grow();

        // Back: to the main menu, or back to the game when opened in-game
        final VisTextButton exitButton = returnScreen == Futtoboru.MENU_SCREEN
                ? new MainMenuButton(game)
                : new VisTextButton(LanguageModLoader.getValue("back"));
        if (returnScreen != Futtoboru.MENU_SCREEN) {
            exitButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    goBack();
                }
            });
        }

        // Esc does the same as the Back / Main Menu button
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Keys.ESCAPE) {
                    goBack();
                    return true;
                }
                return false;
            }
        });
        
        //
        mainContainer.row();
        
        // Add the Quit to Main Menu button NOTE: weird colspan needed to center, might be easy to fix
        mainContainer.add(exitButton).pad(UiTheme.space("xs"));

        // Set debug for Stage and recursive
//        stage.setDebugAll(DarkBlade.DEBUG_TABLES_ENABLED);
        
        // Add Settings Screen Main Container to Stage
        stage.addActor(mainContainer);
    }
    
    /** "Auto (200%)": the Auto entry with the scale Auto picks for this monitor and window now */
    static String autoScaleItem() {
        return LanguageModLoader.getValue("ui_scale_auto") + " (" + UiScale.formatPercent(UiScale.currentAutoScale()) + ")";
    }

    /** the select box row of a stored setting: 0 = Auto, then {@link UiScale#PRESET_SCALES} */
    static int uiScaleIndex(float setting) {
        if (UiScale.isAuto(setting)) {
            return 0;
        }
        for (int i = 0; i < UiScale.PRESET_SCALES.length; i++) {
            if (UiScale.PRESET_SCALES[i] == UiScale.normalize(setting)) {
                return i + 1;
            }
        }
        return 0;
    }

    /** Leaves Settings for the screen it was opened from (after the current frame's input is handled). */
    private void goBack() {
        if (leaving) {
            return;
        }
        leaving = true;
        Gdx.app.postRunnable(() -> ((Futtoboru) game).changeScreen(returnScreen));
    }

    @Override
    public void show() {
        
        // Add input capabilities
        Gdx.input.setInputProcessor(stage);
        
        // Refresh debug logging settings to ensure UI matches current preferences
        if (debugLoggingSettings != null) {
            debugLoggingSettings.refreshSettings();
        }
    }

    @Override
    public void render(float delta) {
        
        // Clear blit
        UiTheme.clearScreen();
        
        // Draw
        stage.act();
        stage.draw();   
    }

    @Override
    public void resize(int width, int height) {
        
        // Update viewport on resize to keep event coordinate for buttons/widgets (and the UI scale, U-03a)
        UiScale.updateStageViewport(stage, width, height);

        // the window changed: Auto may pick another scale
        if (uiScaleSelectBox != null && !autoScaleItem().equals(uiScaleSelectBox.getItems().first())) {
            String[] items = uiScaleSelectBox.getItems().toArray(String.class);
            int selected = uiScaleSelectBox.getSelectedIndex();
            items[0] = autoScaleItem();
            uiScaleSelectBox.setItems(items);
            uiScaleSelectBox.setSelectedIndex(selected);
        }
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

        Log.info(Log.SYSTEM_CONFIG, "HIDE WAS CALLED ON SETTINGS SCREEN - DISPOSE");
        
        // Hide will be called after switching to a separate screen
        this.dispose();
    }

    @Override
    public void dispose() {
        
        // TODO: required?
        stage.dispose();
    }
}