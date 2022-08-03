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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.components.MainMenuButton;
import com.rndmodgames.darkblade.screens.tables.ModsSettingsTable;
import com.rndmodgames.darkblade.screens.tables.SoundSettingsTable;
import com.rndmodgames.futtoboru.game.Futtoboru;
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

	/**
	 * Settings Screen
	 * 
	 * @param parent
	 */
	public SettingsScreen(Game game) {
		
this.game = game;
        
        stage = new Stage(new ScreenViewport());
        
        VisTable mainContainer = new VisTable();

        // TODO: padding relative to resolution so stays relatively centered
        mainContainer.pad(50).setFillParent(true);

        VisTable graphicsSettings = new VisTable();
        VisTable soundSettings = new SoundSettingsTable(this.game);
        VisTable gameplaySettings = new VisTable();
        VisTable modSettings = new ModsSettingsTable(this.game);
        
        VisLabel graphicsSettingsLabel = new VisLabel(LanguageModLoader.getValue("graphic_settings"));
        VisLabel resolutionLabel = new VisLabel(LanguageModLoader.getValue("resolution"));
        VisLabel fullScreenLabel = new VisLabel(LanguageModLoader.getValue("fullscreen"));
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
        
        // 4 Panel Layout
        mainContainer.row().colspan(4).expandX().fillX();
        
        // 
        mainContainer.add(graphicsSettings).pad(10).align(Align.top);
        mainContainer.add(soundSettings).pad(10).align(Align.top);
        mainContainer.add(gameplaySettings).pad(10).align(Align.top);
        mainContainer.add(modSettings).pad(10).align(Align.top);

        // Main Menu Button
        final MainMenuButton exitButton = new MainMenuButton(game);
        
        //
        mainContainer.row();
        
        // Add the Quit to Main Menu button NOTE: weird colspan needed to center, might be easy to fix
        mainContainer.add(exitButton).colspan(16).pad(5);

        // Set debug for Stage and recursive
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