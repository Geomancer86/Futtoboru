package com.rndmodgames.futtoboru.menu;

import java.util.LinkedHashMap;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.components.AutoFocusScrollPane;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.menu.MenuEntries.MenuEntry;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.tables.authority.AuthorityScreenTable;
import com.rndmodgames.futtoboru.tables.club.ClubInfoScreenTable;
import com.rndmodgames.futtoboru.tables.competitions.CompetitionsScreenTable;
import com.rndmodgames.futtoboru.tables.competitions.CupDetailScreenTable;
import com.rndmodgames.futtoboru.tables.competitions.CupPlayoffsTable;
import com.rndmodgames.futtoboru.tables.competitions.LeagueDetailScreenTable;
import com.rndmodgames.futtoboru.tables.draw.CupDrawScreenTable;
import com.rndmodgames.futtoboru.tables.draw.LeagueDrawScreenTable;
import com.rndmodgames.futtoboru.tables.finances.FinancesScreenTable;
import com.rndmodgames.futtoboru.tables.inbox.InboxScreenTable;
import com.rndmodgames.futtoboru.tables.main.HomeScreenTable;
import com.rndmodgames.futtoboru.tables.match.history.MatchHistoryScreenTable;
import com.rndmodgames.futtoboru.tables.match.preview.MatchPreviewScreenTable;
import com.rndmodgames.futtoboru.tables.match.result.MatchResultScreenTable;
import com.rndmodgames.futtoboru.tables.person.PersonDetailsScreenTable;
import com.rndmodgames.futtoboru.tables.schedule.ScheduleScreenTable;
import com.rndmodgames.futtoboru.tables.squad.SquadScreenTable;
import com.rndmodgames.futtoboru.system.Log;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.LanguageModLoader;

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
    public static final int CUP_DETAIL_SCREEN         =  502;
    public static final int CUP_PLAYOFFS_SCREEN       =  503;
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
    public static final int HISTORY_SCREEN       = 40000;   // U-07 champions, cup winners, final tables
    
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
    public static final int CUP_DRAW_SCREEN = 10009;

    // G-04: the retained list (from the Squad screen or the club's April letter)
    public static final int RETAINED_LIST_SCREEN = 10010;

        
    //
    public static int PREVIOUS_SCREEN = -1; //
    public static int BEFORE_MATCH_SCREEN = -1; //
    public static int CURRENT_SCREEN = HOME_SCREEN; // default to home screen

    /** A new or loaded game opens on Home (the static screen ids above outlive one MainGameScreen). */
    public static void resetNavigation() {
        PREVIOUS_SCREEN = -1;
        BEFORE_MATCH_SCREEN = -1;
        CURRENT_SCREEN = HOME_SCREEN;
    }
    
    // Current negotiation offer (v1.0)
    private com.rndmodgames.futtoboru.data.jobs.JobOffer currentNegotiationOffer = null;
    
    /** Left menu buttons by screen id, built from {@link MenuEntries} (U-04b) */
    private final Map<Integer, VisTextButton> entryButtons = new LinkedHashMap<>();

    /** Screens visited since the last menu entry: what Back and Esc go through (U-04b) */
    private final NavigationHistory history = new NavigationHistory();

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
    private com.rndmodgames.futtoboru.tables.history.HistoryScreenTable historyScreenTable = null;
    private ClubInfoScreenTable clubInfoScreenTable = null;
    private FinancesScreenTable financesScreenTable = null;
    private CompetitionsScreenTable competitionsScreenTable = null;
    private LeagueDetailScreenTable leagueDetailScreenTable = null;
    private CupDetailScreenTable cupDetailScreenTable = null;
    private CupPlayoffsTable cupPlayoffsTable = null;
    private LeagueDrawScreenTable leagueDrawScreenTable = null;
    private CupDrawScreenTable cupDrawScreenTable = null;
    
    // Selected league for detail view
    private com.rndmodgames.futtoboru.data.League selectedLeague = null;
    private com.rndmodgames.futtoboru.data.Competition selectedCup = null;
    
    // Job System Screens (v1.0)
    private com.rndmodgames.futtoboru.tables.jobs.ClubBrowserScreenTable clubBrowserScreenTable = null;
    private com.rndmodgames.futtoboru.tables.club.ClubDetailScreenTable clubDetailScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.JobBoardScreenTable jobBoardScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.MyApplicationsScreenTable myApplicationsScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.JobOfferScreenTable jobOfferScreenTable = null;
    private com.rndmodgames.futtoboru.tables.jobs.NegotiationScreenTable negotiationScreenTable = null;
    
    // Player Detail Screen (v1.0)
    private com.rndmodgames.futtoboru.tables.player.PlayerDetailScreenTable playerDetailScreenTable = null;

    // G-04 retained list
    private com.rndmodgames.futtoboru.tables.contracts.RetainedListScreenTable retainedListScreenTable = null;

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
        
        // other windows
        homeScreenTable = new HomeScreenTable(game);
        homeScreenTable.setMenuManager(this);
        personDetailsScreenTable = new PersonDetailsScreenTable(game);
        authorityScreenTable = new AuthorityScreenTable(game);
        inboxScreenTable = new InboxScreenTable(game);
        inboxScreenTable.setMenuManager(this);
        squadScreenTable = new SquadScreenTable(game);
        scheduleScreenTable = new ScheduleScreenTable(game);
        matchPreviewScreenTable = new MatchPreviewScreenTable(game);
        matchResultScreenTable = new MatchResultScreenTable(game);
        matchHistoryScreenTable = new MatchHistoryScreenTable(game);
        historyScreenTable = new com.rndmodgames.futtoboru.tables.history.HistoryScreenTable(game);
        clubInfoScreenTable = new ClubInfoScreenTable(game);
        financesScreenTable = new FinancesScreenTable(game);
        competitionsScreenTable = new CompetitionsScreenTable(game);
        competitionsScreenTable.setMenuManager(this);
        leagueDetailScreenTable = new LeagueDetailScreenTable(game);
        leagueDetailScreenTable.setMenuManager(this);
        
        cupDetailScreenTable = new CupDetailScreenTable(game);
        cupDetailScreenTable.setMenuManager(this);
        
        cupPlayoffsTable = new CupPlayoffsTable(game);
        cupPlayoffsTable.setMenuManager(this);
        
        // Draw Screen (v1.0)
        leagueDrawScreenTable = new LeagueDrawScreenTable(game);
        leagueDrawScreenTable.setMenuManager(this);
        
        cupDrawScreenTable = new CupDrawScreenTable(game);
        cupDrawScreenTable.setMenuManager(this);
        
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

        // G-04 retained list
        retainedListScreenTable = new com.rndmodgames.futtoboru.tables.contracts.RetainedListScreenTable(game);
        retainedListScreenTable.setMenuManager(this);

        // Set menu manager references
        clubBrowserScreenTable.setMenuManager(this);
        clubDetailScreenTable.setMenuManager(this);
        jobOfferScreenTable.setMenuManager(this);
        negotiationScreenTable.setMenuManager(this);
        myApplicationsScreenTable.setMenuManager(this);
        matchHistoryScreenTable.setMainMenuManager(this);
        
        
        // set the current screen by default; a drill-down (e.g. back from Settings) can still go Back to its entry
        history.reset(MenuEntries.entryScreenFor(CURRENT_SCREEN));
        setActiveMainScreen(CURRENT_SCREEN);
        
        /**
         * Dynamic Buttons Menu depending on the CURRENT_JOB
         */
        setDynamicButtonsMenu();
    }

    /**
     * Builds the left menu from {@link MenuEntries} for the owner's profession (U-04b). Called again when the
     * profession changes (a job offer accepted), so it starts from an empty menu.
     */
    public void setDynamicButtonsMenu() {

        buttonsMenu.clearChildren();
        entryButtons.clear();

        int lastGroup = -1;
        for (final MenuEntry entry : MenuEntries.forProfession(isManager())) {

            if (lastGroup >= 0 && entry.group() != lastGroup) {
                buttonsMenu.addSeparator();
            }
            lastGroup = entry.group();

            final VisTextButton button = new VisTextButton(LanguageModLoader.getValue(entry.labelKey()),
                    UiTheme.style("button.toggle"));
            button.setProgrammaticChangeEvents(false);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    openMenuEntry(entry.screen());
                }
            });

            entryButtons.put(entry.screen(), button);
            buttonsMenu.add(button).fill();
            buttonsMenu.row();
        }

        syncMenuState();
    }

    /** @return true if the owner works at a club (the club menu), false for the job-seeker menu (J-01) */
    private boolean isManager() {
        return MenuEntries.hasClub(currentGame);
    }

    /** A menu entry was picked: its screen starts a new Back history. */
    public void openMenuEntry(int screen) {
        history.reset(screen);
        if (screen == MAIN_SQUAD_SCREEN && squadScreenTable != null) {
            squadScreenTable.showPlayers(); // G-01: the menu entry opens on the players list
        }
        setActiveMainScreen(screen);
    }

    /** G-01: the Squad screen on its Team Sheet tab. */
    public void openTeamSheet() {
        openMenuEntry(MAIN_SQUAD_SCREEN);
        if (CURRENT_SCREEN == MAIN_SQUAD_SCREEN) {
            squadScreenTable.showTeamSheet();
        }
    }

    /**
     * Back button / Esc: shows the previous screen of the history.
     *
     * @return false if there was nothing to go back to
     */
    public boolean goBack() {
        int previous = history.back();
        if (previous < 0) {
            return false;
        }
        setActiveMainScreen(previous);
        return true;
    }

    /** @return true if Back has somewhere to go */
    public boolean canGoBack() {
        return history.canGoBack();
    }

    /** The screen asked for cannot be shown (no club, nothing selected): show {@code screen} in its place. */
    private void redirectTo(int screen) {
        history.dropCurrent();
        setActiveMainScreen(screen);
    }

    /** Highlights the menu entry of the current screen and shows or hides the Back button. */
    private void syncMenuState() {
        int active = MenuEntries.entryScreenFor(CURRENT_SCREEN);
        for (Map.Entry<Integer, VisTextButton> e : entryButtons.entrySet()) {
            e.getValue().setChecked(e.getKey() == active);
        }
        if (topMenuTable != null) {
            topMenuTable.setBackButtonVisible(history.canGoBack());
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
        history.visit(screen);

        /**
         * Get the Current Club Instance
         * Can be null for unemployed players
         */
        Club currentClub = currentGame.getCurrentClub();
        
        switch (screen) {

        //
        case HOME_SCREEN:
            homeScreenTable.updateDynamicComponents();
            parentTable.add(homeScreenTable).grow();
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
                Log.info(Log.UI_MENU, "MainMenuManager: WARNING - selectedLeague is null, attempting to recover from draw message");
                Log.info(Log.UI_MENU, "MainMenuManager", "Cannot show draw screen: selectedLeague is null, attempting recovery");
                
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
                                
                                Log.info(Log.UI_MENU, "MainMenuManager: Found mandatory draw message: " + message.getTitle());
                                
                                // Method 1: Get league ID from actionData (preferred method)
                                if (message.getActionData() instanceof Long) {
                                    Long leagueId = (Long) message.getActionData();
                                    Log.info(Log.UI_MENU, "MainMenuManager: Recovering league ID from actionData: " + leagueId);
                                    
                                    // Find league in SaveGame
                                    if (currentGame.getMainAuthority() != null && 
                                        currentGame.getMainAuthority().getLeagues() != null) {
                                        for (com.rndmodgames.futtoboru.data.League l : 
                                             currentGame.getMainAuthority().getLeagues()) {
                                            if (l != null && l.getId() != null && l.getId().equals(leagueId)) {
                                                recoveredLeague = l;
                                                Log.info(Log.UI_MENU, "MainMenuManager: Recovered league by ID: " + recoveredLeague.getName());
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
                                        Log.info(Log.UI_MENU, "MainMenuManager: Attempting to recover league by name from title: " + leagueName);
                                        
                                        // Find league by name
                                        if (currentGame.getMainAuthority() != null && 
                                            currentGame.getMainAuthority().getLeagues() != null) {
                                            for (com.rndmodgames.futtoboru.data.League l : 
                                                 currentGame.getMainAuthority().getLeagues()) {
                                                if (l != null && l.getName() != null && l.getName().equals(leagueName)) {
                                                    recoveredLeague = l;
                                                    Log.info(Log.UI_MENU, "MainMenuManager: Recovered league by name: " + recoveredLeague.getName() + " (ID: " + recoveredLeague.getId() + ")");
                                                    break;
                                                }
                                            }
                                        }
                                        
                                        // If exact match failed, try partial match
                                        if (recoveredLeague == null) {
                                            Log.info(Log.UI_MENU, "MainMenuManager: Exact name match failed, trying partial match");
                                            for (com.rndmodgames.futtoboru.data.League l : 
                                                 currentGame.getMainAuthority().getLeagues()) {
                                                if (l != null && l.getName() != null && l.getName().contains(leagueName)) {
                                                    recoveredLeague = l;
                                                    Log.info(Log.UI_MENU, "MainMenuManager: Recovered league by partial name match: " + recoveredLeague.getName() + " (ID: " + recoveredLeague.getId() + ")");
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
                                                Log.info(Log.UI_MENU, "MainMenuManager: Attempting to recover league by name from content: " + leagueName);
                                                
                                                if (currentGame.getMainAuthority() != null && 
                                                    currentGame.getMainAuthority().getLeagues() != null) {
                                                    for (com.rndmodgames.futtoboru.data.League l : 
                                                         currentGame.getMainAuthority().getLeagues()) {
                                                        if (l != null && l.getName() != null && l.getName().equals(leagueName)) {
                                                            recoveredLeague = l;
                                                            Log.info(Log.UI_MENU, "MainMenuManager: Recovered league by name from content: " + recoveredLeague.getName() + " (ID: " + recoveredLeague.getId() + ")");
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
                    Log.info(Log.UI_MENU, "MainMenuManager: Successfully recovered league, showing draw screen");
                } else {
                    // Failed to recover league, fallback to inbox
                    Log.info(Log.UI_MENU, "MainMenuManager: ERROR - Could not recover league from message, falling back to inbox");
                    Log.error(Log.UI_MENU, "MainMenuManager", "Cannot show draw screen: selectedLeague is null and recovery failed");
                    redirectTo(INBOX_SCREEN);
                    return;
                }
            }
            
            Log.info(Log.UI_MENU, "MainMenuManager: Showing draw screen for league: " + selectedLeague.getName() + " (ID: " + selectedLeague.getId() + ")");
            
            // Set selected league in draw screen
            leagueDrawScreenTable.setSelectedLeague(selectedLeague);
            
            // Update dynamic components
            leagueDrawScreenTable.updateDynamicComponents();
            
            // Add to parent table
            parentTable.add(leagueDrawScreenTable).grow().fill();
            
            break;
            
        case CUP_DRAW_SCREEN:
            Log.info(Log.UI_MENU, "MainMenuManager: Showing cup draw screen");
            
            // Try to find edition ID from draw message
            com.rndmodgames.futtoboru.data.CompetitionEdition edition = null;
            if (currentGame != null && currentGame.getAllMessages() != null) {
                for (com.rndmodgames.futtoboru.data.Message message : currentGame.getAllMessages()) {
                    if (message != null && message.getIsMandatory() != null && message.getIsMandatory() &&
                        "CUP_DRAW".equals(message.getMessageType())) {
                        
                        if (message.getActionData() instanceof Long) {
                            Long editionId = (Long) message.getActionData();
                            // Find edition in any cup
                            for (com.rndmodgames.futtoboru.data.Competition cup : currentGame.getAllCups()) {
                                if (cup.getEditions() != null) {
                                    for (com.rndmodgames.futtoboru.data.CompetitionEdition ed : cup.getEditions()) {
                                        if (ed.getId().equals(editionId)) {
                                            edition = ed;
                                            break;
                                        }
                                    }
                                }
                                if (edition != null) break;
                            }
                        }
                        break;
                    }
                }
            }
            
            if (edition != null) {
                cupDrawScreenTable.setCompetitionEdition(edition);
                parentTable.add(cupDrawScreenTable).grow().fill();
            } else {
                Log.error(Log.UI_MENU, "MainMenuManager", "Could not find competition edition for cup draw");
                redirectTo(INBOX_SCREEN);
            }
            
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
                Log.info(Log.UI_MENU, "MainMenuManager", "Cannot access Squad screen: player is unemployed (no club)");
                redirectTo(HOME_SCREEN);
                return;
            }
            squadScreenTable.setCurrentClub(currentClub);
            
            // Update dynamic components
            squadScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(squadScreenTable).grow();

            break;

        case RETAINED_LIST_SCREEN:
            // G-04: the club's retained list (from the Squad screen or the April letter)
            if (currentClub == null) {
                redirectTo(HOME_SCREEN);
                return;
            }
            retainedListScreenTable.setCurrentClub(currentClub);
            retainedListScreenTable.updateDynamicComponents();
            parentTable.add(retainedListScreenTable).grow();
            break;

        case SCHEDULE_SCREEN:

            /**
             * TODO: fix the calendar/fixtures screen not filling the complete width until we click or do continue game.
             *  NOTE: issue seems related to the scrollbars
             */
            
            // Set the Club for the Schedule Screen
            // NOTE: Redirect to home if unemployed (no club)
            if (currentClub == null) {
                Log.info(Log.UI_MENU, "MainMenuManager", "Cannot access Schedule screen: player is unemployed (no club)");
                redirectTo(HOME_SCREEN);
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
                Log.info(Log.UI_MENU, "MainMenuManager", "Cannot access Match History screen: player is unemployed (no club)");
                redirectTo(HOME_SCREEN);
                return;
            }
            matchHistoryScreenTable.setCurrentClub(currentClub);
            
            // Update dynamic components
            matchHistoryScreenTable.updateDynamicComponents();
            
            // Set as main content
            parentTable.add(matchHistoryScreenTable).grow();
            
            break;
            
        case HISTORY_SCREEN:
            // V-01: the menu entry showed an empty panel (the case was missing here)
            historyScreenTable.updateDynamicComponents();
            parentTable.add(historyScreenTable).grow();
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
                Log.info(Log.UI_MENU, "MainMenuManager: WARNING - selectedLeague is null for league detail screen");
                Log.info(Log.UI_MENU, "MainMenuManager", "No league selected for detail view, redirecting to competitions");
                redirectTo(COMPETITIONS_SCREEN);
                return;
            }
            
            Log.info(Log.UI_MENU, "MainMenuManager: Showing league detail screen for league: " + selectedLeague.getName() + " (ID: " + selectedLeague.getId() + ")");
            
            // Set selected league in league detail screen (this calls updateDynamicComponents internally)
            leagueDetailScreenTable.setSelectedLeague(selectedLeague);
            
            // Set as main content
            parentTable.add(leagueDetailScreenTable).grow().fill();
            
            break;
            
        case CUP_DETAIL_SCREEN:
            if (selectedCup == null) {
                redirectTo(COMPETITIONS_SCREEN);
                return;
            }
            cupDetailScreenTable.setSelectedCup(selectedCup);
            parentTable.add(cupDetailScreenTable).grow().fill();
            break;
            
        case CUP_PLAYOFFS_SCREEN:
            if (selectedCup == null) {
                redirectTo(COMPETITIONS_SCREEN);
                return;
            }
            // Find current edition
            com.rndmodgames.futtoboru.data.CompetitionEdition cupEd = null;
            java.time.LocalDateTime now = currentGame.getGameDate();
            for (com.rndmodgames.futtoboru.data.CompetitionEdition ed : selectedCup.getEditions()) {
                if (ed.getStartDate() != null && ed.getEndDate() != null &&
                    !now.isBefore(ed.getStartDate()) && !now.isAfter(ed.getEndDate())) {
                    cupEd = ed;
                    break;
                }
            }
            if (cupEd == null && !selectedCup.getEditions().isEmpty()) {
                cupEd = selectedCup.getEditions().get(selectedCup.getEditions().size() - 1);
            }
            
            if (cupEd != null) {
                cupPlayoffsTable.setCompetitionEdition(cupEd);
                parentTable.add(cupPlayoffsTable).grow().fill();
            } else {
                redirectTo(COMPETITIONS_SCREEN);
            }
            break;
            
        case CLUB_INFO_SCREEN:
            
            // Update dynamic components
            // NOTE: Redirect to home if unemployed (no club)
            if (currentClub == null) {
                Log.info(Log.UI_MENU, "MainMenuManager", "Cannot access Club Info screen: player is unemployed (no club)");
                redirectTo(HOME_SCREEN);
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
                Log.info(Log.UI_MENU, "MainMenuManager", "Cannot access Finances screen: player is unemployed (no club)");
                redirectTo(HOME_SCREEN);
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
            Log.info(Log.UI_MENU, "MainMenuManager", "=== Switching to CLUB_DETAIL_SCREEN ===");
            Log.info(Log.UI_MENU, "[MainMenuManager] ===== Switching to CLUB_DETAIL_SCREEN =====");
            Log.info(Log.UI_MENU, "[MainMenuManager] clubDetailScreenTable: " + (clubDetailScreenTable != null ? "OK" : "NULL"));
            Log.info(Log.UI_MENU, "[MainMenuManager] selectedClubForDetail: " + (selectedClubForDetail != null ? selectedClubForDetail.getName() : "NULL"));
            
            if (selectedClubForDetail != null) {
                Log.info(Log.UI_MENU, "MainMenuManager", "Calling updateDynamicComponents with club: " + selectedClubForDetail.getName());
                Log.info(Log.UI_MENU, "[MainMenuManager] Calling updateDynamicComponents...");
                clubDetailScreenTable.updateDynamicComponents(selectedClubForDetail);
                
                Log.info(Log.UI_MENU, "MainMenuManager", "Adding clubDetailScreenTable to parentTable...");
                Log.info(Log.UI_MENU, "[MainMenuManager] Adding clubDetailScreenTable to parentTable...");
                parentTable.add(clubDetailScreenTable).grow();
                
                Log.info(Log.UI_MENU, "MainMenuManager", "Screen added successfully");
                Log.info(Log.UI_MENU, "[MainMenuManager] CLUB_DETAIL_SCREEN added to parentTable");
            } else {
                Log.error(Log.UI_MENU, "MainMenuManager", "No club selected for detail view");
                Log.error(Log.UI_MENU, "[MainMenuManager] ERROR: No club selected, redirecting to CLUB_BROWSER_SCREEN");
                redirectTo(CLUB_BROWSER_SCREEN);
            }
            break;
            
        case JOB_BOARD_SCREEN:
            Log.error(Log.UI_MENU, "[MainMenuManager] ===== Switching to JOB_BOARD_SCREEN =====");
            Log.error(Log.UI_MENU, "[MainMenuManager] jobBoardScreenTable: " + (jobBoardScreenTable != null ? "OK" : "NULL"));
            System.err.flush();
            Log.info(Log.UI_MENU, "[MainMenuManager] ===== Switching to JOB_BOARD_SCREEN =====");
            System.out.flush();
            Log.info(Log.UI_MENU, "MainMenuManager", "=== Switching to JOB_BOARD_SCREEN ===");
            
            if (jobBoardScreenTable != null) {
                Log.info(Log.UI_MENU, "MainMenuManager", "Calling updateDynamicComponents...");
                Log.info(Log.UI_MENU, "[MainMenuManager] Calling updateDynamicComponents...");
                jobBoardScreenTable.updateDynamicComponents();
                
                Log.info(Log.UI_MENU, "MainMenuManager", "Adding jobBoardScreenTable to parentTable...");
                Log.info(Log.UI_MENU, "[MainMenuManager] Adding jobBoardScreenTable to parentTable...");
                parentTable.add(jobBoardScreenTable).grow();
                
                Log.info(Log.UI_MENU, "MainMenuManager", "Screen added successfully");
                Log.info(Log.UI_MENU, "[MainMenuManager] JOB_BOARD_SCREEN added to parentTable");
            } else {
                Log.error(Log.UI_MENU, "MainMenuManager", "jobBoardScreenTable is NULL!");
                Log.error(Log.UI_MENU, "[MainMenuManager] ERROR: jobBoardScreenTable is NULL!");
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
                Log.error(Log.UI_MENU, "MainMenuManager", "No player selected for detail view");
                redirectTo(MAIN_SQUAD_SCREEN);
            }
            break;
        
        //
        default:
            Log.info(Log.UI_MENU, "SCREEN NOT SET UP");
            break;
        }
        
        /**
         * NOTE: this is where we set the final alignment of the MAIN / CENTRAL SCREEN
         */
        parentTable.top().left();
        fitContent();

        syncMenuState();
    }
    
    /**
     * V-01: every content screen starts at the top (not centred in the window), and one that has no scroll pane of
     * its own scrolls vertically when it is taller than the window (Authorities, Finances, Club Info at 1280x720).
     */
    private void fitContent() {
        if (parentTable.getChildren().size != 1) {
            return;
        }
        Actor content = parentTable.getChildren().first();
        if (content instanceof Table) {
            ((Table) content).top();
        }
        if (!(content instanceof VisTable) || containsScrollPane(content)) {
            return;
        }
        parentTable.clearChildren();
        AutoFocusScrollPane scroll = new AutoFocusScrollPane((VisTable) content);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setFlickScroll(false);
        scroll.setScrollingDisabled(true, false);
        parentTable.add(scroll).grow();
    }

    private static boolean containsScrollPane(Actor actor) {
        if (actor instanceof ScrollPane) {
            return true;
        }
        if (actor instanceof Group) {
            for (Actor child : ((Group) actor).getChildren()) {
                if (containsScrollPane(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Allow to call this from other places after data is updated
     * 
     *  - this is used to automatically update the current screen, as it will have dynamic data
     */
    public void updateDynamicComponents() {
        
        switch(CURRENT_SCREEN) {
        
        case HOME_SCREEN:
            homeScreenTable.updateDynamicComponents();
            break;

        case MAIN_SQUAD_SCREEN:
            squadScreenTable.updateDynamicComponents();
            break;

        case RETAINED_LIST_SCREEN:
            retainedListScreenTable.updateDynamicComponents();
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
            
        case HISTORY_SCREEN:
            historyScreenTable.updateDynamicComponents();
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
                Log.info(Log.UI_MENU, "MainMenuManager: selectedLeague is null during updateDynamicComponents, redirecting to competitions");
                setActiveMainScreen(COMPETITIONS_SCREEN);
            }
            
            break;
            
        case CUP_DETAIL_SCREEN:
            if (selectedCup != null) {
                cupDetailScreenTable.updateDynamicComponents();
            }
            break;
            
        case CUP_PLAYOFFS_SCREEN:
            if (selectedCup != null) {
                cupPlayoffsTable.updateDynamicComponents();
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
            
        case CUP_DRAW_SCREEN:
            // Content is already updated in setCompetitionEdition
            parentTable.add(cupDrawScreenTable).grow();
            break;
            
        case MATCH_HISTORY_SCREEN:
            
            // Update match history screen with current club
            Club currentClub = currentGame != null ? currentGame.getCurrentClub() : null;
            if (currentClub != null) {
                matchHistoryScreenTable.setCurrentClub(currentClub);
            }
            matchHistoryScreenTable.updateDynamicComponents();
            
            break;

        // J-01: the job screens follow the day too (offers lapse, posts are filled and advertised)
        case CLUB_BROWSER_SCREEN:
            clubBrowserScreenTable.updateDynamicComponents();
            break;

        case CLUB_DETAIL_SCREEN:
            if (selectedClubForDetail != null) {
                clubDetailScreenTable.updateDynamicComponents(selectedClubForDetail);
            }
            break;

        case JOB_BOARD_SCREEN:
            jobBoardScreenTable.updateDynamicComponents();
            break;

        case MY_APPLICATIONS_SCREEN:
            myApplicationsScreenTable.updateDynamicComponents();
            break;

        case JOB_OFFER_SCREEN:
            jobOfferScreenTable.updateDynamicComponents();
            break;

        case NEGOTIATION_SCREEN:
            negotiationScreenTable.updateDynamicComponents();
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
     * G-03: opens the result and report of a played match (any club's: Match History, a cup's schedule, a league's
     * latest results, an inbox result). Continue on the result screen comes back to the screen it was opened from.
     */
    public void showMatchResult(com.rndmodgames.futtoboru.data.Match match) {
        if (match == null || match.getIsPlayed() == null || !match.getIsPlayed()) {
            return;
        }
        matchResultScreenTable.setSelectedMatch(match);
        if (CURRENT_SCREEN != MATCH_RESULT_SCREEN) {
            BEFORE_MATCH_SCREEN = CURRENT_SCREEN;
        }
        setActiveMainScreen(MATCH_RESULT_SCREEN);
    }

    /** The result screen shows the current club's latest played match again (the top bar's Match Result). */
    public void showLatestMatchResult() {
        matchResultScreenTable.setSelectedMatch(null);
        setActiveMainScreen(MATCH_RESULT_SCREEN);
    }

    /** @return the played match with this id (any club's), or null */
    public com.rndmodgames.futtoboru.data.Match findPlayedMatch(Long matchId) {
        if (matchId == null || currentGame == null || currentGame.getAllClubs() == null) {
            return null;
        }
        for (Club club : currentGame.getAllClubs()) {
            if (club == null || club.getPlayedMatches() == null) {
                continue;
            }
            for (com.rndmodgames.futtoboru.data.Match m : club.getPlayedMatches()) {
                if (m != null && matchId.equals(m.getId())) {
                    return m;
                }
            }
        }
        return null;
    }

    public void setSelectedCup(com.rndmodgames.futtoboru.data.Competition cup) {
        this.selectedCup = cup;
    }

    public com.rndmodgames.futtoboru.data.Competition getSelectedCup() {
        return selectedCup;
    }

    public void setSelectedPlayer(com.rndmodgames.futtoboru.data.Player player) {
        this.selectedPlayer = player;
    }

    public com.rndmodgames.futtoboru.data.Player getSelectedPlayer() {
        return selectedPlayer;
    }

    public InboxScreenTable getInboxScreenTable() {
        return inboxScreenTable;
    }

    public com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable getTopMenu() {
        return topMenuTable;
    }

    public void setTopMenuTable(com.rndmodgames.futtoboru.menu.topmenu.MainGameMenuTable topMenuTable) {
        this.topMenuTable = topMenuTable;
        syncMenuState();
    }
}
