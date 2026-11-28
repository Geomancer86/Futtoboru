package com.rndmodgames.futtoboru.tables.jobs;

import com.rndmodgames.localization.LanguageModLoader;
import java.math.BigDecimal;
import com.rndmodgames.futtoboru.engine.finances.Money;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

import com.badlogic.gdx.Game;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rndmodgames.futtoboru.data.Club;
import com.rndmodgames.futtoboru.data.Profession;
import com.rndmodgames.futtoboru.data.jobs.JobOffer;
import com.rndmodgames.futtoboru.data.jobs.OfferStatus;
import com.rndmodgames.futtoboru.engine.jobs.JobManager;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.menu.MainMenuManager;
import com.rndmodgames.futtoboru.system.DatabaseLoader;
import com.rndmodgames.futtoboru.system.SaveGame;
import com.rndmodgames.futtoboru.ui.UiTheme;
import com.rndmodgames.futtoboru.system.Log;

/**
 * Job Offer Screen Table v1
 * 
 * Displays job offers with accept/reject/negotiate options.
 * 
 * @author Geomancer86
 */
public class JobOfferScreenTable extends VisTable {

    Game game;
    SaveGame currentGame;
    JobManager jobManager;
    MainMenuManager menuManager;
    
    VisTable offersListTable;
    VisScrollPane offersScrollPane;
    DecimalFormat df = new DecimalFormat("#,###.00");
    
    public JobOfferScreenTable(Game parent) {
        super(true);
        
        this.game = parent;
        this.currentGame = ((Futtoboru)game).getCurrentGame();
        this.jobManager = ((Futtoboru)game).getJobManager();
        
        // Title
        this.row();
        this.add(new VisLabel(LanguageModLoader.getValue("jobs_offers"))).colspan(4);
        this.row();
        this.addSeparator();
        
        // Offers list
        offersListTable = new VisTable(true);
        offersListTable.top(); // V-01: rows start under the title, not mid-screen
        
        offersScrollPane = new VisScrollPane(offersListTable);
        offersScrollPane.setFadeScrollBars(false);
        
        this.row();
        this.add(offersScrollPane).grow().colspan(4);
        
        updateDynamicComponents();
    }
    
    public void setMenuManager(MainMenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    public void updateDynamicComponents() {
        if (jobManager == null || currentGame == null) {
            return;
        }
        
        offersListTable.clear();
        
        List<JobOffer> offers = jobManager.getPlayerOffers(currentGame.getOwner());
        
        // Also check inbox for offer notifications
        boolean hasOfferInInbox = false;
        if (currentGame.getAllMessages() != null) {
            for (com.rndmodgames.futtoboru.data.Message msg : currentGame.getAllMessages()) {
                if ("JOB_OFFER".equals(msg.getMessageType()) || (msg.getTitle() != null
                        && (msg.getTitle().contains("Job Offer") || msg.getTitle().equals(LanguageModLoader.getValue("jobs_msg_offer_title"))))) {
                    hasOfferInInbox = true;
                    break;
                }
            }
        }
        
        if (offers.isEmpty() && !hasOfferInInbox) {
            offersListTable.row();
            offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_no_offers"))).colspan(4);
            return;
        }
        
        // Show link to inbox if offers exist but not shown here
        if (offers.isEmpty() && hasOfferInInbox) {
            offersListTable.row();
            offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_offers_in_inbox"))).colspan(4);
            offersListTable.row();
            offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_offers_expired"))).colspan(4);
            return;
        }
        
        // Header
        offersListTable.row();
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("table_club"))).width(UiTheme.size("col.wide"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_salary"))).width(UiTheme.size("col.label"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_contract"))).width(UiTheme.size("col.label"));
        offersListTable.add(new VisLabel(LanguageModLoader.getValue("jobs_actions"))).width(UiTheme.size("col.xwide"));
        offersListTable.row();
        offersListTable.addSeparator().colspan(4); // V-01: full width, not the first column only
        
        // Offers
        for (JobOffer offer : offers) {
            // Get job opening
            com.rndmodgames.futtoboru.data.jobs.JobOpening jobOpening = null;
            for (com.rndmodgames.futtoboru.data.jobs.JobOpening job : currentGame.getActiveJobOpenings()) {
                if (job.getId().equals(offer.getJobOpeningId())) {
                    jobOpening = job;
                    break;
                }
            }
            
            if (jobOpening == null) {
                continue;
            }
            
            Club club = currentGame.getClubById(jobOpening.getClubId());
            
            if (club == null) {
                continue;
            }
            
            offersListTable.row();
            offersListTable.add(new VisLabel(club.getName())).width(UiTheme.size("col.wide"));
            offersListTable.add(new VisLabel(LanguageModLoader.format("jobs_per_month", Money.format(offer.getSalary())))).width(UiTheme.size("col.label"));
            offersListTable.add(new VisLabel(LanguageModLoader.format("jobs_months", offer.getContractLengthMonths()))).width(UiTheme.size("col.label"));
            
            // Action buttons
            VisTable buttonsTable = new VisTable(true);
            
            // Accept button
            VisTextButton acceptButton = new VisTextButton(LanguageModLoader.getValue("jobs_accept"));
            acceptButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    if (acceptButton.isPressed() && jobManager != null) {
                        boolean success = jobManager.acceptOffer(offer);
                        if (success) {
                            Log.info(Log.UI_JOBS, "JobOfferScreenTable", "Accepted offer: " + offer.getId());
                            Log.info(Log.UI_JOBS, "[JobOfferScreenTable] Offer accepted successfully");
                            // Refresh menu to show manager screens
                            if (menuManager != null) {
                                menuManager.setDynamicButtonsMenu();
                            }
                            updateDynamicComponents();
                        } else {
                            Log.error(Log.UI_JOBS, "JobOfferScreenTable", "Failed to accept offer: " + offer.getId());
                            Log.error(Log.UI_JOBS, "[JobOfferScreenTable] ERROR: Failed to accept offer!");
                        }
                    }
                }
            });
            buttonsTable.add(acceptButton);
            
            // Reject button
            VisTextButton rejectButton = new VisTextButton(LanguageModLoader.getValue("jobs_reject"));
            rejectButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
                
                @Override
                public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                    if (rejectButton.isPressed() && jobManager != null) {
                        jobManager.rejectOffer(offer);
                        Log.info(Log.UI_JOBS, "JobOfferScreenTable", "Rejected offer: " + offer.getId());
                        updateDynamicComponents();
                    }
                }
            });
            buttonsTable.add(rejectButton);
            
            // Negotiate button (only if can negotiate)
            LocalDateTime gameDate = currentGame != null ? currentGame.getGameDate() : null;
            if (offer.canNegotiate(gameDate)) {
                VisTextButton negotiateButton = new VisTextButton(LanguageModLoader.getValue("jobs_negotiate"));
                negotiateButton.addCaptureListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                    @Override
                    public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                        return true;
                    }
                    
                    @Override
                    public void touchUp(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
                        if (negotiateButton.isPressed() && menuManager != null) {
                            // Store current offer for negotiation screen
                            menuManager.setCurrentNegotiationOffer(offer);
                            menuManager.setActiveMainScreen(MainMenuManager.NEGOTIATION_SCREEN);
                        }
                    }
                });
                buttonsTable.add(negotiateButton);
            }
            
            offersListTable.add(buttonsTable).width(UiTheme.size("col.xwide"));
        }
    }
}

