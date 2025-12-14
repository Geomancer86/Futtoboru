package com.rndmodgames.futtoboru.tables.jobs;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Country;
import com.rndmodgames.futtoboru.engine.jobs.ClubStaffManager;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.SaveGame;

/**
 * Club Browser Screen Table v1
 * 
 * Displays all clubs in the game, allowing unemployed players to browse and view club details.
 * 
 * @author Geomancer86
 */
public class ClubBrowserScreenTable extends VisTable {
    
    Game game;
    SaveGame currentGame;
    JobManager jobManager;
    ClubStaffManager clubStaffManager;
    MainMenuManager menuManager;
    
    VisTable clubsListTable;
    VisScrollPane clubsScrollPane;
    
    public ClubBrowserScreenTable(Game parent) {
        super(true);
        this.setDebug(true);
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        this.clubStaffManager = ((Futtoboru)game).getClubStaffManager();
        
        // Title
        this.row();
        this.add(new VisLabel("Club Browser")).colspan(4);
        this.row();
        this.addSeparator();
        
        // Clubs list
        clubsListTable = new VisTable(true);
        clubsListTable.setDebug(true);
        
        clubsScrollPane = new VisScrollPane(clubsListTable);
        clubsScrollPane.setFadeScrollBars(false);
        
        this.row();
        this.add(clubsScrollPane).grow().colspan(4);
        
        updateDynamicComponents();
    }
    
    public void updateDynamicComponents() {
        if (jobManager == null || currentGame == null) {
            return;
        }
        
        clubsListTable.clear();
        
        List<Club> allClubs = currentGame.getAllClubs();
        
        if (allClubs == null || allClubs.isEmpty()) {
            clubsListTable.row();
            clubsListTable.add(new VisLabel("No clubs available.")).colspan(4);
            return;
        }
        
        // Header
        clubsListTable.row();
        clubsListTable.add(new VisLabel("Club Name")).width(250);
        clubsListTable.add(new VisLabel("Country")).width(150);
        clubsListTable.add(new VisLabel("Open Positions")).width(150);
        clubsListTable.add(new VisLabel("Actions")).width(200);
        clubsListTable.row();
        clubsListTable.addSeparator();
        
        // Clubs
        for (Club club : allClubs) {
            if (club == null || club.getId() == null) {
                continue;
            }
            
            Country country = club.getCountry();
            String countryName = country != null ? country.getCommonName() : "Unknown";
            
            // Count open positions
            final int openPositions = (clubStaffManager != null) ? 
                clubStaffManager.getVacantPositions(club).size() : 0;
            
            clubsListTable.row();
            
            // Club name with right-click support
            final Club currentClub = club; // Make final for inner class
            VisLabel clubNameLabel = new VisLabel(club.getName());
            clubNameLabel.addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    // Left click: view details
                    if (button == Input.Buttons.LEFT) {
                        if (menuManager != null) {
                            menuManager.setSelectedClubForDetail(currentClub);
                            menuManager.setActiveMainScreen(MainMenuManager.CLUB_DETAIL_SCREEN);
                        }
                    }
                    // Right click: apply for job (if positions available)
                    else if (button == Input.Buttons.RIGHT && openPositions > 0) {
                        if (menuManager != null) {
                            menuManager.setSelectedClubForDetail(currentClub);
                            menuManager.setActiveMainScreen(MainMenuManager.CLUB_DETAIL_SCREEN);
                        }
                    }
                }
            });
            clubsListTable.add(clubNameLabel).width(250);
            clubsListTable.add(new VisLabel(countryName)).width(150);
            clubsListTable.add(new VisLabel(openPositions + " vacant")).width(150);
            
            // Action buttons
            VisTable actionButtons = new VisTable(true);
            
            // View Details button
            VisTextButton viewButton = new VisTextButton("View");
            viewButton.addCaptureListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    if (viewButton.isPressed() && menuManager != null) {
                        menuManager.setSelectedClubForDetail(club);
                        menuManager.setActiveMainScreen(MainMenuManager.CLUB_DETAIL_SCREEN);
                    }
                }
            });
            actionButtons.add(viewButton).pad(2);
            
            // Apply for Job button (only if open positions exist)
            if (openPositions > 0) {
                VisTextButton applyButton = new VisTextButton("Apply");
                applyButton.addCaptureListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return true;
                    }
                    
                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        if (applyButton.isPressed() && menuManager != null) {
                            // Go to club detail screen to see available positions and apply
                            menuManager.setSelectedClubForDetail(club);
                            menuManager.setActiveMainScreen(MainMenuManager.CLUB_DETAIL_SCREEN);
                        }
                    }
                });
                actionButtons.add(applyButton).pad(2);
            }
            
            clubsListTable.add(actionButtons).width(200);
        }
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
}

