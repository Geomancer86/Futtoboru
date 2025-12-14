package com.rndmodgames.futtoboru.tables.jobs;

import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.ApplicationStatus;
import com.rndmodgames.futtoboru.data.jobs.JobApplication;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.kotcrab.vis.ui.widget.VisTextButton;

/**
 * My Applications Screen Table v1
 * 
 * Displays all job applications with status.
 * 
 * @author Geomancer86
 */
public class MyApplicationsScreenTable extends VisTable {

    Game game;
    SaveGame currentGame;
    JobManager jobManager;
    
    VisTable applicationsListTable;
    VisScrollPane applicationsScrollPane;
    
    public MyApplicationsScreenTable(Game parent) {
        super(true);
        this.setDebug(true);
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        
        // Title
        this.row();
        this.add(new VisLabel("My Applications")).colspan(3);
        this.row();
        this.addSeparator();
        
        // Applications list
        applicationsListTable = new VisTable(true);
        applicationsListTable.setDebug(true);
        
        applicationsScrollPane = new VisScrollPane(applicationsListTable);
        applicationsScrollPane.setFadeScrollBars(false);
        
        this.row();
        this.add(applicationsScrollPane).grow().colspan(3);
        
        updateDynamicComponents();
    }
    
    public void updateDynamicComponents() {
        if (jobManager == null || currentGame == null) {
            return;
        }
        
        applicationsListTable.clear();
        
        List<JobApplication> applications = jobManager.getPlayerApplications(currentGame.getOwner());
        
        if (applications.isEmpty()) {
            applicationsListTable.row();
            applicationsListTable.add(new VisLabel("No applications submitted.")).colspan(4);
            return;
        }
        
        // Header
        applicationsListTable.row();
        applicationsListTable.add(new VisLabel("Club")).width(200);
        applicationsListTable.add(new VisLabel("Position")).width(150);
        applicationsListTable.add(new VisLabel("Status")).width(150);
        applicationsListTable.add(new VisLabel("Match %")).width(100);
        applicationsListTable.row();
        applicationsListTable.addSeparator();
        
        // Applications
        for (JobApplication application : applications) {
            // Get job opening
            com.rndmodgames.futtoboru.data.jobs.JobOpening jobOpening = null;
            for (com.rndmodgames.futtoboru.data.jobs.JobOpening job : currentGame.getActiveJobOpenings()) {
                if (job.getId().equals(application.getJobOpeningId())) {
                    jobOpening = job;
                    break;
                }
            }
            
            if (jobOpening == null) {
                continue;
            }
            
            Club club = currentGame.getClubById(jobOpening.getClubId());
            Profession profession = DatabaseLoader.getProfessionById(jobOpening.getProfessionId());
            
            if (club == null || profession == null) {
                continue;
            }
            
            applicationsListTable.row();
            applicationsListTable.add(new VisLabel(club.getName())).width(200);
            applicationsListTable.add(new VisLabel(profession.getName())).width(150);
            
            // Status with color
            String statusText = application.getStatus().toString();
            VisLabel statusLabel = new VisLabel(statusText);
            if (application.getStatus() == ApplicationStatus.OFFER_RECEIVED) {
                statusLabel.setColor(0, 1, 0, 1); // Green
            } else if (application.getStatus() == ApplicationStatus.REJECTED) {
                statusLabel.setColor(1, 0, 0, 1); // Red
            }
            applicationsListTable.add(statusLabel).width(150);
            
            applicationsListTable.add(new VisLabel(
                application.getMatchPercentage() != null ? application.getMatchPercentage() + "%" : "N/A"
            )).width(100);
            
            // If offer received, add button to view offer
            if (application.getStatus() == ApplicationStatus.OFFER_RECEIVED) {
                applicationsListTable.row();
                VisTextButton viewOfferButton = new VisTextButton("View Offer");
                viewOfferButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                    @Override
                    public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                        // Switch to job offer screen (will be handled by menu manager)
                        if (menuManager != null) {
                            menuManager.setActiveMainScreen(MainMenuManager.JOB_OFFER_SCREEN);
                        }
                    }
                });
                applicationsListTable.add(viewOfferButton).colspan(4);
            }
        }
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
}

