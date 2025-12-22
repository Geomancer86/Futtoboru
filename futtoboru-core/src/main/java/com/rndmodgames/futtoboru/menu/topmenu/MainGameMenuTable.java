package com.rndmodgames.futtoboru.menu.topmenu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Timer;
import com.kotcrab.vis.ui.widget.BusyBar;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.dialogs.SaveGameDialog;
import com.rndmodgames.futtoboru.engine.FuttoboruGameEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DebugLogManager;
import com.rndmodgames.futtoboru.tables.widgets.CurrentDateAndTimeWidget;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Top Game Menu Table
 * 
 * TODO:
 * 
 * @author Geomancer86
 */
public class MainGameMenuTable extends VisTable {

    //
    Game game;
    
    // Main Menu Manager reference to switch screens / etc
    private MainMenuManager mainMenuManager = null;
    
    /**
     * Dialogs
     */
    private SaveGameDialog saveGameDialog = null;
    
    /**
     * Dynamic Main Game Menu table
     */
    private VisTable dynamicMainGameMenuTable = null;   
    
    /**
     * Screen Components
     */
    VisTable mainButtonContainer = new VisTable(true);
    VisTextButton continueGameButton = new VisTextButton(LanguageModLoader.getValue("continue_game"));
    VisTextButton matchPreviewButton = new VisTextButton(LanguageModLoader.getValue("match_preview"));
    VisTextButton matchResultButton = new VisTextButton(LanguageModLoader.getValue("match_result"));
    VisTextButton drawButton = new VisTextButton("INBOX -> DRAW");  // Mandatory draw button
    BusyBar processingBusyBar = new BusyBar();  // Progress indicator during game processing
    
    /**
     * Processing state flag to prevent double-clicks
     */
    private volatile boolean isProcessing = false;
    
    /**
     * Timestamp of last processing start to enforce cooldown period
     */
    private volatile long lastProcessingStartTime = 0;
    
    /**
     * Minimum cooldown period in milliseconds between processing cycles
     * Prevents rapid triple-clicks from slipping through
     */
    private static final long PROCESSING_COOLDOWN_MS = 200;
    
    /**
     * Fail-safe timeout in milliseconds
     * If processing takes longer than this, we assume it's stuck and reset the state
     */
    private static final long PROCESSING_FAILSAFE_TIMEOUT_MS = 15000; // 15 seconds
    
    /**
     * Timer task for failsafe reset
     */
    private Timer.Task failsafeTask = null;
    
    /**
     * Reference to the continue button's InputListener
     * We keep this listener attached but check flags inside it
     */
    private InputListener continueButtonListener = null;
    
    CurrentDateAndTimeWidget dateTimeWidget = null;
    
    public MainGameMenuTable(Game game, Stage stage) {
        
        super(true);
        pad(5);

        //
        this.game = game;
        
        /**
         * Open Options Drop Down Menu Button/SelectBox -
         */
        final VisSelectBox<String> gameSettingsSelectBox = new VisSelectBox<>();
        
        gameSettingsSelectBox.setItems(LanguageModLoader.getValue("options"),
                                       LanguageModLoader.getValue("save_game"),
                                       LanguageModLoader.getValue("load_game"),
                                       LanguageModLoader.getValue("settings"),
                                       LanguageModLoader.getValue("quit_to_menu"),
                                       LanguageModLoader.getValue("quit_to_desktop"));
        
        gameSettingsSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                /**
                 * Available Game Settings - Save Game v1
                 */
                int selectedOptionIndex = gameSettingsSelectBox.getSelectedIndex();
                
                switch(selectedOptionIndex) {
                
                case 0:
                    // ignore, main options button
                    break;
                
                case 1:
                    /**
                     * Show Save Game Dialog Window
                     */
                    if (saveGameDialog == null) {
                        
                        /**
                         * Pass the reference to the Continue Game button so we can enable that after saving the game once
                         */
                        saveGameDialog = new SaveGameDialog(((Futtoboru)(game)).getCurrentGame(), continueGameButton);  
                    } 

                    // 
                    saveGameDialog.show(stage);
                    break;
                
                default:
                    DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "OPTION NOT IMPLEMENTED!");
                    break;
                }
                
