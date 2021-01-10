package com.rndmodgames.futtoboru.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.dialogs.SaveGameDialog;
import com.rndmodgames.futtoboru.game.Futtuboru;
import com.rndmodgames.futtoboru.screens.main.BTeamScreenTable;
import com.rndmodgames.futtoboru.screens.main.BoardScreenTable;
import com.rndmodgames.futtoboru.screens.main.ClubScreenTable;
import com.rndmodgames.futtoboru.screens.main.CompetitionsScreenTable;
import com.rndmodgames.futtoboru.screens.main.FinancesScreenTable;
import com.rndmodgames.futtoboru.screens.main.HomeScreenTable;
import com.rndmodgames.futtoboru.screens.main.InboxScreenTable;
import com.rndmodgames.futtoboru.screens.main.ScheduleScreenTable;
import com.rndmodgames.futtoboru.screens.main.ScoutingScreenTable;
import com.rndmodgames.futtoboru.screens.main.SquadScreenTable;
import com.rndmodgames.futtoboru.screens.main.StaffScreenTable;
import com.rndmodgames.futtoboru.screens.main.TacticsScreenTable;
import com.rndmodgames.futtoboru.screens.main.TeamReportScreenTable;
import com.rndmodgames.futtoboru.screens.main.TrainingScreenTable;
import com.rndmodgames.futtoboru.screens.main.TransfersScreenTable;
import com.rndmodgames.futtoboru.screens.main.Under18TeamScreenTable;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Main Game Screen v1
 * 
 *  - Day of Week/Time of Day Widget
 *  - Continue Game Button
 *  - Settings Button
 *  
 *  - Main Game Area
 *      - Tab Panels for Main Game Screens
 *      
 *      - Active Main Game Screen
 * 
 * @author Geomancer86
 */
public class MainGameScreen implements Screen {

    Game game;
    Stage stage;
    SaveGame currentGame; // reference for easy access
        
    /**
     * We access to an Initial Unsaved Game Data from Parent (also SAVE/LOAD THROUGH PARENT)
     */
    
    /**
     * Screen Components
     */
    final VisTextButton continueGameButton = new VisTextButton(LanguageModLoader.getValue("continue_game"));
    
    /**
     * Screen Ids
     */
    private static final int HOME_SCREEN         =  1;
    private static final int INBOX_SCREEN        =  2;
    private static final int SQUAD_SCREEN        =  3;
    private static final int SCHEDULE_SCREEN     =  4;
    private static final int COMPETITIONS_SCREEN =  5;
    private static final int UNDER_18_SCREEN     =  6;
    private static final int B_TEAM_SCREEN       =  7;
    private static final int TACTICS_SCREEN      =  8;
    private static final int TEAM_REPORT_SCREEN  =  9;
    private static final int STAFF_SCREEN        = 10;
    private static final int TRAINING_SCREEN     = 11;
    private static final int SCOUTING_SCREEN     = 12;
    private static final int TRANSFERS_SCREEN    = 13;
    private static final int CLUB_SCREEN         = 14;
    private static final int BOARD_SCREEN        = 15;
    private static final int FINANCES_SCREEN     = 16;

    /**
     * Main Screen Tables
     */
    private VisTable mainTable = new VisTable(true);
    private HomeScreenTable homeScreenTable = null;
    private InboxScreenTable inboxScreenTable = null;
    private SquadScreenTable squadScreenTable = null;
    private ScheduleScreenTable scheduleScreenTable = null;
    private CompetitionsScreenTable competitionsScreenTable = null;
    private Under18TeamScreenTable under18TeamScreenTable = null;
    private BTeamScreenTable bTeamScreenTable = null;
    private TacticsScreenTable tacticsScreenTable = null;
    private TeamReportScreenTable teamReportScreenTable = null;
    private StaffScreenTable staffScreenTable = null;
    private TrainingScreenTable trainingScreenTable = null;
    private ScoutingScreenTable scoutingScreenTable = null;
    private TransfersScreenTable transfersScreenTable = null;
    private ClubScreenTable clubScreenTable = null;
    private BoardScreenTable boardScreenTable = null;
    private FinancesScreenTable financesScreenTable = null;
    
