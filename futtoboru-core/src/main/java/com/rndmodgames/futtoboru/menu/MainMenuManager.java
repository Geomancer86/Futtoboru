package com.rndmodgames.futtoboru.menu;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.menu.buttons.AuthorityButton;
import com.rndmodgames.futtoboru.menu.buttons.ClubInfoButton;
import com.rndmodgames.futtoboru.menu.buttons.FinancesButton;
import com.rndmodgames.futtoboru.menu.buttons.HomeButton;
import com.rndmodgames.futtoboru.menu.buttons.InboxButton;
import com.rndmodgames.futtoboru.menu.buttons.MatchHistoryButton;
import com.rndmodgames.futtoboru.menu.buttons.PersonalDetailsButton;
import com.rndmodgames.futtoboru.menu.buttons.ScheduleButton;
import com.rndmodgames.futtoboru.menu.buttons.SquadButton;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.tables.authority.AuthorityScreenTable;
import com.rndmodgames.futtoboru.tables.club.ClubInfoScreenTable;
import com.rndmodgames.futtoboru.tables.finances.FinancesScreenTable;
import com.rndmodgames.futtoboru.tables.inbox.InboxScreenTable;
import com.rndmodgames.futtoboru.tables.main.HomeScreenTable;
import com.rndmodgames.futtoboru.tables.match.history.MatchHistoryScreenTable;
import com.rndmodgames.futtoboru.tables.match.preview.MatchPreviewScreenTable;
import com.rndmodgames.futtoboru.tables.match.result.MatchResultScreenTable;
import com.rndmodgames.futtoboru.tables.person.PersonDetailsScreenTable;
import com.rndmodgames.futtoboru.tables.schedule.ScheduleScreenTable;
import com.rndmodgames.futtoboru.tables.squad.SquadScreenTable;

/**
 * Main Menu Manager v1
 *
 *  - Create and set the Main Menu Buttons
 *      
 *      - Dynamically depending on Job/Club/Status/Etc
 *      - 
 * 
 * @author Geomancer86
 */
public class MainMenuManager {

    // keep track
    SaveGame currentGame;
    
    // main screen reference
    VisTable parentTable;
    
    //
    private VisTable buttonsMenu = new VisTable();
    
    /**
     * Screen Ids
     * 
     * NOTE: screen ids don't make any sense, we will keep this scheme for v1 but needs rework (with menu system)
     */
    public static final int HOME_SCREEN             =  100;
    public static final int INBOX_SCREEN            =  200;
    public static final int PERSON_DETAILS_SCREEN   =  300;
    public static final int AUTHORITY_SCREEN        =  400;
    
    private static final int LEAGUE_SCREEN           =  500;    
    private static final int WORLD_SCREEN            =  600;
    
    //
    public static final int MAIN_SQUAD_SCREEN   = 1000;
    public static final int SCHEDULE_SCREEN     = 2000;
    
    public static final int CLUB_INFO_SCREEN    = 3000;
    public static final int FINANCES_SCREEN     = 4000;
    
    //
    public static final int MATCH_PREVIEW_SCREEN = 10000;
    public static final int MATCH_RESULT_SCREEN  = 20000;
    public static final int MATCH_HISTORY_SCREEN = 30000;
        
    //
    public static int PREVIOUS_SCREEN = -1; // 
    public static int BEFORE_MATCH_SCREEN = -1; //
    public static int CURRENT_SCREEN = HOME_SCREEN; // default to home screen
    
    // Main Game Buttons
    private VisTextButton homeButton = null;
    private VisTextButton personalDetailsButton = null;
    private VisTextButton authorityButton = null;
    private VisTextButton inboxButton = null;
    private VisTextButton squadButton = null;
    private VisTextButton scheduleButton = null;
    private VisTextButton matchHistoryButton = null;
    private VisTextButton clubInfoButton = null;
    private VisTextButton financesButton = null;
    
