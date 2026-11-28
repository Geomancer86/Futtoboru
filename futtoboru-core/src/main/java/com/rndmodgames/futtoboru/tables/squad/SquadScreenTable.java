package com.rndmodgames.futtoboru.tables.squad;

import com.rndmodgames.futtoboru.menu.GameScreenId;

import java.time.temporal.ChronoUnit;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Player;
import com.rndmodgames.futtoboru.engine.team.TeamSheets;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.localization.GameFormat;
import com.rndmodgames.localization.LanguageModLoader;

/**
 * Main Squad Screen Table v1
 * 
 *  - This screen will be used for both the Player accessing it's own team (manager, owner, etc)
 *  - This screen will be used to see the Squad of other teams without access, but we are supposed to know the players from the public information
 *  
 *  - Design for Fog of War on Attributes should be done ASAP
 * 
 * ---
 * Sources:
 * 
 *  - https://3.bp.blogspot.com/-ADphk9k3mNs/VmiRVkpfgDI/AAAAAAAAA1g/lK3Ij3MtTmQ8D8Nkl3BrjRowVTbAGvFTgCPcB/s1600/Squad_.png
 * 
 * @author Geomancer86
 */
public class SquadScreenTable extends VisTable{

    // keep track for easy access
    Futtoboru game;
    SaveGame currentGame;
    
    // Dynamic Club
    private Club displayedClub = null;
    
    // Dynamic Components
    VisLabel clubNameValueLabel = new VisLabel("");
    VisLabel playersCountValueLabel = new VisLabel("");
    VisLabel averagePlayerAgeValueLabel = new VisLabel("");
    
    //
    PlayersListTable playersList;
    VisScrollPane playersScrollPane;

    // G-01: the Players / Team Sheet tabs
    private final VisTextButton playersTab = new VisTextButton(LanguageModLoader.getValue("team_sheet_players_tab"),
            UiTheme.style("button.toggle"));
    private final VisTextButton teamSheetTab = new VisTextButton(LanguageModLoader.getValue("team_sheet"),
            UiTheme.style("button.toggle"));
    private final TeamSheetEditor teamSheetEditor = new TeamSheetEditor();
    private final VisScrollPane teamSheetScrollPane = new VisScrollPane(teamSheetEditor);
    private Cell<Actor> contentCell;
    
    //
    