    /**
     * Main Game Buttons
     */
    private VisTextButton clubButton = new VisTextButton(LanguageModLoader.getValue("club"));
    private VisTextButton homeButton = new VisTextButton(LanguageModLoader.getValue("home"));
    private VisTextButton inboxButton = new VisTextButton(LanguageModLoader.getValue("inbox"));
    private VisTextButton squadButton = new VisTextButton(LanguageModLoader.getValue("squad"));
    private VisTextButton scheduleButton = new VisTextButton(LanguageModLoader.getValue("schedule"));
    private VisTextButton competitionsButton = new VisTextButton(LanguageModLoader.getValue("competitions"));
    private VisTextButton under18TeamButton = new VisTextButton(LanguageModLoader.getValue("under_18_team"));
    private VisTextButton bTeamButton = new VisTextButton(LanguageModLoader.getValue("b_team"));
    private VisTextButton tacticsButton = new VisTextButton(LanguageModLoader.getValue("tactics"));
    private VisTextButton teamReportButton = new VisTextButton(LanguageModLoader.getValue("team_report"));
    private VisTextButton staffButton = new VisTextButton(LanguageModLoader.getValue("staff"));
    private VisTextButton trainingButton = new VisTextButton(LanguageModLoader.getValue("training"));
    private VisTextButton scoutingButton = new VisTextButton(LanguageModLoader.getValue("scouting"));
    private VisTextButton transfersButton = new VisTextButton(LanguageModLoader.getValue("transfers"));
    private VisTextButton boardButton = new VisTextButton(LanguageModLoader.getValue("board"));
    private VisTextButton financesButton = new VisTextButton(LanguageModLoader.getValue("finances"));
    
    /**
     * Dialogs
     */
    private SaveGameDialog saveGameDialog = null;
    