                /**
                 * Clear the selection after click
                 */
                gameSettingsSelectBox.setSelectedIndex(0);
            }
        });
        
        /**
         * Current Date & Time Widget
         */
        dateTimeWidget = new CurrentDateAndTimeWidget(game);

        /**
         * Continue Game Button
         * 
         * CRITICAL: We keep the listener attached and use atomic checks on isProcessing.
         * This prevents the "stuck button" bug where the listener is removed but never re-added.
         */
        continueButtonListener = new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Check if already processing (defense in depth)
                if (isProcessing || continueGameButton.isDisabled()) {
                    return false; // Ignore clicks while processing or disabled
                }
                
                synchronized (MainGameMenuTable.this) {
                    // Double-check inside synchronized block
                    if (isProcessing) {
                        return false;
                    }
                    
                    // Cooldown check
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastProcessingStartTime < PROCESSING_COOLDOWN_MS) {
                        return false;
                    }
                    
                    // Mark as processing IMMEDIATELY
                    isProcessing = true;
                    lastProcessingStartTime = currentTime;
                    
                    // UI Feedback
                    continueGameButton.setDisabled(true);
                    
                    // Start failsafe timer
                    startFailsafeTimer();
                    
                    return true;
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // Verify button is still pressed (user didn't drag away)
                if (!continueGameButton.isPressed()) {
                    resetProcessingState("User dragged away");
                    return;
                }
                
                // All checks passed - proceed with game continuation
                continueGame();
            }
        };
        
        // Add the listener once and keep it
        continueGameButton.addCaptureListener(continueButtonListener);
        
        /**
         * Match Preview Button
         */
        matchPreviewButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                matchPreview();
            }
        });
        
        /**
         * Match Result Button
         */
        matchResultButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                matchResult();
            }
        });
        
        // dynamic main button
        setMainContainerButton();
        
        // dynamic menu table
        dynamicMainGameMenuTable = new VisTable(true);

        add(dynamicMainGameMenuTable).expandX().left();
        
        // Settings SelectBox & Continue Game Button
        VisTable rightMenu = new VisTable(true);
        
        // Container for button and progress bar (stacked vertically)
        VisTable buttonAndProgressContainer = new VisTable(true);
        buttonAndProgressContainer.add(mainButtonContainer);
        buttonAndProgressContainer.row();
        
        // Add BusyBar directly below button (hidden by default)
        processingBusyBar.setVisible(false);
        processingBusyBar.setWidth(150);
        buttonAndProgressContainer.add(processingBusyBar).width(150).height(4).padTop(2);
        
        rightMenu.add(gameSettingsSelectBox).right();
        rightMenu.add(dateTimeWidget).width(100).right();
        rightMenu.add(buttonAndProgressContainer).right();
        
        //
        add(rightMenu).expandX().right();
    }
    
    /**
     * Sets the Main Button depending on the state of the game
     */
    public void setMainContainerButton() {

        // get next action from game engine
        int nextGameAction = ((Futtoboru)(game)).getGameEngine().getNextGameAction();
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "SETTING NEXT GAME ACTION BUTTON: " + nextGameAction);
        
        mainButtonContainer.clear();
        
        switch(nextGameAction) {
        
        //
        case FuttoboruGameEngine.CONTINUE_GAME_ACTION:
            
            // Set continue game button
            // Preserve disabled state if processing
            if (isProcessing) {
                continueGameButton.setDisabled(true);
            } else {
                continueGameButton.setDisabled(false);
            }
            mainButtonContainer.add(continueGameButton);
            
            break;
            
        case FuttoboruGameEngine.MATCH_PREVIEW_ACTION:

            // Set match preview button
            mainButtonContainer.add(matchPreviewButton);
            
            break;
            
        case FuttoboruGameEngine.DRAW_ACTION:
            
            // Set draw button (mandatory - blocks time advancement)
            // Button text: "INBOX -> DRAW"
            drawButton.setDisabled(false);  // Enable button
            
            // Set up draw button listener if not already set
            if (drawButton.getListeners().size == 0) {
                drawButton.addCaptureListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return true;
                    }
                    
                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: INBOX -> DRAW button clicked");
                        
                        if (mainMenuManager != null) {
                            // Find the mandatory draw message and navigate directly to draw screen
                            com.rndmodgames.futtoboru.system.SaveGame currentGame = 
                                ((Futtoboru)(game)).getCurrentGame();
                            
                            if (currentGame != null && currentGame.getAllMessages() != null) {
                                // Find the mandatory LEAGUE_DRAW message
                                for (com.rndmodgames.futtoboru.data.Message message : currentGame.getAllMessages()) {
                                    if (message != null) {
                                        boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                                        boolean isUnread = message.getIsRead() == null || !message.getIsRead();
                                        String messageType = message.getMessageType();
                                        
                                        if (isMandatory && isUnread && 
                                            messageType != null && 
                                            (messageType.equals("LEAGUE_DRAW") || 
                                             messageType.equals("CUP_DRAW") ||
                                             messageType.equals("FIXTURE_DRAW"))) {
                                            
                                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: Found mandatory draw message: " + message.getTitle());
                                            
                                            // Get league ID from actionData
                                            if (message.getActionData() instanceof Long) {
                                                Long leagueId = (Long) message.getActionData();
                                                DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: League ID from actionData: " + leagueId);
                                                
                                                // Find league in SaveGame
                                                com.rndmodgames.futtoboru.data.League league = null;
                                                if (currentGame.getMainAuthority() != null && 
                                                    currentGame.getMainAuthority().getLeagues() != null) {
                                                    for (com.rndmodgames.futtoboru.data.League l : 
                                                         currentGame.getMainAuthority().getLeagues()) {
                                                        if (l != null && l.getId() != null && l.getId().equals(leagueId)) {
                                                            league = l;
                                                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: Found league: " + league.getName());
                                                            break;
                                                        }
                                                    }
                                                }
                                                
                                                if (league != null) {
                                                    // Set selected league and navigate directly to draw screen
                                                    mainMenuManager.setSelectedLeague(league);
                                                    mainMenuManager.setActiveMainScreen(MainMenuManager.LEAGUE_DRAW_SCREEN);
                                                    DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: Navigating directly to league draw screen");
                                                } else {
                                                    DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: ERROR - League not found, falling back to inbox");
                                                    // Fallback: navigate to inbox and select message
                                                    mainMenuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                                                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                                        @Override
                                                        public void run() {
                                                            com.rndmodgames.futtoboru.tables.inbox.InboxScreenTable inboxTable = 
                                                                mainMenuManager.getInboxScreenTable();
                                                            if (inboxTable != null) {
                                                                inboxTable.selectMessageByType(messageType);
                                                            }
                                                        }
                                                    }, 0.1f);
                                                }
                                            } else {
                                                DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: ERROR - actionData is not a Long, falling back to inbox");
                                                // Fallback: navigate to inbox and select message
                                                mainMenuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                                                com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                                                    @Override
                                                    public void run() {
                                                        com.rndmodgames.futtoboru.tables.inbox.InboxScreenTable inboxTable = 
                                                            mainMenuManager.getInboxScreenTable();
                                                        if (inboxTable != null) {
                                                            inboxTable.selectMessageByType(messageType);
                                                        }
                                                    }
                                                }, 0.1f);
                                            }
                                            
                                            return; // Found and handled the message
                                        } else if (isMandatory && isUnread && "CUP_DRAW".equals(messageType)) {
                                            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: Found mandatory cup draw message");
                                            mainMenuManager.setActiveMainScreen(MainMenuManager.CUP_DRAW_SCREEN);
                                            return;
                                        }
                                    }
                                }
                                
                                DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: WARNING - No mandatory draw message found, navigating to inbox");
                                // No mandatory draw message found, just go to inbox
                                mainMenuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                            } else {
                                DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: ERROR - currentGame or getAllMessages is null");
                            }
                        } else {
                            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable: ERROR - mainMenuManager is null");
                        }
                    }
                });
            }
            
            mainButtonContainer.add(drawButton);
            
            break;
            
        default:
            DebugLogManager.getInstance().warn(DebugLogManager.CATEGORY_UI_MENU, "GAME ACTION " + nextGameAction + " NOT IMPLEMENTED!");
        }
    }
    
    /**
     * 
     */
    public void matchResult() {
       
        //
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "LOADING MATCH RESULT SCREEN!");
        
        // Simulate Match and Advance Time
        ((Futtoboru)(game)).getGameEngine().getMatchResult();
        
        mainMenuManager.setActiveMainScreen(MainMenuManager.MATCH_RESULT_SCREEN);
        
        mainButtonContainer.clear();
        mainButtonContainer.add(continueGameButton);
        
        // TODO: after a match the continue game button should take the player back to a screen
    }
    
    /**
     * 
     */
    public void matchPreview() {
        
        //
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "LOADING MATCH PREVIEW SCREEN!");
        
        mainMenuManager.setActiveMainScreen(MainMenuManager.MATCH_PREVIEW_SCREEN);
        
        /**
         * TODO: RESET BUTTON TO MATCH RESULT
         *  - If the player goes back to any other screen, the BUTTON should be reset back to MATCH PREVIEW because
         *      there is no other way to go to the screen for now, no buttons to get there clicking on a match list
         */
        mainButtonContainer.clear();
        mainButtonContainer.add(matchResultButton);
    }
    
    /**
     * Continue game - advances time by one day
     * Prevents double-clicks and shows progress indicator during processing
     */
    private void continueGame() {
        
        // Defense in depth: Verify flag is still set
        if (!isProcessing) {
            DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable", "continueGame() called but isProcessing is false!");
            setMainContainerButton();
            return;
        }
        
        // Show BusyBar (animates automatically when visible)
        processingBusyBar.setVisible(true);
        
        DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable", "Starting game engine processing...");
        
        // Schedule the actual processing to happen after a short delay
        // This gives the UI one frame to render the disabled button and busy bar
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                try {
                    // Continue the game on the Game Engine (this blocks the thread)
                    try {
                        ((Futtoboru)(game)).getGameEngine().continueGame();
                    } catch (Throwable t) {
                        DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable", "CRITICAL ERROR in continueGame()", t);
                    }
                    
                    // After processing completes, update UI on main thread
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // Update the dynamic date widget
                            dateTimeWidget.updateDynamicComponents();
                            
                            // Reset state
                            resetProcessingState("Processing complete");
                            
                            // Handle screen transitions
                            if (MainMenuManager.CURRENT_SCREEN == MainMenuManager.MATCH_RESULT_SCREEN) {
                                mainMenuManager.setActiveMainScreen(MainMenuManager.BEFORE_MATCH_SCREEN);
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable", "Error during continueGame() timer execution", e);
                    Gdx.app.postRunnable(() -> resetProcessingState("Error during processing: " + e.getMessage()));
                }
            }
        }, 0.05f);
    }

    /**
     * Starts a fail-safe timer to reset the button state if processing takes too long
     */
    private void startFailsafeTimer() {
        if (failsafeTask != null) {
            failsafeTask.cancel();
        }
        
        failsafeTask = new Timer.Task() {
            @Override
            public void run() {
                if (isProcessing) {
                    DebugLogManager.getInstance().error(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable", "FAIL-SAFE TRIGGERED: Processing took too long (" + PROCESSING_FAILSAFE_TIMEOUT_MS + "ms). Resetting state.");
                    Gdx.app.postRunnable(() -> resetProcessingState("Fail-safe timeout"));
                }
            }
        };
        
        Timer.schedule(failsafeTask, PROCESSING_FAILSAFE_TIMEOUT_MS / 1000f);
    }

    /**
     * Resets the processing state and re-enables the UI
     * This is the single place where isProcessing is set back to false
     */
    private void resetProcessingState(String reason) {
        synchronized (MainGameMenuTable.this) {
            if (!isProcessing) return;
            
            DebugLogManager.getInstance().log(DebugLogManager.CATEGORY_UI_MENU, "MainGameMenuTable", "Resetting processing state. Reason: " + reason);
            
            isProcessing = false;
            
            if (failsafeTask != null) {
                failsafeTask.cancel();
                failsafeTask = null;
            }
            
            // Hide progress indicator
            processingBusyBar.setVisible(false);
            
            // Refresh main container (this will re-add and enable the correct button)
            setMainContainerButton();
            
            // Explicitly ensure the continue button is enabled if it's the current action
            if (continueGameButton != null) {
                continueGameButton.setDisabled(false);
            }
        }
    }

    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }
}