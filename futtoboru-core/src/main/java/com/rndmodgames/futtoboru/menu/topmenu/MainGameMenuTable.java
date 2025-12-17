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
    private static final long PROCESSING_COOLDOWN_MS = 100;
    
    /**
     * Reference to the continue button's InputListener
     * We remove this listener during processing to prevent queued events from firing
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
                    System.out.println("OPTION NOT IMPLEMENTED!");
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
         * CRITICAL: We store the listener reference so we can remove it during processing
         * This prevents queued events from firing even if they were already in the event queue
         */
        continueButtonListener = new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // ABSOLUTE FIRST CHECK: If button is disabled, DO NOTHING - return false immediately
                // This MUST be the first check, before ANY other code executes
                if (continueGameButton.isDisabled()) {
                    return false;
                }
                
                // CRITICAL: Use synchronized block to atomically check and set processing flag
                synchronized (MainGameMenuTable.this) {
                    // Double-check disabled state (defense in depth)
                    if (continueGameButton.isDisabled()) {
                        return false;
                    }
                    
                    // Check if already processing (defense in depth)
                    if (isProcessing) {
                        continueGameButton.setDisabled(true);
                        return false; // Block if already processing
                    }
                    
                    // LibGDX Best Practice: Debounce - check minimum time since last click
                    long currentTime = System.currentTimeMillis();
                    long timeSinceLastProcessing = currentTime - lastProcessingStartTime;
                    
                    // Block if cooldown hasn't elapsed
                    if (timeSinceLastProcessing < PROCESSING_COOLDOWN_MS) {
                        continueGameButton.setDisabled(true);
                        return false; // Block during cooldown
                    }
                    
                    // ATOMIC: Set processing flag IMMEDIATELY
                    isProcessing = true;
                    lastProcessingStartTime = currentTime;
                    
                    // CRITICAL: Remove listener IMMEDIATELY to prevent queued events
                    continueGameButton.removeCaptureListener(continueButtonListener);
                    
                    // Disable button
                    continueGameButton.setDisabled(true);
                    
                    return true;
                } // End synchronized - flag is now set, no other thread can set it
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // NOTE: We do NOT check isDisabled() here because touchDown intentionally disables
                // the button to prevent additional clicks, but we still need touchUp to proceed
                // for this specific click to call continueGame()
                
                // Verify button is still pressed (user didn't drag away)
                if (!continueGameButton.isPressed()) {
                    // User dragged away - restore state
                    synchronized (MainGameMenuTable.this) {
                        isProcessing = false;
                        if (!continueGameButton.getCaptureListeners().contains(continueButtonListener, true)) {
                            continueGameButton.addCaptureListener(continueButtonListener);
                        }
                        continueGameButton.setDisabled(false);
                    }
                    return;
                }
                
                // Verify processing flag is still set (should be true from touchDown)
                if (!isProcessing) {
                    // Something went wrong - restore state
                    if (!continueGameButton.getCaptureListeners().contains(continueButtonListener, true)) {
                        continueGameButton.addCaptureListener(continueButtonListener);
                    }
                    continueGameButton.setDisabled(false);
                    return;
                }

                // All checks passed - proceed with game continuation
                continueGame();
            }
        };
        
        // Add the listener to the button
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
        
        System.out.println("SETTING NEXT GAME ACTION BUTTON: " + nextGameAction);
        
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
                        System.out.println("MainGameMenuTable: INBOX -> DRAW button clicked");
                        
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
                                            
                                            System.out.println("MainGameMenuTable: Found mandatory draw message: " + message.getTitle());
                                            
                                            // Get league ID from actionData
                                            if (message.getActionData() instanceof Long) {
                                                Long leagueId = (Long) message.getActionData();
                                                System.out.println("MainGameMenuTable: League ID from actionData: " + leagueId);
                                                
                                                // Find league in SaveGame
                                                com.rndmodgames.futtoboru.data.League league = null;
                                                if (currentGame.getMainAuthority() != null && 
                                                    currentGame.getMainAuthority().getLeagues() != null) {
                                                    for (com.rndmodgames.futtoboru.data.League l : 
                                                         currentGame.getMainAuthority().getLeagues()) {
                                                        if (l != null && l.getId() != null && l.getId().equals(leagueId)) {
                                                            league = l;
                                                            System.out.println("MainGameMenuTable: Found league: " + league.getName());
                                                            break;
                                                        }
                                                    }
                                                }
                                                
                                                if (league != null) {
                                                    // Set selected league and navigate directly to draw screen
                                                    mainMenuManager.setSelectedLeague(league);
                                                    mainMenuManager.setActiveMainScreen(MainMenuManager.LEAGUE_DRAW_SCREEN);
                                                    System.out.println("MainGameMenuTable: Navigating directly to draw screen");
                                                } else {
                                                    System.out.println("MainGameMenuTable: ERROR - League not found, falling back to inbox");
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
                                                System.out.println("MainGameMenuTable: ERROR - actionData is not a Long, falling back to inbox");
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
                                        }
                                    }
                                }
                                
                                System.out.println("MainGameMenuTable: WARNING - No mandatory draw message found, navigating to inbox");
                                // No mandatory draw message found, just go to inbox
                                mainMenuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                            } else {
                                System.out.println("MainGameMenuTable: ERROR - currentGame or getAllMessages is null");
                            }
                        } else {
                            System.out.println("MainGameMenuTable: ERROR - mainMenuManager is null");
                        }
                    }
                });
            }
            
            mainButtonContainer.add(drawButton);
            
            break;
            
        default:
            System.out.println("GAME ACTION " + nextGameAction + " NOT IMPLEMENTED!");
        }
    }
    
    /**
     * 
     */
    public void matchResult() {
       
        //
        System.out.println("LOADING MATCH RESULT SCREEN!");
        
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
        System.out.println("LOADING MATCH PREVIEW SCREEN!");
        
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
     * 
     * Note: The processing flag is already set in touchDown() to prevent race conditions.
     * This method just ensures it's still set (defense in depth).
     * 
     * TODO: BUG - Double-click bug still exists: Rapid double-clicks can still process two days in a row.
     * The listener removal approach and disabled state checks are not fully preventing queued events from firing.
     * Need to investigate LibGDX event queue handling and find a more robust solution.
     */
    private void continueGame() {
        
        // Defense in depth: Verify flag is still set (should always be true at this point)
        // If somehow it's not set, we can't proceed safely
        if (!isProcessing) {
            System.err.println("MainGameMenuTable: WARNING - continueGame() called but isProcessing is false!");
            Gdx.app.error("MainGameMenuTable", "WARNING - continueGame() called but isProcessing is false!");
            continueGameButton.setDisabled(false);
            return;
        }
        
        // Disable button immediately and show progress bar
        continueGameButton.setDisabled(true);
        
        // Show BusyBar (animates automatically when visible)
        processingBusyBar.setVisible(true);
        
        System.out.println("MainGameMenuTable: Button disabled, BusyBar visible, scheduling processing");
        Gdx.app.log("MainGameMenuTable", "Button disabled, BusyBar visible, scheduling processing");
        
        // Schedule the actual processing to happen after a short delay
        // This gives the UI one frame to render the disabled button and busy bar
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                try {
                    System.out.println("MainGameMenuTable: Starting game engine continueGame()");
                    Gdx.app.log("MainGameMenuTable", "Starting game engine continueGame()");
                    
                    // Continue the game on the Game Engine (this blocks the thread)
                    ((Futtoboru)(game)).getGameEngine().continueGame();
                    
                    System.out.println("MainGameMenuTable: Game engine continueGame() completed");
                    Gdx.app.log("MainGameMenuTable", "Game engine continueGame() completed");
                    
                    // After processing completes, update UI
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("MainGameMenuTable: Updating UI after processing");
                            Gdx.app.log("MainGameMenuTable", "Updating UI after processing");
                            
                            // Update the dynamic date widget
                            dateTimeWidget.updateDynamicComponents();
                            
                            // Clear processing flag immediately (cooldown check in touchDown will handle rapid clicks)
                            synchronized (MainGameMenuTable.this) {
                                isProcessing = false;
                                System.out.println("MainGameMenuTable: Processing flag cleared");
                                Gdx.app.log("MainGameMenuTable", "Processing flag cleared");
                            }
                            
                            // Hide progress indicator
                            processingBusyBar.setVisible(false);
                            
                            // CRITICAL: Re-add the listener BEFORE re-enabling the button
                            // This ensures the listener is ready to handle new clicks
                            continueGameButton.addCaptureListener(continueButtonListener);
                            
                            // Update button state first (this may re-add the button to the container)
                            setMainContainerButton();
                            
                            // Then explicitly re-enable the button (after setMainContainerButton may have reset it)
                            continueGameButton.setDisabled(false);
                            
                            System.out.println("MainGameMenuTable: Listener re-added, button re-enabled, BusyBar hidden");
                            Gdx.app.log("MainGameMenuTable", "Listener re-added, button re-enabled, BusyBar hidden");
                            
                            // Handle screen transitions
                            if (MainMenuManager.CURRENT_SCREEN == MainMenuManager.MATCH_RESULT_SCREEN) {
                                mainMenuManager.setActiveMainScreen(MainMenuManager.BEFORE_MATCH_SCREEN);
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    // Always re-enable button and hide progress indicator, even if there's an error
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // Clear processing flag immediately
                            synchronized (MainGameMenuTable.this) {
                                isProcessing = false;
                                System.out.println("MainGameMenuTable: Processing flag cleared after error");
                                Gdx.app.log("MainGameMenuTable", "Processing flag cleared after error");
                            }
                            
                            // Hide progress indicator
                            processingBusyBar.setVisible(false);
                            
                            // CRITICAL: Re-add the listener in case of error (it was removed during processing)
                            continueGameButton.addCaptureListener(continueButtonListener);
                            
                            // Update button state
                            setMainContainerButton();
                            
                            // Explicitly re-enable the button
                            continueGameButton.setDisabled(false);
                            
                            System.err.println("MainGameMenuTable: Error during continueGame(): " + e.getMessage());
                            Gdx.app.error("MainGameMenuTable", "Error during continueGame()", e);
                        }
                    });
                }
            }
        }, 0.05f); // Very short delay to allow one render frame to show disabled state
    }

    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }
}