    /**
     * @param parent
     */
    public MainGameScreen(Game parent) {
     
        this.game = parent;
        
        // set saved game if exist
        if ((((Futtuboru)game)).getCurrentGame() != null){
            
            this.currentGame = (((Futtuboru)game)).getCurrentGame();
        }
        
        stage = new Stage(new ScreenViewport());

        final VisTable gameWindowTable = new VisTable(true);
        gameWindowTable.pad(5);
        gameWindowTable.setFillParent(true);
        gameWindowTable.setDebug(true, true);
        
        /**
         * Weekly Calendar Widget
         */

        final VisTable weeklyCalendarWidget = new VisTable(true);
        weeklyCalendarWidget.pad(5);
        
        /**
         * Open Options Drop Down Menu Button/SelectBox -
         */
        final VisSelectBox<String> gameSettingsSelectBox = new VisSelectBox<>();

        /**
         * Options:
         *  - Save Game
         *  -
         */
        gameSettingsSelectBox.setItems(LanguageModLoader.getValue("options"),
                                       LanguageModLoader.getValue("save_game"),
                                       LanguageModLoader.getValue("load_game"),
                                       LanguageModLoader.getValue("settings"),
                                       LanguageModLoader.getValue("quit_to_menu"),
                                       LanguageModLoader.getValue("quit_to_desktop")
                                       );

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
                        saveGameDialog = new SaveGameDialog(((Futtuboru)(game)).getCurrentGame(), continueGameButton);  
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
         * Continue Game Button
         * 
         * - This will be disabled until the Game is Saved Once
         */
        continueGameButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                /**
                 * Continue Game Simulation
                 */
                if (continueGameButton.isDisabled()) {

                    System.out.println("SAVE GAME BEFORE ADVANCING THE SIMULATION!");

                } else {

                    // 
                    advanceGameTurn();
                }
            }
        });

        /**
         * Continue Game Tooltip [SAVE GAME FIRST]
         * 
         * NOTE: button disabled until saving game at least once
         */
        if (!((Futtuboru)(game)).getCurrentGame().getIsSaved()) {
            
            continueGameButton.addListener(new TextTooltip("Save Game Before Advancing the Simulation", VisUI.getSkin()));
            continueGameButton.setDisabled(true);
        }

        final VisTable mainScreenArea = new VisTable();
        
        mainTable.setDebug(true);
        
        /**
         * We Change the Content of Main Table Depending on User Selection (LEFT BUTTONS)
         * 
         *  - Keep existing screens cached and update them on show if required
         */
        setupMainScreenTables();
        
        /**
         * Main Game Buttons / VerticalGroup
         * 
         * This array of buttons to be shown in the Main Game Area, needs to be dynamically generated, because not every time every button will be available:
         * 
         *  - Under 18, Under 19, Reserve Teams, etc. might not be available.
         *  - Player might not have a Job/Club, so no Club, Finances, Training, Schedule, etc.
         */
        final VisTable buttonsMenu = new VisTable();
        
        /**
         * Add the Menu Listener for each Button
         */
        homeButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(HOME_SCREEN);
            }
        });

        // 
        inboxButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(INBOX_SCREEN);
            }
        });
        
        // 
        squadButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(SQUAD_SCREEN);
            }
        });
        
        //
        scheduleButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(SCHEDULE_SCREEN);
            }
        });
        
        //
        competitionsButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(COMPETITIONS_SCREEN);
            }
        });
        
        //
        under18TeamButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(UNDER_18_SCREEN);
            }
        });
        
        //
        bTeamButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(B_TEAM_SCREEN);
            }
        });
        
        //
        tacticsButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(TACTICS_SCREEN);
            }
        });
        
        //
        teamReportButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(TEAM_REPORT_SCREEN);
            }
        });
        
        //
        staffButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(STAFF_SCREEN);
            }
        });
        
        //
        trainingButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(TRAINING_SCREEN);
            }
        });

        //
        scoutingButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(SCOUTING_SCREEN);
            }
        });
        
        //
        transfersButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(TRANSFERS_SCREEN);
            }
        });
        
        //
        clubButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(CLUB_SCREEN);
            }
        });
        
        //
        boardButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(BOARD_SCREEN);
            }
        });
        
        //
        financesButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setActiveMainScreen(FINANCES_SCREEN);
            }
        });
        
        //
        buttonsMenu.add(homeButton).fill();
        buttonsMenu.row();
        buttonsMenu.addSeparator();
        buttonsMenu.row();
        
        buttonsMenu.add(inboxButton).fill();
        buttonsMenu.row();
        buttonsMenu.addSeparator();
        buttonsMenu.row();
        
        buttonsMenu.add(squadButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(scheduleButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(competitionsButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(under18TeamButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(bTeamButton).fill();
        buttonsMenu.row();
        buttonsMenu.addSeparator();
        
        buttonsMenu.add(tacticsButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(teamReportButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(staffButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(trainingButton).fill();
        buttonsMenu.row();
        buttonsMenu.addSeparator();
        
        buttonsMenu.add(scoutingButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(transfersButton).fill();
        buttonsMenu.row();
        buttonsMenu.addSeparator();
        
        buttonsMenu.add(clubButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(boardButton).fill();
        buttonsMenu.row();
        
        buttonsMenu.add(financesButton).fill();
        buttonsMenu.row();
        
        //
        setActiveMainScreen(HOME_SCREEN);
        
        //
        updateMainButtonsVisibility();
        
        // Settings SelectBox & Continue Game Button
        weeklyCalendarWidget.add(gameSettingsSelectBox);
        weeklyCalendarWidget.add(continueGameButton);

        // Main Game Buttons
        gameWindowTable.add(buttonsMenu).top();
        
        // Game Area Screen
        mainScreenArea.add(weeklyCalendarWidget).top().expandX().right();
        mainScreenArea.row();
        mainScreenArea.add(mainTable).grow();

        // 
        gameWindowTable.add(mainScreenArea).grow();
        
        //
        stage.addActor(gameWindowTable);
    }
    
    /**
     * Instantiate screens
     */
    private void setupMainScreenTables() {
       
        // 
        homeScreenTable = new HomeScreenTable(game);
        inboxScreenTable = new InboxScreenTable(game);
        squadScreenTable = new SquadScreenTable(game);
        scheduleScreenTable = new ScheduleScreenTable(game);
        competitionsScreenTable = new CompetitionsScreenTable(game);
        under18TeamScreenTable = new Under18TeamScreenTable(game);
        bTeamScreenTable = new BTeamScreenTable(game);
        tacticsScreenTable = new TacticsScreenTable(game);
        teamReportScreenTable = new TeamReportScreenTable(game);
        staffScreenTable = new StaffScreenTable(game);
        trainingScreenTable = new TrainingScreenTable(game);
        scoutingScreenTable = new ScoutingScreenTable(game);
        transfersScreenTable = new TransfersScreenTable(game);
        clubScreenTable = new ClubScreenTable(game);
        boardScreenTable = new BoardScreenTable(game);
        financesScreenTable = new FinancesScreenTable(game);
    }
    
    /**
     * Handle the Table Switching for the Main Screen
     */
    private void setActiveMainScreen(int screen) {
        
        // Clear the main table
        mainTable.clear();
        
        switch (screen) {
        
        case HOME_SCREEN:
            mainTable.add(homeScreenTable).grow();
            break;
            
        case INBOX_SCREEN:
            mainTable.add(inboxScreenTable).grow();
            break;
            
        case SQUAD_SCREEN:
            mainTable.add(squadScreenTable).grow();
            break;
            
        case SCHEDULE_SCREEN:
            mainTable.add(scheduleScreenTable).grow();
            break;
            
        case COMPETITIONS_SCREEN:
            mainTable.add(competitionsScreenTable).grow();
            break;
            
        case UNDER_18_SCREEN:
            mainTable.add(under18TeamScreenTable).grow();
            break;
            
        case B_TEAM_SCREEN:
            mainTable.add(bTeamScreenTable).grow();
            break;
            
        case TACTICS_SCREEN:
            mainTable.add(tacticsScreenTable).grow();
            break;
            
        case TEAM_REPORT_SCREEN:
            mainTable.add(teamReportScreenTable).grow();
            break;
            
        case STAFF_SCREEN:
            mainTable.add(staffScreenTable).grow();
            break;
            
        case TRAINING_SCREEN:
            mainTable.add(trainingScreenTable).grow();
            break;
            
        case SCOUTING_SCREEN:
            mainTable.add(scoutingScreenTable).grow();
            break;
            
        case TRANSFERS_SCREEN:
            mainTable.add(transfersScreenTable).grow();
            break;
            
        case CLUB_SCREEN:
            mainTable.add(clubScreenTable).grow();
            break;
            
        case BOARD_SCREEN:
            mainTable.add(boardScreenTable).grow();
            break;
            
        case FINANCES_SCREEN:    
            mainTable.add(financesScreenTable).grow();
            break;
        
        default:
            System.out.println("SCREEN NOT SET UP");
            break;
        }
    }
    
    /**
     * TODO: move this to the Simulation
     */
    private void advanceGameTurn() {
        
        System.out.println("ADVANCE THE SIMULATION - 1 TURN");
       
        
        // update the buttons in case the Player got a new Job at a Club
        updateMainButtonsVisibility();
    }
    
    /**
     * Hide or Show Main Buttons depending on Player Profession and Club
     */
    private void updateMainButtonsVisibility() {
        
        System.out.println("UPDATE MAIN BUTTONS VISIBILITY");
        
        System.out.println("club      : " + currentGame.getOwner().getCurrentClub());
        System.out.println("profession: " + currentGame.getOwner().getPrimaryProfession());
        
        
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
}