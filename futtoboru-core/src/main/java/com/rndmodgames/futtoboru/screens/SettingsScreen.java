package com.rndmodgames.futtoboru.screens;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.components.MainMenuButton;
import com.rndmodgames.darkblade.screens.tables.ModsSettingsTable;
import com.rndmodgames.darkblade.screens.tables.SoundSettingsTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.tables.settings.DebugLoggingSettingsTable;
import com.rndmodgames.futtoboru.ui.UiScale;
import com.rndmodgames.localization.LanguageModLoader;

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

	/**
	 * Settings Screen
	 * 
	 * @param parent
	 */
	public SettingsScreen(Game game) {
		
this.game = game;
        
        stage = new Stage(UiScale.newViewport());
        
        VisTable mainContainer = new VisTable();

        // TODO: padding relative to resolution so stays relatively centered
        mainContainer.pad(50);

        VisTable graphicsSettings = new VisTable();
        VisTable soundSettings = new SoundSettingsTable(this.game);
        VisTable gameplaySettings = new VisTable();
        VisTable modSettings = new ModsSettingsTable(this.game);
        this.debugLoggingSettings = new DebugLoggingSettingsTable(this.game);
        
        VisLabel graphicsSettingsLabel = new VisLabel(LanguageModLoader.getValue("graphic_settings"));
        VisLabel resolutionLabel = new VisLabel(LanguageModLoader.getValue("resolution"));
        VisLabel fullScreenLabel = new VisLabel(LanguageModLoader.getValue("fullscreen"));
        VisLabel uiScaleLabel = new VisLabel(LanguageModLoader.getValue("ui_scale"));
        VisLabel dayNightTintLabel = new VisLabel(LanguageModLoader.getValue("day_night_tint"));
        VisLabel damageCountersLabel = new VisLabel(LanguageModLoader.getValue("damage_counters"));
        VisLabel debugPhysicsLabel = new VisLabel(LanguageModLoader.getValue("debug_physics"));
        VisLabel debugPathFindingLabel = new VisLabel(LanguageModLoader.getValue("debug_pathfinding"));
        VisLabel debugSightLabel = new VisLabel(LanguageModLoader.getValue("debug_sight"));
        VisLabel shadowsLabel = new VisLabel(LanguageModLoader.getValue("shadows"));
        
        VisLabel gameplaySettingsLabel = new VisLabel("GAMEPLAY SETTINGS");
        VisLabel modSettingsLabel = new VisLabel("MOD SETTINGS");
        
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
                    
                    if (!line.startsWith("#")) {
                        availableResolutions.add(line);
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
        
        // Set FullScreen to Saved Value
        fullScreenCheckBox.setChecked(Futtoboru.FULLSCREEN);
        fullScreenCheckBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                
                ((Futtoboru) game).toggleFullscreen();
            }
        });

        final VisSelectBox<String> uiScaleSelectBox = new VisSelectBox<>();
        String[] uiScaleItems = new String[UiScale.PRESET_SCALES.length];
        for (int i = 0; i < UiScale.PRESET_SCALES.length; i++) {
            uiScaleItems[i] = UiScale.formatPercent(UiScale.PRESET_SCALES[i]);
        }
        uiScaleSelectBox.setItems(uiScaleItems);
        uiScaleSelectBox.setSelected(UiScale.nearestPresetLabel(Futtoboru.UI_SCALE));
        uiScaleSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float scale = UiScale.parsePercent(uiScaleSelectBox.getSelected());
                if (Math.abs(scale - Futtoboru.UI_SCALE) < 0.001f) {
                    return;
                }
                ((Futtoboru) game).updateUiScale(scale);
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
        graphicsSettings.add(graphicsSettingsLabel).pad(5).colspan(10).expandX().align(Align.center);
        
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Resolution
        graphicsSettings.add(resolutionLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(availableResolutionsSelectBox).pad(5).fill();
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Full Screen
        graphicsSettings.add(fullScreenLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(fullScreenCheckBox).pad(0).align(Align.right);
        graphicsSettings.row().pad(5).expandX().align(Align.center);

        graphicsSettings.add(uiScaleLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(uiScaleSelectBox).pad(5).fill();
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Day-Night Cycle Rendering
        graphicsSettings.add(dayNightTintLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(dayNightTintCheckBox).pad(0).align(Align.right);
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Shadows Rendering
        graphicsSettings.add(shadowsLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(shadowsCheckBox).pad(0).align(Align.right);
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Damage Counters Rendering
        graphicsSettings.add(damageCountersLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(damageCountersCheckBox).pad(0).align(Align.right);
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Debug Physics Label
        graphicsSettings.add(debugPhysicsLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(debugPhysicsCheckBox).pad(0).align(Align.right);
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Debug Path Finding
        graphicsSettings.add(debugPathFindingLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(debugPathFindingCheckBox).pad(0).align(Align.right);
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Debug Sight
        graphicsSettings.add(debugSightLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(debugSightCheckBox).pad(0).align(Align.right);
        graphicsSettings.row().pad(5).expandX().align(Align.center);
        
        // Table Debug
        graphicsSettings.add(tableDebugLabel).pad(5).fill().align(Align.center);
        graphicsSettings.add(tableDebugCheckBox).pad(0).align(Align.right);
        
        graphicsSettings.row().fill();

        // Gameplay Settings Panel
        gameplaySettings.add(gameplaySettingsLabel).pad(5);
        
        // Stack panels vertically so 150–200% UI scale does not clip the five-column row
        mainContainer.row();
        mainContainer.add(graphicsSettings).pad(10).growX().fillX();
        mainContainer.row();
        mainContainer.add(soundSettings).pad(10).growX().fillX();
        mainContainer.row();
        mainContainer.add(gameplaySettings).pad(10).growX().fillX();
        mainContainer.row();
        mainContainer.add(modSettings).pad(10).growX().fillX();
        mainContainer.row();
        mainContainer.add(debugLoggingSettings).pad(10).growX().fillX();

        // Main Menu Button
        final MainMenuButton exitButton = new MainMenuButton(game);
        
        //
        mainContainer.row();
        
        // Add the Quit to Main Menu button NOTE: weird colspan needed to center, might be easy to fix
        mainContainer.add(exitButton).colspan(20).pad(5);

        // Set debug for Stage and recursive
//        stage.setDebugAll(DarkBlade.DEBUG_TABLES_ENABLED);
        
        // Add Settings Screen Main Container to Stage
        UiScale.addOverflowScroll(stage, mainContainer);
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

        System.out.println("HIDE WAS CALLED ON SETTINGS SCREEN - DISPOSE");
        
        // Hide will be called after switching to a separate screen
        this.dispose();
    }

    @Override
    public void dispose() {
        
        // TODO: required?
        stage.dispose();
    }
}