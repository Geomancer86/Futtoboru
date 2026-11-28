package com.rndmodgames.futtoboru.tables.jobs;

import com.rndmodgames.localization.LanguageModLoader;
import java.util.List;

import com.badlogic.gdx.Game;
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
import com.rndmodgames.futtoboru.ui.UiTheme;

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
    MainMenuManager menuManager;
    
    public MyApplicationsScreenTable(Game parent) {
        super(true);
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        
        // Title
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("my_applications"))).colspan(3);
        this.row();
        this.addSeparator();
        
        // Applications list
        applicationsListTable = new VisTable(true);
        applicationsListTable.top(); // V-01: rows start under the title, not mid-screen
        
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
            applicationsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_no_applications"))).colspan(4);
            return;
        }
        
        // Header
        applicationsListTable.row();
        applicationsListTable.add(new VisLabel(LanguageModLoader.getValue("table_club"))).width(UiTheme.size("col.wide"));
        applicationsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_position"))).width(UiTheme.size("col.label"));
        applicationsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_status"))).width(UiTheme.size("col.label"));
        applicationsListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_match_percent"))).width(UiTheme.size("col.number"));
        applicationsListTable.row();
        applicationsListTable.addSeparator().colspan(4); // V-01: full width, not the first column only
        
        // Applications
        for (JobApplication application : applications) {
            com.rndmodgames.futtoboru.data.jobs.JobOpening jobOpening = jobManager.getJobOpeningById(application.getJobOpeningId());
            if (jobOpening == null) {
                continue;
            }
            
            Club club = currentGame.getClubById(jobOpening.getClubId());
            Profession profession = DatabaseLoader.getInstance().getProfessionById(jobOpening.getProfessionId());
            
            if (club == null || profession == null) {
                continue;
            }
            
            applicationsListTable.row();
            applicationsListTable.add(new VisLabel(club.getName())).width(UiTheme.size("col.wide"));
            applicationsListTable.add(new VisLabel(profession.getName())).width(UiTheme.size("col.label"));
            
            // Status with color
            String statusText = JobBoardScreenTable.statusName(application.getStatus());
            VisLabel statusLabel = new VisLabel(statusText);
            if (application.getStatus() == ApplicationStatus.OFFER_RECEIVED) {
                statusLabel.setColor(UiTheme.color("application.offer")); // Green
            } else if (application.getStatus() == ApplicationStatus.REJECTED) {
                statusLabel.setColor(UiTheme.color("application.rejected")); // Red
            }
            applicationsListTable.add(statusLabel).width(UiTheme.size("col.label"));
            
            applicationsListTable.add(new VisLabel(
                application.getMatchPercentage() != null ? application.getMatchPercentage() + "%" : LanguageModLoader.getValue("not_available")
            )).width(UiTheme.size("col.number"));
            
            // If an offer is waiting for an answer, add button to view offer (J-01: not for a lapsed one)
            final com.rndmodgames.futtoboru.data.jobs.JobOpening finalJobOpening = jobOpening; // Make final for inner class
            boolean offerWaiting = application.getStatus() == ApplicationStatus.OFFER_RECEIVED
                    && jobManager.getPlayerOffers(currentGame.getOwner()).stream()
                            .anyMatch(o -> finalJobOpening.getId().equals(o.getJobOpeningId()));
            if (offerWaiting) {
                applicationsListTable.row();
                VisTextButton viewOfferButton = new VisTextButton(LanguageModLoader.getValue("jobs_view_offer"));
                viewOfferButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                    @Override
                    public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                        return true;
                    }
                    
                    @Override
                    public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                        if (viewOfferButton.isPressed()) {
                            // Find the actual JobOffer object
                            com.rndmodgames.futtoboru.data.jobs.JobOffer offer = jobManager.getPlayerOffers(currentGame.getOwner()).stream()
                                .filter(o -> o.getJobOpeningId().equals(finalJobOpening.getId()))
                                .findFirst().orElse(null);
                            
                            if (offer != null && menuManager != null) {
                                menuManager.setCurrentNegotiationOffer(offer);
                                menuManager.setActiveMainScreen(MainMenuManager.JOB_OFFER_SCREEN);
                            }
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