    //
    public SquadScreenTable(Game parent) {
        
        // default vis spacing
        super (true);
        
        //
        this.game = ((Futtoboru)parent);
        this.currentGame = game.getSession().getGame();
        
        // 
        playersList = new PlayersListTable(game);
        
        // Wrap players list in scroll pane for horizontal scrolling (v1.0)
        playersScrollPane = new VisScrollPane(playersList);
        playersScrollPane.setFadeScrollBars(false);
        playersScrollPane.setScrollingDisabled(false, false); // Enable both horizontal and vertical scrolling
        
        /**
         * SQUAD SCREEN:
         * 
         *  TODO WIP:
         *  
         *      - No Players Label
         *      - List of Players Component
         *      
         *      - Players Design
         *      - Random Players Generator
         *      - Players Script Loader (for existing players/database)
         *      
         *          - Name
         *          - Nationality
         *          - Birthdate/Age
         *          - Attributes
         *          - Contracts/Salaries
         *          - History
         *          - Fitness
         *          - Training
         *          - Injuries
         *          
         * ------
         * Screen Headers:
         *  - 
         * 
         * Screen Content:
         * 
         */
        VisLabel nameLabel = new VisLabel(LanguageModLoader.getValue("main_squad"));
        VisLabel playersAtClubLabel = new VisLabel(LanguageModLoader.getValue("players_at_club"));
        VisLabel averageAgeLabel = new VisLabel(LanguageModLoader.getValue("average_player_age"));
        
        // Name
        this.add(nameLabel);
        this.add(clubNameValueLabel);
        this.row();
        
        // Players at Club Count
        this.add(playersAtClubLabel);
        this.add(playersCountValueLabel);
        this.row();
        
        // Average Players Age
        this.add(averageAgeLabel);
        this.add(averagePlayerAgeValueLabel);
        this.row();
        
        // G-01: Players | Team Sheet
        ButtonGroup<VisTextButton> tabs = new ButtonGroup<>(playersTab, teamSheetTab);
        tabs.setMinCheckCount(1);
        tabs.setMaxCheckCount(1);
        playersTab.setChecked(true);
        ChangeListener switchTab = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showTab();
            }
        };
        playersTab.addListener(switchTab);
        teamSheetTab.addListener(switchTab);
        VisTable tabRow = new VisTable(true);
        tabRow.add(playersTab);
        tabRow.add(teamSheetTab);
        this.add(tabRow).colspan(2).left();
        this.row();
        teamSheetScrollPane.setFadeScrollBars(false);
        teamSheetScrollPane.setScrollingDisabled(false, false);

        // G-04: the retained list (engagements for next season; open from 16 April), beside the tabs
        VisTextButton retainedListButton = new VisTextButton(LanguageModLoader.getValue("contracts_retained_list"));
        retainedListButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getGameEngine() != null && game.getGameEngine().getMainMenuManager() != null) {
                    game.getGameEngine().getMainMenuManager().setActiveMainScreen(
                            GameScreenId.RETAINED_LIST);
                }
            }
        });
        tabRow.add(retainedListButton);

        // Players List Component (wrapped in scroll pane for horizontal scrolling)
        this.addSeparator().colspan(2);
        this.row();
        contentCell = this.add((Actor) playersScrollPane).colspan(2).grow().fill();
    }

    /** G-01: shows the team sheet tab. */
    public void showTeamSheet() {
        teamSheetTab.setChecked(true);
        showTab();
    }

    /** G-01: shows the players list tab (what the Squad menu entry opens on). */
    public void showPlayers() {
        playersTab.setChecked(true);
        if (displayedClub != null) {
            showTab();
        }
    }

    private void showTab() {
        if (teamSheetTab.isChecked()) {
            if (displayedClub != null) {
                boolean own = TeamSheets.isManaged(displayedClub, game.getSession().getGame());
                teamSheetEditor.show(displayedClub, TeamSheets.standing(displayedClub), own, null);
            }
            contentCell.setActor(teamSheetScrollPane);
        } else {
            contentCell.setActor(playersScrollPane);
        }
    }
    
    /**
     * 
     */
    public void updateDynamicComponents() {
        
        //
        clubNameValueLabel.setText(displayedClub.getName());
        
        // No Players at Club
        if (displayedClub.getPlayers().isEmpty()) {
            
            //
            playersCountValueLabel.setText(LanguageModLoader.getValue("no_players_at_club"));
            
        } else {
            
            // Count of Players & Player List Component
            playersCountValueLabel.setText(LanguageModLoader.format("squad_player_count", displayedClub.getPlayers().size()));
            
            /**
             * Average Player Age
             * 
             * TODO: this calculation might be used on many places, move elsewhere
             * 
             * TODO: count MONTHS for decimal years and a better year average
             */
            long accumulatedAge = 0;
            
            for (Player player : displayedClub.getPlayers()) {
                
                long yearsOld = ChronoUnit.YEARS.between(player.getPerson().getBirthDate(), game.getSession().getGame().getGameDate());
                
                // add up players age
                accumulatedAge += yearsOld;
            }
            
            double averagePlayerAge = (double) accumulatedAge / (double) displayedClub.getPlayers().size();
            
            //
            averagePlayerAgeValueLabel.setText(LanguageModLoader.format("squad_years_old", GameFormat.decimal(averagePlayerAge, 1)));
        }
        
        // Update Players List Component
        playersList.setCurrentPlayers(displayedClub.getPlayers());
        
        //
        playersList.updateDynamicComponents();

        // G-01: the team sheet tab follows the displayed club
        showTab();
    }

    public Club getCurrentClub() {
        return displayedClub;
    }

    public void setCurrentClub(Club currentClub) {
        this.displayedClub = currentClub;
    }
}