    //
    private HomeScreenTable homeScreenTable = null;
    private PersonDetailsScreenTable personDetailsScreenTable = null;
    private AuthorityScreenTable authorityScreenTable = null;
    private InboxScreenTable inboxScreenTable = null;
    private SquadScreenTable squadScreenTable = null;
    private ScheduleScreenTable scheduleScreenTable = null;
    private MatchPreviewScreenTable matchPreviewScreenTable = null;
    private MatchResultScreenTable matchResultScreenTable = null;
    private MatchHistoryScreenTable matchHistoryScreenTable = null;
    private ClubInfoScreenTable clubInfoScreenTable = null;
    private FinancesScreenTable financesScreenTable = null;
    
    /**
     * 
     */
    public MainMenuManager(Game game, SaveGame currentGame, VisTable parentTable) {
        
        //
        this.currentGame = currentGame;
        
        // this will always .grow()
        this.parentTable = parentTable;
        this.parentTable.setDebug(true);
        
        // other windows
        homeScreenTable = new HomeScreenTable(game);
        personDetailsScreenTable = new PersonDetailsScreenTable(game);
        authorityScreenTable = new AuthorityScreenTable(game);
        inboxScreenTable = new InboxScreenTable(game);
        squadScreenTable = new SquadScreenTable(game);
        scheduleScreenTable = new ScheduleScreenTable(game);
        matchPreviewScreenTable = new MatchPreviewScreenTable(game);
        matchResultScreenTable = new MatchResultScreenTable(game);
        matchHistoryScreenTable = new MatchHistoryScreenTable(game);
        clubInfoScreenTable = new ClubInfoScreenTable(game);
        financesScreenTable = new FinancesScreenTable(game);
        
        // custom buttons with logic to switch screen/tables
        homeButton = new HomeButton(this);
        personalDetailsButton = new PersonalDetailsButton(this);
        authorityButton = new AuthorityButton(this);
        inboxButton = new InboxButton(this);
        squadButton = new SquadButton(this);
        scheduleButton = new ScheduleButton(this);
        matchHistoryButton = new MatchHistoryButton(this);
        clubInfoButton = new ClubInfoButton(this);
        financesButton = new FinancesButton(this);
        
        
        // set the current screen by default
        setActiveMainScreen(CURRENT_SCREEN);
        
        /**
         * Dynamic Buttons Menu depending on the CURRENT_JOB
         */
        setDynamicButtonsMenu();
    }

    /**
     * 
     */
    public void setDynamicButtonsMenu() {
       
        // if UNEMPLOYED/RETIRED
        if (currentGame.getOwner().getPrimaryProfession().getId().equals(2L)) {
            
            // Home
            buttonsMenu.add(homeButton).fill();
            buttonsMenu.row();
            
            // Inbox
            buttonsMenu.add(inboxButton).fill();
            buttonsMenu.row();
            
            // Personal Details
            buttonsMenu.add(personalDetailsButton).fill();
            buttonsMenu.row();
            
            // Sport Authorities
            buttonsMenu.add(authorityButton).fill();
            buttonsMenu.row();
            
        }
        
        /**
         * if MANAGER
         * 
         * TODO WIP:
         * 
         *   Team Screens:
         *      - Squad
         *      - Schedule
         *      - Formations/Lineup/Tactics
         *      - Training
         *      - Board
         *      - Staff
         */
        if (currentGame.getOwner().getPrimaryProfession().getId().equals(3L)) {
            
            // Home
            buttonsMenu.add(homeButton).fill();
            buttonsMenu.row();
            
            // Inbox
            buttonsMenu.add(inboxButton).fill();
            buttonsMenu.row();
            
            // Squad
            buttonsMenu.add(squadButton).fill();
            buttonsMenu.row();
            
            // Matches Area Separator
            buttonsMenu.addSeparator();
            
            // Schedule
            buttonsMenu.add(scheduleButton).fill();
            buttonsMenu.row();
            
            // Match History
            buttonsMenu.add(matchHistoryButton).fill();
            buttonsMenu.row();
            
            // Club Area Separator
            buttonsMenu.addSeparator();
            
            // Club Info
            buttonsMenu.add(clubInfoButton).fill();
            buttonsMenu.row();
            
            // Finances
            buttonsMenu.add(financesButton).fill();
            buttonsMenu.row();
            
            // Separator
            buttonsMenu.addSeparator();
            
            // Personal Details
            buttonsMenu.add(personalDetailsButton).fill();
            buttonsMenu.row();
            
            // Sport Authorities
            buttonsMenu.add(authorityButton).fill();
            buttonsMenu.row();
        }
    }
    
