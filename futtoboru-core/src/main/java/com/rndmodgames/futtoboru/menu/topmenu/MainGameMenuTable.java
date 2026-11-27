package com.rndmodgames.futtoboru.menu.topmenu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

import java.util.function.BooleanSupplier;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

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
     * True from the moment a Continue click is accepted until the day advance has finished and the UI
     * has been refreshed. Only touched on the render thread (input, libGDX Timer tasks and posted
     * runnables all run there), so no volatile/synchronized is needed.
     */
    private boolean isProcessing = false;
    
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
     * The continue button's click listener (attached once, for the lifetime of the table)
     */
    private ContinueClickListener continueButtonListener = null;
    
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
                    Log.info(Log.UI_MENU, "OPTION NOT IMPLEMENTED!");
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
         * One listener, attached once and kept. It fires only on a completed click (press and release
         * over the button, so dragging away cancels), ignores clicks while a day advance is in progress
         * or the button is disabled, and swallows the second click of a double-click (P2-1).
         * See ContinueClickListener.
         */
        continueButtonListener = new ContinueClickListener(
                () -> isProcessing || continueGameButton.isDisabled(),
                this::startContinueGame);
        continueGameButton.addListener(continueButtonListener);
        
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
        processingBusyBar.setWidth(UiTheme.size("busybar.w"));
        buttonAndProgressContainer.add(processingBusyBar).width(UiTheme.size("busybar.w")).height(UiTheme.size("busybar.h")).padTop(UiTheme.space("xxs"));
        
        rightMenu.add(gameSettingsSelectBox).right();
        rightMenu.add(dateTimeWidget).width(UiTheme.size("widget.date")).right();
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
        
        Log.info(Log.UI_MENU, "SETTING NEXT GAME ACTION BUTTON: " + nextGameAction);
        
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
                        Log.info(Log.UI_MENU, "MainGameMenuTable: INBOX -> DRAW button clicked");
                        
                        if (mainMenuManager != null) {
                            // Find the mandatory draw message and navigate to draw screen
                            // Check both getAllMessages() (already delivered) and scheduledMessages (not yet delivered)
                            com.rndmodgames.futtoboru.system.SaveGame currentGame = 
                                ((Futtoboru)(game)).getCurrentGame();
                            
                            com.rndmodgames.futtoboru.data.Message messageToProcess = null;
                            java.time.LocalDateTime currentDate = currentGame != null ? currentGame.getGameDate() : null;
                            
                            // First check getAllMessages() for already delivered messages
                            if (currentGame != null && currentGame.getAllMessages() != null) {
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
                                            messageToProcess = message;
                                            Log.info(Log.UI_MENU, "MainGameMenuTable: Found mandatory draw message in getAllMessages: " + message.getTitle());
                                            break;
                                        }
                                    }
                                }
                            }
                            
                            // If not found in getAllMessages(), check scheduledMessages for ANY mandatory draw messages
                            // When user clicks INBOX->DRAW button, we deliver the message regardless of scheduled date
                            // This allows the user to perform the draw even if the message was scheduled for a future date
                            if (messageToProcess == null && currentGame != null && currentGame.getScheduledMessages() != null) {
                                for (com.rndmodgames.futtoboru.data.Message message : currentGame.getScheduledMessages()) {
                                    if (message != null) {
                                        boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                                        String messageType = message.getMessageType();
                                        
                                        // Deliver ANY mandatory draw message when button is clicked, regardless of scheduled date
                                        // This ensures the user can access the draw screen when the button is shown
                                        if (isMandatory && 
                                            messageType != null && 
                                            (messageType.equals("LEAGUE_DRAW") || 
                                             messageType.equals("CUP_DRAW") ||
                                             messageType.equals("FIXTURE_DRAW"))) {
                                            // Message is scheduled but not yet delivered - deliver it now so user can access draw screen
                                            if (((Futtoboru)(game)).getGameEngine() != null &&
                                                ((Futtoboru)(game)).getGameEngine().getMessageManager() != null) {
                                                Log.info(Log.UI_MENU, "MainGameMenuTable: Found scheduled mandatory draw message, delivering: " + message.getTitle() + 
                                                    " (scheduled: " + message.getScheduledDate() + ", current: " + currentDate + ")");
                                                ((Futtoboru)(game)).getGameEngine().getMessageManager().deliverMessage(message);
                                                messageToProcess = message;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Process the message if found
                            if (messageToProcess != null) {
                                String messageType = messageToProcess.getMessageType();
                                
                                Log.info(Log.UI_MENU, "MainGameMenuTable: Found mandatory draw message: " + messageToProcess.getTitle());
                                
                                // Handle CUP_DRAW separately
                                if ("CUP_DRAW".equals(messageType)) {
                                    Log.info(Log.UI_MENU, "MainGameMenuTable: Found mandatory cup draw message");
                                    mainMenuManager.setActiveMainScreen(MainMenuManager.CUP_DRAW_SCREEN);
                                    return;
                                }
                                
                                // Handle LEAGUE_DRAW and FIXTURE_DRAW
                                if ("LEAGUE_DRAW".equals(messageType) || "FIXTURE_DRAW".equals(messageType)) {
                                    // Get league ID from actionData
                                    if (messageToProcess.getActionData() instanceof Long) {
                                        Long leagueId = (Long) messageToProcess.getActionData();
                                        Log.info(Log.UI_MENU, "MainGameMenuTable: League ID from actionData: " + leagueId);
                                        
                                        // Find league in SaveGame
                                        com.rndmodgames.futtoboru.data.League league = null;
                                        if (currentGame != null && currentGame.getMainAuthority() != null && 
                                            currentGame.getMainAuthority().getLeagues() != null) {
                                            for (com.rndmodgames.futtoboru.data.League l : 
                                                 currentGame.getMainAuthority().getLeagues()) {
                                                if (l != null && l.getId() != null && l.getId().equals(leagueId)) {
                                                    league = l;
                                                    Log.info(Log.UI_MENU, "MainGameMenuTable: Found league: " + league.getName());
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        if (league != null) {
                                            // Set selected league and navigate directly to draw screen
                                            mainMenuManager.setSelectedLeague(league);
                                            mainMenuManager.setActiveMainScreen(MainMenuManager.LEAGUE_DRAW_SCREEN);
                                            Log.info(Log.UI_MENU, "MainGameMenuTable: Navigating directly to league draw screen");
                                            return;
                                        } else {
                                            Log.warn(Log.UI_MENU, "MainGameMenuTable: ERROR - League not found, falling back to inbox");
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
                                            return;
                                        }
                                    } else {
                                        Log.warn(Log.UI_MENU, "MainGameMenuTable: ERROR - actionData is not a Long, falling back to inbox");
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
                                        return;
                                    }
                                }
                            }
                            
                            // No mandatory draw message found
                            Log.warn(Log.UI_MENU, "MainGameMenuTable: WARNING - No mandatory draw message found, navigating to inbox");
                            mainMenuManager.setActiveMainScreen(MainMenuManager.INBOX_SCREEN);
                        } else {
                            Log.error(Log.UI_MENU, "MainGameMenuTable: ERROR - mainMenuManager is null");
                        }
                    }
                });
            }
            
            mainButtonContainer.add(drawButton);
            
            break;
            
        default:
            Log.warn(Log.UI_MENU, "GAME ACTION " + nextGameAction + " NOT IMPLEMENTED!");
        }
    }
    
    /**
     * 
     */
    public void matchResult() {
       
        //
        Log.info(Log.UI_MENU, "LOADING MATCH RESULT SCREEN!");
        
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
        Log.info(Log.UI_MENU, "LOADING MATCH PREVIEW SCREEN!");
        
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
     * Called by the continue button listener for an accepted click: marks the advance as in progress
     * (later clicks are ignored until resetProcessingState()) and starts it.
     */
    private void startContinueGame() {
        isProcessing = true;
        continueGameButton.setDisabled(true);
        startFailsafeTimer();
        continueGame();
    }

    /**
     * Continue game - advances time by one day.
     * Shows the progress indicator during processing; isProcessing is set by startContinueGame().
     */
    private void continueGame() {

        // Show BusyBar (animates automatically when visible)
        processingBusyBar.setVisible(true);
        
        Log.info(Log.UI_MENU, "MainGameMenuTable", "Starting game engine processing...");
        
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
                        Log.error(Log.UI_MENU, "MainGameMenuTable", "CRITICAL ERROR in continueGame()", t);
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
                    Log.error(Log.UI_MENU, "MainGameMenuTable", "Error during continueGame() timer execution", e);
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
                    Log.error(Log.UI_MENU, "MainGameMenuTable", "FAIL-SAFE TRIGGERED: Processing took too long (" + PROCESSING_FAILSAFE_TIMEOUT_MS + "ms). Resetting state.");
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
        if (!isProcessing) return;

        Log.info(Log.UI_MENU, "MainGameMenuTable", "Resetting processing state. Reason: " + reason);

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

    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }

    /**
     * Click listener for the Continue button (P2-1: Continue sometimes advanced two days).
     *
     * Root cause of P2-1: the old listener guarded with an in-progress flag plus a 200 ms cooldown
     * measured from the first press. A day advance is short (0.05 s Timer delay + ~50 ms engine work,
     * then one frame to reset the flag), so when the second press of a double-click arrives (usually
     * 200-400 ms after the first) the flag is already cleared and the cooldown has run out: the second
     * click is not a re-entrant call the flag could catch, it is a fresh click that starts a second
     * advance.
     *
     * Fix: a double-click counts as one Continue. libGDX's ClickListener counts consecutive taps
     * (tapCountInterval, 0.4 s by default, measured between releases); a click with tap count > 1 is
     * the tail of a multi-click and is ignored, then the count is reset so a third quick click is a new
     * Continue (deliberate fast clicking still advances, one day per click pair). Clicks are also
     * ignored while {@code blocked} is true (advance in progress or button disabled).
     */
    static class ContinueClickListener extends ClickListener {

        private final BooleanSupplier blocked;
        private final Runnable onContinue;

        ContinueClickListener(BooleanSupplier blocked, Runnable onContinue) {
            this.blocked = blocked;
            this.onContinue = onContinue;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {

            if (getTapCount() > 1) {
                // second click of a double-click: swallow it and start counting afresh
                setTapCount(0);
                return;
            }

            if (blocked.getAsBoolean()) {
                return;
            }

            onContinue.run();
        }
    }
}