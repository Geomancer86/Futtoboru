package com.rndmodgames.futtoboru.menu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.menu.buttons.AuthorityButton;
import com.rndmodgames.futtoboru.menu.buttons.ClubInfoButton;
import com.rndmodgames.futtoboru.menu.buttons.CompetitionsButton;
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
import com.rndmodgames.futtoboru.tables.competitions.CompetitionsScreenTable;
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
    public static final int LEAGUE_DETAIL_SCREEN      =  501;    
    private static final int WORLD_SCREEN            =  600;
    
    //
    public static final int MAIN_SQUAD_SCREEN   = 1000;
    public static final int SCHEDULE_SCREEN     = 2000;
    public static final int CLUB_INFO_SCREEN    = 3000;
    public static final int FINANCES_SCREEN     = 4000;
    public static final int COMPETITIONS_SCREEN = 5000;
    
    //
    public static final int MATCH_PREVIEW_SCREEN = 10000;
    public static final int MATCH_RESULT_SCREEN  = 20000;
    public static final int MATCH_HISTORY_SCREEN = 30000;
    
    // Job System Screens (v1.0)
    public static final int CLUB_BROWSER_SCREEN = 10001;
    public static final int CLUB_DETAIL_SCREEN = 10002;
    public static final int JOB_BOARD_SCREEN = 10003;
    public static final int MY_APPLICATIONS_SCREEN = 10004;
    public static final int JOB_OFFER_SCREEN = 10005;
    public static final int NEGOTIATION_SCREEN = 10006;
    
    // Player Detail Screen (v1.0)
    public static final int PLAYER_DETAIL_SCREEN = 10007;
    
    // Draw Screen (v1.0)
    public static final int LEAGUE_DRAW_SCREEN = 10008;
        
    //
    public static int PREVIOUS_SCREEN = -1; //
    public static int BEFORE_MATCH_SCREEN = -1; //
    public static int CURRENT_SCREEN = HOME_SCREEN; // default to home screen
    
    // Current negotiation offer (v1.0)
    private com.rndmodgames.futtoboru.data.jobs.JobOffer currentNegotiationOffer = null;
    
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
    private VisTextButton competitionsButton = null;
    
    // Job System Buttons (v1.0)
    private VisTextButton clubBrowserButton = null;
    private VisTextButton jobBoardButton = null;
    private VisTextButton myApplicationsButton = null;
    
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
    private CompetitionsScreenTable competitionsScreenTable = null;
    private com.rndmodgames.futtoboru.tables.competitions.LeagueDetailScreenTable leagueDetailScreenTable = null;
    private com.rndmodgames.futtoboru.tables.draw.LeagueDrawScreenTable leagueDrawScreenTable = null;
    
    // Selected league for detail view
    private com.rndmodgames.futtoboru.data.League selectedLeague = null;
    
    // Job System Screens (v1.0)
    private com.rndmodgames.futtoboru.tables.jobs.ClubBrowserScreenTable clubBrowserScreenTable = null;
    private com.rndmodgames.futtoboru.tables.club.ClubDetailScreenTable clubDetailScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.JobBoardScreenTable jobBoardScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.MyApplicationsScreenTable myApplicationsScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.JobOfferScreenTable jobOfferScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.NegotiationScreenTable negotiationScreenTable = null;
    
    // Player Detail Screen (v1.0)
    private com.rndmodgames.futtoboru.tables.player.PlayerDetailScreenTable playerDetailScreenTable = null;
    
    // Selected club for detail view and job application
    private Club selectedClubForDetail = null;
    private Club selectedClubForJobApplication = null;
    
    // Selected player for detail view (v1.0)
    private com.rndmodgames.futtoboru.data.Player selectedPlayer = null;
    
    // Reference to top menu table for button state updates (v1.0)
    private com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable topMenuTable = null;
    
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
        inboxScreenTable.setMenuManager(this);
        squadScreenTable = new SquadScreenTable(game);
        scheduleScreenTable = new ScheduleScreenTable(game);
        matchPreviewScreenTable = new MatchPreviewScreenTable(game);
        matchResultScreenTable = new MatchResultScreenTable(game);
        matchHistoryScreenTable = new MatchHistoryScreenTable(game);
        clubInfoScreenTable = new ClubInfoScreenTable(game);
        financesScreenTable = new FinancesScreenTable(game);
        competitionsScreenTable = new CompetitionsScreenTable(game);
        competitionsScreenTable.setMenuManager(this);
        leagueDetailScreenTable = new com.rndmodgames.futtoboru.tables.competitions.LeagueDetailScreenTable(game);
        leagueDetailScreenTable.setMenuManager(this);
        
        // Draw Screen (v1.0)
        leagueDrawScreenTable = new com.rndmodgames.futtoboru.tables.draw.LeagueDrawScreenTable(game);
        leagueDrawScreenTable.setMenuManager(this);
        
        // Job System Screens (v1.0)
        clubBrowserScreenTable = new com.rndmodgames.futtoboru.tables.jobs.ClubBrowserScreenTable(game);
        clubDetailScreenTable = new com.rndmodgames.futtoboru.tables.club.ClubDetailScreenTable(game);
        jobBoardScreenTable = new com.rndmodgames.futtoboru.tables.jobs.JobBoardScreenTable(game);
        myApplicationsScreenTable = new com.rndmodgames.futtoboru.tables.jobs.MyApplicationsScreenTable(game);
        jobOfferScreenTable = new com.rndmodgames.futtoboru.tables.jobs.JobOfferScreenTable(game);
        negotiationScreenTable = new com.rndmodgames.futtoboru.tables.jobs.NegotiationScreenTable(game);
        
        // Player Detail Screen (v1.0)
        playerDetailScreenTable = new com.rndmodgames.futtoboru.tables.player.PlayerDetailScreenTable(game);
        playerDetailScreenTable.setMenuManager(this);
        
        // Set menu manager references
        clubBrowserScreenTable.setMenuManager(this);
        clubDetailScreenTable.setMenuManager(this);
        jobOfferScreenTable.setMenuManager(this);
        negotiationScreenTable.setMenuManager(this);
        myApplicationsScreenTable.setMenuManager(this);
        matchHistoryScreenTable.setMainMenuManager(this);
        
        
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
        competitionsButton = new CompetitionsButton(this);
        
        // Job System Buttons (v1.0)
        clubBrowserButton = new com.rndmodgames.futtoboru.menu.buttons.ClubBrowserButton(this);
        jobBoardButton = new com.rndmodgames.futtoboru.menu.buttons.JobBoardButton(this);
        myApplicationsButton = new com.rndmodgames.futtoboru.menu.buttons.MyApplicationsButton(this);
        
        
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
            
            // Job System (v1.0)
            buttonsMenu.add(clubBrowserButton).fill();
            buttonsMenu.row();
            
            buttonsMenu.add(jobBoardButton).fill();
            buttonsMenu.row();
            
            buttonsMenu.add(myApplicationsButton).fill();
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
            
            // Club Competitions
            buttonsMenu.add(competitionsButton).fill();
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
         * Can be null for unemployed players
         */
        Club currentClub = currentGame.getCurrentClub();
        
        switch (screen) {

        //
        case HOME_SCREEN:
            parentTable.add(homeScreenTable);
            break;
            
        // 
        case INBOX_SCREEN:
            // Update inbox to show latest messages
            inboxScreenTable.updateDynamicComponents();
            parentTable.add(inboxScreenTable).grow();
            break;
            
        case LEAGUE_DRAW_SCREEN:
            
            // Ensure selected league is set before showing draw screen
            if (selectedLeague == null) {
                System.out.println("MainMenuManager: WARNING - selectedLeague is null, attempting to recover from draw message");
                Gdx.app.log("MainMenuManager", "Cannot show draw screen: selectedLeague is null, attempting recovery");
                
                // Try to recover league from mandatory draw message
                com.rndmodgames.futtoboru.data.League recoveredLeague = null;
                if (currentGame != null && currentGame.getAllMessages() != null) {
                    // Find the mandatory LEAGUE_DRAW message
                    for (com.rndmodgames.futtoboru.data.Message message : currentGame.getAllMessages()) {
                        if (message != null) {
                            boolean isMandatory = message.getIsMandatory() != null && message.getIsMandatory();
                            String messageType = message.getMessageType();
                            
                            if (isMandatory && 
                                messageType != null && 
                                (messageType.equals("LEAGUE_DRAW") || 
                                 messageType.equals("FIXTURE_DRAW"))) {
                                
                                System.out.println("MainMenuManager: Found mandatory draw message: " + message.getTitle());
                                
                                // Method 1: Get league ID from actionData (preferred method)
                                if (message.getActionData() instanceof Long) {
                                    Long leagueId = (Long) message.getActionData();
                                    System.out.println("MainMenuManager: Recovering league ID from actionData: " + leagueId);
                                    
                                    // Find league in SaveGame
                                    if (currentGame.getMainAuthority() != null && 
                                        currentGame.getMainAuthority().getLeagues() != null) {
                                        for (com.rndmodgames.futtoboru.data.League l : 
                                             currentGame.getMainAuthority().getLeagues()) {
                                            if (l != null && l.getId() != null && l.getId().equals(leagueId)) {
                                                recoveredLeague = l;
                                                System.out.println("MainMenuManager: Recovered league by ID: " + recoveredLeague.getName());
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    // Method 2: Extract league name from message title (fallback)
                                    // Title format: "League Fixture Draw - {League Name}"
                                    String title = message.getTitle();
                                    if (title != null && title.contains(" - ")) {
                                        String leagueName = title.substring(title.lastIndexOf(" - ") + 3).trim();
                                        System.out.println("MainMenuManager: Attempting to recover league by name from title: " + leagueName);
                                        
                                        // Find league by name
                                        if (currentGame.getMainAuthority() != null && 
                                            currentGame.getMainAuthority().getLeagues() != null) {
                                            for (com.rndmodgames.futtoboru.data.League l : 
                                                 currentGame.getMainAuthority().getLeagues()) {
                                                if (l != null && l.getName() != null && l.getName().equals(leagueName)) {
                                                    recoveredLeague = l;
                                                    System.out.println("MainMenuManager: Recovered league by name: " + recoveredLeague.getName() + " (ID: " + recoveredLeague.getId() + ")");
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        // If exact match failed, try partial match
                                        if (recoveredLeague == null) {
                                            System.out.println("MainMenuManager: Exact name match failed, trying partial match");
                                            for (com.rndmodgames.futtoboru.data.League l : 
                                                 currentGame.getMainAuthority().getLeagues()) {
                                                if (l != null && l.getName() != null && l.getName().contains(leagueName)) {
                                                    recoveredLeague = l;
                                                    System.out.println("MainMenuManager: Recovered league by partial name match: " + recoveredLeague.getName() + " (ID: " + recoveredLeague.getId() + ")");
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Method 3: Try to extract from message content as last resort
                                    if (recoveredLeague == null && message.getPlainTextMessage() != null) {
                                        String content = message.getPlainTextMessage();
                                        // Content format: "The fixture draw for the {League Name} is ready..."
                                        if (content.contains("fixture draw for the ")) {
                                            int startIdx = content.indexOf("fixture draw for the ") + "fixture draw for the ".length();
                                            int endIdx = content.indexOf(" is", startIdx);
                                            if (endIdx > startIdx) {
                                                String leagueName = content.substring(startIdx, endIdx).trim();
                                                System.out.println("MainMenuManager: Attempting to recover league by name from content: " + leagueName);
                                                
                                                if (currentGame.getMainAuthority() != null && 
                                                    currentGame.getMainAuthority().getLeagues() != null) {
                                                    for (com.rndmodgames.futtoboru.data.League l : 
                                                         currentGame.getMainAuthority().getLeagues()) {
                                                        if (l != null && l.getName() != null && l.getName().equals(leagueName)) {
                                                            recoveredLeague = l;
                                                            System.out.println("MainMenuManager: Recovered league by name from content: " + recoveredLeague.getName() + " (ID: " + recoveredLeague.getId() + ")");
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // If we found a league, stop searching
                                if (recoveredLeague != null) {
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if (recoveredLeague != null) {
                    // Successfully recovered league, set it and continue
                    this.selectedLeague = recoveredLeague;
                    System.out.println("MainMenuManager: Successfully recovered league, showing draw screen");
                } else {
                    // Failed to recover league, fallback to inbox
                    System.out.println("MainMenuManager: ERROR - Could not recover league from message, falling back to inbox");
                    Gdx.app.error("MainMenuManager", "Cannot show draw screen: selectedLeague is null and recovery failed");
                    setActiveMainScreen(INBOX_SCREEN);
                    return;
                }
            }
            
            System.out.println("MainMenuManager: Showing draw screen for league: " + selectedLeague.getName() + " (ID: " + selectedLeague.getId() + ")");
            
            // Set selected league in draw screen
            leagueDrawScreenTable.setSelectedLeague(selectedLeague);
            
            // Update dynamic components
            leagueDrawScreenTable.updateDynamicComponents();
            
            // Add to parent table
            parentTable.add(leagueDrawScreenTable).grow().fill();
            
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
            // NOTE: Redirect to home if unemployed (no club)
            if (currentClub == null) {
                Gdx.app.log("MainMenuManager", "Cannot access Squad screen: player is unemployed (no club)");
                setActiveMainScreen(HOME_SCREEN);
                return;
            }
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
            // NOTE: Redirect to home if unemployed (no club)
            if (currentClub == null) {
                Gdx.app.log("MainMenuManager", "Cannot access Schedule screen: player is unemployed (no club)");
                setActiveMainScreen(HOME_SCREEN);
                return;
            }
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
            // NOTE: Redirect to home if unemployed (no club)
            if (currentClub == null) {
                Gdx.app.log("MainMenuManager", "Cannot access Match History screen: player is unemployed (no club)");
                setActiveMainScreen(HOME_SCREEN);
                return;
            }
            matchHistoryScreenTable.setCurrentClub(currentClub);
            
            // Update dynamic components
            matchHistoryScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(matchHistoryScreenTable).grow();
            
            break;
            
        case COMPETITIONS_SCREEN:
            
            // Update dynamic components
            competitionsScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(competitionsScreenTable).grow();
            
            break;
            
        case LEAGUE_DETAIL_SCREEN:
            
            // Ensure selected league is set before showing league detail screen
            if (selectedLeague == null) {
                System.out.println("MainMenuManager: WARNING - selectedLeague is null for league detail screen");
                Gdx.app.log("MainMenuManager", "No league selected for detail view, redirecting to competitions");
                setActiveMainScreen(COMPETITIONS_SCREEN);
                return;
            }
            
            System.out.println("MainMenuManager: Showing league detail screen for league: " + selectedLeague.getName() + " (ID: " + selectedLeague.getId() + ")");
            
            // Set selected league in league detail screen (this calls updateDynamicComponents internally)
            leagueDetailScreenTable.setSelectedLeague(selectedLeague);
            
            // Set as main content
            parentTable.add(leagueDetailScreenTable).grow().fill();
            
            break;
            
        case CLUB_INFO_SCREEN:
            
            // Update dynamic components
            // NOTE: Redirect to home if unemployed (no club)
            if (currentClub == null) {
                Gdx.app.log("MainMenuManager", "Cannot access Club Info screen: player is unemployed (no club)");
                setActiveMainScreen(HOME_SCREEN);
                return;
            }
            clubInfoScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(clubInfoScreenTable).grow();
            
            break;
            
        case FINANCES_SCREEN:
            
            // Update dynamic components
            // NOTE: Redirect to home if unemployed (no club)
            if (currentClub == null) {
                Gdx.app.log("MainMenuManager", "Cannot access Finances screen: player is unemployed (no club)");
                setActiveMainScreen(HOME_SCREEN);
                return;
            }
            financesScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(financesScreenTable).grow();
            
            break;
        
        // Job System Screens (v1.0)
        case CLUB_BROWSER_SCREEN:
            clubBrowserScreenTable.updateDynamicComponents();
            parentTable.add(clubBrowserScreenTable).grow();
            break;
            
        case CLUB_DETAIL_SCREEN:
            Gdx.app.log("MainMenuManager", "=== Switching to CLUB_DETAIL_SCREEN ===");
            System.out.println("[MainMenuManager] ===== Switching to CLUB_DETAIL_SCREEN =====");
            System.out.println("[MainMenuManager] clubDetailScreenTable: " + (clubDetailScreenTable != null ? "OK" : "NULL"));
            System.out.println("[MainMenuManager] selectedClubForDetail: " + (selectedClubForDetail != null ? selectedClubForDetail.getName() : "NULL"));
            
            if (selectedClubForDetail != null) {
                Gdx.app.log("MainMenuManager", "Calling updateDynamicComponents with club: " + selectedClubForDetail.getName());
                System.out.println("[MainMenuManager] Calling updateDynamicComponents...");
                clubDetailScreenTable.updateDynamicComponents(selectedClubForDetail);
                
                Gdx.app.log("MainMenuManager", "Adding clubDetailScreenTable to parentTable...");
                System.out.println("[MainMenuManager] Adding clubDetailScreenTable to parentTable...");
                parentTable.add(clubDetailScreenTable).grow();
                
                Gdx.app.log("MainMenuManager", "Screen added successfully");
                System.out.println("[MainMenuManager] CLUB_DETAIL_SCREEN added to parentTable");
            } else {
                Gdx.app.error("MainMenuManager", "No club selected for detail view");
                System.err.println("[MainMenuManager] ERROR: No club selected, redirecting to CLUB_BROWSER_SCREEN");
                setActiveMainScreen(CLUB_BROWSER_SCREEN);
            }
            break;
            
        case JOB_BOARD_SCREEN:
            System.err.println("[MainMenuManager] ===== Switching to JOB_BOARD_SCREEN =====");
            System.err.println("[MainMenuManager] jobBoardScreenTable: " + (jobBoardScreenTable != null ? "OK" : "NULL"));
            System.err.flush();
            System.out.println("[MainMenuManager] ===== Switching to JOB_BOARD_SCREEN =====");
            System.out.flush();
            Gdx.app.log("MainMenuManager", "=== Switching to JOB_BOARD_SCREEN ===");
            
            if (jobBoardScreenTable != null) {
                Gdx.app.log("MainMenuManager", "Calling updateDynamicComponents...");
                System.out.println("[MainMenuManager] Calling updateDynamicComponents...");
                jobBoardScreenTable.updateDynamicComponents();
                
                Gdx.app.log("MainMenuManager", "Adding jobBoardScreenTable to parentTable...");
                System.out.println("[MainMenuManager] Adding jobBoardScreenTable to parentTable...");
                parentTable.add(jobBoardScreenTable).grow();
                
                Gdx.app.log("MainMenuManager", "Screen added successfully");
                System.out.println("[MainMenuManager] JOB_BOARD_SCREEN added to parentTable");
            } else {
                Gdx.app.error("MainMenuManager", "jobBoardScreenTable is NULL!");
                System.err.println("[MainMenuManager] ERROR: jobBoardScreenTable is NULL!");
            }
            break;
            
        case MY_APPLICATIONS_SCREEN:
            myApplicationsScreenTable.updateDynamicComponents();
            parentTable.add(myApplicationsScreenTable).grow();
            break;
            
        case JOB_OFFER_SCREEN:
            jobOfferScreenTable.updateDynamicComponents();
            parentTable.add(jobOfferScreenTable).grow();
            break;
            
        case NEGOTIATION_SCREEN:
            if (currentNegotiationOffer != null) {
                negotiationScreenTable.setCurrentOffer(currentNegotiationOffer);
            }
            negotiationScreenTable.updateDynamicComponents();
            parentTable.add(negotiationScreenTable).grow();
            break;
            
        case PLAYER_DETAIL_SCREEN:
            if (selectedPlayer != null) {
                playerDetailScreenTable.updateDynamicComponents(selectedPlayer);
                parentTable.add(playerDetailScreenTable).grow();
            } else {
                Gdx.app.error("MainMenuManager", "No player selected for detail view");
                setActiveMainScreen(MAIN_SQUAD_SCREEN);
            }
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
     * 
     *  - this is used to automatically update the current screen, as it will have dynamic data
     */
    public void updateDynamicComponents() {
        
        switch(CURRENT_SCREEN) {
        
        case MAIN_SQUAD_SCREEN:
            squadScreenTable.updateDynamicComponents();
            break;
        
        case AUTHORITY_SCREEN:
            authorityScreenTable.updateDynamicComponents();
            break;
        
        case SCHEDULE_SCREEN:
            scheduleScreenTable.updateDynamicComponents();
            break;
            
        case FINANCES_SCREEN:
            financesScreenTable.updateDynamicComponents();
            break;
            
        case PLAYER_DETAIL_SCREEN:
            // Refresh player detail screen if a player is selected
            if (selectedPlayer != null) {
                playerDetailScreenTable.updateDynamicComponents(selectedPlayer);
            }
            break;
            
        case INBOX_SCREEN:
            inboxScreenTable.updateDynamicComponents();
            break;
            
        case COMPETITIONS_SCREEN:
            competitionsScreenTable.updateDynamicComponents();
            break;
            
        case LEAGUE_DETAIL_SCREEN:
            
            // Update dynamic components with selected league
            // Note: Only update if league is selected - don't re-add screen to parentTable
            // The screen is already added in setActiveMainScreen, just update its content
            if (selectedLeague != null) {
                // Ensure the screen's selectedLeague is in sync with MainMenuManager's selectedLeague
                // Sync the reference first, then update (to avoid triggering update twice)
                leagueDetailScreenTable.syncSelectedLeague(selectedLeague);
                leagueDetailScreenTable.updateDynamicComponents();
            } else {
                // If selectedLeague is null, redirect to competitions screen
                System.out.println("MainMenuManager: selectedLeague is null during updateDynamicComponents, redirecting to competitions");
                setActiveMainScreen(COMPETITIONS_SCREEN);
            }
            
            break;
            
        case LEAGUE_DRAW_SCREEN:
            
            // Update dynamic components with selected league
            if (selectedLeague != null) {
                leagueDrawScreenTable.setSelectedLeague(selectedLeague);
            }
            
            // Set as main content
            parentTable.add(leagueDrawScreenTable).grow();
            
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
    
    /**
     * Set current negotiation offer (v1.0)
     */
    public void setCurrentNegotiationOffer(com.rndmodgames.futtoboru.data.jobs.JobOffer offer) {
        this.currentNegotiationOffer = offer;
    }
    
    /**
     * Set selected club for detail view (v1.0)
     */
    public void setSelectedClubForDetail(Club club) {
        this.selectedClubForDetail = club;
    }
    
    /**
     * Set selected club for job application (v1.0)
     */
    public void setSelectedClubForJobApplication(Club club) {
        this.selectedClubForJobApplication = club;
    }
    
    /**
     * Get selected club for detail view (v1.0)
     */
    public Club getSelectedClubForDetail() {
        return selectedClubForDetail;
    }
    
    /**
     * Get selected club for job application (v1.0)
     */
    public Club getSelectedClubForJobApplication() {
        return selectedClubForJobApplication;
    }
    
    /**
     * Set selected league for detail view (v1.0)
     */
    public void setSelectedLeague(com.rndmodgames.futtoboru.data.League league) {
        this.selectedLeague = league;
    }
    
    /**
     * Get selected league for detail view (v1.0)
     */
    public com.rndmodgames.futtoboru.data.League getSelectedLeague() {
        return selectedLeague;
    }
    
    /**
     * Set selected player for detail view (v1.0)
     */
    public void setSelectedPlayer(com.rndmodgames.futtoboru.data.Player player) {
        this.selectedPlayer = player;
    }
    
    /**
     * Get selected player for detail view (v1.0)
     */
    public com.rndmodgames.futtoboru.data.Player getSelectedPlayer() {
        return selectedPlayer;
    }
    
    /**
     * Get inbox screen table (v1.0)
     * Used by MainGameMenuTable to select specific messages
     */
    public InboxScreenTable getInboxScreenTable() {
        return inboxScreenTable;
    }
    
    /**
     * Get top menu table (v1.0)
     * Used to refresh button state after draw completion
     */
    public com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable getTopMenu() {
        return topMenuTable;
    }
    
    /**
     * Set top menu table reference (v1.0)
     * Called from MainGameScreen after creating MainGameMenuTable
     */
    public void setTopMenuTable(com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable topMenuTable) {
        this.topMenuTable = topMenuTable;
    }
}