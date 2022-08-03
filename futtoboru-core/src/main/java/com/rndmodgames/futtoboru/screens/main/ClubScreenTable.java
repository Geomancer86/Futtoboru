package com.rndmodgames.futtoboru.screens.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.screens.main.club.ClubGeneralTableScreen;
import com.rndmodgames.futtoboru.screens.main.club.ClubProfileTableScreen;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Club Screen - Main Game
 * 
 * Sub-Screens:
 * 
 *      - Profile           []
 *      - General           [WIP]
 *      - News              []
 *      - Facilities        []
 *      - Affiliates        []
 *      - History           []
 * 
 * Sources:
 *  - https://redledslick.weebly.com/uploads/1/2/3/7/123738267/627008043.png
 *  - http://3.bp.blogspot.com/-C0-GO_Wvrow/TpWkRL53I-I/AAAAAAAAAMI/dpGyMl7AC-0/s1600/clubscreen.jpg
 *  - https://twitter.com/davorfernandez/status/1301744546666237959/photo/1
 *  - https://i2.wp.com/culturageek.com.ar/wp-content/uploads/2020/05/manchester-FM.gif?resize=1000%2C538&ssl=1
 * 
 * @author Geomancer86
 */
public class ClubScreenTable extends VisTable {

    Game game;
    Stage stage;
    
    /**
     * Sub Screen IDs
     */
    public static final int CLUB_PROFILE_SCREEN = 1;
    public static final int CLUB_GENERAL_SCREEN = 2;
    
    /**
     * Club Screen Components
     */
    private VisTable clubScreenTable = null;
    private ClubGeneralTableScreen clubGeneralTableScreen = null;
    private ClubProfileTableScreen clubProfileTableScreen = null;
    
    /**
     * Club Screen Dynamic Menus
     */
    private VisTable clubScreenDynamicMenuTable = new VisTable(true);
    
    private VisTextButton clubProfileButton = new VisTextButton(LanguageModLoader.getValue("profile"));
    private VisTextButton clubGeneralButton = new VisTextButton(LanguageModLoader.getValue("general"));
    private VisTextButton clubNewsButton = new VisTextButton(LanguageModLoader.getValue("news"));
    private VisTextButton clubFacilitiesButton = new VisTextButton(LanguageModLoader.getValue("facilities"));
    private VisTextButton clubAffiliatesButton = new VisTextButton(LanguageModLoader.getValue("affiliates"));
    private VisTextButton clubHistoryButton = new VisTextButton(LanguageModLoader.getValue("history"));
    
    /**
     * 
     * @param parent
     */
    public ClubScreenTable(Game parent) {
        
        this.game = parent;
        this.stage = new Stage(new ScreenViewport());

        /**
         * Initialize Club Screen and Dynamic Menu Tables
         */
        clubScreenTable = new VisTable(true);
        clubScreenTable.setDebug(true, true);
        
        clubScreenTable.pad(5);
        clubScreenTable.top().left();

        clubGeneralTableScreen = new ClubGeneralTableScreen();
        clubProfileTableScreen = new ClubProfileTableScreen();
        
        /**
         * Initialize Dynamic Menu
         * 
         * - Defaults to Player Current Club
         */
        updateClubScreenDynamicMenu(((Futtoboru)game).getCurrentGame().getOwner().getCurrentClub());
        
        // add the Club Screen Table
        this.add(clubScreenTable).expand().top().left();
    }
    
    /**
     * Add the Main Game Dynamic Menus that will Switch Between Club Sub-Screens
     * 
     * Changes the Club to be Shown in the Club Screen and Sub-Screens
     */
    public void updateClubScreenDynamicMenu(Club club) {

        // Set Dynamic Menu
        clubScreenDynamicMenuTable.clear();
        clubScreenDynamicMenuTable.add(clubProfileButton).expandX().left();
        clubScreenDynamicMenuTable.add(clubGeneralButton).expandX().left();
        
        //
        clubProfileButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setClubSubScreen(CLUB_PROFILE_SCREEN, club);
            }
        });
        
        //
        clubGeneralButton.addCaptureListener(new InputListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                //
                setClubSubScreen(CLUB_GENERAL_SCREEN, club);
            }
        });

//        clubScreenDynamicMenuTable.add(clubNewsButton).expandX().left();
//        clubScreenDynamicMenuTable.add(clubFacilitiesButton).expandX().left();
//        clubScreenDynamicMenuTable.add(clubAffiliatesButton).expandX().left();
//        clubScreenDynamicMenuTable.add(clubHistoryButton).expandX().left();
    }
    
    /**
     * 
     */
    public VisTable getClubScreenDynamicMenu() {

        return clubScreenDynamicMenuTable;
    }
    
    public void setClubSubScreen(int subScreen, Club club) {
     
        clubScreenTable.clear();
        
        switch (subScreen) {
        
        case CLUB_PROFILE_SCREEN:
            
            // Club Profile
            clubProfileTableScreen.setClub(club);
            clubScreenTable.add(clubProfileTableScreen);
            
            break;
            
        case CLUB_GENERAL_SCREEN:
            
            // Club General
            clubGeneralTableScreen.setClub(club);
            clubScreenTable.add(clubGeneralTableScreen);
            
            break;
        
        default:
            System.out.println("SUB SCREEN NOT IMPLEMENTED");
            break;
        }
    }
}