    /**
     * Handle the Table Switching for the Main Screen
     */
    public void setActiveMainScreen(int screen) {

        // Clear the main table
        parentTable.clear();
        
        // Set it before changing it
        PREVIOUS_SCREEN = CURRENT_SCREEN;
        
        // Set/Change the active screen
        CURRENT_SCREEN = screen;

        /**
         * Get the Current Club Instance
         */
        Club currentClub = currentGame.getCurrentClub();
        
        switch (screen) {

        //
        case HOME_SCREEN:
            parentTable.add(homeScreenTable);
            break;
            
        // 
        case INBOX_SCREEN:
            parentTable.add(inboxScreenTable);
            break;
           
        // 
        case AUTHORITY_SCREEN:
            
            //
            authorityScreenTable.updateDynamicComponents();
            parentTable.add(authorityScreenTable).grow();
            
            break;
        
        //  
        case PERSON_DETAILS_SCREEN:
            
            //
            personDetailsScreenTable.updateDynamicPersonComponents(currentGame.getOwner());
            parentTable.add(personDetailsScreenTable).grow();
            
            break;

        //
        case MAIN_SQUAD_SCREEN:
            
            // Set the Squad that player controls to show on Squad Screen
            // NOTE: this will fail if the user wants to access this screen and don't have an assigned club, shouldn't happen
            squadScreenTable.setCurrentClub(currentClub);
            
            // Update dynamic components
            squadScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(squadScreenTable).grow();

            break;            
            
        case SCHEDULE_SCREEN:

            /**
             * TODO: fix the calendar/fixtures screen not filling the complete width until we click or do continue game.
             *  NOTE: issue seems related to the scrollbars
             */
            
            // Set the Club for the Schedule Screen
            scheduleScreenTable.setCurrentClub(currentClub);
            
            // Update dynamic components
            scheduleScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(scheduleScreenTable).grow();
            
            break;
            
        case MATCH_PREVIEW_SCREEN:
            
            // Update dynamic components
            matchPreviewScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(matchPreviewScreenTable).grow();
            
            // to get back to the previous screen after the match
            BEFORE_MATCH_SCREEN = PREVIOUS_SCREEN;
            
            break;
            
        case MATCH_RESULT_SCREEN:

            // Update dynamic components
            matchResultScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(matchResultScreenTable).grow();
            
            break;
            
        case MATCH_HISTORY_SCREEN:

            // Set Match History Current Club
            matchHistoryScreenTable.setCurrentClub(currentClub);
            
            // Update dynamic components
            matchHistoryScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(matchHistoryScreenTable).grow();
            
            break;
            
        case CLUB_INFO_SCREEN:
            
            // Update dynamic components
            clubInfoScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(clubInfoScreenTable).grow();
            
            break;
            
        case FINANCES_SCREEN:
            
            // Update dynamic components
            financesScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(financesScreenTable).grow();
            
            break;
        
        //
        default:
            System.out.println("SCREEN NOT SET UP");
            break;
        }
        
        /**
         * NOTE: this is where we set the final alignment of the MAIN / CENTRAL SCREEN
         */
        parentTable.top().left();
    }
    
    /**
     * Allow to call this from other places after data is updated
     */
    public void updateDynamicComponents() {
        
        switch(CURRENT_SCREEN) {
        
        case AUTHORITY_SCREEN:
            authorityScreenTable.updateDynamicComponents();
            break;
        
        case SCHEDULE_SCREEN:
            scheduleScreenTable.updateDynamicComponents();
            break;

        default:
            //ignore
            break;
        }
    }

    //
    public VisTable getButtonsMenu() {
        return buttonsMenu;
    }

    public void setButtonsMenu(VisTable buttonsMenu) {
        this.buttonsMenu = buttonsMenu;
    }
}